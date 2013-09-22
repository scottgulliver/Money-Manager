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
	public static final int NO_CATEGORY_SELECTED = -1;
	
    Transaction transaction;
    boolean newTransaction;
    Map<String, Account> accountsMap = new HashMap<String, Account>();
    Map<String, Category> categoriesMap = new HashMap<String, Category>();
	Category cachedCategory;
    Transaction relatedTransaction; // other half of a transaction
	
	boolean useNewCategory;
	Category newCategory;

    boolean isIncomeType;

    public AddTransactionModel(Transaction transaction, int accountID, Context context) {
        this.transaction = transaction;

        relatedTransaction = new Transaction();
        relatedTransaction.dontReport = true;
        relatedTransaction.transferToTransaction = -1;

        if (this.transaction == null)
        {
            this.transaction = new Transaction();
			this.transaction.account = accountID;
			this.transaction.category = NO_CATEGORY_SELECTED;
			this.transaction.dateTime = new Date();
            this.transaction.transferFromTransaction = -1;
            this.transaction.transferToTransaction = -1;
            newTransaction = true;
        }
        else if (transaction.isTransfer)
        {
            relatedTransaction = transaction.getRelatedTransferTransaction(context);
        }
		
		useNewCategory = false;
		newCategory = new Category();

        ArrayList<Account> accounts = DatabaseManager.getInstance(context).GetAllAccounts();
        for(Account account : accounts)
        {
            if (account.id != accountID)
                accountsMap.put(account.name, account);
        }


        ArrayList<Category> categories = DatabaseManager.getInstance(context).GetAllCategories();
        categories = Misc.getCategoriesInGroupOrder(categories);
        for(Category category : categories)
        {
            categoriesMap.put(category.name, category);
        }
    }

    public boolean getIsReceivingParty()
    {
        return transaction.isReceivingParty();
    }

	public double getValue()
	{
        double value = transaction.value;
        if (isIncomeType || (getIsTransfer() && getIsReceivingParty()))
        {
            value *= -1.0f;
        }
		return value;
	}
	
	public void setValue(double value)
	{
        if (isIncomeType || (getIsTransfer() && getIsReceivingParty()))
        {
            value *= -1.0f;
        }
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
        relatedTransaction.description = description;
		notifyObservers(this);
	}

	public void setDate(Date date)
	{
		transaction.dateTime = date;
        relatedTransaction.dateTime = date;
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

	public ArrayList<String> getValidCategoryNames()
	{
        ArrayList<String> categoryNames = new ArrayList<String>();
        for(Map.Entry<String, Category> entry : categoriesMap.entrySet())
        {
            if (entry.getValue().income == isIncomeType)
            {
                categoryNames.add(entry.getKey());
            }
        }

        return categoryNames;
	}

    public ArrayList<Category> getValidCategories()
    {
        ArrayList<Category> categories = new ArrayList<Category>();
        for(String categoryName : getValidCategoryNames())
        {
            categories.add(categoriesMap.get(categoryName));
        }

        return categories;
    }

    public Category[] getAllCategories()
    {
        return categoriesMap.values().toArray(new Category[categoriesMap.values().size()]);
    }

    public String[] getAllCategoryNames()
    {
        return categoriesMap.keySet().toArray(new String[categoriesMap.keySet().size()]);
    }

    private String getCategoryName(Category category)
    {
    	for(Map.Entry<String, Category> entry : categoriesMap.entrySet())
        {
            if (entry.getValue().id == category.id)
            {
                return entry.getKey();
            }
        }

        throw new RuntimeException("Category not found.");
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
		for(Category testCategory : getAllCategories())
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

    public void setCategory(Category category) {
        transaction.category = category.id;
        cachedCategory = category;
        notifyObservers(this);
    }

    public void setCategory(String categoryName) {
        Category category = categoriesMap.get(categoryName);
        setCategory(category);
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

    		for(Category currentCategory : getAllCategories())
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
    		if (newTransaction)
    			DatabaseManager.getInstance(context).InsertTransaction(transaction);
    		else
    			DatabaseManager.getInstance(context).UpdateTransaction(transaction);
    	}
    	else
    	{
            if (isNewTransaction())
            {
                DatabaseManager.getInstance(context).InsertTransaction(transaction);
                DatabaseManager.getInstance(context).InsertTransaction(relatedTransaction);
                transaction.transferToTransaction = relatedTransaction.id;
                relatedTransaction.transferFromTransaction = transaction.id;
                DatabaseManager.getInstance(context).UpdateTransaction(transaction);
                DatabaseManager.getInstance(context).UpdateTransaction(relatedTransaction);
            }
            else
            {
                DatabaseManager.getInstance(context).UpdateTransaction(transaction);
                DatabaseManager.getInstance(context).UpdateTransaction(relatedTransaction);
            }
		}
	}

    public boolean getUseNewCategory() {
        return useNewCategory;
    }

    public void setUseNewCategory(boolean useNewCategory) {
        this.useNewCategory = useNewCategory;
    }

    public boolean isIncomeType() {
        return isIncomeType;
    }

    public void setIncomeType(boolean incomeType) {
        this.isIncomeType = incomeType;
    }

    public void setIsTransfer(boolean isTransfer) {
        transaction.isTransfer = isTransfer;
        transaction.dontReport = isTransfer;
        notifyObservers(this);
    }

    public boolean isReceivingParty() {
        return transaction.isReceivingParty();
    }

    public void setTransferAccount(Account account)
    {
        relatedTransaction.account = account.id;
        notifyObservers(this);
    }

    public Account getTransferAccount(Context context)
    {
        if (relatedTransaction.account != -1)
        {
            return relatedTransaction.getAccount(context);
        }

        return null;
    }

    public Transaction getRelatedTransferTransaction(AddTransactionActivity addTransactionActivity) {
        //todo
        return null;
    }
}
