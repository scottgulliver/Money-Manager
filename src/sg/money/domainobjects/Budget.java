package sg.money.domainobjects;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;

import sg.money.DatabaseManager;

public class Budget
{
	public enum NotificationType{
		None,
		Daily,
		Weekly,
		Monthly
	}
	
	public int id;
	public String name;
	public double value;
	public NotificationType notifyType;
	public ArrayList<Account> accounts = new ArrayList<Account>();
	public ArrayList<Category> categories = new ArrayList<Category>();
	
	/* test comment for budget class */
	
	public double getSpent(Context context, Calendar startDate, Calendar endDate)
	{
		ArrayList<Transaction> transactions = DatabaseManager.getInstance(context).GetAllTransactions(startDate.getTime(), endDate.getTime());
		double spending = 0;
        for(Transaction transaction : transactions)
        {
        	if (!DatabaseManager.getInstance(context).GetCategory(transaction.category).useInReports)
        		continue;
        	
        	if (!accounts.isEmpty())
        	{
            	boolean isAccout = false;
            	for(Account account : accounts)
            	{
            		if (transaction.account == account.id)
        			{
        				isAccout = true;
        				break;
        			}
            	}
            	if (!isAccout)
            		continue;
        	}
        	if (!categories.isEmpty())
        	{
            	boolean isCategory = false;
            	for(Category category : categories)
            	{
            		if (transaction.category == category.id)
            		{
            			isCategory = true;
        				break;
        			}
            	}
            	if (!isCategory)
            		continue;
        	}
        	spending += transaction.getRealValue(context);
        }
        
        return spending;
	}
	
	public String getCompletePercentage(Context context, Calendar startDate, Calendar endDate)
	{
		return getCompletePercentage(getSpent(context, startDate, endDate));
	}
	
	public String getCompletePercentage(double spent)
	{
		DecimalFormat df = new DecimalFormat("#");
		double percentageConversion = 100.0 / value;
		percentageConversion *= spent;
		return df.format(percentageConversion) + "%";
	}
}
