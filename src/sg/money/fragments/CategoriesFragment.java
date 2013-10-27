package sg.money.fragments;

import java.util.ArrayList;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import sg.money.adapters.CategoriesExpandableListAdapter;
import sg.money.domainobjects.Category;
import sg.money.common.DatabaseManager;
import sg.money.utils.DialogButtons;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.activities.SettingsActivity;
import sg.money.activities.AddCategoryActivity;

public class CategoriesFragment extends HostActivityFragmentBase implements OnItemLongClickListener, OnGroupClickListener, OnChildClickListener
{
	private ExpandableListView m_categoriesList;
    private ArrayList<Category> m_categories;
    private CategoriesExpandableListAdapter m_adapter;

    private static final int REQUEST_ADDCATEGORY = 0;


    /* Fragment overrides */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.activity_categories, null);

		m_categoriesList = (ExpandableListView)v.findViewById(R.id.categoriesList);
        
        View emptyView = v.findViewById(android.R.id.empty);
    	((TextView)v.findViewById(R.id.empty_text)).setText("No categories");
    	((TextView)v.findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	m_categoriesList.setEmptyView(emptyView);
    	

        setActionMode(null);
        m_categoriesList.setItemsCanFocus(false);
        m_categoriesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        m_categoriesList.setOnGroupClickListener(this);
        m_categoriesList.setOnChildClickListener(this);
        m_categoriesList.setOnItemLongClickListener(this);

		UpdateList();

        if (savedInstanceState == null)
        {
            getParentActivity().invalidateOptionsMenu();
        }

        return v;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getParentActivity().getSupportMenuInflater().inflate(R.menu.activity_categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_addcategory: {
                Intent intent = new Intent(getParentActivity(), AddCategoryActivity.class);
                startActivityForResult(intent, REQUEST_ADDCATEGORY);
                break;
            }

            case R.id.menu_settings: {
                startActivity(new Intent(getParentActivity(), SettingsActivity.class));
                break;
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADDCATEGORY: {
                UpdateList();
                break;
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        menu.findItem(R.id.menu_addcategory).setVisible(!drawerIsOpen);
        return true;
    }


    /* Listener callbacks */

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActionMode() == null) {
            setActionMode(getParentActivity().startActionMode(new ModeCallback()));
        }

        m_categoriesList.setItemChecked(position, !m_categoriesList.isItemChecked(position));
        changeItemCheckState(position, m_categoriesList.isItemChecked(position));

        return true;
    }

    public void changeItemCheckState(int position, boolean checked) {
        m_adapter.SetSelected(position, checked);
        m_adapter.notifyDataSetChanged();
        final int checkedCount = m_adapter.GetSelectedItems().size();
        switch (checkedCount) {
            case 0:
                getActionMode().setSubtitle(null);
                break;
            case 1:
                getActionMode().getMenu().clear();
                getActionMode().setSubtitle("" + checkedCount + " selected");
                getActionMode().getMenuInflater().inflate(R.menu.standard_cab, getActionMode().getMenu());
                break;
            default:
                getActionMode().getMenu().clear();
                getActionMode().getMenuInflater().inflate(R.menu.standard_cab_multiple, getActionMode().getMenu());
                getActionMode().setSubtitle("" + checkedCount + " selected");
                break;
        }

        if (m_adapter.GetSelectedItems().size() == 0)
            getActionMode().finish();
    }

    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        onItemClick((int) m_adapter.getChildId(groupPosition, childPosition), m_adapter.getPosition(groupPosition, childPosition));
        return true;
    }

    public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
        onItemClick((int) m_adapter.getGroupId(groupPosition), m_adapter.getPosition(groupPosition, -1));
        return true;
    }

    private void onItemClick(int categoryId, int position)
    {
        if (getActionMode() == null)
        {
            EditItem(getCategoryFromId(categoryId));
        }
        else
        {
            m_categoriesList.setItemChecked(position, !m_categoriesList.isItemChecked(position));
            changeItemCheckState(position, m_categoriesList.isItemChecked(position));
        }
    }


    /* Methods */
	
	private Category getCategoryFromId(int id)
	{
		for(Category category : m_categories)
		{
			if (category.getId() == id)
				return category;
		}
		
		return null;
	}

	private void UpdateList() {
		m_categories = DatabaseManager.getInstance(getParentActivity())
				.GetAllCategories();

		m_adapter = new CategoriesExpandableListAdapter(getParentActivity(), m_categories);
		m_categoriesList.setAdapter(m_adapter);
		for(int i = 0; i < m_adapter.getGroupCount(); i++)
			m_categoriesList.expandGroup(i);
	}
	
	private void EditItem(Category selectedItem)
	{			
		Intent intent = new Intent(getParentActivity(), AddCategoryActivity.class);
		intent.putExtra("ID", selectedItem.getId());
		startActivityForResult(intent, REQUEST_ADDCATEGORY);
	}
	
	private void confirmDeleteItems(final ActionMode mode)
	{
		Misc.showConfirmationDialog(getParentActivity(),
                m_adapter.GetSelectedItems().size() == 1
                        ? "Delete 1 category?"
                        : "Delete " + m_adapter.GetSelectedItems().size() + " categories?",
                DialogButtons.OkCancel,
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteItems();
                        mode.finish();
                    }
                },
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mode.finish();
                    }
                }
        );
	}
	
	private void DeleteItems()
	{
		ArrayList<Category> selectedItems = m_adapter.GetSelectedItems();
		ArrayList<Category> permanentItems = new ArrayList<Category>();
		for(Category selectedItem : selectedItems)
		{
			if (!selectedItem.isPermanent())
				DatabaseManager.getInstance(getParentActivity()).DeleteCategory(selectedItem);
			else
				permanentItems.add(selectedItem);
		}
		UpdateList();
		
		if (!permanentItems.isEmpty())
		{
			String msg = "Can't delete ";
			for(Category category : permanentItems)
				msg += category.getName() + (permanentItems.get(permanentItems.size()-1) != category ? ", " : ".");
			Toast.makeText(getParentActivity(), msg, Toast.LENGTH_SHORT).show();
		}
	}


    /* ModeCallback class */
	
	private final class ModeCallback implements ActionMode.Callback {
	   	 
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = getParentActivity().getSupportMenuInflater();
            inflater.inflate(R.menu.standard_cab, menu);
            return true;
        }
 
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        public void onDestroyActionMode(ActionMode mode) {
			m_adapter.ClearSelected();
	        m_adapter.notifyDataSetChanged();
	        m_categoriesList.clearChoices();
 
            if (mode == getActionMode()) {
            	setActionMode(null);
            }
        }
 
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.cab_edit:
            	EditItem(m_adapter.GetSelectedItems().get(0));
                mode.finish();
                return true;
            case R.id.cab_delete:
            	confirmDeleteItems(mode);
                return true;
            default:
                return false;
        }
        }
    }
}
