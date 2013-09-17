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
				AddCategory(new Category("Starting Balance", -2416974, true, true, false, -1));
			}
			else
			{
				incomeStartBalance.isPermanent = true;
				UpdateCategory(incomeStartBalance);
			}
			
			if (expenseStartBalance == null)
			{
				AddCategory(new Category("Starting Balance", -1416974, false, true, false, -1));
			}
			else
			{
				expenseStartBalance.isPermanent = true;
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
				if (category.name.equals("Starting Balance") && category.income)
					incomeStartBalance = category;
				else if (category.name.equals("Starting Balance") && !category.income)
					expenseStartBalance = category;
			}
			
			incomeStartBalance.useInReports = false;
			UpdateCategory(incomeStartBalance);
			
			expenseStartBalance.useInReports = false;
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
				if (category.name.equals("Uncategorised") && category.income)
					incomeUncategorised = category;
				else if (category.name.equals("Uncategorised") && !category.income)
					expenseUncategorised = category;
			}

			if (incomeUncategorised == null)
			{
				AddCategory(new Category("Uncategorised", -3743758, true, true, true, -1));
			}
			else
			{
				incomeUncategorised.isPermanent = true;
				UpdateCategory(incomeUncategorised);
			}

			if (expenseUncategorised == null)
			{
				AddCategory(new Category("Uncategorised", -1743758, false, true, true, -1));
			}
			else
			{
				expenseUncategorised.isPermanent = true;
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
                if (category.name.equals("Resturants & Bars") && !category.income)
                    misspeltResAndBars = category;
            }
            if (misspeltResAndBars != null)
            {
                misspeltResAndBars.name = "Restaurants & Bars";
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
		String desc = trans.description.replace("'", "''");
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
						+trans.value+",'"
						+desc+"',"
						+trans.category+",'"
						+df.format(trans.dateTime)+"',"
						+trans.account+","
						+(trans.dontReport?1:0)+","
						+(trans.dontReport?1:0)+","
						+trans.transferToTransaction+","
						+trans.transferFromTransaction+","
						+(trans.reconciled?1:0)
						+")";
		Log.i("SQL", sql);
		
		db.execSQL(sql);
		
		sql = "SELECT MAX("+colTransID+") FROM "+transTable;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		trans.id = c.getInt(0);
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
				transaction.id = c.getInt((c.getColumnIndex(colTransID)));
				transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
				transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
				transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
				Date theDate = (Date)df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.dateTime = theDate;
				transaction.account = c.getInt((c.getColumnIndex(colTransAccount)));
				transaction.dontReport = (c.getInt((c.getColumnIndex(colTransDontReport)))==1);
				transaction.isTransfer = (c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
				transaction.transferToTransaction = c.getInt((c.getColumnIndex(colTransToTrans)));
				transaction.transferFromTransaction = c.getInt((c.getColumnIndex(colTransFromTrans)));
				transaction.reconciled = (c.getInt((c.getColumnIndex(colTransReconciled)))==1);
				
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
			
			String sql = "SELECT "+colTransID+","+colTransValue+","+colTransDesc+","+colTransCategory+","+colTransAccount+","+colTransDate+","+colTransDontReport+","+colTransIsTransfer+","+colTransToTrans+","+colTransFromTrans+","+colTransReconciled+" FROM "+transTable+"";
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
				transaction.dontReport = (c.getInt((c.getColumnIndex(colTransDontReport)))==1);
				transaction.isTransfer = (c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
				transaction.transferToTransaction = c.getInt((c.getColumnIndex(colTransToTrans)));
				transaction.transferFromTransaction = c.getInt((c.getColumnIndex(colTransFromTrans)));
				transaction.dateTime = theDate;
				transaction.account = c.getInt((c.getColumnIndex(colTransAccount)));
				transaction.reconciled = (c.getInt((c.getColumnIndex(colTransReconciled)))==1);
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
				transaction.id = c.getInt((c.getColumnIndex(colTransID)));
				transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
				transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
				transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
				Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
				transaction.dateTime = theDate;
				transaction.account = accountID;
				transaction.dontReport = (c.getInt((c.getColumnIndex(colTransDontReport)))==1);
				transaction.isTransfer = (c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
				transaction.transferToTransaction = c.getInt((c.getColumnIndex(colTransToTrans)));
				transaction.transferFromTransaction = c.getInt((c.getColumnIndex(colTransFromTrans)));
				transaction.reconciled = (c.getInt((c.getColumnIndex(colTransReconciled)))==1);
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
			transaction.value = c.getDouble((c.getColumnIndex(colTransValue)));
			transaction.description = c.getString((c.getColumnIndex(colTransDesc)));
			transaction.category = c.getInt((c.getColumnIndex(colTransCategory)));
			Date theDate =  (Date) df.parse(c.getString((c.getColumnIndex(colTransDate))));
			transaction.dateTime = theDate;
			transaction.account = c.getInt((c.getColumnIndex(colTransAccount)));
			transaction.dontReport = (c.getInt((c.getColumnIndex(colTransDontReport)))==1);
			transaction.isTransfer = (c.getInt((c.getColumnIndex(colTransIsTransfer)))==1);
			transaction.transferToTransaction = c.getInt((c.getColumnIndex(colTransToTrans)));
			transaction.transferFromTransaction = c.getInt((c.getColumnIndex(colTransFromTrans)));
			transaction.reconciled = (c.getInt((c.getColumnIndex(colTransReconciled)))==1);
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
		
		String sql = "UPDATE "+transTable+" SET "+
						colTransValue+" = "+trans.value+", "+
						colTransDesc+" = '"+desc+"', "+
						colTransCategory+" = "+trans.category+", "+
						colTransDate+" = '"+df.format(trans.dateTime)+"', "+
						colTransAccount+" = "+trans.account+", "+
						colTransDontReport+" = "+(trans.dontReport?1:0)+", "+
						colTransIsTransfer+" = "+(trans.isTransfer?1:0)+", "+
						colTransFromTrans+" = "+trans.transferFromTransaction+", "+
						colTransToTrans+" = "+trans.transferToTransaction+", "+
						colTransReconciled+" = "+(trans.reconciled?1:0)+
						" WHERE "+colTransID+" = "+trans.id;
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
										+colCategoriesIsPermanent+","
										+colCategoriesUseInReports+","
										+colCategoriesParent+
											") VALUES ('"
										+name+"',"
										+category.color+","
										+(category.income?1:0)+","
										+(category.isPermanent?1:0)+","
										+(category.useInReports?1:0)+","
										+category.parentCategoryId
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
			
			String sql = "SELECT "+colCategoriesID+","+colCategoriesName+","+colCategoriesColor+","+colCategoriesIsIncome+","+colCategoriesIsPermanent+","+colCategoriesUseInReports+","+colCategoriesParent+" FROM "+categoriesTable+"";
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
				caetgory.useInReports = (c.getInt(c.getColumnIndex(colCategoriesUseInReports)) == 1);
				caetgory.parentCategoryId = c.getInt((c.getColumnIndex(colCategoriesParent)));
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
				category.id = id;
				category.name = c.getString((c.getColumnIndex(colCategoriesName)));
				category.color = c.getInt((c.getColumnIndex(colCategoriesColor)));
				category.income = (c.getInt(c.getColumnIndex(colCategoriesIsIncome)) == 1);
				category.isPermanent = (c.getInt(c.getColumnIndex(colCategoriesIsPermanent)) == 1);
				category.useInReports = (c.getInt(c.getColumnIndex(colCategoriesUseInReports)) == 1);
				category.parentCategoryId = c.getInt((c.getColumnIndex(colCategoriesParent)));
				
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

        /* Make all associated transactions 'uncategorised' */
		
		Category uncategorisedCategory = null;
		ArrayList<Category> categories = GetAllCategories();
		for(Category testCategory : categories)
		{
			if (testCategory.name.equals("Uncategorised") && (testCategory.income == category.income))
			{
				uncategorisedCategory = testCategory;
				break;
			}
		}
		
		ArrayList<Transaction> transactions = GetAllTransactions();
		for(Transaction transaction : transactions)
		{
			if (transaction.category == category.id)
			{
				transaction.category = uncategorisedCategory.id;
				UpdateTransaction(transaction);
			}
		}

        if (category.parentCategoryId == -1)
        {
            for(Category testCategory : GetAllCategories())
            {
                if (testCategory.parentCategoryId == category.id)
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
		
		String name = category.name.replace("'", "''");
		
		String sql = "UPDATE "+categoriesTable+" SET "
							+colCategoriesName+" = '"+name
							+"',"+colCategoriesIsIncome+" = "+(category.income?"1":"0")
							+","+colCategoriesIsPermanent+" = "+(category.isPermanent?"1":"0")
							+","+colCategoriesUseInReports+" = "+(category.useInReports?"1":"0")
							+","+colCategoriesColor+" = "+(category.color)
							+","+colCategoriesParent+" = "+(category.parentCategoryId)
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
		
		Category incomeUncategorised = null;
		Category expenseUncategorised = null;
		ArrayList<Category> categories = GetAllCategories();
		for(Category category : categories)
		{
			if (category.name.equals("Uncategorised"))
			{
				if (category.income)
					incomeUncategorised = category;
				else
					expenseUncategorised = category;
			}
		}
		
		ArrayList<Transaction> transactions = GetAllTransactions();
		for(Transaction transaction : transactions)
		{
			if (transaction.account == account.id)
			{
				if (transaction.isTransfer)
				{
					//update the related entry, so that it still shows as an entry, but doesn't act as a transfer.
					Transaction relatedTransaction = transaction.getRelatedTransferTransaction(_context);
					relatedTransaction.description += " (" + relatedTransaction.getTransferDescription(_context) + ")";
					relatedTransaction.category = relatedTransaction.isReceivingParty() 
														? incomeUncategorised.id
														: expenseUncategorised.id;
					relatedTransaction.isTransfer = false;
					relatedTransaction.transferToTransaction = -1;
					relatedTransaction.transferFromTransaction = -1;
					UpdateTransaction(relatedTransaction);
				}
				DeleteTransaction(transaction);
			}
		}
		
		String sql = "DELETE FROM "+accountsTable+" WHERE "+colAccountsID+" = "+account.id;
		Log.i("SQL", sql);
		db.execSQL(sql);
		
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
				budget.notifyType = Budget.NotificationType.fromInteger(c.getInt((c.getColumnIndex(colBudgetsNotify))));
				
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
				budget.notifyType = Budget.NotificationType.fromInteger(c.getInt((c.getColumnIndex(colBudgetsNotify))));
				
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






