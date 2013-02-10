package sg.money;

import java.util.ArrayList;
import android.content.Context;

public class Account
{  
	public int id;
	public String name;
	public double value; 
	
	public Account(String name)
	{
		this.name = name;
	}
	
	public Account(Context context, int id, String name)
	{
		this.id = id;
		this.name = name;
		
		if (id > -1)
		{
			ArrayList<Transaction> transactions = DatabaseManager.getInstance(context).GetAllTransactions(id);
			for(Transaction transaction : transactions)
				value += transaction.value;
		}
	}
}

