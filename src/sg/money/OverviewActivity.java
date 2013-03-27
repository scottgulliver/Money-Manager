package sg.money;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class OverviewActivity extends BaseActivity
{
	ListView lstCategories;
	PieChartView pieChartView;
	double totalSpent;
	ArrayList<Category> categories;
	ArrayList<Transaction> transactions;
	ArrayList<Category> categoriesShown;
	ArrayList<String> categoryStringsShown;
	TextView txtIncome;
	TextView txtExpenditure;
	ScrollView scrollView;
	TextView txtMonth;
	TextView txtSpendingByCategory;
	
	PopupWindow popupWindow;
	TextView txtTitle;
	ListView lstSelection;
	SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
	String currentMonth;
	
	//Bundle State Data
	static final String STATE_MONTH = "stateMonth";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        //get the controls
        txtMonth = (TextView)findViewById(R.id.txtMonth);
        lstCategories = (ListView)findViewById(R.id.lstCategories);
		txtIncome = (TextView)findViewById(R.id.txtIncome);
		txtExpenditure = (TextView)findViewById(R.id.txtExpenditure);
		scrollView = (ScrollView)findViewById(R.id.scrollView);
		pieChartView = (PieChartView)findViewById(R.id.pieChartView);
		txtSpendingByCategory = (TextView)findViewById(R.id.txtSpendingByCategory);
        
        //set up the collections
        transactions = DatabaseManager.getInstance(OverviewActivity.this).GetAllTransactions();
    	Collections.sort(transactions, new DateComparator());
    	Collections.reverse(transactions);    	
    	categories = DatabaseManager.getInstance(OverviewActivity.this).GetAllCategories();
    	
    	if (savedInstanceState != null)
    	{
    		currentMonth = savedInstanceState.getString(STATE_MONTH);
        	setData(currentMonth);
    	}
    	else
    	{
        	setData("");
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_MONTH, currentMonth);
        
        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    private void setData(String month)
    {
    	if (month.equals(""))
    	{ 
    		Calendar currentDate = Calendar.getInstance();
    	    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 1);
    	    
        	month = monthYearFormat.format(currentDate.getTime());
    	}
    	
    	txtMonth.setText(month);
    	currentMonth = month;
    	
    	Calendar startDate = new GregorianCalendar();
    	try {
			startDate.setTime(monthYearFormat.parse(month));
		} catch (ParseException e) {
			e.printStackTrace(); 
		}
    	Calendar endDate = (Calendar)startDate.clone();
    	endDate.add(Calendar.MONTH, 1);
    	endDate.add(Calendar.SECOND, -1);
    	
    	transactions = DatabaseManager.getInstance(OverviewActivity.this).GetAllTransactions(startDate.getTime(), endDate.getTime());
    	//Toast.makeText(OverviewActivity.this, transactions.size() + " transactions.", Toast.LENGTH_SHORT).show();
    	
    	if (transactions.size() == 0)
    	{
    		txtSpendingByCategory.setText("(No spending to show)");
    		pieChartView.setVisibility(View.GONE);
    		lstCategories.setVisibility(View.GONE);
    	}
    	else
    	{
    		txtSpendingByCategory.setText("Spending per category");
    		pieChartView.setVisibility(View.VISIBLE);
    		lstCategories.setVisibility(View.VISIBLE);
    	}
    	
    	for(Transaction transaction : transactions)
    	{
    		Category thisCategory = null;
    		for(Category category : categories)
    		{
    			if (category.id == transaction.category)
    			{
    				thisCategory = category;
    				break;
    			}
    		}
    		if (!thisCategory.income)
    		{
    		}
    	}
		
    	
		{RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pieChartView.getLayoutParams();
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		params.height = displaymetrics.widthPixels;
		pieChartView.setLayoutParams(params);}

		setSegments();

		CategoryListAdapter adapter2 = new CategoryListAdapter(this, categoriesShown, categoryStringsShown);
		lstCategories.setAdapter(adapter2);
		
		{RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lstCategories.getLayoutParams();
		params.height = (int) ((float)categoriesShown.size() * Misc.dipsToPixels(getResources(), 50));
		lstCategories.setLayoutParams(params);}
		
		double income = 0;
		double expenditure = 0;
		for(Transaction transaction : transactions)
    	{
			if (transaction.dontReport)
				continue;
				
    		Category thisCategory = null;
    		for(Category category : categories)
    		{
    			if (category.id == transaction.category)
    			{
    				thisCategory = category;
    				break;
    			}
    		}
    		
    		if (!thisCategory.useInReports)
    			continue;
    		
    		if (thisCategory.income)
    		{
    			income += transaction.value;
    		}
    		else
    		{
    			expenditure += (transaction.value * -1.0);
    		}
    	}
		
		txtIncome.setText(Misc.formatValue(OverviewActivity.this, income));
		txtExpenditure.setText(Misc.formatValue(OverviewActivity.this, expenditure));

		scrollView.post(new Runnable() { 
	        public void run() { 
	        	scrollView.scrollTo(0, 0);
	        } 
		});
    }
    
    private void setSegments()
    {
		categoriesShown = new ArrayList<Category>();
		categoryStringsShown = new ArrayList<String>();
		Random rnd = new Random(System.currentTimeMillis());
		
		ArrayList<PieChartSegment> categorySegments = new ArrayList<PieChartSegment>();
		
		//get the total value of everything
		double total = 0;
		for(Transaction transaction : transactions)
		{
			if (transaction.dontReport)
				continue;
			if (!getCategory(transaction.category).income && getCategory(transaction.category).useInReports)
				total -= transaction.value;
		}
		
		//add each category in turn
		for(Category category : categories)
		{
			if (category.income || !category.useInReports)
				continue;
			
			double categoryValue = 0;
			for(Transaction transaction : transactions)
			{
				if (transaction.dontReport)
					continue;
				if (transaction.category == category.id)
					categoryValue -= transaction.value;
			} 
			
			if (categoryValue > 0d)
			{
				PieChartSegment categorySegment = new PieChartSegment();
				categorySegment.angle = (float) ((categoryValue / total) * 360d);
				categorySegment.color = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
				categorySegment.color = category.color;
				categorySegments.add(categorySegment);
				categoriesShown.add(category);
				DecimalFormat df = new DecimalFormat("#.##");
				categoryStringsShown.add(Misc.formatValue(OverviewActivity.this, categoryValue) + "  -  " + df.format((categoryValue / total)*100) + "%");
			}
		}
		
		pieChartView.setSegments(categorySegments);
    }
    
    private Category getCategory(int id)
    {
    	for(Category category : categories)
		{
			if (category.id == id)
				return category;
		}
    	
    	return null;
    }
    

	public class DateComparator implements Comparator<Transaction> {
	    public int compare(Transaction o1, Transaction o2) {
	        return o1.dateTime.compareTo(o2.dateTime);
	    }
	}
	
	private Dialog createDialog()
	{
		transactions = DatabaseManager.getInstance(OverviewActivity.this).GetAllTransactions();
		Collections.sort(transactions, new DateComparator());
    	Collections.reverse(transactions);

    	final ArrayList<String> months = new ArrayList<String>();
    	if (transactions.size() == 0)
    	{
    		Calendar currentDate = Calendar.getInstance();
		    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    months.add(monthYearFormat.format(currentDate.getTime()));
    	}
    	else
    	{
	    	Transaction oldestTransaction = transactions.get(transactions.size()-1);
	    	Calendar oldestDate = Calendar.getInstance();
	    	oldestDate.setTime(oldestTransaction.dateTime);
	    	oldestDate.set(oldestDate.get(Calendar.YEAR), oldestDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    
		    Calendar currentDate = Calendar.getInstance();
		    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    //oldestDate.set(2012, 1, 1);
	    	while(true)
	    	{
	    		months.add(monthYearFormat.format(currentDate.getTime()));
	    		currentDate.add(Calendar.MONTH, -1);
	    		if (currentDate.before(oldestDate))
	    			break;
	    	}
    	}
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
	    builder.setTitle("Select a month")
	           .setItems(months.toArray(new CharSequence[months.size()]), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int position) {
	               setData(months.get(position));
	           }
	    }).setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	    return builder.create();
	}
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_overview, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
	    {
		    case android.R.id.home:
	            Intent parentActivityIntent = new Intent(this, TransactionsActivity.class);
	            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(parentActivityIntent);
	            finish();
	            return true;
            
	    	case R.id.menu_month:{
	    		createDialog().show();
	    		break;
	    		}
	    	
	        case R.id.menu_settings:{
	        	startActivityForResult(new Intent(OverviewActivity.this, SettingsActivity.class), REQUEST_SETTINGS);
                break;
            	}
	    }
	    return true;
    }

	static final int REQUEST_SETTINGS = 10;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case REQUEST_SETTINGS:
			{
				setData(currentMonth);
				break;
			}
		}
    }
}
