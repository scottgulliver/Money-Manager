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
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;

public class DatabaseManager extends SQLiteOpenHelper
{
    private static Context s_context;
	private static DatabaseManager s_instance;
	
    public static DatabaseManager getInstance(Context context) {
        s_context = context;
        if(s_instance == null) {
            s_instance = new DatabaseManager(context);
        }
        return s_instance;
    }

    static final String dbName="finance_test";
	
	static final String transTable="Transactions";
	static final String colTransID="TransactionID";
	static final String colTransValue="Value";
	static final String colTransDesc="Description";
	static final String colTransCategory="CategoryID";
	static final String colTransDate="Date";
	static final String colTransAccount="AccountID";
	static final String colTransDontReport="DontReport";
	static final String colTransIsTransfer="IsTransfer";
	static final String colTransToTrans="ToTrans";
	static final String colTransFromTrans="FromTrans";
	static final String colTransReconciled="Reconciled";

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
	static final String colCategoriesUseInReports="UseInReports";
	static final String colCategoriesParent="ParentCategory";

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
		super(context, dbName, null, 22);
		
		df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.ENGLISH);
	}
	
	public void onCreate(SQLiteDatabase db)
	{
		declareDatabase(db);
		
		String sql = "CREATE TABLE "+transTable+" ("+
						colTransID+ " INTEGER PRIMARY KEY , "+
						colTransValue+ " REAL , "+
						colTransDesc+" TEXT , "+
						colTransCategory+" INTEGER , "+
						colTransDate+" DATETIME , "+
						colTransAccount + " INTEGER, "+
						colTransDontReport+" INTEGER ,"+
						colTransIsTransfer + " INTEGER, "+
						colTransFromTrans + " INTEGER, "+
						colTransToTrans + " INTEGER, "+
						colTransReconciled + " INTEGER default 0"+
					")";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+configTable+" ("+colConfigID+ " INTEGER PRIMARY KEY , "+
		colConfigName+ " TEXT , "+colConfigValue+" TEXT )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+categoriesTable+" ("+colCategoriesID+ " INTEGER PRIMARY KEY , "+
		colCategoriesName+ " TEXT , "+colCategoriesColor+" INTEGER , "+colCategoriesIsIncome+" INTEGER, "+
		colCategoriesIsPermanent+" INTEGER default 0, "+colCategoriesUseInReports+" INTEGER default 1, "+colCategoriesParent+" INTEGER default -1)";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "CREATE TABLE "+accountsTable+" ("+colAccountsID+ " INTEGER PRIMARY KEY , "+
		colAccountsName+" TEXT , "+colAccountsValue+" REAL )";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		createBudgetsTables();
		
		populateDefaultCategories(db);
		populateDefaultAccounts(db);
		
		clearDatabase();
	}
	
	private void populateDefaultCategories(SQLiteDatabase db)
	{
		String sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Car Expenses',-6744954,0) ";
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
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Restaurants & Bars',-7757600,0) ";
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
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+","+colCategoriesUseInReports+") VALUES ('Starting Balance',-1416974,0,1,0) ";
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
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+") VALUES ('Uncategorised',-3743758,0,1) ";
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
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+","+colCategoriesUseInReports+") VALUES ('Starting Balance',-2416974,1,1,0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+") VALUES ('Misc (In)',-5416974,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+categoriesTable+" ("+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+") VALUES ('Uncategorised',-1743758,1,1) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
	}
	
	private void populateDefaultAccounts(SQLiteDatabase db)
	{
		String sql = "INSERT INTO "+accountsTable+" ("+colAccountsName+","+colAccountsValue+") VALUES ('Cash', 0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		sql = "INSERT INTO "+accountsTable+" ("+colAccountsName+","+colAccountsValue+") VALUES ('Bank Account', 0) ";
		Log.i("SQL", sql);
		db.execSQL(sql);
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
					if (category.getId() == transaction.getCategory())
					{
						validCategory = true;
						break;
					}
				}
				for(Account account : accounts)
				{
					if (account.getId() == transaction.getAccount())
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
				if (category.getName().equals("Starting Balance") && category.isIncome())
					incomeStartBalance = category;
				else if (category.getName().equals("Starting Balance") && !category.isIncome())
					expenseStartBalance = category;
			}

			if (incomeStartBalance == null)
			{
				AddCategory(new Category("Starting Balance", -2416974, true, true, false, -1));
			}
			else
			{
				incomeStartBalance.setIsPermanent(true);
				UpdateCategory(incomeStartBalance);
			}
			
			if (expenseStartBalance == null)
			{
				AddCategory(new Category("Starting Balance", -1416974, false, true, false, -1));
			}
			else
			{
				expenseStartBalance.setIsPermanent(true);
				UpdateCategory(expenseStartBalance);
			}
		}
		
		if (oldVersion <= 15)
		{
			String sql = "ALTER TABLE "+categoriesTable+" ADD COLUMN "+colCategoriesUseInReports+" INTEGER default 1";
			Log.i("SQL", sql);
			db.execSQL(sql);
			
			//check categories exist
			ArrayList<Category> categories = GetAllCategories();
			Category incomeStartBalance = null;
			Category expenseStartBalance = null;
			for(Category category : categories)
			{
				if (category.getName().equals("Starting Balance") && category.isIncome())
					incomeStartBalance = category;
				else if (category.getName().equals("Starting Balance") && !category.isIncome())
					expenseStartBalance = category;
			}
			
			incomeStartBalance.setUseInReports(false);
			UpdateCategory(incomeStartBalance);
			
			expenseStartBalance.setUseInReports(false);
			UpdateCategory(expenseStartBalance);
		}
		
		if (oldVersion <= 16)
		{
			String sql = "ALTER TABLE "+transTable+" ADD COLUMN "+colTransDontReport+" INTEGER";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		if (oldVersion <= 17)
		{
			//check categories exist
			ArrayList<Category> categories = GetAllCategories();
			Category incomeUncategorised = null;
			Category expenseUncategorised = null;
			for(Category category : categories)
			{
				if (category.getName().equals("Uncategorised") && category.isIncome())
					incomeUncategorised = category;
				else if (category.getName().equals("Uncategorised") && !category.isIncome())
					expenseUncategorised = category;
			}

			if (incomeUncategorised == null)
			{
				AddCategory(new Category("Uncategorised", -3743758, true, true, true, -1));
			}
			else
			{
				incomeUncategorised.setIsPermanent(true);
				UpdateCategory(incomeUncategorised);
			}

			if (expenseUncategorised == null)
			{
				AddCategory(new Category("Uncategorised", -1743758, false, true, true, -1));
			}
			else
			{
				expenseUncategorised.setIsPermanent(true);
				UpdateCategory(expenseUncategorised);
			}
		}
		
		if (oldVersion <= 18)
		{
			execSQL(db, "ALTER TABLE "+transTable+" ADD COLUMN "+colTransIsTransfer+" INTEGER");
			execSQL(db, "ALTER TABLE "+transTable+" ADD COLUMN "+colTransFromTrans+" INTEGER");
			execSQL(db, "ALTER TABLE "+transTable+" ADD COLUMN "+colTransToTrans+" INTEGER");
		}

		if (oldVersion <= 19)
		{
			execSQL(db, "ALTER TABLE "+transTable+" ADD COLUMN "+colTransReconciled+" INTEGER default 0");
		}
		
		if (oldVersion <= 20)
		{
			execSQL(db, "ALTER TABLE "+categoriesTable+" ADD COLUMN "+colCategoriesParent+" INTEGER default -1");
		}

        if (oldVersion <= 21)
        {
            Category misspeltResAndBars = null;
            ArrayList<Category> categories = GetAllCategories();
            for(Category category : categories)
            {
                if (category.getName().equals("Resturants & Bars") && !category.isIncome())
                    misspeltResAndBars = category;
            }
            if (misspeltResAndBars != null)
            {
                misspeltResAndBars.setName("Restaurants & Bars");
                UpdateCategory(misspeltResAndBars);
            }
        }

		clearDatabase();
	}
	
	private void execSQL(SQLiteDatabase db, String sql)
	{
		Log.i("SQL", sql);
		db.execSQL(sql);
	}
	
	
	/* ********************** TRANSACTIONS ******************************************/
	
	public void InsertTransaction(Transaction trans)
	{
		String desc = trans.getDescription().replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "INSERT INTO "+transTable+" ("
						+colTransValue+","
						+colTransDesc+","
						+colTransCategory+","
						+colTransDate+","
						+colTransAccount+","
						+colTransDontReport+","
						+colTransIsTransfer+","
						+colTransToTrans+","
						+colTransFromTrans+","
						+colTransReconciled
						+") VALUES ("
						+trans.getValue()+",'"
						+desc+"',"
						+trans.getCategory()+",'"
						+df.format(trans.getDateTime())+"',"
						+trans.getAccount()+","
						+(trans.isDontReport()?1:0)+","
						+(trans.isDontReport()?1:0)+","
						+trans.getTransferToTransaction()+","
						+trans.getTransferFromTransaction()+","
						+(trans.isReconciled()?1:0)
						+")";
		Log.i("SQL", sql);
		
		db.execSQL(sql);
		
		sql = "SELECT MAX("+colTransID+") FROM "+transTable;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		trans.setId(c.getInt(0));
		c.close();
	}
	
	public ArrayList<Transaction> GetAllTransactions(Date startDate, Date endDate)
	{
		try
		{
			SQLiteDatabase db= getDatabase(this, DATABASE_READ_MODE);
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+","+colTransDontReport+","+colTransIsTransfer+","+colTransToTrans+","+colTransFromTrans+","+colTransReconciled+" FROM "+transTable;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			while (c.isAfterLast() == false)
			{
				Transaction transaction = new Transaction();
				transaction.setId(c.getInt((c.getColumnIndex(colTransID))));
				transaction.setValue(c.getDouble((c.getColumnIndex(colTransValue))));
				transaction.setDescription(c.getString((c.getColumnIndex(colTransDesc))));
				transaction.setCategory(c.getInt((c.getColumnIndex(colTransCategory))));
				Date theDate = (Date)df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.setDateTime(theDate);
				transaction.setAccount(c.getInt((c.getColumnIndex(colTransAccount))));
				transaction.setDontReport(c.getInt((c.getColumnIndex(colTransDontReport)))==1);
				transaction.setTransfer(c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
				transaction.setTransferToTransaction(c.getInt((c.getColumnIndex(colTransToTrans))));
				transaction.setTransferFromTransaction(c.getInt((c.getColumnIndex(colTransFromTrans))));
				transaction.setReconciled(c.getInt((c.getColumnIndex(colTransReconciled)))==1);
				
				if (transaction.getDateTime().compareTo(startDate) >= 0 && transaction.getDateTime().compareTo(endDate) <= 0)
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
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+","+colTransDontReport+","+colTransIsTransfer+","+colTransToTrans+","+colTransFromTrans+","+colTransReconciled+" FROM "+transTable+"";
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			while (c.isAfterLast() == false)
			{
				Transaction transaction = new Transaction();
				transaction.setId(c.getInt((c.getColumnIndex(colTransID))));
				transaction.setValue(c.getDouble((c.getColumnIndex(colTransValue))));
				transaction.setDescription(c.getString((c.getColumnIndex(colTransDesc))));
				transaction.setCategory(c.getInt((c.getColumnIndex(colTransCategory))));
				Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.setDontReport(c.getInt((c.getColumnIndex(colTransDontReport)))==1);
				transaction.setTransfer(c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
				transaction.setTransferToTransaction(c.getInt((c.getColumnIndex(colTransToTrans))));
				transaction.setTransferFromTransaction(c.getInt((c.getColumnIndex(colTransFromTrans))));
				transaction.setDateTime(theDate);
				transaction.setAccount(c.getInt((c.getColumnIndex(colTransAccount))));
				transaction.setReconciled(c.getInt((c.getColumnIndex(colTransReconciled)))==1);
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
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransDate+","+colTransDontReport+","+colTransIsTransfer+","+colTransToTrans+","+colTransFromTrans+","+colTransReconciled+" FROM "+transTable+" WHERE "+colTransAccount+" = "+accountID;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			
			while (c.isAfterLast() == false)
			{
				Transaction transaction = new Transaction();
				transaction.setId(c.getInt((c.getColumnIndex(colTransID))));
				transaction.setValue(c.getDouble((c.getColumnIndex(colTransValue))));
				transaction.setDescription(c.getString((c.getColumnIndex(colTransDesc))));
				transaction.setCategory(c.getInt((c.getColumnIndex(colTransCategory))));
				Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.setDateTime(theDate);
				transaction.setAccount(accountID);
				transaction.setDontReport(c.getInt((c.getColumnIndex(colTransDontReport)))==1);
				transaction.setTransfer(c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
				transaction.setTransferToTransaction(c.getInt((c.getColumnIndex(colTransToTrans))));
				transaction.setTransferFromTransaction(c.getInt((c.getColumnIndex(colTransFromTrans))));
				transaction.setReconciled(c.getInt((c.getColumnIndex(colTransReconciled)))==1);
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
			
			String sql = "SELECT "+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+","+colTransDontReport+","+colTransIsTransfer+","+colTransToTrans+","+colTransFromTrans+","+colTransReconciled+" FROM "+transTable+" WHERE "+colTransID+"="+id;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			Transaction transaction = new Transaction();
			transaction.setValue(c.getDouble((c.getColumnIndex(colTransValue))));
			transaction.setDescription(c.getString((c.getColumnIndex(colTransDesc))));
			transaction.setCategory(c.getInt((c.getColumnIndex(colTransCategory))));
			Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
			transaction.setDateTime(theDate);
			transaction.setAccount(c.getInt((c.getColumnIndex(colTransAccount))));
			transaction.setDontReport(c.getInt((c.getColumnIndex(colTransDontReport)))==1);
			transaction.setTransfer(c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
			transaction.setTransferToTransaction(c.getInt((c.getColumnIndex(colTransToTrans))));
			transaction.setTransferFromTransaction(c.getInt((c.getColumnIndex(colTransFromTrans))));
			transaction.setReconciled(c.getInt((c.getColumnIndex(colTransReconciled)))==1);
			transaction.setId(id);
			
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
		
		String sql = "DELETE FROM "+transTable+" WHERE "+colTransID+" = "+transaction.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
	public void UpdateTransaction(Transaction trans)
	{
		String desc = trans.getDescription().replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "UPDATE "+transTable+" SET "+
						colTransValue+" = "+trans.getValue()+", "+
						colTransDesc+" = '"+desc+"', "+
						colTransCategory+" = "+trans.getCategory()+", "+
						colTransDate+" = '"+df.format(trans.getDateTime())+"', "+
						colTransAccount+" = "+trans.getAccount()+", "+
						colTransDontReport+" = "+(trans.isDontReport()?1:0)+", "+
						colTransIsTransfer+" = "+(trans.isTransfer()?1:0)+", "+
						colTransFromTrans+" = "+trans.getTransferFromTransaction()+", "+
						colTransToTrans+" = "+trans.getTransferToTransaction()+", "+
						colTransReconciled+" = "+(trans.isReconciled()?1:0)+
						" WHERE "+colTransID+" = "+trans.getId();
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
		String name = category.getName().replace("'", "''");
		
		SQLiteDatabase db=getDatabase(this, DATABASE_WRITE_MODE);
		
		String sql = "INSERT INTO "+categoriesTable+" ("
										+colCategoriesName+","
										+colCategoriesColor+","
										+colCategoriesIsIncome+","
										+colCategoriesIsPermanent+","
										+colCategoriesUseInReports+","
										+colCategoriesParent+
											") VALUES ('"
										+name+"',"
										+category.getColor()+","
										+(category.isIncome()?1:0)+","
										+(category.isPermanent()?1:0)+","
										+(category.isUseInReports()?1:0)+","
										+category.getParentCategoryId()
										+") ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		ArrayList<Category> categories = GetAllCategories();
		for(Category thisCategory : categories)
		{
			if (thisCategory.getName().equals(category.getName()) && 
				thisCategory.isIncome() == category.isIncome() && 
				thisCategory.getColor() == category.getColor() && 
				thisCategory.isPermanent() == category.isPermanent())
			{
				category.setId(thisCategory.getId());
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
			
			String sql = "SELECT "+colCategoriesID+","+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+","+colCategoriesUseInReports+","+colCategoriesParent+" FROM "+categoriesTable+"";
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			ArrayList<Category> categories = new ArrayList<Category>();
			
			while (c.isAfterLast() == false)
			{
				Category caetgory = new Category();
				caetgory.setId(c.getInt((c.getColumnIndex(colCategoriesID))));
				caetgory.setName(c.getString((c.getColumnIndex(colCategoriesName))));
				caetgory.setColor(c.getInt((c.getColumnIndex(colCategoriesColor))));
				caetgory.setIncome(c.getInt(c.getColumnIndex(colCategoriesIsIncome)) == 1);
				caetgory.setIsPermanent(c.getInt(c.getColumnIndex(colCategoriesIsPermanent)) == 1);
				caetgory.setUseInReports(c.getInt(c.getColumnIndex(colCategoriesUseInReports)) == 1);
				caetgory.setParentCategoryId(c.getInt((c.getColumnIndex(colCategoriesParent))));
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
			
			String sql = "SELECT "+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+","+colCategoriesUseInReports+","+colCategoriesParent+" FROM "+categoriesTable+" WHERE "+colCategoriesID+" = "+id;
			Log.i("SQL", sql);
			
			Cursor c = db.rawQuery(sql , null);
			c.moveToFirst();
			
			Category category = new Category();
			
			while (c.isAfterLast() == false)
			{
				category.setId(id);
				category.setName(c.getString((c.getColumnIndex(colCategoriesName))));
				category.setColor(c.getInt((c.getColumnIndex(colCategoriesColor))));
				category.setIncome(c.getInt(c.getColumnIndex(colCategoriesIsIncome)) == 1);
				category.setIsPermanent(c.getInt(c.getColumnIndex(colCategoriesIsPermanent)) == 1);
				category.setUseInReports(c.getInt(c.getColumnIndex(colCategoriesUseInReports)) == 1);
				category.setParentCategoryId(c.getInt((c.getColumnIndex(colCategoriesParent))));
				
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
		
		String sql = "DELETE FROM "+categoriesTable+" WHERE "+colCategoriesID+" = "+category.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);

        /* Make all associated transactions 'uncategorised' */
		
		Category uncategorisedCategory = null;
		ArrayList<Category> categories = GetAllCategories();
		for(Category testCategory : categories)
		{
			if (testCategory.getName().equals("Uncategorised") && (testCategory.isIncome() == category.isIncome()))
			{
				uncategorisedCategory = testCategory;
				break;
			}
		}
		
		ArrayList<Transaction> transactions = GetAllTransactions();
		for(Transaction transaction : transactions)
		{
			if (transaction.getCategory() == category.getId())
			{
				transaction.setCategory(uncategorisedCategory.getId());
				UpdateTransaction(transaction);
			}
		}

        if (category.getParentCategoryId() == -1)
        {
            for(Category testCategory : GetAllCategories())
            {
                if (testCategory.getParentCategoryId() == category.getId())
                {
                    DeleteCategory(testCategory);
                }
            }
        }
		
		clearDatabase();
	}
	
	public void UpdateCategory(Category category)
	{
		SQLiteDatabase db=getDatabase(this, DATABASE_WRITE_MODE);
		
		String name = category.getName().replace("'", "''");
		
		String sql = "UPDATE "+categoriesTable+" SET "
							+colCategoriesName+" = '"+name
							+"',"+colCategoriesIsIncome+" = "+(category.isIncome()?"1":"0")
							+","+colCategoriesIsPermanent+" = "+(category.isPermanent()?"1":"0")
							+","+colCategoriesUseInReports+" = "+(category.isUseInReports()?"1":"0")
							+","+colCategoriesColor+" = "+(category.getColor())
							+","+colCategoriesParent+" = "+(category.getParentCategoryId())
							+" WHERE "+colCategoriesID+" = "+category.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
/* ********************** ACCOUNTS ******************************************/
	
	public void AddAccount(Account account)
	{
		String name = account.getName().replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "INSERT INTO "+accountsTable+" ("+colAccountsName+","+colAccountsValue+
													") VALUES ('"+name+"',"+account.getValue()+") ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		//find the new id of this account
		sql = "SELECT "+colAccountsID+" FROM "+accountsTable+" WHERE "+colAccountsName+" = '"+name+"'";
		Log.i("SQL", sql);
		Cursor c = db.rawQuery(sql , null);
		c.moveToFirst();
		account.setId(c.getInt(c.getColumnIndex(colAccountsID)));
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
				
				Account account = new Account(s_context, id, name);
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
				
				account = new Account(s_context, id, name);
				
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
		
		Category incomeUncategorised = null;
		Category expenseUncategorised = null;
		ArrayList<Category> categories = GetAllCategories();
		for(Category category : categories)
		{
			if (category.getName().equals("Uncategorised"))
			{
				if (category.isIncome())
					incomeUncategorised = category;
				else
					expenseUncategorised = category;
			}
		}
		
		ArrayList<Transaction> transactions = GetAllTransactions();
		for(Transaction transaction : transactions)
		{
			if (transaction.getAccount() == account.getId())
			{
				if (transaction.isTransfer())
				{
					//update the related entry, so that it still shows as an entry, but doesn't act as a transfer.
					Transaction relatedTransaction = transaction.getRelatedTransferTransaction(s_context);
					relatedTransaction.setDescription(relatedTransaction.getDescription() + " (" + relatedTransaction.getTransferDescription(s_context) + ")");
					relatedTransaction.setCategory(relatedTransaction.isReceivingParty()
														? incomeUncategorised.getId()
														: expenseUncategorised.getId());
					relatedTransaction.setTransfer(false);
					relatedTransaction.setTransferToTransaction(-1);
					relatedTransaction.setTransferFromTransaction(-1);
					UpdateTransaction(relatedTransaction);
				}
				DeleteTransaction(transaction);
			}
		}
		
		String sql = "DELETE FROM "+accountsTable+" WHERE "+colAccountsID+" = "+account.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
	public void UpdateAccount(Account account)
	{
		String name = account.getName().replace("'", "''");
		SQLiteDatabase db=this.getWritableDatabase();
		
		String sql = "UPDATE "+accountsTable+" SET "+colAccountsName+" = '"+name+"' WHERE "+colAccountsID+" = "+account.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
	}
	
	
/* ************************* Budgets ****************************************************/
	
	public void AddBudget(Budget budget)
	{
		SQLiteDatabase db = getDatabase(this, DATABASE_WRITE_MODE);
		
		String name = budget.getName().replace("'", "''");
		
		String sql = "INSERT INTO "+budgetsTable+" ("+colBudgetsName+","+colBudgetsValue+","+colBudgetsNotify+
													") VALUES ('"+name+"',"+budget.getValue()+","+budget.getNotifyType().getValue()+") ";
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		//find the actual id of this budget
		sql = "SELECT "+colBudgetsID+" FROM "+budgetsTable+" WHERE "+colBudgetsName+" = '"+name+"'";
		Log.i("SQL", sql);
		Cursor c = db.rawQuery(sql , null);
		c.moveToFirst();
		budget.setId(c.getInt(c.getColumnIndex(colBudgetsID)));
		c.close();
		
		for(Category category : budget.getCategories())
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.getId()+","+category.getId()+","+1+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		for(Account account : budget.getAccounts())
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.getId()+","+account.getId()+","+2+") ";
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
				budget.setId(c.getInt((c.getColumnIndex(colBudgetsID))));
				budget.setName(c.getString((c.getColumnIndex(colBudgetsName))));
				budget.setValue(c.getDouble((c.getColumnIndex(colBudgetsValue))));
				budget.setNotifyType(Budget.NotificationType.fromInteger(c.getInt((c.getColumnIndex(colBudgetsNotify)))));
				
				/* find links */
				sql = "SELECT "+colBudgetLinksForeignID+","+colBudgetLinksForeignType+" FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.getId();
				Log.i("SQL", sql);
				
				Cursor linkCur = db.rawQuery(sql , null);
				linkCur.moveToFirst();
				while (linkCur.isAfterLast() == false)
				{
					if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 1)
					{
						Category category = GetCategory(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.getCategories().add(category);
					}
					else if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 2)
					{
						Account account = GetAccount(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.getAccounts().add(account);
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
				budget.setId(id);
				budget.setName(c.getString((c.getColumnIndex(colBudgetsName))));
				budget.setValue(c.getDouble((c.getColumnIndex(colBudgetsValue))));
				budget.setNotifyType(Budget.NotificationType.fromInteger(c.getInt((c.getColumnIndex(colBudgetsNotify)))));
				
				/* find links */
				sql = "SELECT "+colBudgetLinksForeignID+","+colBudgetLinksForeignType+" FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.getId();
				Log.i("SQL", sql);
				
				Cursor linkCur = db.rawQuery(sql , null);
				linkCur.moveToFirst();
				while (linkCur.isAfterLast() == false)
				{
					if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 1)
					{
						Category category = GetCategory(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.getCategories().add(category);
					}
					else if (linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignType)) == 2)
					{
						Account account = GetAccount(linkCur.getInt(linkCur.getColumnIndex(colBudgetLinksForeignID)));
						budget.getAccounts().add(account);
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
		
		String sql = "DELETE FROM "+budgetsTable+" WHERE "+colBudgetsID+" = "+budget.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "DELETE FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		clearDatabase();
	}
	
	public void UpdateBudget(Budget budget)
	{
		SQLiteDatabase db = getDatabase(this, DATABASE_WRITE_MODE);
		
		String name = budget.getName().replace("'", "''");
		
		String sql = "UPDATE "+budgetsTable+" SET "+colBudgetsName+" = '"+name+"', "+colBudgetsValue+" = "+budget.getValue()+", "+colBudgetsNotify+" = "+budget.getNotifyType().getValue()+" WHERE "+colBudgetsID+" = "+budget.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		sql = "DELETE FROM "+budgetLinksTable+" WHERE "+colBudgetLinksBudgetID+" = "+budget.getId();
		Log.i("SQL", sql);
		db.execSQL(sql);
		
		for(Category category : budget.getCategories())
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.getId()+","+category.getId()+","+1+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		for(Account account : budget.getAccounts())
		{
			sql = "INSERT INTO "+budgetLinksTable+" ("+colBudgetLinksBudgetID+","+colBudgetLinksForeignID+","+colBudgetLinksForeignType+
					") VALUES ("+budget.getId()+","+account.getId()+","+2+") ";
			Log.i("SQL", sql);
			db.execSQL(sql);
		}
		
		clearDatabase();
	}
}






