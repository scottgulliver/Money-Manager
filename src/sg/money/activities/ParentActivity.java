package sg.money.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockActionBarDrawerToggle;
import com.actionbarsherlock.app.SherlockActionBarDrawerToggleDelegate;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import java.util.ArrayList;
import sg.money.fragments.AccountsFragment;
import sg.money.fragments.BudgetsFragment;
import sg.money.fragments.CategoriesFragment;
import sg.money.adapters.DrawerArrayAdapter;
import sg.money.domainobjects.DrawerArrayItem;
import sg.money.fragments.HostActivityFragmentBase;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.fragments.OverviewFragment;
import sg.money.R;
import sg.money.utils.Settings;
import sg.money.fragments.TransactionsHolderFragment;

/**
 * Holds the main navigation drawer, and uses fragments to populate its view.
 */
public class ParentActivity extends BaseFragmentActivity implements ActionBarDrawerToggle.DelegateProvider
{

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
	
	
    public static final String INTENTEXTRA_CONTENTTYPE = "INTENTEXTRA_CONTENTTYPE";

    private DrawerLayout m_drawerLayout;
    private ListView m_drawerList;
    private SherlockActionBarDrawerToggle m_drawerToggle;
    private SherlockActionBarDrawerToggleDelegate m_drawerDelegate;
    private HostActivityFragmentBase m_currentFragment;
    private ArrayList<DrawerArrayItem> m_drawerItems;
    private ActionMode m_actionMode;
	
	
	/* Activity overrides */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_parent);

        m_drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        m_drawerList = (ListView)findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        m_drawerItems = new ArrayList<DrawerArrayItem>();
        m_drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Transactions.name(), R.drawable.sort_by_size));
        m_drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Accounts.name(), R.drawable.bank));
        m_drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Categories.name(), R.drawable.categories));
        m_drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Overview.name(), R.drawable.overview));
        m_drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Budgets.name(), R.drawable.percent));
        m_drawerList.setAdapter(new DrawerArrayAdapter(this, m_drawerItems));
        // Set the list's click listener
        m_drawerList.setOnItemClickListener(new DrawerItemClickListener());

        m_drawerDelegate = new SherlockActionBarDrawerToggleDelegate(this);
        m_drawerToggle = new SherlockActionBarDrawerToggle(this, m_drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                if (m_actionMode != null)
                {
                    m_actionMode.finish();
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        m_drawerLayout.setDrawerListener(m_drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(savedInstanceState == null)
        {
            if (getIntent() != null && getIntent().hasExtra(INTENTEXTRA_CONTENTTYPE))
            {
                changeContent((HostActivityFragmentTypes)getIntent().getSerializableExtra(INTENTEXTRA_CONTENTTYPE));
            }
            else
            {
                changeContent(HostActivityFragmentTypes.Transactions);
            }
        }

        //open the drawer if this is the first run
        if (!Settings.getFirstTimeDrawerOpened(this))
        {
            m_drawerLayout.openDrawer(m_drawerList);
            Settings.setFirstTimeDrawerOpened(this, true);
        }
    }

    @Override
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return m_drawerDelegate;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        m_drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        m_drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (m_currentFragment != null)
        {
            m_currentFragment.onCreateOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        m_currentFragment.onPrepareOptionsMenu(menu, m_drawerLayout.isDrawerOpen(m_drawerList));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (m_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        m_currentFragment.onOptionsItemSelected(item);

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        m_currentFragment.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	
	/* Other methods */

    public ActionMode getActionMode()
    {
        return m_actionMode;
    }

    public void setActionMode(ActionMode actionMode)
    {
        this.m_actionMode = actionMode;
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        if (position > m_drawerItems.size())
        {
            throw new RuntimeException("Unexpected positon.");
        }

        changeContent(HostActivityFragmentTypes.valueOf(m_drawerItems.get(position).getText()));
    }

    public void changeContent(HostActivityFragmentTypes newType)
    {
        switch(newType)
        {
            case Transactions:
                changeFragment(TransactionsHolderFragment.class, newType.name());
                break;
            case Accounts:
                changeFragment(AccountsFragment.class, newType.name());
                break;
            case Categories:
                changeFragment(CategoriesFragment.class, newType.name());
                break;
            case Overview:
                changeFragment(OverviewFragment.class, newType.name());
                break;
            case Budgets:
                changeFragment(BudgetsFragment.class, newType.name());
                break;
        }
    }

    private <T extends HostActivityFragmentBase> void changeFragment(Class<T> fragmentClass, String title)
    {
        try {
            HostActivityFragmentBase fragment = fragmentClass.newInstance();
            getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.parentFrame, fragment)
				.commit();
            m_currentFragment = fragment;
            setTitle(title);
            m_drawerLayout.closeDrawer(m_drawerList);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
