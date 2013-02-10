package sg.money;

import java.util.ArrayList;

public class Budget
{
	public int id;
	public String name;
	public double value;
	public ArrayList<Account> accounts = new ArrayList<Account>();
	public ArrayList<Category> categories = new ArrayList<Category>();
}