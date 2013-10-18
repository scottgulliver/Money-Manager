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
import sg.money.widgets.BudgetDateView;
import sg.money.DatabaseManager;
import sg.money.R;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;
import sg.money.utils.Misc;

public class BudgetListAdapter extends BaseAdapter {

	private ArrayList<Budget> m_budgets;
    private static LayoutInflater m_inflater;
    private ArrayList<Budget> m_selectedItems;
    private ArrayList<Transaction> m_transactions;
    private Activity m_activity;
	
    private final int COLOR_SELECTED = Color.rgb(133, 194, 215);
	
	
	/* Constructor */
 
    public BudgetListAdapter(Activity activity, ArrayList<Budget> budgets, ArrayList<Transaction> transactions) {
    	m_activity = activity;
        m_budgets = budgets;
        m_transactions = transactions;
        m_inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_selectedItems = new ArrayList<Budget>();
    }
	
	
	/* Methods */
 
    public int getCount() {
        return m_budgets.size();
    }
    
    public void clearSelected()
    {
    	m_selectedItems.clear();
    }
    
    public void setSelected(int position, boolean selected)
    {
    	Budget item = m_budgets.get(position);
    	if (selected && !m_selectedItems.contains(item))
		{
    		m_selectedItems.add(item);
		}
    	else if (!selected && m_selectedItems.contains(item))
		{
    		m_selectedItems.remove(item);
		}
    }
    
    public ArrayList<Budget> getSelectedItems()
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
            vi = m_inflater.inflate(R.layout.budget_item_layout, null);
		}

        TextView nameText = (TextView)vi.findViewById(R.id.budget_name);
        BudgetDateView progress = (BudgetDateView)vi.findViewById(R.id.budget_progress);
 
        Budget budget = m_budgets.get(position);
 
        //set values
		
        nameText.setText(budget.getName());
        progress.setBudget(budget.getValue());
        
        double spending = 0;
        for(Transaction transaction : m_transactions)
        {
			if (transaction.isDontReport())
				continue;
			
        	if (!DatabaseManager.getInstance(m_activity).GetCategory(transaction.getCategory()).isUseInReports())
        		continue;
        	
        	if (!budget.getAccounts().isEmpty())
        	{
            	boolean isAccout = false;
            	for(Account account : budget.getAccounts())
            	{
            		if (transaction.getAccount() == account.getId())
        			{
        				isAccout = true;
        				break;
        			}
            	}
            	if (!isAccout)
            		continue;
        	}
        	if (!budget.getCategories().isEmpty())
        	{
            	boolean isCategory = false;
            	for(Category category : budget.getCategories())
            	{
            		if (transaction.getCategory() == category.getId())
            		{
            			isCategory = true;
        				break;
        			}
            	}
            	if (!isCategory)
            		continue;
        	}
        	spending += transaction.getRealValue(m_activity);
        }
        progress.setToDate(spending);
        
        TextView typeText = (TextView)vi.findViewById(R.id.budget_extra);
        typeText.setText(Misc.formatValue(m_activity, spending) + " / " + Misc.formatValue(m_activity, budget.getValue()));
        
        if (m_selectedItems.contains(budget))
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
