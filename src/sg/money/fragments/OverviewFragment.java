package sg.money.fragments;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import sg.money.domainobjects.Category;
import sg.money.adapters.CategoryListAdapter;
import sg.money.DatabaseManager;
import sg.money.utils.Misc;
import sg.money.widgets.PieChartSegment;
import sg.money.widgets.PieChartView;
import sg.money.R;
import sg.money.activities.SettingsActivity;
import sg.money.domainobjects.Transaction;

public class OverviewFragment extends HostActivityFragmentBase
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
	CheckBox chkShowSubcategories;
	
	PopupWindow popupWindow;
	TextView txtTitle;
	ListView lstSelection;
	SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
	String currentMonth;
	
	//Bundle State Data
	static final String STATE_MONTH = "stateMonth";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_transactions);
        View v = inflater.inflate(R.layout.activity_overview, null);

        getParentActivity().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        getParentActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        //get the controls
        txtMonth = (TextView)v.findViewById(R.id.txtMonth);
        lstCategories = (ListView)v.findViewById(R.id.lstCategories1);
		txtIncome = (TextView)v.findViewById(R.id.txtIncome);
		txtExpenditure = (TextView)v.findViewById(R.id.txtExpenditure);
		scrollView = (ScrollView)v.findViewById(R.id.scrollView);
		pieChartView = (PieChartView)v.findViewById(R.id.pieChartView);
		txtSpendingByCategory = (TextView)v.findViewById(R.id.txtSpendingByCategory);
		chkShowSubcategories = (CheckBox)v.findViewById(R.id.chkShowSubcategories);
        
        //set up the collections
        transactions = DatabaseManager.getInstance(getParentActivity()).GetAllTransactions();
    	Collections.sort(transactions, new DateComparator());
    	Collections.reverse(transactions);    	
    	categories = DatabaseManager.getInstance(getParentActivity()).GetAllCategories();
    	categories = Misc.getCategoriesInGroupOrder(categories);
    	
    	chkShowSubcategories.setChecked(true);
    	chkShowSubcategories.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setData(currentMonth);
			}}
    	);
    	
    	if (savedInstanceState != null)
    	{
    		currentMonth = savedInstanceState.getString(STATE_MONTH);
        	setData(currentMonth);
    	}
    	else
    	{
        	setData("");
    	}

        return v;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_MONTH, currentMonth);
        
        super.onSaveInstanceState(savedInstanceState);
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
    	
    	transactions = DatabaseManager.getInstance(getParentActivity()).GetAllTransactions(startDate.getTime(), endDate.getTime());
    	//Toast.makeText(OverviewFragment.this, transactions.size() + " transactions.", Toast.LENGTH_SHORT).show();
    	
    	if (transactions.size() == 0)
    	{
    		txtSpendingByCategory.setText("(No spending to show)");
    		pieChartView.setVisibility(View.GONE);
    		lstCategories.setVisibility(View.GONE);
    		chkShowSubcategories.setVisibility(View.GONE);
    	}
    	else
    	{
    		txtSpendingByCategory.setText("Spending per category");
    		pieChartView.setVisibility(View.VISIBLE);
    		lstCategories.setVisibility(View.VISIBLE);
    		chkShowSubcategories.setVisibility(View.VISIBLE);
    	}
    	
    	
		{RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) pieChartView.getLayoutParams();
		DisplayMetrics displaymetrics = new DisplayMetrics();
        getParentActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		params.height = displaymetrics.widthPixels;
		pieChartView.setLayoutParams(params);}

		setSegments();

		CategoryListAdapter adapter2 = new CategoryListAdapter(getParentActivity(), categoriesShown, categoryStringsShown);
		lstCategories.setAdapter(adapter2);
		
		{RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lstCategories.getLayoutParams();
		params.height = (int) ((float)categoriesShown.size() * Misc.dipsToPixels(getResources(), 50));
		lstCategories.setLayoutParams(params);}
		
		double income = 0;
		double expenditure = 0;
		for(Transaction transaction : transactions)
    	{
			if (transaction.isDontReport())
				continue;
				
    		Category thisCategory = null;
    		for(Category category : categories)
    		{
    			if (category.getId() == transaction.getCategory())
    			{
    				thisCategory = category;
    				break;
    			}
    		}
    		
    		if (!thisCategory.isUseInReports())
    			continue;
    		
    		if (thisCategory.isIncome())
    		{
    			income += transaction.getValue();
    		}
    		else
    		{
    			expenditure += (transaction.getValue() * -1.0);
    		}
    	}
		
		txtIncome.setText(Misc.formatValue(getParentActivity(), income));
		txtExpenditure.setText(Misc.formatValue(getParentActivity(), expenditure));

		scrollView.post(new Runnable() { 
	        public void run() { 
	        	scrollView.scrollTo(0, 0);
	        } 
		});
    }
    
    private class CategoryValuePair
    {
    	public Category category;
    	public double value;
    	
    	public CategoryValuePair(Category cat, double val)
    	{
    		category = cat;
    		value = val;
    	}
    }
    
    private double getCategoryValue(Category category, boolean getSubcategoryValue)
    {
    	//get my value
		double categoryValue = 0;
		for(Transaction transaction : transactions)
		{
			if (transaction.isDontReport())
				continue;
			if (!getCategory(transaction.getCategory()).isIncome() && getCategory(transaction.getCategory()).isUseInReports() && transaction.getCategory() == category.getId())
				categoryValue -= transaction.getValue();
		} 
		
		if (getSubcategoryValue && category.getParentCategoryId() == -1)
		{
			for(Category subCat : categories)
			{
				if (subCat.getParentCategoryId() == category.getId())
					categoryValue += getCategoryValue(subCat, false);
			}
		}
		
		return categoryValue;
    }
    
    private void setSegments()
    {
		categoriesShown = new ArrayList<Category>();
		categoryStringsShown = new ArrayList<String>();
		
		ArrayList<PieChartSegment> categorySegments = new ArrayList<PieChartSegment>();
		
		//get the total value of everything
		double total = 0;
		for(Transaction transaction : transactions)
		{
			if (transaction.isDontReport())
				continue;
			if (!getCategory(transaction.getCategory()).isIncome() && getCategory(transaction.getCategory()).isUseInReports())
				total -= transaction.getValue();
		}
		
		ArrayList<CategoryValuePair> segmentList = new ArrayList<CategoryValuePair>();
		
		for(Category category : categories)
		{
			if (category.isIncome() || !category.isUseInReports())
				continue;
			
			if (!chkShowSubcategories.isChecked() && category.getParentCategoryId() != -1)
				continue; // we will pick this up in the value of the parent
			
			segmentList.add(new CategoryValuePair(
					category, getCategoryValue(category, !chkShowSubcategories.isChecked())));
		}
		
		total = 0;
		for(CategoryValuePair catValPair : segmentList)
		{
			if (catValPair.value > 0d)
			{
				total += catValPair.value;
			}
		}
		
		//add each category in turn
		for(CategoryValuePair catValPair : segmentList)
		{
			if (catValPair.value > 0d)
			{
				PieChartSegment categorySegment = new PieChartSegment();
				categorySegment.setAngle((float) ((catValPair.value / total) * 360d));
				categorySegment.setColor(catValPair.category.getColor());
				categorySegments.add(categorySegment);
				categoriesShown.add(catValPair.category);
				DecimalFormat df = new DecimalFormat("#.##");
				categoryStringsShown.add(Misc.formatValue(getParentActivity(), catValPair.value) + "  -  " + df.format((catValPair.value / total)*100) + "%");
			}
		}
		
		pieChartView.setSegments(categorySegments);
    }
    
    private Category getCategory(int id)
    {
    	for(Category category : categories)
		{
			if (category.getId() == id)
				return category;
		}
    	
    	return null;
    }
    

	public class DateComparator implements Comparator<Transaction> {
	    public int compare(Transaction o1, Transaction o2) {
	        return o1.getDateTime().compareTo(o2.getDateTime());
	    }
	}
	
	private Dialog createDialog()
	{
		transactions = DatabaseManager.getInstance(getParentActivity()).GetAllTransactions();
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
	    	oldestDate.setTime(oldestTransaction.getDateTime());
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
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
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
        getParentActivity().getSupportMenuInflater().inflate(R.menu.activity_overview, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
	    {
	    	case R.id.menu_month:{
	    		createDialog().show();
	    		break;
	    		}
	    	
	        case R.id.menu_settings:{
	        	startActivityForResult(new Intent(getParentActivity(), SettingsActivity.class), REQUEST_SETTINGS);
                break;
            	}
	    }
	    return true;
    }

	static final int REQUEST_SETTINGS = 10;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        menu.findItem(R.id.menu_month).setVisible(!drawerIsOpen);
        return true;
    }
}
