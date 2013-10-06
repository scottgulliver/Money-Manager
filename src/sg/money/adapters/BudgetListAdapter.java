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

public class BudgetListAdapter extends BaseAdapter
{
	private ArrayList<Budget> budgets;
    private static LayoutInflater inflater=null;
    private ArrayList<Budget> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
    private ArrayList<Transaction> transactions;
    private Activity activity;
 
    public BudgetListAdapter(Activity activity, ArrayList<Budget> budgets, ArrayList<Transaction> transactions) {
    	this.activity = activity;
        this.budgets = budgets;
        this.transactions = transactions;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Budget>();
    }
 
    public int getCount() {
        return budgets.size();
    }
    
    public void ClearSelected()
    {
    	selectedItems.clear();
    }
    
    public void SetSelected(int position, boolean selected)
    {
    	Budget item = budgets.get(position);
    	if (selected && !selectedItems.contains(item))
    		selectedItems.add(item);
    	else if (!selected && selectedItems.contains(item))
    		selectedItems.remove(item);
    }
    
    public ArrayList<Budget> GetSelectedItems()
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
            vi = inflater.inflate(R.layout.budget_item_layout, null);

        TextView nameText = (TextView)vi.findViewById(R.id.budget_name);
        BudgetDateView progress = (BudgetDateView)vi.findViewById(R.id.budget_progress);
 
        Budget budget = budgets.get(position);
 
        //set values
        nameText.setText(budget.getName());
        progress.setBudget(budget.getValue());
        
        double spending = 0;
        for(Transaction transaction : transactions)
        {
			if (transaction.isDontReport())
				continue;
			
        	if (!DatabaseManager.getInstance(activity).GetCategory(transaction.getCategory()).isUseInReports())
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
        	spending += transaction.getRealValue(activity);
        }
        progress.setToDate(spending);
        
        TextView typeText = (TextView)vi.findViewById(R.id.budget_extra);
        typeText.setText(Misc.formatValue(activity, spending) + " / " + Misc.formatValue(activity, budget.getValue()));
        
        if (selectedItems.contains(budget))
        	vi.setBackgroundColor(COLOR_SELECTED);
        else
        	vi.setBackgroundColor(Color.TRANSPARENT);

        return vi;
    }
}
