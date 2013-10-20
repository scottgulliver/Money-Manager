package sg.money.adapters;

import java.util.ArrayList;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import sg.money.widgets.BudgetDateView;
import sg.money.common.DatabaseManager;
import sg.money.R;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;
import sg.money.utils.Misc;

public class BudgetListAdapter extends BaseListAdapter<Budget> {

    private ArrayList<Transaction> m_transactions;
	
	
	/* Constructor */
 
    public BudgetListAdapter(Activity activity, ArrayList<Budget> budgets, ArrayList<Transaction> transactions) {
        super(activity, budgets);
        m_transactions = transactions;
    }
	
	
	/* Methods */

    @Override
    protected int getLayoutResourceId() {
        return R.layout.budget_item_layout;
    }

    @Override
    protected void buildView(View view, Budget budget) {

        TextView nameText = (TextView)view.findViewById(R.id.budget_name);
        BudgetDateView progress = (BudgetDateView)view.findViewById(R.id.budget_progress);

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
                boolean isAccount = false;
                for(Account account : budget.getAccounts())
                {
                    if (transaction.getAccount() == account.getId())
                    {
                        isAccount = true;
                        break;
                    }
                }
                if (!isAccount)
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

        TextView typeText = (TextView)view.findViewById(R.id.budget_extra);
        typeText.setText(Misc.formatValue(m_activity, spending) + " / " + Misc.formatValue(m_activity, budget.getValue()));

        if (m_selectedItems.contains(budget))
        {
            view.setBackgroundColor(COLOR_SELECTED);
        }
        else
        {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
