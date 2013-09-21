package sg.money.models;

import android.content.*;
import android.graphics.*;
import android.preference.*;
import android.view.*;
import java.util.*;
import sg.money.*;
import sg.money.activities.*;
import sg.money.domainobjects.*;
import sg.money.utils.*;

public class AddTransactionModel extends SimpleObservable
{
	public static final String ADD_CATEGORY_STRING = "< Add new category >";
	public static final int NO_CATEGORY_SELECTED = -1;
	
    Transaction transaction;
    boolean newTransaction;
    ArrayList<Category> categories;
    Map<String, Account> accountsMap = new HashMap<String, Account>();
    ArrayList<String> categoryNames;
    Transaction editTransaction;
	Category cachedCategory;
	
	boolean useNewCategory;
	Category newCategory;

    public AddTransactionModel(Transaction transaction, int accountID, Context context) {
        this.transaction = transaction;
        if (this.transaction == null)
        {
            this.transaction = new Transaction();
			this.transaction.account = accountID;
			this.transaction.category = NO_CATEGORY_SELECTED;
			this.transaction.dateTime = new Date();
            newTransaction = true;
        }
		
		useNewCategory = false;
		newCategory = new Category();
		
		categories = DatabaseManager.getInstance(context).GetAllCategories();
    	categories = Misc.getCategoriesInGroupOrder(categories);


    	ArrayList<Account> accounts = DatabaseManager.getInstance(context).GetAllAccounts();
    	for(Account account : accounts)
    	{
    		if (account.id != accountID)
    			accountsMap.put(account.name, account);
    	}
    }

	public double getValue()
	{
		return transaction.value;
	}
	
	public void setValue(double value)
	{
		transaction.value = value;
		notifyObservers(this);
	}

	public boolean getDontReport()
	{
		return transaction.dontReport;
	}
	
	public void setDontReport(boolean dontReport)
	{
		transaction.dontReport = dontReport;
		notifyObservers(this);
	}

	public String getDescription()
	{
		return transaction.description;
	}
	
	public void setDescription(String description)
	{
		transaction.description = description;
		notifyObservers(this);
	}

	public void setDate(Date date)
	{
		transaction.dateTime = date;
		notifyObservers(this);
	}
	
	public Date getDate()
	{
		return transaction.dateTime;
	}

	public boolean getIsTransfer()
	{
		return transaction.isTransfer;
	}

    public boolean isNewTransaction() {
        return newTransaction;
    }
	
	public Account[] getAccounts()
	{
		return accountsMap.values().toArray(new Account[accountsMap.values().size()]);
	}
	
	public String[] getAccountNames()
	{
		return accountsMap.keySet().toArray(new String[accountsMap.keySet().size()]);
	}
	
	public ArrayList<String> getCategoryNames()
	{
		categoryNames = new ArrayList<String>();
    	for(Category category : categories)
    	{
    		if (category.income == category.income)
    		{
    			categoryNames.add(getCategoryName(category));
    		}
    	}

    	categoryNames.add(ADD_CATEGORY_STRING);
		
		return categoryNames;
	}

    private String getCategoryName(Category category)
    {
    	return Misc.getCategoryName(category, categories);
    }
	
	public Category getCategory()
	{
		if (transaction.id == NO_CATEGORY_SELECTED)
		{
			if (useNewCategory)
			{
				return newCategory;
			}
			
			return null;
		}
		
		if (cachedCategory.id == transaction.id)
		{
			return cachedCategory;
		}
		
		//cache is invalid, so fetch from collection
		Category category = null;
		for(Category testCategory : categories)
		{
			if (testCategory.id == transaction.category)
			{
				category = testCategory;
				break;
			}
		}
		
		cachedCategory = category;
		return cachedCategory;
	}

    public String validate()
    {
    	if (String.valueOf(transaction.value).trim().equals(""))
    	{
    		return "Please enter a value.";
    	}

    	if (transaction.description.trim().equals(""))
    	{
    		return "Please enter a description.";
    	}

    	if (useNewCategory)
    	{
    		if (newCategory.name.trim() == "")
    		{
	   			return "Please enter a name for the new category.";
    		}

    		for(Category currentCategory : categories)
        	{
        		if (newCategory.name.trim().equals(currentCategory.name.trim())
					&& currentCategory.income == newCategory.income)
            	{
            		return "A category with this name already exists.";
            	}
        	}
    	}

    	return null;
    }
	
	public void commit(Context context)
	{
    	if (useNewCategory)
    	{
        	Random rnd = new Random(System.currentTimeMillis());
        	newCategory.color = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
			DatabaseManager.getInstance(context).AddCategory(newCategory);
      
			transaction.category = newCategory.id;
		}
		
		if (!transaction.isTransfer)
		{
    		if (!selectedCategory.income)
    			editTransaction.value *= -1.0f;

    		if (newTransaction)
    			DatabaseManager.getInstance(context).InsertTransaction(editTransaction);
    		else
    			DatabaseManager.getInstance(context).UpdateTransaction(editTransaction);
    	}
    	else
    	{
    		Transaction fromTransaction, toTransaction;
    		if (creatingNew)
    		{
    			fromTransaction = editTransaction;
        		toTransaction = new Transaction();
    		}
    		else
    		{
    			fromTransaction = editTransaction.isReceivingParty() 
					? editTransaction.getRelatedTransferTransaction(this) 
					: editTransaction;
        		toTransaction = editTransaction.isReceivingParty() 
					? editTransaction
					: editTransaction.getRelatedTransferTransaction(this);
    		}

    		double value = Double.valueOf(txtValue.getText().toString());

        	Calendar c = Calendar.getInstance();
        	c.setTime(buttonDate);

    		//transfer from transaction
    		fromTransaction.value = value * -1.0f;
    		fromTransaction.description = txtDesc.getText().toString().trim();
        	fromTransaction.dateTime = c.getTime();

    		//transfer to transaction
        	toTransaction.value = value;
        	toTransaction.description = txtDesc.getText().toString().trim();
        	toTransaction.dateTime = c.getTime();

        	if (creatingNew)
        	{
        		fromTransaction.transferFromTransaction = -1;
        		toTransaction.transferToTransaction = -1;
            	fromTransaction.dontReport = true; 
            	fromTransaction.isTransfer = true;
            	toTransaction.dontReport = true;
        		toTransaction.isTransfer = true;
        	}

        	if (creatingNew || !editTransaction.isReceivingParty())
        	{
            	fromTransaction.account = accountID;
            	toTransaction.account = accountsMap.get(spnTransferAccount.getSelectedItem()).id;
        	}

    		if (creatingNew)
    		{
    			DatabaseManager.getInstance(context).InsertTransaction(fromTransaction);
    			DatabaseManager.getInstance(context).InsertTransaction(toTransaction);

    			fromTransaction.transferToTransaction = toTransaction.id;
    			toTransaction.transferFromTransaction = fromTransaction.id;
    			DatabaseManager.getInstance(context).UpdateTransaction(fromTransaction);
    			DatabaseManager.getInstance(context).UpdateTransaction(toTransaction);
    		}
    		else
    		{
    			DatabaseManager.getInstance(context).UpdateTransaction(fromTransaction);
    			DatabaseManager.getInstance(context).UpdateTransaction(toTransaction);
    		}
		}
	}
}
