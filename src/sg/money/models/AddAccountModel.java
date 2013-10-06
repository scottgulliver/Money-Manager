package sg.money.models;

import sg.money.domainobjects.Account;
import android.content.*;
import android.os.Parcel;
import android.os.Parcelable;

import sg.money.*;
import sg.money.domainobjects.*;
import java.util.*;

public class AddAccountModel extends SimpleObservable implements Parcelable {

    private Account account;
	private Double startingBalance;
    private boolean newAccount;
	private ArrayList<Account> currentAccounts;

    public AddAccountModel(Account account) {
        this.account = account;
        if (this.account == null)
        {
            this.account = new Account("");
            newAccount = true;
        }

        startingBalance = 0d;
    }

    public String getAccountName() {
        return account.getName();
    }

    public void setAccountName(String accountName) {
        account.setName(accountName);
		notifyObservers(this);
    }

    public Double getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(Double startingBalance) {
        this.startingBalance = startingBalance;
		notifyObservers(this);
    }

    public boolean isNewAccount()
    {
        return newAccount;
    }
	
	private ArrayList<Account> getCurrentAccounts(Context context)
	{
		if (currentAccounts == null)
		{
			currentAccounts = DatabaseManager.getInstance(context).GetAllAccounts();
		}
		
		return currentAccounts;
	}
	
	public String validate(Context context)
	{
    	if (account.getName().trim().equals(""))
    	{
    		return "Please enter a name.";
    	}

    	for(Account currentAccount : getCurrentAccounts(context))
    	{
    		if (currentAccount.getId() == account.getId()) 
				continue;

    		if (account.getName().trim().equals(currentAccount.getName().trim()))
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
			DatabaseManager.getInstance(context).AddAccount(account);
			
			if (startingBalance != 0.0)
			{
				Transaction transaction = new Transaction();
				transaction.account = account.getId();
				transaction.description = "Starting balance for account";
				transaction.dateTime = Calendar.getInstance().getTime();
				transaction.value = startingBalance;

				ArrayList<Category> categories = DatabaseManager.getInstance(context).GetAllCategories();
				for(Category category : categories)
				{
					if (category.name.equals("Starting Balance") && category.income == startingBalance > 0)
					{
						transaction.category = category.id;
						break;
					}
				}

				DatabaseManager.getInstance(context).InsertTransaction(transaction);
			}
		}
		else
		{
			DatabaseManager.getInstance(context).UpdateAccount(account);
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
        account = in.readParcelable(Account.class.getClassLoader());
        startingBalance = in.readDouble();
        newAccount = in.readInt() == 1;
        currentAccounts = new ArrayList<Account>(Arrays.asList((Account[])in.readParcelableArray(Account.class.getClassLoader())));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(account, flags);
        parcel.writeDouble(startingBalance);
        parcel.writeInt(newAccount ? 1 : 0);
        parcel.writeParcelableArray((Parcelable[])currentAccounts.toArray(), flags);
    }

    /* End Implementation of Parcelable */
}
