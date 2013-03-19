package sg.money;

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

public class TransactionsListAdapter extends BaseAdapter {
	private Activity activity;
    private ArrayList<Transaction> transactions;
    private ArrayList<Category> categories;
    private static LayoutInflater inflater=null;
    private ArrayList<Transaction> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
 
    public TransactionsListAdapter(Activity activity, ArrayList<Transaction> transactions, ArrayList<Category> categories) {
        this.activity = activity;
    	this.transactions = transactions;
        this.categories = categories;
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
 
        Transaction transactionData = new Transaction();
        transactionData = transactions.get(position);
 
        //set values
        descText.setText(transactionData.description);
        categoryText.setText(getCategory(transactionData.category).name);
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
        
        if (selectedItems.contains(transactionData))
        	vi.setBackgroundColor(COLOR_SELECTED);
        else
        	vi.setBackgroundColor(Color.TRANSPARENT);

        return vi;
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