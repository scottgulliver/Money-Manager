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

public class AccountListAdapter extends BaseAdapter {
 
    private ArrayList<Account> accounts;
    private static LayoutInflater inflater=null;
    private ArrayList<Account> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
    private Activity activity;
 
    public AccountListAdapter(Activity activity, ArrayList<Account> accounts) {
    	this.activity = activity;
        this.accounts = accounts;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Account>();
    }
 
    public int getCount() {
        return accounts.size();
    }
    
    public void ClearSelected()
    {
    	selectedItems.clear();
    }
    
    public void SetSelected(int position, boolean selected)
    {
    	Account item = accounts.get(position);
    	if (selected && !selectedItems.contains(item))
    		selectedItems.add(item);
    	else if (!selected && selectedItems.contains(item))
    		selectedItems.remove(item);
    }
    
    public ArrayList<Account> GetSelectedItems()
    {
    	return selectedItems;
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.account_item_layout, null);

        TextView nameText = (TextView)vi.findViewById(R.id.account_name);
        TextView balanceText = (TextView)vi.findViewById(R.id.account_balance);
 
        Account account = accounts.get(position);
 
        //set values
        nameText.setText(account.name);
        balanceText.setText(Misc.formatValue(activity, account.value));
        if (account.value >= 0)
        	balanceText.setTextColor(Color.argb(255, 102, 153, 0));
        else
        	balanceText.setTextColor(Color.argb(255, 204, 0, 0));
        
        if (selectedItems.contains(account))
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