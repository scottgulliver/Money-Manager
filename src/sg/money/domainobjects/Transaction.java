package sg.money.domainobjects;

import java.util.Date;
import android.content.Context;

import sg.money.DatabaseManager;

public class Transaction
{
	public int id;
	public double value;
	public String description;
	public int category;
	public Date dateTime;
	public int account;
	public boolean dontReport;
	public boolean isTransfer;
	public int transferToTransaction;
	public int transferFromTransaction;
	public boolean reconciled;
	
	public double getRealValue(Context context)
	{
		Category category = DatabaseManager.getInstance(context).GetCategory(this.category);
		return (category.income ? value : value * -1.0);
	}
	
	public Transaction getRelatedTransferTransaction(Context context)
	{
		if (!isTransfer)
			return null; //todo throw exception here.
		
		return DatabaseManager.getInstance(context).GetTransaction(transferFromTransaction != -1 
																	? transferFromTransaction 
																	: transferToTransaction);
	}
	
	public boolean isReceivingParty()
	{
		if (!isTransfer)
			return false; //todo throw exception here.
		
		return transferFromTransaction != -1;
	}
	
	public Account getAccount(Context context)
	{
		return DatabaseManager.getInstance(context).GetAccount(account);
	}
	
	public String getTransferDescription(Context context)
	{
		return "Transfer "+(isReceivingParty() ? "from " : "to ")+getRelatedTransferTransaction(context).getAccount(context).name;
	}
}
