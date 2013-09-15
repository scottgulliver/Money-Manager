package sg.money.models;

import sg.money.domainobjects.Account;
import android.content.*;
import sg.money.*;
import sg.money.domainobjects.*;
import java.util.*;

public class AddAccountModel extends SimpleObservable {

    private Account account;
	private Double startingBalance;
    private boolean newAccount;
	private ArrayList<Account> currentAccounts;

    public AddAccountModel() {
        this(new Account(""));
        newAccount = true;
    }

    public AddAccountModel(Account account) {
        this.account = account;
    }

    public String getAccountName() {
        return account.name;
    }

    public void setAccountName(String accountName) {
        account.name = accountName;
    }

    public Double getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(Double startingBalance) {
        this.startingBalance = startingBalance;
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
    	if (account.name.trim().equals(""))
    	{
    		return "Please enter a name.";
    	}

    	for(Account currentAccount : getCurrentAccounts(context))
    	{
    		if (currentAccount.id == account.id) 
				continue;

    		if (account.name.trim().equals(currentAccount.name.trim()))
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
				transaction.account = account.id;
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
}
