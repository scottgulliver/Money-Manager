package sg.money;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class TransactionsActivity extends BaseFragmentActivity
{
    static final int REQUEST_ADDTRANSACTION = 0;
    static final int REQUEST_SETTINGS = 10;
    
    ViewPager viewPager;
    TabsAdapter tabsAdapter;
    PagerTitleStrip titleStrip;
    Fragment currentFragment;
    
    public Account selectedAccount = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);

	        setContentView(R.layout.activity_transactions);
	        viewPager = (ViewPager) findViewById(R.id.pager);
	        titleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
	        //viewPager.setId(R.id.pager);
	        viewPager.setOffscreenPageLimit(3);

	        final ActionBar bar = getActionBar();
	        //bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	        //bar.setDisplayOptions(0,  ActionBar.DISPLAY_SHOW_TITLE);
	        
	        tabsAdapter = new TabsAdapter(this, viewPager);
	        
	        ArrayList<Account> accounts = DatabaseManager.getInstance(TransactionsActivity.this).GetAllAccounts();
	        
	        for(Account account : accounts)
	        {
	        	Bundle tabBundle = new Bundle();
	        	tabBundle.putInt("AccountID", account.id);
		        tabsAdapter.addTab(bar.newTab().setText(account.name), TransactionsFragment.class, tabBundle, account);
	        }
	        
	        selectedAccount = accounts.get(0);
	        
	        if (savedInstanceState != null)
	        {
	        	//bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
	        }
	        
	        if (getIntent().getIntExtra("AccountID", -1) != -1)
	        {
	        	int index = -1;
	        	for(Account account : accounts)
	        	{
	        		if (account.id == getIntent().getIntExtra("AccountID", 0))
	        		{
	        			index = accounts.indexOf(account);
	        			break;
	        		}
	        	}
	        	viewPager.setCurrentItem(index);
		        selectedAccount = accounts.get(index);
	        }
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }
    
    public static class TabsAdapter extends FragmentPagerAdapter
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
			actionBar = activity.getActionBar();
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
			context.currentFragment = Fragment.instantiate(context, info.clss.getName(), info.args);
			fragments.add(context.currentFragment);
			return context.currentFragment;
		}
		
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		public void onPageSelected(int position) {
			//actionBar.setSelectedNavigationItem(position);
			context.selectedAccount = tabs.get(position).account;
			context.currentFragment = fragments.get(position);
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
        getMenuInflater().inflate(R.menu.activity_transactions, menu);
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
	    	
	        case R.id.menu_settings:{
	        	startActivityForResult(new Intent(TransactionsActivity.this, SettingsActivity.class), REQUEST_SETTINGS);
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
					UpdateUI();
					//((TransactionsFragment)currentFragment).UpdateList(); FIX THIS AND ADD BACK IN
				}
				break;
			}
			case REQUEST_SETTINGS:
			{
				UpdateUI();
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
    }

	@Override
	protected int thisActivity() {
		return BaseFragmentActivity.ACTIVITY_TRANSACTIONS;
	}
	
	public void UpdateUI()
	{
		for(Fragment fragment : tabsAdapter.fragments)
		{
			((TransactionsFragment)fragment).UpdateList();
		}
	}
}