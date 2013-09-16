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
import sg.money.adapters.DrawerArrayItem;
import sg.money.fragments.HostActivityFragmentBase;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.fragments.OverviewFragment;
import sg.money.R;
import sg.money.utils.Settings;
import sg.money.fragments.TransactionsHolderFragment;

public class ParentActivity extends BaseFragmentActivity implements ActionBarDrawerToggle.DelegateProvider
{
    public static final String INTENTEXTRA_CONTENTTYPE = "INTENTEXTRA_CONTENTTYPE";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    SherlockActionBarDrawerToggle mDrawerToggle;
    private SherlockActionBarDrawerToggleDelegate mDrawerDelegate;
    private HostActivityFragmentBase currentFragment;
    ArrayList<DrawerArrayItem> drawerItems;
    ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_parent);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerItems = new ArrayList<DrawerArrayItem>();
        drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Transactions.name(), R.drawable.sort_by_size));
        drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Accounts.name(), R.drawable.sort_by_size));
        drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Categories.name(), R.drawable.sort_by_size));
        drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Overview.name(), R.drawable.sort_by_size));
        drawerItems.add(new DrawerArrayItem(HostActivityFragmentTypes.Budgets.name(), R.drawable.sort_by_size));
        mDrawerList.setAdapter(new DrawerArrayAdapter(this, drawerItems));
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
                if (actionMode != null)
                {
                    actionMode.finish();
                }
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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
            mDrawerLayout.openDrawer(mDrawerList);
            Settings.setFirstTimeDrawerOpened(this, true);
        }
    }

    public ActionMode getActionMode()
    {
        return actionMode;
    }

    public void setActionMode(ActionMode actionMode)
    {
        this.actionMode = actionMode;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("tab", ((ParentActivity)getActivity()).getSupportActionBar().getSelectedNavigationIndex());
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
        if (position > drawerItems.size())
        {
            throw new RuntimeException("Unexpected positon.");
        }

        changeContent(HostActivityFragmentTypes.valueOf(drawerItems.get(position).getText()));
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
            currentFragment = fragment;
            setTitle(title);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentFragment != null)
        {
            currentFragment.onCreateOptionsMenu(menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        currentFragment.onPrepareOptionsMenu(menu, mDrawerLayout.isDrawerOpen(mDrawerList));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        currentFragment.onOptionsItemSelected(item);

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        currentFragment.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }
}
