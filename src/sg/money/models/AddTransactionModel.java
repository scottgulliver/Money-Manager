package sg.money.models;

import android.content.*;
import android.graphics.*;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.*;

import java.io.Serializable;
import java.util.*;
import sg.money.*;
import sg.money.activities.*;
import sg.money.domainobjects.*;
import sg.money.utils.*;

public class AddTransactionModel extends SimpleObservable implements Parcelable
{	
    Transaction transaction;
    boolean newTransaction;
    Map<String, Account> accountsMap = new HashMap<String, Account>();
    Map<String, Category> categoriesMap = new HashMap<String, Category>();
	Category cachedCategory;
    Transaction relatedTransaction; // other half of a transaction
	
	boolean useNewCategory;
	Category newCategory;

    boolean isIncomeType;

    public AddTransactionModel(Transaction transaction, int accountID, int defaultCategoryID, Context context) {
        this.transaction = transaction;

        relatedTransaction = new Transaction();
        relatedTransaction.setDontReport(true);
        relatedTransaction.setTransferToTransaction(-1);

        if (this.transaction == null)
        {
            this.transaction = new Transaction();
			this.transaction.setAccount(accountID);
			this.transaction.setCategory(defaultCategoryID);
			this.transaction.dateTime = new Date();
            this.transaction.transferFromTransaction = -1;
            this.transaction.transferToTransaction = -1;
			this.transaction.description = "";
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
            if (account.getId() != accountID)
                accountsMap.put(account.getName(), account);
        }


        ArrayList<Category> categories = DatabaseManager.getInstance(context).GetAllCategories();
        categories = Misc.getCategoriesInGroupOrder(categories);
        for(Category category : categories)
        {
            categoriesMap.put(category.getName(), category);
        }

        if (!newTransaction)
        {
            isIncomeType = getCategory().isIncome();
        }
    }

    public boolean getIsReceivingParty()
    {
        return transaction.isReceivingParty();
    }

	public double getValue()
	{
        double value = transaction.value;
        if (value != 0 && shouldReverseValue())
        {
            value *= -1.0f;
        }
		return value;
	}
	
	private boolean shouldReverseValue()
	{
		if (!getIsTransfer() && !isIncomeType || (getIsTransfer() && getIsReceivingParty()))
		{
			return true;
		}
		
		return false;
	}
	
	public void setValue(double value)
	{
        if (shouldReverseValue())
        {
            value *= -1.0f;
        }

		Log.e("sg.money", isIncomeType ? "isincometype" : "-");
		Log.e("sg.money", getIsTransfer() ? "istransfer" : "-");
		Log.e("sg.money", getIsReceivingParty() ? "isreceivingparty" : "-");
		Log.e("sg.money", "transaction value: " + transaction.value + ", value: " + value);
		if (transaction.value != value)
		{
			transaction.value = value;
			notifyObservers(this);
		}
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
		if (!transaction.description.equals(description)
			|| !relatedTransaction.description.equals(description))
		{
			transaction.description = description;
        	relatedTransaction.description = description;
			notifyObservers(this);
		}
	}

	public void setDate(Date date)
	{
		if (transaction.dateTime.compareTo(date) != 0
			|| relatedTransaction.dateTime.compareTo(date) != 0)
		{
			transaction.dateTime = date;
        	relatedTransaction.dateTime = date;
			notifyObservers(this);
		}
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
            if (entry.getValue().isIncome() == isIncomeType)
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
            if (entry.getValue().getId() == category.getId())
            {
                return entry.getKey();
            }
        }

        throw new RuntimeException("Category not found.");
    }
	
	public Category getCategory()
	{
		if (useNewCategory)
		{
			return newCategory;
		}
		
		if (cachedCategory != null 
			&& cachedCategory.getId() == transaction.id)
		{
			return cachedCategory;
		}
		
		//cache is invalid, so fetch from collection
		Category category = null;
		for(Category testCategory : getAllCategories())
		{
			if (testCategory.getId() == transaction.category)
			{
				category = testCategory;
				break;
			}
		}
		
		cachedCategory = category;
		return cachedCategory;
	}

    public void setCategory(Category category) {
		
		if (transaction.category != category.getId())
		{
        	transaction.category = category.getId();
        	cachedCategory = category;
        	notifyObservers(this);
		}
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
    		if (newCategory.getName().trim() == "")
    		{
	   			return "Please enter a name for the new category.";
    		}

    		for(Category currentCategory : getAllCategories())
        	{
        		if (newCategory.getName().trim().equals(currentCategory.getName().trim())
					&& currentCategory.isIncome() == newCategory.isIncome())
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
        	newCategory.setColor(Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
			DatabaseManager.getInstance(context).AddCategory(newCategory);
      
			transaction.category = newCategory.getId();
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
        relatedTransaction.account = account.getId();
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

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<AddTransactionModel> CREATOR = new Parcelable.Creator<AddTransactionModel>() {
        public AddTransactionModel createFromParcel(Parcel in) {
            return new AddTransactionModel(in);
        }

        public AddTransactionModel[] newArray(int size) {
            return new AddTransactionModel[size];
        }
    };

    private AddTransactionModel(Parcel in) {
        transaction = in.readParcelable(Transaction.class.getClassLoader());
        newTransaction = in.readInt() == 1;
        accountsMap = (HashMap<String, Account>)in.readSerializable();
        categoriesMap = (HashMap<String, Category>)in.readSerializable();
        cachedCategory = in.readParcelable(Category.class.getClassLoader());
        relatedTransaction = in.readParcelable(Transaction.class.getClassLoader());
        useNewCategory = in.readInt() == 1;
        newCategory = in.readParcelable(Category.class.getClassLoader());
        isIncomeType = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(transaction, flags);
        parcel.writeInt(newTransaction ? 1 : 0);
        parcel.writeSerializable((Serializable)accountsMap);
        parcel.writeSerializable((Serializable)categoriesMap);
        parcel.writeParcelable(cachedCategory, flags);
        parcel.writeParcelable(relatedTransaction, flags);
        parcel.writeInt(useNewCategory ? 1 : 0);
        parcel.writeParcelable(newCategory, flags);
        parcel.writeInt(isIncomeType ? 1 : 0);
    }

    /* End Implementation of Parcelable */
}
