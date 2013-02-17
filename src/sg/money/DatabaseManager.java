package sg.money;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper
{
	static Context _context;
	private static DatabaseManager instance = null;
	
   public static DatabaseManager getInstance(Context context) {
	  _context = context;
      if(instance == null) {
         instance = new DatabaseManager(context);
      }
      return instance;
   }
	   
	static final String dbName="finance_test";
	
	static final String transTable="Transactions";
	static final String colTransID="TransactionID";
	static final String colTransValue="Value";
	static final String colTransDesc="Description";
	static final String colTransCategory="CategoryID";
	static final String colTransDate="Date";
	static final String colTransAccount="AccountID";

	static final String configTable="Config";
	static final String colConfigID="ConfigID";
	static final String colConfigName="Name";
	static final String colConfigValue="Value";

	static final String categoriesTable="Categories";
	static final String colCategoriesID="CategoryID";
	static final String colCategoriesName="Name";
	static final String colCategoriesColor="Color";
	static final String colCategoriesIsIncome="IsIncome";
	static final String colCategoriesIsPermanent="IsPermanent";

	static final String accountsTable="Accounts";
	static final String colAccountsID="AccountID";
	static final String colAccountsName="Name";
	static final String colAccountsValue="Color";

	static final String budgetsTable="Budgets";
	static final String colBudgetsID="BudgetID";
	static final String colBudgetsName="Name";
	static final String colBudgetsValue="Value";
	static final String colBudgetsNotify="NotifyType";

	static final String budgetLinksTable="BudgetLinks";
	static final String colBudgetLinksID="ID";
	static final String colBudgetLinksBudgetID="BudgetID";
	static final String colBudgetLinksForeignID="ForeignID"; // 1 = category, 2 = account
	static final String colBudgetLinksForeignType="ForeignType";

	DateFormat df;
	
	static SQLiteDatabase currentDbInstance = null;
	static int databaseOriginLevel = 0;
	static final int DATABASE_READ_MODE = 0;
	static final int DATABASE_WRITE_MODE = 1;
	
	private static void declareDatabase(SQLiteDatabase db)
	{
		currentDbInstance = db;
		databaseOriginLevel = 1;
	}
	
	private static SQLiteDatabase getDatabase(DatabaseManager instance, int databaseAccessMode)
	{
		if (databaseOriginLevel == 0)
		{
			if (databaseAccessMode == DATABASE_WRITE_MODE)
				currentDbInstance = instance.getWritableDatabase();
			else
				currentDbInstance = instance.getReadableDatabase();
		}
		
		databaseOriginLevel++;
		return currentDbInstance; 
	}
	
	private static void clearDatabase()
	{
		databaseOriginLevel--;

		if (databaseOriginLevel == 0)
			currentDbInstance = null;
	}
	 
	protected DatabaseManager(Context context)
	{
		super(context, dbName, null, 15); 
		
		df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.ENGLISH);
	}
	
	public void onCreate(SQLiteDatabase db)
	{
		declareDatabase(db);
		
		String sql = "CREATE TABLE "+transTable+" ("+colTransID+ " INTEGER PRIMARY KEY , "+
		colTransValue+ " REAL , "+colTransDesc+" TEXT , "+colTransCategory+" INTEGER , " +colTransDate+" DATETIME , "+
		colTransAccount + " INTEGER )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+configTable+" ("+colConfigID+ " INTEGER PRIMARY KEY , "+
		colConfigName+ " TEXT , "+colConfigValue+" TEXT )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+categoriesTable+" ("+colCategoriesID+ " INTEGER PRIMARY KEY , "+
		colCategoriesName+ " TEXT , "+colCategoriesColor+" INTEGER , "+colCategoriesIsIncome+" INTEGER, "+
		colCategoriesIsPermanent+" INTEGER default 0)";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+accountsTable+" ("+colAccountsID+ " INTEGER PRIMARY KEY , "+
		colAccountsName+" TEXT , "+colAccountsValue+" REAL )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		createBudgetsTables();
		
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Car Expenses',-6744954,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Cash Withdrawal',-9463754,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Childcare',-14962744,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Education',-1443965,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Clothing',-4472573,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Personal Care',-15840501,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Books & Magazines',-5493947,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Games',-14371514,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Going Out',-5155917,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Leisure & Hobbies',-9442770,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Music',-1111693,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Resturants & Bars',-7757600,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Sports & Fitness',-13370380,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Groceries',-7829092,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Healthcare',-13182160,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Holidays',-1116446,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Home Expenses',-3117841,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Household Goods',-7774149,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Interest Paid',-1478934,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Bill Payment',-7072528,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+") VALUES ('Starting Balance',-1416974,0,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Standing Order',-5007468,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Transfer (Out)',-15638137,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Gifts',-9107353,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Pet Expenses',-6883084,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Transportation',-6343758,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Misc (Out)',-3343758,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Benefit Recieved',-7473619,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Bonus Recieved',-9491295,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Business Income',-9726782,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Salary',-15940294,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Pension Income',-5494270,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Transfer (In)',-16230878,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Loan',-12416974,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+") VALUES ('Starting Balance',-2416974,1,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Misc (In)',-5416974,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		
		sql = "INSERT INTO "+accountsTable+" ("+colAccountsName+","+colAccountsValue+") VALUES ('Cash', 0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+accountsTable+" ("+colAccountsName+","+colAccountsValue+") VALUES ('Bank Account', 0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);

		clearDatabase();
	}
	
	private void createBudgetsTables()
	{
		SQLiteDatabase db= getDatabase(this, DATABASE_WRITE_MODE);
		
		String sql = "CREATE TABLE "+budgetsTable+" ("+colBudgetsID+ " INTEGER PRIMARY KEY , "+
				colBudgetsName+" TEXT , "+colBudgetsValue+" REAL , "+colBudgetsNotify+" INTEGER )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+budgetLinksTable+" ("+colBudgetLinksID+ " INTEGER PRIMARY KEY , "
				+colBudgetLinksBudgetID+" INTEGER , "+colBudgetLinksForeignID+" INTEGER , "+colBudgetLinksForeignType+" INTEGER )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{ 
		declareDatabase(db);
		Log.v("MONEY", "oldDbVersion: "+String.valueOf(oldVersion));
		
		if (oldVersion <= 11)
		{
			//cleanup relations which don't exist
			ArrayList<Transaction> transactions = GetAllTransactions();
			ArrayList<Transaction> transactionsToRemove = new ArrayList<Transaction>();
			ArrayList<Category> categories = GetAllCategories();
			ArrayList<Account> accounts = GetAllAccounts();
			
			for(Transaction transaction : transactions)
			{
				boolean validCategory = false;
				boolean validAccount = false;

				for(Category category : categories)
				{
					if (category.id == transaction.category)
					{
						validCategory = true;
						break;
					}
				}
				for(Account account : accounts)
				{
					if (account.id == transaction.account)
					{
						validAccount = true;
						break;
					}
				}
				
				if (!validAccount || !validCategory)
					transactionsToRemove.add(transaction);
			}
			
			for(Transaction transaction : transactionsToRemove)
				DeleteTransaction(transaction);
		}
		
		if (oldVersion <= 12)
		{
			createBudgetsTables();
		}
		
		if (oldVersion <= 13)
		{
			String sql = "ALTER TABLE "+budgetsTable+" ADD COLUMN "+colBudgetsNotify+" INTEGER";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		if (oldVersion <= 14)
		{
			String sql = "ALTER TABLE "+categoriesTable+" ADD COLUMN "+colCategoriesIsPermanent+" INTEGER default 0";
			Log.i("SQL", sql);
			db.execSQL(sql);
			
			//check categories exist
			ArrayList<Category> categories = GetAllCategories();
			Category incomeStartBalance = null;
			Category expenseStartBalance = null;
			for(Category category : categories)
			{
				if (category.name.equals("Starting Balance") && category.income)
					incomeStartBalance = category;
				else if (category.name.equals("Starting Balance") && !category.income)
					expenseStartBalance = category;
			}

			if (incomeStartBalance == null)
			{
				AddCategory(new Category("Starting Balance", -2416974, true, true));
			}
			else
			{
				incomeStartBalance.isPermanent = true;
				UpdateCategory(incomeStartBalance);
			}
			
			if (expenseStartBalance == null)
			{
				AddCategory(new Category("Starting Balance", -1416974, false, true));
			}
			else
			{
				expenseStartBalance.isPermanent = true;
				UpdateCategory(expenseStartBalance);
			}
		}
		
		clearDatabase();
	}
	
	
	/* ********************** TRANSACTIONS ******************************************/
	
	public void InsertTransaction(Transaction trans)
	{
		String desc = trans.description.replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "INSERT INTO "+transTable+" ("+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransDate+","+colTransAccount+") VALUES ("+
								trans.value+",'"+desc+"',"+trans.category+",'"+df.format(trans.dateTime)+"',"+trans.account+")";
		Log.i("SQL", sql);
		
		db.execSQL(sql);
	}
	
	public ArrayList<Transaction> GetAllTransactions(Date startDate, Date endDate)
	{
		try
		{
			SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+" FROM "+transTable;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			while (c.isAfterLast() == false)
			{
				Transaction transaction = new Transaction();
				transaction.id = c.getInt((c.getColumnIndex(colTransID)));
				transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
				transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
				transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
				Date theDate = (Date)df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.dateTime = theDate;
				transaction.account = c.getInt((c.getColumnIndex(colTransAccount)));
				
				if (transaction.dateTime.compareTo(startDate) >= 0 && transaction.dateTime.compareTo(endDate) <= 0)
					transactions.add(transaction);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return transactions; 
		}
		catch(Exception ex)
		{
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public ArrayList<Transaction> GetAllTransactions()
	{
		try
		{
			SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+" FROM "+transTable+"";
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			while (c.isAfterLast() == false)
			{
				Transaction transaction = new Transaction();
				transaction.id = c.getInt((c.getColumnIndex(colTransID)));
				transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
				transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
				transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
				Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.dateTime = theDate;
				transaction.account = c.getInt((c.getColumnIndex(colTransAccount)));
				transactions.add(transaction);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return transactions; 
		}
		catch(Exception ex)
		{
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public ArrayList<Transaction> GetAllTransactions(int accountID)
	{
		try
		{
			SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransDate+" FROM "+transTable+" WHERE "+colTransAccount+" = "+accountID;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			while (c.isAfterLast() == false)
			{
				Transaction transaction = new Transaction();
				transaction.id = c.getInt((c.getColumnIndex(colTransID)));
				transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
				transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
				transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
				Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.dateTime = theDate;
				transaction.account = accountID;
				transactions.add(transaction);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return transactions; 
		}
		catch(Exception ex)
		{
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public Transaction GetTransaction(int id)
	{
		try
		{
			SQLiteDatabase db=this.getReadableDatabase();
			
			String sql = "SELECT "+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+" FROM "+transTable+" WHERE "+colTransID+"="+id;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			Transaction transaction = new Transaction();
			transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
			transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
			transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
			Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
			transaction.dateTime = theDate;
			transaction.account = c.getInt((c.getColumnIndex(colTransAccount)));
			transaction.id = id;
			
			c.close();
			
			return transaction; 
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	public void DeleteTransaction(Transaction transaction)
	{
		SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
		
		String sql = "DELETE FROM "+transTable+" WHERE "+colTransID+" = "+transaction.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
	public void UpdateTransaction(Transaction trans)
	{
		String desc = trans.description.replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "UPDATE "+transTable+" SET "+colTransValue+" = "+trans.value+", "+colTransDesc+" = '"
						+desc+"', "+colTransCategory+" = "+trans.category+", "+colTransDate+" = '"
						+df.format(trans.dateTime)+"', "+colTransAccount+" = "+trans.account+" WHERE "+colTransID+" = "+trans.id;
		Log.i("SQL", sql);
		
		db.execSQL(sql); 
	}
	
	
	/* ********************** CONFIGURATION ******************************************/
	
	public void SetConfiguration(String name, String value)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "DELETE FROM "+configTable+" WHERE "+colConfigName+" = '"+name+"'";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "INSERT INTO "+configTable+" ("+colConfigName+", "+colConfigValue+") VALUES ('"+name+"', '"+value+"') ";
		Log.i("SQL", sql);
		db.execSQL(sql);
	}
	
	public String GetConfiguration(String name)
	{
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "SELECT "+colConfigValue+" FROM "+configTable+" WHERE "+colConfigName+" = '"+name+"'";
		Log.i("SQL", sql);
		
		Cursor c = db.rawQuery(sql , null);
		c.moveToFirst();
		
		String value = "";
		
		while (c.isAfterLast() == false)
		{
			value = c.getString((c.getColumnIndex(colConfigValue)));
       	    c.moveToNext();
        }
		
		c.close();
		
		return value;
	}
	
	
	/* ********************** CATEGORIES ******************************************/
	
	public void AddCategory(Category category)
	{
		String name = category.name.replace("'", "''");
		
		SQLiteDatabase db=getDatabase(this, DATABASE_WRITE_MODE);
		
		String sql = "INSERT INTO "+categoriesTable+" ("
										+colCategoriesName+","
										+colCategoriesColor+","
										+colCategoriesIsIncome+","
										+colCategoriesIsPermanent+
											") VALUES ('"
										+name+"',"
										+category.color+","
										+(category.income?1:0)+","
										+(category.isPermanent?1:0)
										+") ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		ArrayList<Category> categories = GetAllCategories();
		for(Category thisCategory : categories)
		{
			if (thisCategory.name.equals(category.name) && 
				thisCategory.income == category.income && 
				thisCategory.color == category.color && 
				thisCategory.isPermanent == category.isPermanent)
			{
				category.id = thisCategory.id;
				break;
			}
		}
		
		clearDatabase();
	}
	
	public ArrayList<Category> GetAllCategories()
	{
		try
		{
			SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colCategoriesID+","+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+" FROM "+categoriesTable+"";
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Category> categories = new ArrayList<Category>();
			
			while (c.isAfterLast() == false)
			{
				Category caetgory = new Category();
				caetgory.id = c.getInt((c.getColumnIndex(colCategoriesID)));
				caetgory.name = c.getString((c.getColumnIndex(colCategoriesName)));
				caetgory.color = c.getInt((c.getColumnIndex(colCategoriesColor)));
				caetgory.income = (c.getInt(c.getColumnIndex(colCategoriesIsIncome)) == 1);
				caetgory.isPermanent = (c.getInt(c.getColumnIndex(colCategoriesIsPermanent)) == 1);
				categories.add(caetgory);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return categories; 
		}
		catch(Exception ex)
		{
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public Category GetCategory(int id)
	{
		try
		{
			SQLiteDatabase db=this.getReadableDatabase();
			
			String sql = "SELECT "+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+" FROM "+categoriesTable+" WHERE "+colCategoriesID+" = "+id;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			Category category = new Category();
			
			while (c.isAfterLast() == false)
			{
				category.id = id;
				category.name = c.getString((c.getColumnIndex(colCategoriesName)));
				category.color = c.getInt((c.getColumnIndex(colCategoriesColor)));
				category.income = (c.getInt(c.getColumnIndex(colCategoriesIsIncome)) == 1);
				category.isPermanent = (c.getInt(c.getColumnIndex(colCategoriesIsPermanent)) == 1);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return category; 
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	public void DeleteCategory(Category category)
	{
		SQLiteDatabase db=getDatabase(this, DATABASE_WRITE_MODE);
		
		String sql = "DELETE FROM "+categoriesTable+" WHERE "+colCategoriesID+" = "+category.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		ArrayList<Transaction> transactions = GetAllTransactions();
		for(Transaction transaction : transactions)
		{
			if (transaction.category == category.id)
				DeleteTransaction(transaction);
		}
		
		clearDatabase();
	}
	
	public void UpdateCategory(Category category)
	{
		SQLiteDatabase db=getDatabase(this, DATABASE_WRITE_MODE);
		
		String name = category.name.replace("'", "''");
		
		String sql = "UPDATE "+categoriesTable+" SET "
							+colCategoriesName+" = '"+name
							+"',"+colCategoriesIsIncome+" = "+(category.income?"1":"0")
							+","+colCategoriesIsPermanent+" = "+(category.isPermanent?"1":"0")
							+","+colCategoriesColor+" = "+(category.color)
							+" WHERE "+colCategoriesID+" = "+category.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
/* ********************** ACCOUNTS ******************************************/
	
	public void AddAccount(Account account)
	{
		String name = account.name.replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "INSERT INTO "+accountsTable+" ("+colAccountsName+","+colAccountsValue+
													") VALUES ('"+name+"',"+account.value+") ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		//find the new id of this account
		sql = "SELECT "+colAccountsID+" FROM "+accountsTable+" WHERE "+colAccountsName+" = '"+name+"'";
		Log.i("SQL", sql);
		Cursor c = db.rawQuery(sql , null);
		c.moveToFirst();
		account.id = c.getInt(c.getColumnIndex(colAccountsID));
		c.close();
	}
	
	public ArrayList<Account> GetAllAccounts()
	{
		try
		{
			SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colAccountsID+","+colAccountsName+","+colAccountsValue+" FROM "+accountsTable+"";
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Account> accounts = new ArrayList<Account>();
			
			while (c.isAfterLast() == false)
			{
				Integer id = c.getInt((c.getColumnIndex(colAccountsID)));
				String name = c.getString((c.getColumnIndex(colAccountsName)));
				
				Account account = new Account(_context, id, name);
				accounts.add(account);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return accounts; 
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public Account GetAccount(int id)
	{
		try
		{
			SQLiteDatabase db=this.getReadableDatabase();
			
			String sql = "SELECT "+colAccountsName+","+colAccountsValue+" FROM "+accountsTable+" WHERE "+colAccountsID+" = "+id;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			Account account = null;
			while (c.isAfterLast() == false)
			{
				String name = c.getString((c.getColumnIndex(colAccountsName)));
				
				account = new Account(_context, id, name);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return account; 
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	public void DeleteAccount(Account account)
	{
		SQLiteDatabase db=getDatabase(this, DATABASE_WRITE_MODE);
		
		String sql = "DELETE FROM "+accountsTable+" WHERE "+colAccountsID+" = "+account.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		ArrayList<Transaction> transactions = GetAllTransactions();
		for(Transaction transaction : transactions)
		{
			if (transaction.account == account.id)
				DeleteTransaction(transaction);
		}
		
		clearDatabase();
	}
	
	public void UpdateAccount(Account account)
	{
		String name = account.name.replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "UPDATE "+accountsTable+" SET "+colAccountsName+" = '"+name+"' WHERE "+colAccountsID+" = "+account.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
	}
	
	
/* ************************* Budgets ****************************************************/
	
	public void AddBudget(Budget budget)
	{
		SQLiteDatabase db = getDatabase(this, DATABASE_WRITE_MODE);
		
		String name = budget.name.replace("'", "''");
		
		String sql = "INSERT INTO "+budgetsTable+" ("+colBudgetsName+","+colBudgetsValue+","+colBudgetsNotify+
													") VALUES ('"+name+"',"+budget.value+","+budget.notifyType+") ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		//find the actual id of this budget
		sql = "SELECT "+colBudgetsID+" FROM "+budgetsTable+" WHERE "+colBudgetsName+" = '"+name+"'";
		Log.i("SQL", sql);
		Cursor c = db.rawQuery(sql , null);
		c.moveToFirst();
		budget.id = c.getInt(c.getColumnIndex(colBudgetsID));
		c.close();
		
		for(Category category : budget.categories) 
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.id+","+category.id+","+1+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		for(Account account : budget.accounts)
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.id+","+account.id+","+2+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		clearDatabase();
	}
	
	public ArrayList<Budget> GetAllBudgets()
	{
		try
		{
			SQLiteDatabase db = getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colBudgetsID+","+colBudgetsName+","+colBudgetsValue+","+colBudgetsNotify+" FROM "+budgetsTable+"";
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Budget> budgets = new ArrayList<Budget>();
			
			while (c.isAfterLast() == false)
			{
				Budget budget = new Budget();
				budget.id = c.getInt((c.getColumnIndex(colBudgetsID)));
				budget.name = c.getString((c.getColumnIndex(colBudgetsName)));
				budget.value = c.getDouble((c.getColumnIndex(colBudgetsValue)));
				budget.notifyType = c.getInt((c.getColumnIndex(colBudgetsNotify)));
				
				/* find links */
				sql = "SELECT "+colBudgetLinksForeignID+","+colBudgetLinksForeignType+" FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.id;
				Log.i("SQL", sql);
				
				Cursor linkCur = db.rawQuery(sql , null);
				linkCur.moveToFirst();
				while (linkCur.isAfterLast() == false)
				{
					if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 1)
					{
						Category category = GetCategory(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.categories.add(category);
					}
					else if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 2)
					{
						Account account = GetAccount(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.accounts.add(account);
					}
					linkCur.moveToNext();
				}
				linkCur.close();
				/* end find links */
				
				budgets.add(budget);
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return budgets; 
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public Budget GetBudget(int id)
	{
		try
		{
			SQLiteDatabase db = getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colBudgetsName+","+colBudgetsValue+","+colBudgetsNotify+" FROM "+budgetsTable+" WHERE "+colBudgetsID+" = "+id;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();

			Budget budget = new Budget();
			
			while (c.isAfterLast() == false)
			{
				budget.id = id;
				budget.name = c.getString((c.getColumnIndex(colBudgetsName)));
				budget.value = c.getDouble((c.getColumnIndex(colBudgetsValue)));
				budget.notifyType = c.getInt((c.getColumnIndex(colBudgetsNotify)));
				
				/* find links */
				sql = "SELECT "+colBudgetLinksForeignID+","+colBudgetLinksForeignType+" FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.id;
				Log.i("SQL", sql);
				
				Cursor linkCur = db.rawQuery(sql , null);
				linkCur.moveToFirst();
				while (linkCur.isAfterLast() == false)
				{
					if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 1)
					{
						Category category = GetCategory(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.categories.add(category);
					}
					else if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 2)
					{
						Account account = GetAccount(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.accounts.add(account);
					}
					linkCur.moveToNext();
				}
				linkCur.close();
				/* end find links */
				
	       	    c.moveToNext();
	        }
			
			c.close();
			
			return budget; 
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
		finally
		{
			clearDatabase();
		}
	}
	
	public void DeleteBudget(Budget budget)
	{
		SQLiteDatabase db = getDatabase(this, DATABASE_WRITE_MODE);
		
		String sql = "DELETE FROM "+budgetsTable+" WHERE "+colBudgetsID+" = "+budget.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "DELETE FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
	public void UpdateBudget(Budget budget)
	{
		SQLiteDatabase db = getDatabase(this, DATABASE_WRITE_MODE);
		
		String name = budget.name.replace("'", "''");
		
		String sql = "UPDATE "+budgetsTable+" SET "+colBudgetsName+" = '"+name+"', "+colBudgetsValue+" = "+budget.value+", "+colBudgetsNotify+" = "+budget.notifyType+" WHERE "+colBudgetsID+" = "+budget.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "DELETE FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		for(Category category : budget.categories)
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.id+","+category.id+","+1+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		for(Account account : budget.accounts)
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.id+","+account.id+","+2+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		clearDatabase();
	}
}






