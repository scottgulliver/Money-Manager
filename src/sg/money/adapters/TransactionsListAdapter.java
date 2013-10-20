package sg.money.adapters;

import java.util.ArrayList;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.*;
import sg.money.common.DatabaseManager;
import sg.money.R;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;
import sg.money.utils.Misc;

public class TransactionsListAdapter extends BaseListAdapter<Transaction> {

    private ArrayList<Category> m_categories;
	private boolean m_showReconcileOptions;
	private boolean m_greyOutReconciled;

    final int COLOR_RECONCILED = Color.rgb(220, 220, 235);
	
	
	/* Constructor */
 
    public TransactionsListAdapter(Activity activity, ArrayList<Transaction> transactions,
           ArrayList<Category> categories, boolean showReconcileOptions, boolean greyOutReconciled) {
        super(activity, transactions);

        this.m_categories = categories;
		this.m_showReconcileOptions = showReconcileOptions;
		this.m_greyOutReconciled = greyOutReconciled;
    }
	
	
	/* Methods */

    private Transaction getTransaction(int id)
    {
    	for(Transaction transaction : m_items)
    	{
    		if (transaction.getId() == id)
    			return transaction;
    	}

    	return null;
    }
    
    private Category getCategory(int id)
    {
    	for(Category category : m_categories)
    	{
    		if (category.getId() == id)
    			return category;
    	}
    	
    	return null;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.transaction_item_layout;
    }

    @Override
    protected void buildView(View view, Transaction transaction) {
        TextView descText = (TextView)view.findViewById(R.id.transaction_desc);
        TextView categoryText = (TextView)view.findViewById(R.id.transaction_category);
        TextView dateText = (TextView)view.findViewById(R.id.transaction_date);
        TextView valueText = (TextView)view.findViewById(R.id.transaction_value);
        CheckBox chkReconciled = (CheckBox)view.findViewById(R.id.chkReconciled);
        RelativeLayout layoutReconciled = (RelativeLayout)view.findViewById(R.id.layoutReconciled);

        //set values
        descText.setText(transaction.getDescription());
        categoryText.setText(transaction.isTransfer()
                ? transaction.getTransferDescription(m_activity)
                : getCategory(transaction.getCategory()).getName());
        valueText.setText(Misc.formatValue(m_activity, transaction.getValue()));

        try
        {
            dateText.setText(Misc.formatDate(m_activity, transaction.getDateTime()));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        if (transaction.getValue() >= 0)
        {
            valueText.setTextColor(Color.argb(255, 102, 153, 0));
        }
        else
        {
            valueText.setTextColor(Color.argb(255, 204, 0, 0));
        }

        descText.setTextColor(transaction.isReconciled() && m_greyOutReconciled
                ? Color.argb(255, 100, 100, 100)
                : Color.argb(255, 34, 34, 34));

        layoutReconciled.setVisibility(m_showReconcileOptions ? View.VISIBLE : View.GONE);
        chkReconciled.setChecked(transaction.isReconciled());
        final int id = transaction.getId();
        chkReconciled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton p1, boolean p2)
            {
                chkReconciledChanged(p1, p2, id);
            }
        });

        if (m_selectedItems.contains(transaction))
        {
            view.setBackgroundColor(COLOR_SELECTED);
        }
        else if (transaction.isReconciled() && m_greyOutReconciled)
        {
            view.setBackgroundColor(COLOR_RECONCILED);
        }
        else
        {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
	
	private void chkReconciledChanged(CompoundButton view, boolean newValue, int transactionId)
	{
		Transaction transaction = getTransaction(transactionId);
		transaction.setReconciled(newValue);
		DatabaseManager.getInstance(m_activity).UpdateTransaction(transaction);
		notifyDataSetChanged();
	}
}
