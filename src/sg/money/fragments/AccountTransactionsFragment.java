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
import sg.money.common.DatabaseManager;
import sg.money.utils.DialogButtons;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.domainobjects.Transaction;
import sg.money.adapters.TransactionsListAdapter;
import sg.money.activities.AddTransactionActivity;
import sg.money.activities.ParentActivity;

public class AccountTransactionsFragment extends Fragment implements OnItemLongClickListener, OnItemClickListener
{
	private ListView m_transactionsList;
    private TextView m_txtTotal;
    private ArrayList<Transaction> m_transactions;
    private ArrayList<Category> m_categories;
    private int m_accountID;
    private TransactionsListAdapter m_adapter;
    private ParentActivity m_parentActivity;
    private TransactionsHolderFragment m_parentFragment;

    private static final int REQUEST_ADDTRANSACTION = 0;


    /* Fragment overrides */
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        m_accountID = getArguments().getInt("AccountID");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_parentFragment = (TransactionsHolderFragment)getParentFragment();
        m_parentActivity = (ParentActivity) m_parentFragment.getActivity();

    	View v = inflater.inflate(R.layout.fragment_transactions, null);
    	
    	m_transactionsList = (ListView)v.findViewById(R.id.transactionsListing);
    	
    	View emptyView = v.findViewById(android.R.id.empty);
    	((TextView)v.findViewById(R.id.empty_text)).setText("No transactions");
    	((TextView)v.findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	m_transactionsList.setEmptyView(emptyView);

        m_parentFragment.setActionMode(null);
        m_transactionsList.setItemsCanFocus(false);
        m_transactionsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        m_transactionsList.setOnItemClickListener(this);
        m_transactionsList.setOnItemLongClickListener(this);
        
        m_txtTotal = (TextView)v.findViewById(R.id.txtTotal);
        
        UpdateList();
        
        return v;
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


    /* Listener callbacks */

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (m_parentFragment.getActionMode() == null)
		{
			onListItemClick(parent, view, position, id);
		}
		else
		{
			changeItemCheckState(position, m_transactionsList.isItemChecked(position));
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (m_parentFragment.getActionMode() == null) {
            m_parentFragment.setActionMode(m_parentActivity.startActionMode(new ModeCallback()));
        }

		m_transactionsList.setItemChecked(position, !m_transactionsList.isItemChecked(position));
		changeItemCheckState(position, m_transactionsList.isItemChecked(position));
        
		return true;
	}

    public void changeItemCheckState(int position, boolean checked) {
        m_adapter.setSelected(position, checked);
        m_adapter.notifyDataSetChanged();
        final int checkedCount = m_adapter.getSelectedItems().size();
        switch (checkedCount) {
            case 0:
                m_parentFragment.getActionMode().setSubtitle(null);
                break;
            case 1:
                m_parentFragment.getActionMode().getMenu().clear();
                m_parentFragment.getActionMode().setSubtitle("" + checkedCount + " selected");
                m_parentFragment.getActionMode().getMenuInflater().inflate(R.menu.standard_cab, m_parentFragment.getActionMode().getMenu());
                break;
            default:
                m_parentFragment.getActionMode().getMenu().clear();
                m_parentFragment.getActionMode().getMenuInflater().inflate(R.menu.standard_cab_multiple, m_parentFragment.getActionMode().getMenu());
                m_parentFragment.getActionMode().setSubtitle("" + checkedCount + " selected");
                break;
        }

        if (m_adapter.getSelectedItems().size() == 0)
            m_parentFragment.getActionMode().finish();
    }

    protected void onListItemClick(AdapterView<?> l, View v, int position, long id)
    {
        EditItem(m_transactions.get(position));
    }


    /* Methods */

	public void focusLost()
	{
		if (m_parentFragment.getActionMode() != null)
            m_parentFragment.getActionMode().finish();
	}

    public class DateComparator implements Comparator<Transaction> {
        public int compare(Transaction o1, Transaction o2) {
	        return o1.getDateTime().compareTo(o2.getDateTime());
	    }

	}

    public void UpdateList()
    {
        ArrayList<Transaction> allTransactions = DatabaseManager.getInstance(AccountTransactionsFragment.this.getActivity()).GetAllTransactions(m_accountID);
        m_categories = DatabaseManager.getInstance(AccountTransactionsFragment.this.getActivity()).GetAllCategories();

        Double total = 0.0;
        for(Transaction transaction : allTransactions)
            total += transaction.getValue();

        m_txtTotal.setText(Misc.formatValue(m_parentActivity, total));

        m_transactions = new ArrayList<Transaction>();
        for(Transaction transaction : allTransactions)
        {
            if (m_parentFragment.showReconciledTransactions() || !transaction.isReconciled())
                m_transactions.add(transaction);
        }

        Collections.sort(m_transactions, new DateComparator());
        Collections.reverse(m_transactions);

        // Getting m_adapter by passing xml data ArrayList
        m_adapter =new TransactionsListAdapter(m_parentActivity, m_transactions, m_categories, m_parentFragment.isInReconcileMode(), m_parentFragment.useReconcile());
        m_transactionsList.setAdapter(m_adapter);
    }

    private void EditItem(Transaction selectedItem)
    {
        Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
        intent.putExtra("AccountID", selectedItem.getAccount());
        intent.putExtra("ID", selectedItem.getId());
        startActivityForResult(intent, REQUEST_ADDTRANSACTION);
    }

    private void confirmDeleteItems(final ActionMode mode)
    {
        Misc.showConfirmationDialog(getActivity(),
                m_adapter.getSelectedItems().size() == 1
                        ? "Delete 1 transaction?"
                        : "Delete " + m_adapter.getSelectedItems().size() + " transactions?",
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
        ArrayList<Transaction> selectedItems = m_adapter.getSelectedItems();
        for(Transaction selectedItem : selectedItems)
        {
            if (selectedItem.isTransfer())
            {
                Transaction releatedTransaction = selectedItem.getRelatedTransferTransaction(getActivity());
                DatabaseManager.getInstance(getActivity()).DeleteTransaction(releatedTransaction);
            }

            DatabaseManager.getInstance(getActivity()).DeleteTransaction(selectedItem);
        }
        UpdateParentUI();
    }

    private void UpdateParentUI()
    {
        m_parentFragment.UpdateTransactions();
    }


    /* ModeCallback class */

    private final class ModeCallback implements ActionMode.Callback {
   	 
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = m_parentActivity.getSupportMenuInflater();
            inflater.inflate(R.menu.standard_cab, menu);
            return true;
        }
 
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        public void onDestroyActionMode(ActionMode mode) {
        	m_adapter.clearSelected();
	        m_adapter.notifyDataSetChanged();
	        m_transactionsList.clearChoices();
 
            if (mode == m_parentFragment.getActionMode()) {
                m_parentFragment.setActionMode(null);
            }
        }
 
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.cab_edit:
            	EditItem(m_adapter.getSelectedItems().get(0));
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
