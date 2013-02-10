package sg.money;

import java.util.Date;

import android.app.Activity;

public class Transaction
{
	public int id;
	public double value;
	public String description;
	public int category;
	public Date dateTime;
	public int account;
	
	public double getRealValue(Activity activity)
	{
		Category category = DatabaseManager.getInstance(activity).GetCategory(this.category);
		return (category.income ? value : value * -1.0);
	}
}
