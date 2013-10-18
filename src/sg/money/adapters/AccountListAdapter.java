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
import sg.money.R;
import sg.money.domainobjects.Account;
import sg.money.utils.Misc;

public class AccountListAdapter extends BaseAdapter {
 
    private ArrayList<Account> m_accounts;
    private static LayoutInflater m_inflater;
    private ArrayList<Account> m_selectedItems;
    private Activity m_activity;

    private final int COLOR_SELECTED = Color.rgb(133, 194, 215);
	
	
	/* Constructor */
 
    public AccountListAdapter(Activity activity, ArrayList<Account> accounts) {
    	m_activity = activity;
        m_accounts = accounts;
        m_inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_selectedItems = new ArrayList<Account>();
    }
	
	
	/* Methods */
 
    public int getCount() {
        return m_accounts.size();
    }
    
    public void clearSelected()
    {
    	m_selectedItems.clear();
    }
    
    public void setSelected(int position, boolean selected)
    {
    	Account item = m_accounts.get(position);
    	if (selected && !m_selectedItems.contains(item))
		{
    		m_selectedItems.add(item);
		}
    	else if (!selected && m_selectedItems.contains(item))
		{
    		m_selectedItems.remove(item);
		}
    }
    
    public ArrayList<Account> getSelectedItems()
    {
    	return m_selectedItems;
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
		{
            vi = m_inflater.inflate(R.layout.account_item_layout, null);
		}

        TextView nameText = (TextView)vi.findViewById(R.id.account_name);
        TextView balanceText = (TextView)vi.findViewById(R.id.account_balance);
 
        Account account = m_accounts.get(position);
 
        //set values
		
        nameText.setText(account.getName());
        balanceText.setText(Misc.formatValue(m_activity, account.getValue()));
        if (account.getValue() >= 0)
		{
        	balanceText.setTextColor(Color.argb(255, 102, 153, 0));
		}
        else
		{
        	balanceText.setTextColor(Color.argb(255, 204, 0, 0));
		}
        
        if (m_selectedItems.contains(account))
		{
        	vi.setBackgroundColor(COLOR_SELECTED);
		}
        else
		{
        	vi.setBackgroundColor(Color.TRANSPARENT);
		}

        return vi;
    }
}
