package sg.money.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import sg.money.domainobjects.Category;
import sg.money.DatabaseManager;
import sg.money.utils.DialogButtons;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.domainobjects.Transaction;
import sg.money.adapters.TransactionsListAdapter;
import sg.money.activities.AddTransactionActivity;
import sg.money.activities.ParentActivity;

public class AccountTransactionsFragment extends Fragment implements OnItemLongClickListener, OnItemClickListener
{
    static final int REQUEST_ADDTRANSACTION = 0;

	ListView transactionsList;
	TextView txtTotal;
	ArrayList<Transaction> transactions;
	ArrayList<Category> categories;
	int accountID;
	TransactionsListAdapter adapter;
	public ParentActivity parentActivity;
    public TransactionsHolderFragment parentFragment;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        accountID = getArguments().getInt("AccountID");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        parentFragment = (TransactionsHolderFragment)getParentFragment();
        parentActivity = (ParentActivity)parentFragment.getActivity();

    	View v = inflater.inflate(R.layout.fragment_transactions, null);
    	
    	transactionsList = (ListView)v.findViewById(R.id.transactionsListing);
    	
    	View emptyView = v.findViewById(android.R.id.empty);
    	((TextView)v.findViewById(R.id.empty_text)).setText("No transactions");
    	((TextView)v.findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	transactionsList.setEmptyView(emptyView);

        parentFragment.setActionMode(null);
        transactionsList.setItemsCanFocus(false);
        transactionsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        transactionsList.setOnItemClickListener(this);
        transactionsList.setOnItemLongClickListener(this);
        
        txtTotal = (TextView)v.findViewById(R.id.txtTotal);
        
        UpdateList();
        
        return v;
    }

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parentFragment.getActionMode() == null)
		{
			onListItemClick(parent, view, position, id);
		}
		else
		{
			changeItemCheckState(position, transactionsList.isItemChecked(position));
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (parentFragment.getActionMode() == null) {
            parentFragment.setActionMode(parentActivity.startActionMode(new ModeCallback()));
        }

		transactionsList.setItemChecked(position, !transactionsList.isItemChecked(position));
		changeItemCheckState(position, transactionsList.isItemChecked(position));
        
		return true;
	}
	
	public void focusLost()
	{
		if (parentFragment.getActionMode() != null)
            parentFragment.getActionMode().finish();
	}
	
	public void changeItemCheckState(int position, boolean checked) {
        adapter.SetSelected(position, checked);
        adapter.notifyDataSetChanged();
    	final int checkedCount = adapter.GetSelectedItems().size();
        switch (checkedCount) {
            case 0:
                parentFragment.getActionMode().setSubtitle(null);
                break;
            case 1:
                parentFragment.getActionMode().getMenu().clear();
                parentFragment.getActionMode().setSubtitle("" + checkedCount + " selected");
                parentFragment.getActionMode().getMenuInflater().inflate(R.menu.standard_cab, parentFragment.getActionMode().getMenu());
                break;
            default:
                parentFragment.getActionMode().getMenu().clear();
                parentFragment.getActionMode().getMenuInflater().inflate(R.menu.standard_cab_multiple, parentFragment.getActionMode().getMenu());
                parentFragment.getActionMode().setSubtitle("" + checkedCount + " selected");
                break;
        }
        
        if (adapter.GetSelectedItems().size() == 0)
            parentFragment.getActionMode().finish();
    }
    
    public class DateComparator implements Comparator<Transaction> {
	    public int compare(Transaction o1, Transaction o2) {
	        return o1.dateTime.compareTo(o2.dateTime);
	    }
	}
    
    public void UpdateList()
    {
    	ArrayList<Transaction> allTransactions = DatabaseManager.getInstance(AccountTransactionsFragment.this.getActivity()).GetAllTransactions(accountID);
    	categories = DatabaseManager.getInstance(AccountTransactionsFragment.this.getActivity()).GetAllCategories();

		Double total = 0.0;
		for(Transaction transaction : allTransactions)
			total += transaction.value;

		txtTotal.setText(Misc.formatValue(parentActivity, total));
		
		transactions = new ArrayList<Transaction>();
		for(Transaction transaction : allTransactions)
		{
			if (parentFragment.showReconciledTransactions() || !transaction.reconciled)
				transactions.add(transaction);
		}

		Collections.sort(transactions, new DateComparator());
    	Collections.reverse(transactions);
    	 
        // Getting adapter by passing xml data ArrayList
		adapter=new TransactionsListAdapter(parentActivity, transactions, categories, parentFragment.isInReconcileMode(), parentFragment.useReconcile());
		transactionsList.setAdapter(adapter);
    }
    
    protected void onListItemClick(AdapterView<?> l, View v, int position, long id)
	{
    	EditItem(transactions.get(position));
	}
    
    private final class ModeCallback implements ActionMode.Callback {
   	 
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = parentActivity.getSupportMenuInflater();
            inflater.inflate(R.menu.standard_cab, menu);
            return true;
        }
 
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        public void onDestroyActionMode(ActionMode mode) {
        	adapter.ClearSelected();
	        adapter.notifyDataSetChanged();
	        transactionsList.clearChoices();
 
            if (mode == parentFragment.getActionMode()) {
                parentFragment.setActionMode(null);
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
	
	private void EditItem(Transaction selectedItem)
	{
		Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
		intent.putExtra("AccountID", selectedItem.account);
		intent.putExtra("ID", selectedItem.id);
    	startActivityForResult(intent, REQUEST_ADDTRANSACTION);
	}
	
	private void confirmDeleteItems(final ActionMode mode)
	{
		Misc.showConfirmationDialog(getActivity(), 
				adapter.GetSelectedItems().size() == 1 
					? "Delete 1 transaction?"
					: "Delete " + adapter.GetSelectedItems().size() + " transactions?", 
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
		ArrayList<Transaction> selectedItems = adapter.GetSelectedItems();
		for(Transaction selectedItem : selectedItems)
		{
			if (selectedItem.isTransfer)
			{
				Transaction releatedTransaction = selectedItem.getRelatedTransferTransaction(getActivity());
				DatabaseManager.getInstance(getActivity()).DeleteTransaction(releatedTransaction);
			}
			
			DatabaseManager.getInstance(getActivity()).DeleteTransaction(selectedItem);
		}
		UpdateParentUI();
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
        parentFragment.UpdateTransactions();
	}
}
