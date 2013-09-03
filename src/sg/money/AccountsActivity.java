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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AccountsActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener
{
    static final int REQUEST_ADDACCOUNT = 0;

	ListView accountsList;
	ArrayList<Account> accounts;
	AccountListAdapter adapter;
	ActionMode actionMode;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        accountsList = (ListView)findViewById(R.id.accountsList);
        
        View emptyView = findViewById(android.R.id.empty);
    	((TextView)findViewById(R.id.empty_text)).setText("No accounts");
    	((TextView)findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	accountsList.setEmptyView(emptyView);
        
        actionMode = null;
        accountsList.setItemsCanFocus(false);
        accountsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        accountsList.setOnItemClickListener(this);
        accountsList.setOnItemLongClickListener(this);
        
        UpdateList();
    }

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (actionMode == null)
		{
			onListItemClick(parent, view, position, id);
		}
		else
		{
			changeItemCheckState(position, accountsList.isItemChecked(position));
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (actionMode == null) {
        	actionMode = startActionMode(new ModeCallback());
        }

		accountsList.setItemChecked(position, !accountsList.isItemChecked(position));
		changeItemCheckState(position, accountsList.isItemChecked(position));
        
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
        getSupportMenuInflater().inflate(R.menu.activity_accounts, menu);
        return true;
    }
    
    private void UpdateList()
    {
    	accounts = DatabaseManager.getInstance(AccountsActivity.this).GetAllAccounts();
 
		adapter = new AccountListAdapter(this, accounts);
		accountsList.setAdapter(adapter);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
		    case android.R.id.home:
	            Intent parentActivityIntent = new Intent(this, TransactionsActivity.class);
	            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(parentActivityIntent);
	            finish();
	            return true;
        
	    	case R.id.menu_addaccount:{
	    		Intent intent = new Intent(this, AddAccountActivity.class);
	        	startActivityForResult(intent, REQUEST_ADDACCOUNT);
	    		break;
	    		}
	    	
	    	case R.id.menu_managecategories:{
	    		Intent intent = new Intent(this, CategoriesActivity.class);
	        	startActivity(intent);
	        	break;
	    		}
	    	
	        case R.id.menu_settings:{
	        	startActivityForResult(new Intent(AccountsActivity.this, SettingsActivity.class), REQUEST_SETTINGS);
                break;
            	}
	    }
	    return true;
	}

    
    protected void onListItemClick(AdapterView<?> l, View v, int position, long id)
	{
    	Account account = accounts.get(position);
    	
		Intent intent=new Intent();
	    intent.putExtra("AccountID", account.id);
	    setResult(RESULT_OK, intent);
	    finish();
	}

	static final int REQUEST_SETTINGS = 10;
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case REQUEST_ADDACCOUNT:
			{
				if (resultCode == RESULT_OK)
				{
					UpdateList();
				}
				break;
			}
			case REQUEST_SETTINGS:
			{
				UpdateList();
				break;
			}
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
	        accountsList.clearChoices();
 
            if (mode == actionMode) {
            	actionMode = null;
            }
        }
 
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.cab_edit:
            	EditItem();
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
	
	private void EditItem()
	{
		Account selectedItem = adapter.GetSelectedItems().get(0);
		Intent intent = new Intent(this, AddAccountActivity.class);
		intent.putExtra("ID", selectedItem.id);
    	startActivityForResult(intent, REQUEST_ADDACCOUNT);
	}
	
	private void confirmDeleteItems(final ActionMode mode)
	{
		Misc.showConfirmationDialog(this, 
				adapter.GetSelectedItems().size() == 1 
					? "Delete 1 account?"
					: "Delete " + adapter.GetSelectedItems().size() + " accounts?", 
					DialogButtons.OkCancel,
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
		ArrayList<Account> selectedItems = adapter.GetSelectedItems();
		for(Account selectedItem : selectedItems)
		{
			DatabaseManager.getInstance(AccountsActivity.this).DeleteAccount(selectedItem);
		}
		UpdateList();
	}
}
