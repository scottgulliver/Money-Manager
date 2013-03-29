package sg.money;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;

public class TransactionsActivity extends BaseFragmentActivity
{
    static final int REQUEST_ADDTRANSACTION = 0;
    static final int REQUEST_VIEWACCOUNTS = 1;
    static final int REQUEST_SETTINGS = 10;
    
    static final String SETTING_LASTACCOUNTVIEWED = "SETTING_LASTACCOUNTVIEWED";
    
    ViewPager viewPager;
    static TabsAdapter tabsAdapter;
    PagerTitleStrip titleStrip;
    
    public Account selectedAccount = null;
    
    static ArrayList<Account> accounts;
    
    ActionBar actionBar;
    
    Menu menu;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);

	        setContentView(R.layout.activity_transactions);
	        viewPager = (ViewPager) findViewById(R.id.pager);
	        titleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
	        viewPager.setOffscreenPageLimit(3);

	        actionBar = getSupportActionBar();
	        
	        UpdateUI();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    private void UpdateUI()
    {
    	tabsAdapter = new TabsAdapter(this, viewPager);
    	actionBar.removeAllTabs();
        
        accounts = DatabaseManager.getInstance(TransactionsActivity.this).GetAllAccounts();
        
        for(Account account : accounts)
        {
        	Bundle tabBundle = new Bundle();
        	tabBundle.putInt("AccountID", account.id);
	        tabsAdapter.addTab(actionBar.newTab().setText(account.name), TransactionsFragment.class, tabBundle, account);
        }
        
        if (accounts.isEmpty())
        {
        	Bundle tabBundle = new Bundle();
        	tabBundle.putString("EmptyText", "No accounts are created");
        	tabBundle.putString("EmptyHint", "Go to Accounts to create one.");
	        tabsAdapter.addTab(actionBar.newTab().setText("(No accounts)"), EmptyListFragment.class, tabBundle, null);
        	viewPager.setCurrentItem(0);
        }
        
        selectedAccount = !accounts.isEmpty() ? accounts.get(0) : null;

        //load last viewed page 
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Integer selectedPosition = sharedPref.getInt(SETTING_LASTACCOUNTVIEWED, -1);
        if (selectedPosition != -1 && selectedPosition < accounts.size())
        {
        	viewPager.setCurrentItem(selectedPosition);
	        selectedAccount = accounts.get(selectedPosition);
        }
        
        this.menu.clear();
        this.onCreateOptionsMenu(this.menu);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
    
    public static class TabsAdapter extends FragmentStatePagerAdapter
    	implements ActionBar.TabListener, ViewPager.OnPageChangeListener 
	{
    	private final TransactionsActivity context;
    	private final ActionBar actionBar;
    	private final ViewPager viewPager;
    	public final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();
    	public final ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    	
    	static final class TabInfo
    	{
    		private final Class<?> clss;
    		private final String title;
    		private final Bundle args;
    		private final Account account;
    		
    		TabInfo(Class<?> _class, Bundle _args, String _title, Account _account)
    		{
    			clss = _class;
    			args = _args;
    			title = _title;
    			account = _account;
    		}
    	}
    	
    	@Override
    	public CharSequence getPageTitle(int position) {
    		return tabs.get(position).title;
    	}
    	
		public TabsAdapter(TransactionsActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			context = activity;
			actionBar = activity.getSupportActionBar();
			viewPager = pager;
			viewPager.setAdapter(this);
			viewPager.setOnPageChangeListener(this);
		}
		
		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args, Account account)
		{
			TabInfo info = new TabInfo(clss, args, tab.getText().toString(), account);
			tab.setTag(info);
			tab.setTabListener(this);
			tabs.add(info);
			actionBar.addTab(tab);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return tabs.size();
		}
		
		@Override
		public Fragment getItem(int position) {
			TabInfo info = tabs.get(position);
			Fragment fragment = Fragment.instantiate(context, info.clss.getName(), info.args);
			fragments.add(fragment);
			return fragment;
		}
		
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		public void onPageSelected(int position) {
			context.selectedAccount = tabs.get(position).account;

			//save this, so we load it by default next time
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = sharedPref.edit();
			editor.putInt(SETTING_LASTACCOUNTVIEWED, position);
			editor.commit();
			RemovefragmentsFocus();
		}
		
		public void onPageScrollStateChanged(int state)
		{
			if (state ==1)
			{
				
			}
		}
		
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for(int i = 0; i < tabs.size(); i++)
			{
				if (tabs.get(i) == tag) {
					viewPager.setCurrentItem(i);
				}
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_transactions, menu);
        MenuItem addTransaction = menu.findItem(R.id.menu_addtransaction);
        addTransaction.setVisible(accounts.size() > 0);
        this.menu = menu;
        return true; 
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	    	case R.id.menu_addtransaction:{
	    		Intent intent = new Intent(TransactionsActivity.this, AddTransactionActivity.class);
	    		intent.putExtra("AccountID", selectedAccount.id);
	        	startActivityForResult(intent, REQUEST_ADDTRANSACTION);
	    		break;
	    		}

	    	case R.id.menu_viewaccounts:{
	    		Intent intent = new Intent(this, AccountsActivity.class);
	        	startActivityForResult(intent, REQUEST_VIEWACCOUNTS);
	        	break;
	    		}

	    	case R.id.menu_viewbudgets:{
	    		Intent intent = new Intent(this, BudgetsActivity.class);
	        	startActivity(intent);
	        	break;
	    		}

	    	case R.id.menu_viewoverview:{
	    		Intent intent = new Intent(this, OverviewActivity.class);
	        	startActivity(intent);
	        	break;
	    		} 
	    	
	    	case R.id.menu_managecategories:{
	    		Intent intent = new Intent(this, CategoriesActivity.class);
	        	startActivity(intent);
	        	break;
	    		}
	    	
	        case R.id.menu_settings:{
	        	startActivityForResult(new Intent(TransactionsActivity.this, SettingsActivity.class), REQUEST_SETTINGS);
                break;
            	}
	    	
	        case R.id.menu_feedback:{
	        	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	        	emailIntent.setType("plain/text");
	        	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"pennypressgames@gmail.com"});
	        	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback for Money Manager");
	        	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                break;
            	}
	    	
	        case R.id.menu_exit:{
	        	finish();
                break;
            	}
	    }
	    return true;
	}
    
    protected void onListItemClick(AdapterView<?> l, View v, int position, long id)
	{
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case REQUEST_ADDTRANSACTION:
			{
				if (resultCode == RESULT_OK)
				{
					UpdateTransactions();
					//((TransactionsFragment)currentFragment).UpdateList(); FIX THIS AND ADD BACK IN
				}
				break;
			}
			case REQUEST_VIEWACCOUNTS:
			{
				UpdateUI();
				if (resultCode == RESULT_OK)
				{
					int accountId = data.getIntExtra("AccountID", -1);
					if (accountId != -1)
			        {
			        	int index = -1;
			        	for(Account account : accounts)
			        	{
			        		if (account.id == accountId)
			        		{
			        			index = accounts.indexOf(account);
			        			break;
			        		}
			        	}
			        	viewPager.setCurrentItem(index,false);
				        selectedAccount = accounts.get(index);
			        }
				}
				break;
			}
			case REQUEST_SETTINGS:
			{
				UpdateTransactions();
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
    }

	public void UpdateTransactions()
	{
		if (accounts.isEmpty())
			return;
		
		for(Fragment fragment : tabsAdapter.fragments)
		{
			((TransactionsFragment)fragment).UpdateList();
		}
	}
	
	public static void RemovefragmentsFocus()
	{
		if (accounts.isEmpty())
			return;
		
		for(Fragment fragment : tabsAdapter.fragments)
		{
			((TransactionsFragment)fragment).focusLost();
		}
	}
}