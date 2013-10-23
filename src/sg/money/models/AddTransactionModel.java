package sg.money.models;

import android.content.*;
import android.graphics.*;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.*;

import sg.money.common.DatabaseManager;
import sg.money.domainobjects.*;

public class AddTransactionModel extends Observable implements Parcelable
{	
    private Transaction m_transaction;
    private boolean m_newTransaction;
    private HashMap<String, Account> m_accountsMap;
    private HashMap<String, Category> m_categoriesMap;
	private Category m_cachedCategory;
    private Transaction m_relatedTransaction; // other half of a transaction
	private boolean m_useNewCategory;
	private Category m_newCategory;
    private boolean m_isIncomeType;
	
	
	/* Constructor */

    public AddTransactionModel(Transaction transaction, int accountID, Context context) {
        m_transaction = transaction;
		
		m_accountsMap = new HashMap<String, Account>();
		m_categoriesMap = new HashMap<String, Category>();

        Date now = new Date();

        m_relatedTransaction = new Transaction();
        m_relatedTransaction.setDontReport(true);
        m_relatedTransaction.setTransferToTransaction(-1);
        m_relatedTransaction.setDescription("");
        m_relatedTransaction.setDateTime(now);
        m_relatedTransaction.setTransfer(true);

        //find a valid account to transfer to
        ArrayList<Account> allAccounts = DatabaseManager.getInstance(context).GetAllAccounts();
        for(Account account : allAccounts)
        {
            if (account.getId() != accountID)
            {
                m_relatedTransaction.setAccount(account.getId());
                break;
            }
        }

        if (m_transaction == null)
        {
            m_transaction = new Transaction();
			m_transaction.setAccount(accountID);
			m_transaction.setDateTime(now);
            m_transaction.setTransferFromTransaction(-1);
            m_transaction.setTransferToTransaction(-1);
			m_transaction.setDescription("");
            m_newTransaction = true;
        }
        else if (transaction.isTransfer())
        {
            m_relatedTransaction = transaction.getRelatedTransferTransaction(context);
        }

		m_useNewCategory = false;
		m_newCategory = new Category();

        ArrayList<Account> accounts = DatabaseManager.getInstance(context).GetAllAccounts();
        for(Account account : accounts)
        {
            if (account.getId() != accountID)
                m_accountsMap.put(account.getName(), account);
        }


        ArrayList<Category> categories = DatabaseManager.getInstance(context).GetAllCategories();
        categories = Category.getCategoriesInGroupOrder(categories);
        for(Category category : categories)
        {
            m_categoriesMap.put(category.getName(), category);
        }

        if (!m_newTransaction && !m_transaction.isTransfer())
        {
            m_isIncomeType = getCategory().isIncome();
        }
    }
	
	
	/* Getters / setters */

    public boolean getIsReceivingParty()
    {
        return m_transaction.isReceivingParty();
    }

	public double getValue()
	{
        double value = m_transaction.getValue();
        if (value != 0 && shouldReverseValue())
        {
            value *= -1.0f;
        }
		return value;
	}
	
	public void setValue(double value)
	{
        if (shouldReverseValue())
        {
            value *= -1.0f;
        }
		
		if (m_transaction.getValue() != value)
		{
			m_transaction.setValue(value);
            m_relatedTransaction.setValue(value * -1.0f);
			notifyObservers(this);
		}
	}

	public boolean getDontReport()
	{
		return m_transaction.isDontReport();
	}
	
	public void setDontReport(boolean dontReport)
	{
		if (m_transaction.isDontReport() != dontReport)
		{
			m_transaction.setDontReport(dontReport);
			notifyObservers(this);
		}
	}

	public String getDescription()
	{
		return m_transaction.getDescription();
	}
	
	public void setDescription(String description)
	{
		if (!m_transaction.getDescription().equals(description)
			|| !m_relatedTransaction.getDescription().equals(description))
		{
			m_transaction.setDescription(description);
        	m_relatedTransaction.setDescription(description);
			notifyObservers(this);
		}
	}

	public void setDate(Date date)
	{
		if (m_transaction.getDateTime().compareTo(date) != 0
			|| m_relatedTransaction.getDateTime().compareTo(date) != 0)
		{
			m_transaction.setDateTime(date);
        	m_relatedTransaction.setDateTime(date);
			notifyObservers(this);
		}
	}
	
	public Date getDate()
	{
		return m_transaction.getDateTime();
	}

	public boolean getIsTransfer()
	{
		return m_transaction.isTransfer();
	}

    public boolean isNewTransaction() {
        return m_newTransaction;
    }
	
	public Account[] getAccounts()
	{
		return m_accountsMap.values().toArray(new Account[m_accountsMap.values().size()]);
	}
	
	public String[] getAccountNames()
	{
		return m_accountsMap.keySet().toArray(new String[m_accountsMap.keySet().size()]);
	}
	
	public Category getCategory()
	{
		if (m_useNewCategory)
		{
			return m_newCategory;
		}
		
		if (m_cachedCategory != null 
			&& m_cachedCategory.getId() == m_transaction.getId())
		{
			return m_cachedCategory;
		}
		
		//cache is invalid, so fetch from collection
		Category category = null;
		for(Category testCategory : getAllCategories())
		{
			if (testCategory.getId() == m_transaction.getCategory())
			{
				category = testCategory;
				break;
			}
		}
		
		m_cachedCategory = category;
		return m_cachedCategory;
	}

    public void setCategory(Category category) {
		
		if (m_transaction.getCategory() != category.getId())
		{
        	m_transaction.setCategory(category.getId());
        	m_cachedCategory = category;
        	notifyObservers(this);
		}
    }

    public void setCategory(String categoryName) {
        Category category = m_categoriesMap.get(categoryName);
        setCategory(category);
    }

    public boolean getUseNewCategory() {
        return m_useNewCategory;
    }

    public void setUseNewCategory(boolean useNewCategory) {
        this.m_useNewCategory = useNewCategory;
    }

    public boolean isIncomeType() {
        return m_isIncomeType;
    }

    public void setIncomeType(boolean incomeType) {
        this.m_isIncomeType = incomeType;
    }

    public void setIsTransfer(boolean isTransfer) {
        m_transaction.setTransfer(isTransfer);
        m_transaction.setDontReport(isTransfer);
        notifyObservers(this);
    }

    public boolean isReceivingParty() {
        return m_transaction.isReceivingParty();
    }

    public void setTransferAccount(Account account)
    {
        if (m_relatedTransaction.getAccount() != account.getId())
        {
            m_relatedTransaction.setAccount(account.getId());
            notifyObservers(this);
        }
    }

    public Account getTransferAccount(Context context)
    {
        if (m_relatedTransaction.getAccount() != -1)
        {
            return m_relatedTransaction.getAccount(context);
        }

        return null;
    }
	
	
	/* Methods */

	private boolean shouldReverseValue()
	{
		if ((!getIsTransfer() && !m_isIncomeType) || (getIsTransfer() && !getIsReceivingParty()))
		{
			return true;
		}

		return false;
	}

	public ArrayList<String> getValidCategoryNames()
	{
        ArrayList<String> categoryNames = new ArrayList<String>();
        for(Map.Entry<String, Category> entry : m_categoriesMap.entrySet())
        {
            if (entry.getValue().isIncome() == m_isIncomeType)
            {
                categoryNames.add(entry.getKey());
            }
        }

        return categoryNames;
	}

    public Category[] getAllCategories()
    {
        return m_categoriesMap.values().toArray(new Category[m_categoriesMap.values().size()]);
    }

    public String validate()
    {
    	if (m_transaction.getValue() == 0)
    	{
    		return "Please enter a value.";
    	}

    	if (m_transaction.getDescription().trim().equals(""))
    	{
    		return "Please enter a description.";
    	}

    	if (m_useNewCategory)
    	{
    		if (m_newCategory.getName().trim() == "")
    		{
	   			return "Please enter a name for the new category.";
    		}

    		for(Category currentCategory : getAllCategories())
        	{
        		if (m_newCategory.getName().trim().equals(currentCategory.getName().trim())
					&& currentCategory.isIncome() == m_newCategory.isIncome())
            	{
            		return "A category with this name already exists.";
            	}
        	}
    	}

    	return null;
    }
	
	public void commit(Context context)
	{
    	if (m_useNewCategory)
    	{
        	Random rnd = new Random(System.currentTimeMillis());
        	m_newCategory.setColor(Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
			DatabaseManager.getInstance(context).AddCategory(m_newCategory);
      
			m_transaction.setCategory(m_newCategory.getId());
		}

		if (!m_transaction.isTransfer())
		{
    		if (m_newTransaction)
    			DatabaseManager.getInstance(context).InsertTransaction(m_transaction);
    		else
    			DatabaseManager.getInstance(context).UpdateTransaction(m_transaction);
    	}
    	else
    	{
            if (isNewTransaction())
            {
                DatabaseManager.getInstance(context).InsertTransaction(m_transaction);
                DatabaseManager.getInstance(context).InsertTransaction(m_relatedTransaction);
                m_transaction.setTransferToTransaction(m_relatedTransaction.getId());
                m_relatedTransaction.setTransferFromTransaction(m_transaction.getId());
                DatabaseManager.getInstance(context).UpdateTransaction(m_transaction);
                DatabaseManager.getInstance(context).UpdateTransaction(m_relatedTransaction);
            }
            else
            {
                DatabaseManager.getInstance(context).UpdateTransaction(m_transaction);
                DatabaseManager.getInstance(context).UpdateTransaction(m_relatedTransaction);
            }
		}
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
        m_transaction = in.readParcelable(Transaction.class.getClassLoader());
        m_newTransaction = in.readInt() == 1;
        m_accountsMap = (HashMap<String, Account>)in.readSerializable();
        m_categoriesMap = (HashMap<String, Category>)in.readSerializable();
        m_cachedCategory = in.readParcelable(Category.class.getClassLoader());
        m_relatedTransaction = in.readParcelable(Transaction.class.getClassLoader());
        m_useNewCategory = in.readInt() == 1;
        m_newCategory = in.readParcelable(Category.class.getClassLoader());
        m_isIncomeType = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(m_transaction, flags);
        parcel.writeInt(m_newTransaction ? 1 : 0);
        parcel.writeSerializable((Serializable)m_accountsMap);
        parcel.writeSerializable((Serializable)m_categoriesMap);
        parcel.writeParcelable(m_cachedCategory, flags);
        parcel.writeParcelable(m_relatedTransaction, flags);
        parcel.writeInt(m_useNewCategory ? 1 : 0);
        parcel.writeParcelable(m_newCategory, flags);
        parcel.writeInt(m_isIncomeType ? 1 : 0);
    }

    /* End Implementation of Parcelable */
}
