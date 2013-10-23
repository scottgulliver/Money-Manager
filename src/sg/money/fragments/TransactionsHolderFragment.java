package sg.money.fragments;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.view.*;
import com.actionbarsherlock.app.*;
import com.actionbarsherlock.app.ActionBar.*;
import java.util.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import sg.money.domainobjects.Account;
import sg.money.common.DatabaseManager;
import sg.money.R;
import sg.money.utils.Settings;
import sg.money.activities.SettingsActivity;
import sg.money.activities.AddTransactionActivity;
import sg.money.activities.ParentActivity;

public class TransactionsHolderFragment extends HostActivityFragmentBase
{
    private ViewPager m_viewPager;
    private static TabsAdapter m_tabsAdapter;
    private Account m_selectedAccount;
    private static ArrayList<Account> m_accounts;
    private ActionBar m_actionBar;
    private Menu m_menu;
	private boolean m_inReconcileMode;
	private boolean m_showReconciled;

    private static final int REQUEST_ADDTRANSACTION = 0;
    private static final int REQUEST_VIEWACCOUNTS = 1;
    private static final int REQUEST_VIEWCATEGORIES = 2;
    private static final int REQUEST_SETTINGS = 10;
    private static final String SETTING_SHOWRECONCILED = "SETTING_SHOWRECONCILED";


    /* Getters / setters */

    public boolean isInReconcileMode()
    {
        return m_inReconcileMode;
    }

    public boolean showReconciledTransactions()
    {
        return m_showReconciled || m_inReconcileMode;
    }


    /* Fragment overrides */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.activity_transactions, null);

        m_viewPager = (ViewPager) v.findViewById(R.id.pager);
        m_viewPager.setOffscreenPageLimit(3);

        m_actionBar = ((ParentActivity)getActivity()).getSupportActionBar();
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		m_showReconciled = sharedPref.getBoolean(SETTING_SHOWRECONCILED, true);
        
        UpdateUI();

        return v;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getParentActivity().getSupportMenuInflater().inflate(R.menu.activity_transactions, menu);
		conditionMenu(menu);
        m_menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        menu.findItem(R.id.menu_addtransaction).setVisible(!drawerIsOpen);
		conditionMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_addtransaction:{
                Intent intent = new Intent(getParentActivity(), AddTransactionActivity.class);
                intent.putExtra("AccountID", m_selectedAccount.getId());
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
                m_inReconcileMode = true;
                UpdateTransactions();
                m_menu.clear();
                onCreateOptionsMenu(this.m_menu);
                break;
				}

            case R.id.menu_acceptreconcilechanges:{
				m_inReconcileMode = false;
				UpdateTransactions();
				m_menu.clear();
				onCreateOptionsMenu(this.m_menu);
				break;
				}

            case R.id.menu_showhidereconciled:{
                m_showReconciled = !m_showReconciled;

                // can this be done on exit / async?
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getParentActivity());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(SETTING_SHOWRECONCILED, m_showReconciled);
                editor.commit();

                UpdateTransactions();
                m_menu.clear();
                onCreateOptionsMenu(this.m_menu);
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
                break;
            }
            case REQUEST_VIEWACCOUNTS:
            {
                UpdateUI();
                if (resultCode == Activity.RESULT_OK)
                {
                    int accountId = data.getIntExtra("AccountID", -1);
                    if (accountId != -1)
                    {
                        int index = -1;
                        for(Account account : m_accounts)
                        {
                            if (account.getId() == accountId)
                            {
                                index = m_accounts.indexOf(account);
                                break;
                            }
                        }
                        m_viewPager.setCurrentItem(index, false);
                        m_selectedAccount = m_accounts.get(index);
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


    /* Methods */
    
    private void UpdateUI()
    {
    	m_tabsAdapter = new TabsAdapter(this, m_viewPager);
    	m_actionBar.removeAllTabs();
        
        m_accounts = DatabaseManager.getInstance(getActivity()).GetAllAccounts();
        
        for(Account account : m_accounts)
        {
        	Bundle tabBundle = new Bundle();
        	tabBundle.putInt("AccountID", account.getId());
	        m_tabsAdapter.addTab(m_actionBar.newTab().setText(account.getName()), AccountTransactionsFragment.class, tabBundle, account);
        }
        
        if (m_accounts.isEmpty())
        {
        	Bundle tabBundle = new Bundle();
        	tabBundle.putString("EmptyText", "No m_accounts are created");
        	tabBundle.putString("EmptyHint", "Go to Accounts to create one.");
	        m_tabsAdapter.addTab(m_actionBar.newTab().setText("(No m_accounts)"), EmptyListFragment.class, tabBundle, null);
        	m_viewPager.setCurrentItem(0);
        }
        
        m_selectedAccount = !m_accounts.isEmpty() ? m_accounts.get(0) : null;

        //load last viewed page
		Integer defaultAccountId = Settings.getDefaultAccount(getActivity());
        if (defaultAccountId != -1)
        {
            Account defaultAccount = null;
            for(Account account : m_accounts)
            {
                if (account.getId() == defaultAccountId)
                {
                    defaultAccount = account;
                    break;
                }
            }
            if (defaultAccount != null)
            {
                m_viewPager.setCurrentItem(m_accounts.indexOf(defaultAccount));
                m_selectedAccount = defaultAccount;
            }
        }
        
        if (m_menu != null)
        {
	        this.m_menu.clear();
            ((ParentActivity)getActivity()).onCreateOptionsMenu(this.m_menu);
        }
    }

	private void conditionMenu(Menu menu)
	{
        MenuItem addTransaction = menu.findItem(R.id.menu_addtransaction);
        addTransaction.setVisible(m_accounts.size() > 0 && !m_inReconcileMode);

        MenuItem showHideReconciled = menu.findItem(R.id.menu_showhidereconciled);
        showHideReconciled.setVisible(!m_inReconcileMode && m_accounts.size() > 0);
        showHideReconciled.setTitle(m_showReconciled ? "Hide Reconciled" : "Show Reconciled");

        MenuItem reconcileTransactions = menu.findItem(R.id.menu_reconcile);
        reconcileTransactions.setVisible(!m_inReconcileMode && m_accounts.size() > 0);

        MenuItem finishReconciliation = menu.findItem(R.id.menu_acceptreconcilechanges);
        finishReconciliation.setVisible(m_inReconcileMode && m_accounts.size() > 0);	
	}

	public void UpdateTransactions()
	{
		if (m_accounts.isEmpty())
			return;
		
		for(Fragment fragment : m_tabsAdapter.fragments)
		{
			((AccountTransactionsFragment)fragment).UpdateList();
		}
	}
	
	public static void removefragmentsFocus()
	{
		if (m_accounts.isEmpty())
			return;
		
		for(Fragment fragment : m_tabsAdapter.fragments)
		{
			((AccountTransactionsFragment)fragment).focusLost();
		}
	}


    /* TabsAdapter class */

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
            context.m_selectedAccount = newAccountSelection;

            //save this, so we load it by default next time
            Settings.setDefaultAccount(context.getActivity(), newAccountSelection.getId());
            removefragmentsFocus();
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
}
