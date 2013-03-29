package sg.money;

import java.util.Date;
import android.content.Context;

public class Transaction
{
	public int id;
	public double value;
	public String description;
	public int category;
	public Date dateTime;
	public int account;
	public boolean dontReport;
	
	public double getRealValue(Context context)
	{
		Category category = DatabaseManager.getInstance(context).GetCategory(this.category);
		return (category.income ? value : value * -1.0);
	}
}
