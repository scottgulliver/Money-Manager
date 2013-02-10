package sg.money;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
 
public abstract class BaseFragmentActivity extends FragmentActivity
{
	protected static final int ACTIVITY_OVERVIEW = 0;
	protected static final int ACTIVITY_ACCOUNTS = 1;
	protected static final int ACTIVITY_TRANSACTIONS = 2;
	protected static final int ACTIVITY_CATEGORIES = 3;
	protected static final int ACTIVITY_BUDGETS = 4;
	
	ArrayList<String> activityNames;
	ActionBar actionBar;
	boolean firstSelection;
	
	private ArrayList<String> buildArray()
	{
		firstSelection = true;
		ArrayList<String> array = new ArrayList<String>();
		array.add("Overview");
		array.add("Accounts");
		array.add("Transactions");
		array.add("Categories");
		array.add("Budgets");
		return array;
	}
	
	private void SpinnerOnItemSelected(int position, long itemId)
	{
		if (firstSelection)
		{
			firstSelection = false;
			return;
		}
		switch(position)
		{
		case ACTIVITY_ACCOUNTS:
			startActivity(new Intent(this, AccountsActivity.class));
			finish();
			break;
		case ACTIVITY_TRANSACTIONS:
			startActivity(new Intent(this, TransactionsActivity.class));
			finish();
			break;
		case ACTIVITY_CATEGORIES:
			startActivity(new Intent(this, CategoriesActivity.class));
			finish();
			break;
		case ACTIVITY_OVERVIEW:
			startActivity(new Intent(this, OverviewActivity.class));
			finish();
			break;
		case ACTIVITY_BUDGETS:
			startActivity(new Intent(this, BudgetsActivity.class));
			finish();
			break;
		}

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    actionBar.setDisplayShowTitleEnabled(false);
	    actionBar.setDisplayUseLogoEnabled(true);
		
		activityNames = buildArray();
		
		SetSpinnerItems();
	}
	
	protected abstract int thisActivity();
	
	private void SetSpinnerItems()
	{
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(actionBar.getThemedContext(),android.R.layout.simple_spinner_dropdown_item, activityNames);
		//spinner.setAdapter(arrayAdapter);
		
		SpinnerAdapter mSpinnerAdapter = arrayAdapter;
		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			  public boolean onNavigationItemSelected(int position, long itemId) {
			    SpinnerOnItemSelected(position, itemId);
			    return true;
			  }
			};
	    actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
	    actionBar.setSelectedNavigationItem(thisActivity());
	}
}
