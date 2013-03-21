package sg.money;

import java.util.ArrayList;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;

public class CategoriesActivity extends BaseActivity {
	static final int REQUEST_ADDCATEGORY = 0;

	ListView categoriesList;
	ArrayList<Category> categories;
	CategoryListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categories);
        getActionBar().setDisplayHomeAsUpEnabled(true);

		categoriesList = (ListView) findViewById(R.id.categoriesList);
        
        View emptyView = findViewById(android.R.id.empty);
    	((TextView)findViewById(R.id.empty_text)).setText("No categories");
    	((TextView)findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	categoriesList.setEmptyView(emptyView);
    	
		categoriesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);		
		categoriesList.setMultiChoiceModeListener(multiChoiceListner);
		categoriesList.setOnItemClickListener( 
				new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
					{
						onListItemClick(arg0, arg1, arg2, arg3);
					}
				});

		UpdateList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_categories, menu);
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
	
	MultiChoiceModeListener multiChoiceListner = new MultiChoiceModeListener()
	{
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.standard_cab, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
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

        public void onDestroyActionMode(ActionMode mode) {
			adapter.ClearSelected();
        }
        
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        	final int checkedCount = categoriesList.getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                	mode.getMenu().clear();
                    mode.setSubtitle("" + checkedCount + " selected");
                    mode.getMenuInflater().inflate(R.menu.standard_cab, mode.getMenu());
                    break;
                default:
                	mode.getMenu().clear();
                    mode.getMenuInflater().inflate(R.menu.standard_cab_multiple, mode.getMenu());
                    mode.setSubtitle("" + checkedCount + " selected");
                    break;
            }
            
            adapter.SetSelected(position, checked);
            adapter.notifyDataSetChanged();
        }
    };
}