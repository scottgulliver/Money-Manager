package sg.money.models;

import sg.money.common.DatabaseManager;
import sg.money.domainobjects.Account;
import android.content.*;
import android.os.Parcel;
import android.os.Parcelable;

import sg.money.domainobjects.*;
import java.util.*;

public class AddAccountModel extends Observable implements Parcelable {

    private Account m_account;
	private Double m_startingBalance;
    private boolean m_newAccount;
	private ArrayList<Account> m_currentAccounts;
	
	
	/* Constructor */

    public AddAccountModel(Account account) {
        m_account = account;
        if (m_account == null)
        {
            m_account = new Account("");
            m_newAccount = true;
        }

        m_startingBalance = 0d;
    }
	
	
	/* Getters / setters */

    public String getAccountName() {
        return m_account.getName();
    }

    public void setAccountName(String accountName) {
        m_account.setName(accountName);
		notifyObservers(this);
    }

    public Double getStartingBalance() {
        return m_startingBalance;
    }

    public void setStartingBalance(Double startingBalance) {
        this.m_startingBalance = startingBalance;
		notifyObservers(this);
    }

    public boolean isNewAccount()
    {
        return m_newAccount;
    }
	
	private ArrayList<Account> getCurrentAccounts(Context context)
	{
		if (m_currentAccounts == null)
		{
			m_currentAccounts = DatabaseManager.getInstance(context).GetAllAccounts();
		}
		
		return m_currentAccounts;
	}
	
	
	/* Methods */
	
	public String validate(Context context)
	{
    	if (m_account.getName().trim().equals(""))
    	{
    		return "Please enter a name.";
    	}

    	for(Account currentAccount : getCurrentAccounts(context))
    	{
    		if (currentAccount.getId() == m_account.getId()) 
				continue;

    		if (m_account.getName().trim().equals(currentAccount.getName().trim()))
        	{
        		return "An account with this name already exists.";
        	}
    	}

    	return null;
    }
	
	public void commit(Context context)
	{
		if (isNewAccount())
		{
			DatabaseManager.getInstance(context).AddAccount(m_account);
			
			if (m_startingBalance != 0.0)
			{
				Transaction transaction = new Transaction();
				transaction.setAccount(m_account.getId());
				transaction.setDescription("Starting balance for account");
				transaction.setDateTime(Calendar.getInstance().getTime());
				transaction.setValue(m_startingBalance);

				ArrayList<Category> categories = DatabaseManager.getInstance(context).GetAllCategories();
				for(Category category : categories)
				{
					if (category.getName().equals("Starting Balance") && category.isIncome() == m_startingBalance > 0)
					{
						transaction.setCategory(category.getId());
						break;
					}
				}

				DatabaseManager.getInstance(context).InsertTransaction(transaction);
			}
		}
		else
		{
			DatabaseManager.getInstance(context).UpdateAccount(m_account);
		}
	}

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<AddAccountModel> CREATOR = new Parcelable.Creator<AddAccountModel>() {
        public AddAccountModel createFromParcel(Parcel in) {
            return new AddAccountModel(in);
        }

        public AddAccountModel[] newArray(int size) {
            return new AddAccountModel[size];
        }
    };

    private AddAccountModel(Parcel in) {
        m_account = in.readParcelable(Account.class.getClassLoader());
        m_startingBalance = in.readDouble();
        m_newAccount = in.readInt() == 1;
        m_currentAccounts = new ArrayList<Account>(Arrays.asList((Account[])in.readParcelableArray(Account.class.getClassLoader())));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(m_account, flags);
        parcel.writeDouble(m_startingBalance);
        parcel.writeInt(m_newAccount ? 1 : 0);
        parcel.writeParcelableArray((Parcelable[])m_currentAccounts.toArray(), flags);
    }

    /* End Implementation of Parcelable */
}
