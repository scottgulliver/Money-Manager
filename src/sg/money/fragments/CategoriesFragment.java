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
import sg.money.DatabaseManager;
import sg.money.utils.DialogButtons;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.activities.SettingsActivity;
import sg.money.activities.AddCategoryActivity;

public class CategoriesFragment extends HostActivityFragmentBase implements OnItemLongClickListener, OnGroupClickListener, OnChildClickListener
{
	static final int REQUEST_ADDCATEGORY = 0;

	ExpandableListView categoriesList;
	ArrayList<Category> categories;
	CategoriesExpandableListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.activity_categories, null);

		categoriesList = (ExpandableListView)v.findViewById(R.id.categoriesList);
        
        View emptyView = v.findViewById(android.R.id.empty);
    	((TextView)v.findViewById(R.id.empty_text)).setText("No categories");
    	((TextView)v.findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	categoriesList.setEmptyView(emptyView);
    	

        setActionMode(null);
        categoriesList.setItemsCanFocus(false);
        categoriesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        categoriesList.setOnGroupClickListener(this);
        categoriesList.setOnChildClickListener(this);
        categoriesList.setOnItemLongClickListener(this);

        /*(new Handler()).post(new Runnable() {
            public void run() {
                categoriesList.setIndicatorBounds((int)(categoriesList.getRight()- Misc.dipsToPixels(getResources(), 40)), categoriesList.getRight());
            }
        });*/
		UpdateList();

        return v;
	}
	
	private Category getCategoryFromId(int id)
	{
		for(Category category : categories)
		{
			if (category.id == id)
				return category;
		}
		
		return null;
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (getActionMode() == null) {
        	setActionMode(getParentActivity().startActionMode(new ModeCallback()));
        }
		
		categoriesList.setItemChecked(position, !categoriesList.isItemChecked(position));
		changeItemCheckState(position, categoriesList.isItemChecked(position));
        
		return true;
	}
	
	public void changeItemCheckState(int position, boolean checked) {
        adapter.SetSelected(position, checked);
        adapter.notifyDataSetChanged();
    	final int checkedCount = adapter.GetSelectedItems().size();
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
        
        if (adapter.GetSelectedItems().size() == 0)
            getActionMode().finish();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getParentActivity().getSupportMenuInflater().inflate(R.menu.activity_categories, menu);
		return true;
	}

	private void UpdateList() {
		categories = DatabaseManager.getInstance(getParentActivity())
				.GetAllCategories();

		adapter = new CategoriesExpandableListAdapter(getParentActivity(), categories);
		categoriesList.setAdapter(adapter);
		for(int i = 0; i < adapter.getGroupCount(); i++)
			categoriesList.expandGroup(i);
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
	
	private void EditItem(Category selectedItem)
	{			
		Intent intent = new Intent(getParentActivity(), AddCategoryActivity.class);
		intent.putExtra("ID", selectedItem.id);
		startActivityForResult(intent, REQUEST_ADDCATEGORY);
	}
	
	private void confirmDeleteItems(final ActionMode mode)
	{
		Misc.showConfirmationDialog(getParentActivity(),
                adapter.GetSelectedItems().size() == 1
                        ? "Delete 1 category?"
                        : "Delete " + adapter.GetSelectedItems().size() + " categories?",
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
		ArrayList<Category> selectedItems = adapter.GetSelectedItems();
		ArrayList<Category> permanentItems = new ArrayList<Category>();
		for(Category selectedItem : selectedItems)
		{
			if (!selectedItem.isPermanent)
				DatabaseManager.getInstance(getParentActivity()).DeleteCategory(selectedItem);
			else
				permanentItems.add(selectedItem);
		}
		UpdateList();
		
		if (!permanentItems.isEmpty())
		{
			String msg = "Can't delete ";
			for(Category category : permanentItems)
				msg += category.name + (permanentItems.get(permanentItems.size()-1) != category ? ", " : ".");
			Toast.makeText(getParentActivity(), msg, Toast.LENGTH_SHORT).show();
		}
	}
	
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
			adapter.ClearSelected();
	        adapter.notifyDataSetChanged();
	        categoriesList.clearChoices();
 
            if (mode == getActionMode()) {
            	setActionMode(null);
            }
        }
 
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.cab_edit:
            	EditItem(adapter.GetSelectedItems().get(0));
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

	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		onItemClick((int)adapter.getChildId(groupPosition, childPosition), adapter.getPosition(groupPosition, childPosition));
		return true;
	}

	public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
		onItemClick((int)adapter.getGroupId(groupPosition), adapter.getPosition(groupPosition, -1));
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
			categoriesList.setItemChecked(position, !categoriesList.isItemChecked(position));
			changeItemCheckState(position, categoriesList.isItemChecked(position));
		}
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        menu.findItem(R.id.menu_addcategory).setVisible(!drawerIsOpen);
        return true;
    }
}
