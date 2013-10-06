package sg.money.fragments;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.*;
import com.actionbarsherlock.app.ActionBar.*;

import java.util.*;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import sg.money.domainobjects.Account;
import sg.money.DatabaseManager;
import sg.money.R;
import sg.money.utils.Settings;
import sg.money.activities.SettingsActivity;
import sg.money.activities.AddTransactionActivity;
import sg.money.activities.ParentActivity;

public class TransactionsHolderFragment extends HostActivityFragmentBase
{
    static final int REQUEST_ADDTRANSACTION = 0;
    static final int REQUEST_VIEWACCOUNTS = 1;
	static final int REQUEST_VIEWCATEGORIES = 2;
    static final int REQUEST_SETTINGS = 10;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.activity_transactions, null);

        viewPager = (ViewPager) v.findViewById(R.id.pager);
        titleStrip = (PagerTitleStrip) v.findViewById(R.id.pager_title_strip);
        viewPager.setOffscreenPageLimit(3);

        actionBar = ((ParentActivity)getActivity()).getSupportActionBar();
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		showReconciled = sharedPref.getBoolean(SETTING_SHOWRECONCILED, true);
        
        UpdateUI();

        return v;
    }
    
    private void UpdateUI()
    {
    	tabsAdapter = new TabsAdapter(this, viewPager);
    	actionBar.removeAllTabs();
        
        accounts = DatabaseManager.getInstance(getActivity()).GetAllAccounts();
        
        for(Account account : accounts)
        {
        	Bundle tabBundle = new Bundle();
        	tabBundle.putInt("AccountID", account.getId());
	        tabsAdapter.addTab(actionBar.newTab().setText(account.getName()), AccountTransactionsFragment.class, tabBundle, account);
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
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Integer defaultAccountId = Settings.getDefaultAccount(getActivity());
        if (defaultAccountId != -1)
        {
            Account defaultAccount = null;
            for(Account account : accounts)
            {
                if (account.getId() == defaultAccountId)
                {
                    defaultAccount = account;
                    break;
                }
            }
            if (defaultAccount != null)
            {
                viewPager.setCurrentItem(accounts.indexOf(defaultAccount));
                selectedAccount = defaultAccount;
            }
        }
        
        if (menu != null)
        {
	        this.menu.clear();
            ((ParentActivity)getActivity()).onCreateOptionsMenu(this.menu);
        }
    }
    
    public static class TabsAdapter extends FragmentStatePagerAdapter
    	implements ActionBar.TabListener, ViewPager.OnPageChangeListener 
	{
    	private final TransactionsHolderFragment context;
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
    	
		public TabsAdapter(TransactionsHolderFragment activity, ViewPager pager) {
			super(activity.getChildFragmentManager());
			context = activity;
			actionBar = ((ParentActivity)activity.getActivity()).getSupportActionBar();
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
			Fragment fragment = Fragment.instantiate(context.getActivity(), info.clss.getName(), info.args);
			fragments.add(fragment);
			return fragment;
		}
		
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		public void onPageSelected(int position) {
            Account newAccountSelection = tabs.get(position).account;
			context.selectedAccount = newAccountSelection;

			//save this, so we load it by default next time
            Settings.setDefaultAccount(context.getActivity(), newAccountSelection.getId());
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
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		useReconcile = sharedPref.getBoolean(getString(R.string.pref_usereconcile_key), true);
		
		if (!useReconcile)
			inReconcileMode = false;
			
		if ((oldValue != useReconcile) && updateMenu && menu != null)
		{
			menu.clear();
            ((ParentActivity)getActivity()).onCreateOptionsMenu(menu);
		}
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

	public void UpdateTransactions()
	{
		if (accounts.isEmpty())
			return;
			
		updateCanReconcile(true);
		
		for(Fragment fragment : tabsAdapter.fragments)
		{
			((AccountTransactionsFragment)fragment).UpdateList();
		}
	}
	
	public static void RemovefragmentsFocus()
	{
		if (accounts.isEmpty())
			return;
		
		for(Fragment fragment : tabsAdapter.fragments)
		{
			((AccountTransactionsFragment)fragment).focusLost();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getParentActivity().getSupportMenuInflater().inflate(R.menu.activity_transactions, menu);

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
    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        menu.findItem(R.id.menu_addtransaction).setVisible(!drawerIsOpen);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_addtransaction:{
                Intent intent = new Intent(getParentActivity(), AddTransactionActivity.class);
                intent.putExtra("AccountID", selectedAccount.getId());
                startActivityForResult(intent, REQUEST_ADDTRANSACTION);
                break;
            }

            case R.id.menu_settings:{
                startActivityForResult(new Intent(getParentActivity(), SettingsActivity.class), REQUEST_SETTINGS);
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
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getParentActivity());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(SETTING_SHOWRECONCILED, showReconciled);
                editor.commit();

                UpdateTransactions();
                menu.clear();
                onCreateOptionsMenu(this.menu);
                break;
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ADDTRANSACTION:
            {
                UpdateTransactions();
                //((AccountTransactionsFragment)currentFragment).UpdateList(); FIX THIS AND ADD BACK IN
                break;
            }
            case REQUEST_VIEWACCOUNTS:
            {
                UpdateUI();
                if (resultCode == getParentActivity().RESULT_OK)
                {
                    int accountId = data.getIntExtra("AccountID", -1);
                    if (accountId != -1)
                    {
                        int index = -1;
                        for(Account account : accounts)
                        {
                            if (account.getId() == accountId)
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
}
