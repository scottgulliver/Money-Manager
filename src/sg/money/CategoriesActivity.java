package sg.money;

import java.util.ArrayList;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CategoriesActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener
{
	static final int REQUEST_ADDCATEGORY = 0;

	ListView categoriesList;
	ArrayList<Category> categories;
	CategoryListAdapter adapter;
	ActionMode actionMode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categories);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		categoriesList = (ListView) findViewById(R.id.categoriesList);
        
        View emptyView = findViewById(android.R.id.empty);
    	((TextView)findViewById(R.id.empty_text)).setText("No categories");
    	((TextView)findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	categoriesList.setEmptyView(emptyView);

        actionMode = null;
        categoriesList.setItemsCanFocus(false);
        categoriesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        categoriesList.setOnItemClickListener(this);
        categoriesList.setOnItemLongClickListener(this);

		UpdateList();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (actionMode == null)
		{
			onListItemClick(parent, view, position, id);
		}
		else
		{
			changeItemCheckState(position, categoriesList.isItemChecked(position));
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (actionMode == null) {
        	actionMode = startActionMode(new ModeCallback());
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
                actionMode.setSubtitle(null);
                break;
            case 1:
            	actionMode.getMenu().clear();
            	actionMode.setSubtitle("" + checkedCount + " selected");
            	actionMode.getMenuInflater().inflate(R.menu.standard_cab, actionMode.getMenu());
                break;
            default:
            	actionMode.getMenu().clear();
            	actionMode.getMenuInflater().inflate(R.menu.standard_cab_multiple, actionMode.getMenu());
            	actionMode.setSubtitle("" + checkedCount + " selected");
                break;
        }
        
        if (adapter.GetSelectedItems().size() == 0)
        	actionMode.finish();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_categories, menu);
		return true;
	}

	private void UpdateList() {
		categories = DatabaseManager.getInstance(CategoriesActivity.this)
				.GetAllCategories();

		adapter = new CategoryListAdapter(this, categories);
		categoriesList.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

	    case android.R.id.home:
            Intent parentActivityIntent = new Intent(this, TransactionsActivity.class);
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
            
		case R.id.menu_addcategory: {
			Intent intent = new Intent(this, AddCategoryActivity.class);
			startActivityForResult(intent, REQUEST_ADDCATEGORY);
			break;
		}

		case R.id.menu_settings: { 
        	startActivity(new Intent(CategoriesActivity.this, SettingsActivity.class));
			break;
		}
		}
		return true;
	}

	protected void onListItemClick(AdapterView<?> l, View v, int position, long id) {
    	EditItem(categories.get(position));
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ADDCATEGORY: {
			if (resultCode == RESULT_OK) {
				UpdateList();
			}
			break;
		}
		}
	}
	
	private void EditItem(Category selectedItem)
	{			
		Intent intent = new Intent(this, AddCategoryActivity.class);
		intent.putExtra("ID", selectedItem.id);
		startActivityForResult(intent, REQUEST_ADDCATEGORY);
	}
	
	private void confirmDeleteItems(final ActionMode mode)
	{
		Misc.showConfirmationDialog(this, 
				adapter.GetSelectedItems().size() == 1 
					? "Delete 1 category?"
					: "Delete " + adapter.GetSelectedItems().size() + " categories?", 
				new OnClickListener() { public void onClick(DialogInterface dialog, int which) {
						DeleteItems();
	                    mode.finish();
					}
				},
				new OnClickListener() { public void onClick(DialogInterface dialog, int which) {
                    mode.finish();
				}
			});
	}
	
	private void DeleteItems()
	{
		ArrayList<Category> selectedItems = adapter.GetSelectedItems();
		ArrayList<Category> permanentItems = new ArrayList<Category>();
		for(Category selectedItem : selectedItems)
		{
			if (!selectedItem.isPermanent)
				DatabaseManager.getInstance(CategoriesActivity.this).DeleteCategory(selectedItem);
			else
				permanentItems.add(selectedItem);
		}
		UpdateList();
		
		if (!permanentItems.isEmpty())
		{
			String msg = "Can't delete ";
			for(Category category : permanentItems)
				msg += category.name + (permanentItems.get(permanentItems.size()-1) != category ? ", " : ".");
			Toast.makeText(CategoriesActivity.this, msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	private final class ModeCallback implements ActionMode.Callback {
	   	 
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = getSupportMenuInflater();
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
 
            if (mode == actionMode) {
            	actionMode = null;
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
    };
}