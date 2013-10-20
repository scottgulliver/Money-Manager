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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import sg.money.domainobjects.Category;
import sg.money.adapters.CategoryListAdapter;
import sg.money.common.DatabaseManager;
import sg.money.utils.Misc;
import sg.money.widgets.PieChartSegment;
import sg.money.widgets.PieChartView;
import sg.money.R;
import sg.money.activities.SettingsActivity;
import sg.money.domainobjects.Transaction;

public class OverviewFragment extends HostActivityFragmentBase
{
	private ListView m_lstCategories;
    private PieChartView m_pieChartView;
    private ArrayList<Category> m_categories;
    private ArrayList<Transaction> m_transactions;
    private ArrayList<Category> m_categoriesShown;
    private ArrayList<String> m_categoryStringsShown;
    private TextView m_txtIncome;
    private TextView m_txtExpenditure;
    private ScrollView m_scrollView;
    private TextView m_txtMonth;
    private TextView m_txtSpendingByCategory;
    private CheckBox m_chkShowSubcategories;
    private SimpleDateFormat m_monthYearFormat;
    private String m_currentMonth;
	
	//Bundle State Data
	private static final String STATE_MONTH = "stateMonth";


    /* Fragment overrides */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        m_monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);

        View v = inflater.inflate(R.layout.activity_overview, null);

        getParentActivity().getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        getParentActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        //get the controls
        m_txtMonth = (TextView)v.findViewById(R.id.txtMonth);
        m_lstCategories = (ListView)v.findViewById(R.id.lstCategories1);
		m_txtIncome = (TextView)v.findViewById(R.id.txtIncome);
		m_txtExpenditure = (TextView)v.findViewById(R.id.txtExpenditure);
		m_scrollView = (ScrollView)v.findViewById(R.id.scrollView);
		m_pieChartView = (PieChartView)v.findViewById(R.id.pieChartView);
		m_txtSpendingByCategory = (TextView)v.findViewById(R.id.txtSpendingByCategory);
		m_chkShowSubcategories = (CheckBox)v.findViewById(R.id.chkShowSubcategories);
        
        //set up the collections
        m_transactions = DatabaseManager.getInstance(getParentActivity()).GetAllTransactions();
    	Collections.sort(m_transactions, new DateComparator());
    	Collections.reverse(m_transactions);
    	m_categories = DatabaseManager.getInstance(getParentActivity()).GetAllCategories();
    	m_categories = Category.getCategoriesInGroupOrder(m_categories);
    	
    	m_chkShowSubcategories.setChecked(true);
    	m_chkShowSubcategories.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setData(m_currentMonth);
            }
        }
        );
    	
    	if (savedInstanceState != null)
    	{
    		m_currentMonth = savedInstanceState.getString(STATE_MONTH);
        	setData(m_currentMonth);
    	}
    	else
    	{
        	setData("");
    	}

        return v;
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_MONTH, m_currentMonth);
        
        super.onSaveInstanceState(savedInstanceState);
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
                setData(m_currentMonth);
                break;
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        menu.findItem(R.id.menu_month).setVisible(!drawerIsOpen);
        return true;
    }


    /* Methods */
    
    private void setData(String month)
    {
    	if (month.equals(""))
    	{ 
    		Calendar currentDate = Calendar.getInstance();
    	    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 1);
    	    
        	month = m_monthYearFormat.format(currentDate.getTime());
    	}
    	
    	m_txtMonth.setText(month);
    	m_currentMonth = month;
    	
    	Calendar startDate = new GregorianCalendar();
    	try {
			startDate.setTime(m_monthYearFormat.parse(month));
		} catch (ParseException e) {
			e.printStackTrace(); 
		}
    	Calendar endDate = (Calendar)startDate.clone();
    	endDate.add(Calendar.MONTH, 1);
    	endDate.add(Calendar.SECOND, -1);
    	
    	m_transactions = DatabaseManager.getInstance(getParentActivity()).GetAllTransactions(startDate.getTime(), endDate.getTime());
    	
    	if (m_transactions.size() == 0)
    	{
    		m_txtSpendingByCategory.setText("(No spending to show)");
    		m_pieChartView.setVisibility(View.GONE);
    		m_lstCategories.setVisibility(View.GONE);
    		m_chkShowSubcategories.setVisibility(View.GONE);
    	}
    	else
    	{
    		m_txtSpendingByCategory.setText("Spending per category");
    		m_pieChartView.setVisibility(View.VISIBLE);
    		m_lstCategories.setVisibility(View.VISIBLE);
    		m_chkShowSubcategories.setVisibility(View.VISIBLE);
    	}
    	
    	
		{RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_pieChartView.getLayoutParams();
		DisplayMetrics displaymetrics = new DisplayMetrics();
        getParentActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		params.height = displaymetrics.widthPixels;
		m_pieChartView.setLayoutParams(params);}

		setSegments();

		CategoryListAdapter adapter2 = new CategoryListAdapter(getParentActivity(), m_categoriesShown, m_categoryStringsShown);
		m_lstCategories.setAdapter(adapter2);
		
		{RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_lstCategories.getLayoutParams();
		params.height = (int) ((float) m_categoriesShown.size() * Misc.dipsToPixels(getResources(), 50));
		m_lstCategories.setLayoutParams(params);}
		
		double income = 0;
		double expenditure = 0;
		for(Transaction transaction : m_transactions)
    	{
			if (transaction.isDontReport())
				continue;
				
    		Category thisCategory = null;
    		for(Category category : m_categories)
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
		
		m_txtIncome.setText(Misc.formatValue(getParentActivity(), income));
		m_txtExpenditure.setText(Misc.formatValue(getParentActivity(), expenditure));

		m_scrollView.post(new Runnable() {
            public void run() {
                m_scrollView.scrollTo(0, 0);
            }
        });
    }
    
    private double getCategoryValue(Category category, boolean getSubcategoryValue)
    {
    	//get my value
		double categoryValue = 0;
		for(Transaction transaction : m_transactions)
		{
			if (transaction.isDontReport())
				continue;
			if (!getCategory(transaction.getCategory()).isIncome() && getCategory(transaction.getCategory()).isUseInReports() && transaction.getCategory() == category.getId())
				categoryValue -= transaction.getValue();
		} 
		
		if (getSubcategoryValue && category.getParentCategoryId() == -1)
		{
			for(Category subCat : m_categories)
			{
				if (subCat.getParentCategoryId() == category.getId())
					categoryValue += getCategoryValue(subCat, false);
			}
		}
		
		return categoryValue;
    }
    
    private void setSegments()
    {
		m_categoriesShown = new ArrayList<Category>();
		m_categoryStringsShown = new ArrayList<String>();
		
		ArrayList<PieChartSegment> categorySegments = new ArrayList<PieChartSegment>();
		
		//get the total value of everything
		double total = 0;
		for(Transaction transaction : m_transactions)
		{
			if (transaction.isDontReport())
				continue;
			if (!getCategory(transaction.getCategory()).isIncome() && getCategory(transaction.getCategory()).isUseInReports())
				total -= transaction.getValue();
		}
		
		ArrayList<CategoryValuePair> segmentList = new ArrayList<CategoryValuePair>();
		
		for(Category category : m_categories)
		{
			if (category.isIncome() || !category.isUseInReports())
				continue;
			
			if (!m_chkShowSubcategories.isChecked() && category.getParentCategoryId() != -1)
				continue; // we will pick this up in the value of the parent
			
			segmentList.add(new CategoryValuePair(
					category, getCategoryValue(category, !m_chkShowSubcategories.isChecked())));
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
				m_categoriesShown.add(catValPair.category);
				DecimalFormat df = new DecimalFormat("#.##");
				m_categoryStringsShown.add(Misc.formatValue(getParentActivity(), catValPair.value) + "  -  " + df.format((catValPair.value / total) * 100) + "%");
			}
		}
		
		m_pieChartView.setSegments(categorySegments);
    }
    
    private Category getCategory(int id)
    {
    	for(Category category : m_categories)
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
		m_transactions = DatabaseManager.getInstance(getParentActivity()).GetAllTransactions();
		Collections.sort(m_transactions, new DateComparator());
    	Collections.reverse(m_transactions);

    	final ArrayList<String> months = new ArrayList<String>();
    	if (m_transactions.size() == 0)
    	{
    		Calendar currentDate = Calendar.getInstance();
		    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    months.add(m_monthYearFormat.format(currentDate.getTime()));
    	}
    	else
    	{
	    	Transaction oldestTransaction = m_transactions.get(m_transactions.size()-1);
	    	Calendar oldestDate = Calendar.getInstance();
	    	oldestDate.setTime(oldestTransaction.getDateTime());
	    	oldestDate.set(oldestDate.get(Calendar.YEAR), oldestDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    
		    Calendar currentDate = Calendar.getInstance();
		    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 0);
	    	while(true)
	    	{
	    		months.add(m_monthYearFormat.format(currentDate.getTime()));
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


    /* CategoryValuePair class */

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
}
