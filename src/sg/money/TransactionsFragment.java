package sg.money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
 
public class TransactionsFragment extends Fragment
{
    static final int REQUEST_ADDTRANSACTION = 0;

	ListView transactionsList;
	TextView txtTotal;
	ArrayList<Transaction> transactions;
	ArrayList<Category> categories;
	int accountID;
	TransactionsListAdapter adapter;
	public TransactionsActivity parentActivity;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        accountID = getArguments().getInt("AccountID");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) 
    {
    	View v = inflater.inflate(R.layout.fragment_transactions, null);
    	
    	transactionsList = (ListView)v.findViewById(R.id.transactionsListing);
        transactionsList.setOnItemClickListener( 
				new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
					{
						onListItemClick(arg0, arg1, arg2, arg3);
					}
				});

        transactionsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);		
        transactionsList.setMultiChoiceModeListener(multiChoiceListner);
        
        txtTotal = (TextView)v.findViewById(R.id.txtTotal);
        
        UpdateList();
        
        return v;
    }
    
    public class DateComparator implements Comparator<Transaction> {
	    public int compare(Transaction o1, Transaction o2) {
	        return o1.dateTime.compareTo(o2.dateTime);
	    }
	}
    
    public void UpdateList()
    {
    	transactions = DatabaseManager.getInstance(TransactionsFragment.this.getActivity()).GetAllTransactions(accountID);
    	categories = DatabaseManager.getInstance(TransactionsFragment.this.getActivity()).GetAllCategories();

		Collections.sort(transactions, new DateComparator());
    	Collections.reverse(transactions);
    	 
        // Getting adapter by passing xml data ArrayList
		adapter=new TransactionsListAdapter(this.getActivity(), transactions, categories);
		transactionsList.setAdapter(adapter);
		
		Double total = 0.0;
		for(Transaction transaction : transactions)
			total += transaction.value;
		
		txtTotal.setText(Misc.formatValue(getActivity(), total));
    }
    
    protected void onListItemClick(AdapterView<?> l, View v, int position, long id)
	{
    	EditItem(transactions.get(position));
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
        	final int checkedCount = transactionsList.getCheckedItemCount();
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
	
	private void EditItem(Transaction selectedItem)
	{
		Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
		intent.putExtra("AccountID", selectedItem.account);
		intent.putExtra("ID", selectedItem.id);
    	startActivityForResult(intent, REQUEST_ADDTRANSACTION);
	}
	
	private void DeleteItems()
	{
		ArrayList<Transaction> selectedItems = adapter.GetSelectedItems();
		for(Transaction selectedItem : selectedItems)
		{
			DatabaseManager.getInstance(getActivity()).DeleteTransaction(selectedItem);
		}
		UpdateList();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case REQUEST_ADDTRANSACTION:
			{
				UpdateParentUI();
				break;
			}
		}
    }
	
	private void UpdateParentUI()
	{
		Activity a = getActivity();

		if(a instanceof TransactionsActivity) {
		    ((TransactionsActivity)a).UpdateUI();
		}
	}
}
