package sg.money;

public class Category
{
	public int id;
	public String name;
	public int color;
	public boolean income;
	public boolean isPermanent = false;
	
	public Category()
	{
	}
	
	public Category(String name, int color, boolean income, boolean isPermanent)
	{
		this.name = name;
		this.color = color;
		this.income = income;
		this.isPermanent = isPermanent;
	}
}
