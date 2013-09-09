package sg.money;

import android.content.*;
import android.content.SharedPreferences.*;
import android.content.res.Configuration;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.*;
import com.actionbarsherlock.app.ActionBar.*;
import com.actionbarsherlock.view.*;
import java.util.*;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class TransactionsActivity extends BaseFragmentActivity implements ActionBarDrawerToggle.DelegateProvider
{
    static final int REQUEST_ADDTRANSACTION = 0;
    static final int REQUEST_VIEWACCOUNTS = 1;
	static final int REQUEST_VIEWCATEGORIES = 2;
    static final int REQUEST_SETTINGS = 10;
    
    static final String SETTING_LASTACCOUNTVIEWED = "SETTING_LASTACCOUNTVIEWED";
	static final String SETTING_SHOWRECONCILED = "SETTING_SHOWRECONCILED";
    
    ViewPager viewPager;
    static TabsAdapter tabsAdapter;
    PagerTitleStrip titleStrip;
    
    public Account selectedAccount = null;
    
    static ArrayList<Account> accounts;
    
    ActionBar actionBar;
    
    Menu menu;
	
	private boolean useReconcile;
	private boolean inReconcileMode;
	
	private boolean showReconciled;
	
	private String[] mLinks;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    SherlockActionBarDrawerToggle mDrawerToggle;
    private SherlockActionBarDrawerToggleDelegate mDrawerDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transactions);
        viewPager = (ViewPager) findViewById(R.id.pager);
        titleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        viewPager.setOffscreenPageLimit(3);

        actionBar = getSupportActionBar();
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		showReconciled = sharedPref.getBoolean(SETTING_SHOWRECONCILED, true);
        
        UpdateUI();

        mLinks = getResources().getStringArray(R.array.drawer_links_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mLinks));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerDelegate = new SherlockActionBarDrawerToggleDelegate(this);
        mDrawerToggle = new SherlockActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return mDrawerDelegate;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Create a new fragment and specify the planet to show based on position
		/*Fragment fragment = new PlanetFragment();
		Bundle args = new Bundle();
		args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		fragment.setArguments(args);

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
			.replace(R.id.content_frame, fragment)
			.commit();

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);*/
	}

	@Override
	public void setTitle(CharSequence title) {
		//mTitle = title;
		//getActionBar().setTitle(mTitle);
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
		 
		updateCanReconcile(true);
        
        selectedAccount = !accounts.isEmpty() ? accounts.get(0) : null;

        //load last viewed page 
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Integer selectedPosition = sharedPref.getInt(SETTING_LASTACCOUNTVIEWED, -1);
        if (selectedPosition != -1 && selectedPosition < accounts.size())
        {
        	viewPager.setCurrentItem(selectedPosition);
	        selectedAccount = accounts.get(selectedPosition);
        }
        
        if (menu != null)
        {
	        this.menu.clear();
	        this.onCreateOptionsMenu(this.menu);
        }
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
	
	private void updateCanReconcile(boolean updateMenu)
	{
		boolean oldValue = useReconcile;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		useReconcile = sharedPref.getBoolean(getString(R.string.pref_usereconcile_key), true);
		
		if (!useReconcile)
			inReconcileMode = false;
			
		if ((oldValue != useReconcile) && updateMenu && menu != null)
		{
			menu.clear();
			onCreateOptionsMenu(menu);
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_transactions, menu);
       
	    MenuItem addTransaction = menu.findItem(R.id.menu_addtransaction);
        addTransaction.setVisible(accounts.size() > 0);
		
		updateCanReconcile(false);

        MenuItem reconcileTransactions = menu.findItem(R.id.menu_reconcile);
		MenuItem showHideReconciled = menu.findItem(R.id.menu_showhidereconciled);
        reconcileTransactions.setVisible(useReconcile);
		showHideReconciled.setVisible(useReconcile && !inReconcileMode);
		reconcileTransactions.setTitle(inReconcileMode ? "Finish Reconciling" : "Reconcile Transactions");
		showHideReconciled.setTitle(showReconciled ? "Hide Reconciled" : "Show Reconciled");
        
		this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.menu_addtransaction).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
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
	        	startActivityForResult(intent, REQUEST_VIEWCATEGORIES);
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
				
			case R.id.menu_reconcile:{
				inReconcileMode = !inReconcileMode;
				UpdateTransactions();
				menu.clear();
				onCreateOptionsMenu(this.menu);
				break;
				}
				
			case R.id.menu_showhidereconciled:{
					showReconciled = !showReconciled;
					
				// can this be done on exit / async?
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
				Editor editor = sharedPref.edit();
				editor.putBoolean(SETTING_SHOWRECONCILED, showReconciled);
				editor.commit();
					
				UpdateTransactions();
				menu.clear();
				onCreateOptionsMenu(this.menu);
				break;
			}
	    	
	        case R.id.menu_exit:{
	        	finish();
                break;
            	}
	    }
	    return true;
	}
	
	public boolean isInReconcileMode()
	{
		return inReconcileMode;
	}
	
	public boolean showReconciledTransactions()
	{
		return showReconciled || inReconcileMode || !useReconcile;
	}
	
	public boolean useReconcile()
	{
		return useReconcile;
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
				UpdateTransactions();
				//((TransactionsFragment)currentFragment).UpdateList(); FIX THIS AND ADD BACK IN
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
			case REQUEST_VIEWCATEGORIES:
			case REQUEST_SETTINGS:
			{
				UpdateUI();
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
			
		updateCanReconcile(true);
		
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
