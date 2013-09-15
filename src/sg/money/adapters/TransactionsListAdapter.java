package sg.money.adapters;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.*;

import sg.money.DatabaseManager;
import sg.money.R;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;
import sg.money.utils.Misc;

public class TransactionsListAdapter extends BaseAdapter {
	private Activity activity;
    private ArrayList<Transaction> transactions;
    private ArrayList<Category> categories;
    private static LayoutInflater inflater=null;
    private ArrayList<Transaction> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
    final int COLOR_RECONCILED = Color.rgb(220, 220, 235);
	private boolean showReconcileOptions;
	private boolean greyOutReconciled;
 
    public TransactionsListAdapter(Activity activity, ArrayList<Transaction> transactions, ArrayList<Category> categories, boolean showReconcileOptions, boolean greyOutReconciled) {
        this.activity = activity;
    	this.transactions = transactions;
        this.categories = categories;
		this.showReconcileOptions = showReconcileOptions;
		this.greyOutReconciled = greyOutReconciled;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Transaction>();
    }
 
    public int getCount() {
        return transactions.size();
    }
    
    public void ClearSelected()
    {
    	selectedItems.clear();
    }
    
    public void SetSelected(int position, boolean selected)
    {
    	Transaction item = transactions.get(position);
    	if (selected && !selectedItems.contains(item))
    		selectedItems.add(item);
    	else if (!selected && selectedItems.contains(item))
    		selectedItems.remove(item);
    }
    
    public ArrayList<Transaction> GetSelectedItems()
    {
    	return selectedItems;
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }

    private Transaction getTransaction(int id)
    {
    	for(Transaction transaction : transactions)
    	{
    		if (transaction.id == id)
    			return transaction;
    	}

    	return null;
    }
    
    private Category getCategory(int id)
    {
    	for(Category category : categories)
    	{
    		if (category.id == id)
    			return category;
    	}
    	
    	return null;
    }
 
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.transaction_item_layout, null);

        TextView descText = (TextView)vi.findViewById(R.id.transaction_desc);
        TextView categoryText = (TextView)vi.findViewById(R.id.transaction_category);
        TextView dateText = (TextView)vi.findViewById(R.id.transaction_date);
        TextView valueText = (TextView)vi.findViewById(R.id.transaction_value);
		CheckBox chkReconciled = (CheckBox)vi.findViewById(R.id.chkReconciled);
		RelativeLayout layoutReconciled = (RelativeLayout)vi.findViewById(R.id.layoutReconciled);
 
        Transaction transactionData = new Transaction();
        transactionData = transactions.get(position);
 
        //set values
        descText.setText(transactionData.description);
    	categoryText.setText(transactionData.isTransfer
    			? transactionData.getTransferDescription(activity)
    			: getCategory(transactionData.category).name);
        valueText.setText(Misc.formatValue(activity, transactionData.value));

        try
        {
        	dateText.setText(Misc.formatDate(activity, transactionData.dateTime));
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        if (transactionData.value >= 0)
        	valueText.setTextColor(Color.argb(255, 102, 153, 0));
        else
        	valueText.setTextColor(Color.argb(255, 204, 0, 0));

        descText.setTextColor(transactionData.reconciled && greyOutReconciled ? Color.argb(255, 100, 100, 100) : Color.argb(255, 34, 34, 34));

        layoutReconciled.setVisibility(showReconcileOptions ? View.VISIBLE : View.GONE);
		chkReconciled.setChecked(transactionData.reconciled);
		final int id = transactionData.id;
		chkReconciled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton p1, boolean p2)
				{
					chkReconciledChanged(p1, p2, id);
				}
		});
        
        if (selectedItems.contains(transactionData))
        	vi.setBackgroundColor(COLOR_SELECTED);
        else if (transactionData.reconciled && greyOutReconciled)
			vi.setBackgroundColor(COLOR_RECONCILED);
		else
        	vi.setBackgroundColor(Color.TRANSPARENT);

        return vi;
    }
	
	private void chkReconciledChanged(CompoundButton view, boolean newValue, int transactionId)
	{
		Transaction transaction = getTransaction(transactionId);
		transaction.reconciled = newValue;
		DatabaseManager.getInstance(activity).UpdateTransaction(transaction);
		notifyDataSetChanged();
	}
    
    boolean tryParseInt(String value)  
    {  
         try  
         {  
             Integer.parseInt(value);  
             return true;  
          } catch(NumberFormatException nfe)  
          {  
              return false;  
          }  
    }
}
