package sg.money;

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Intent;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;

public class AccountsActivity extends BaseActivity
{
    static final int REQUEST_ADDACCOUNT = 0;

	ListView accountsList;
	ArrayList<Account> accounts;
	AccountListAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        
        accountsList = (ListView)findViewById(R.id.accountsList);
        accountsList.setOnItemClickListener( 
				new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
					{
						onListItemClick(arg0, arg1, arg2, arg3);
					}
				});
        accountsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);		
        accountsList.setMultiChoiceModeListener(multiChoiceListner);
        
        UpdateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_accounts, menu);
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
	    	case R.id.menu_addaccount:{
	    		Intent intent = new Intent(this, AddAccountActivity.class);
	        	startActivityForResult(intent, REQUEST_ADDACCOUNT);
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

    	Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
    	transactionsIntent.putExtra("AccountID", account.id);
		startActivity(transactionsIntent);
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
                	EditItem();
                    mode.finish();
                    return true;
                case R.id.cab_delete:
    				DeleteItems();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
			adapter.ClearSelected();
        }
        
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        	final int checkedCount = accountsList.getCheckedItemCount();
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
	
	private void EditItem()
	{
		Account selectedItem = adapter.GetSelectedItems().get(0);
		Intent intent = new Intent(this, AddAccountActivity.class);
		intent.putExtra("ID", selectedItem.id);
    	startActivityForResult(intent, REQUEST_ADDACCOUNT);
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

	@Override
	protected int thisActivity()
	{
		return ACTIVITY_ACCOUNTS;
	}
}
