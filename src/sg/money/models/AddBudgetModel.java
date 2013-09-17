package sg.money.models;

import android.content.*;
import java.util.*;
import sg.money.*;
import sg.money.domainobjects.*;
import sg.money.utils.*;

public class AddBudgetModel extends SimpleObservable {

    private Budget budget;
    private ArrayList<Account> currentAccounts;
    private ArrayList<Category> currentCategories;
    private ArrayList<Budget> currentBudgets;
    private ArrayList<String> notifyTypeOptions;
    private boolean newBudget;

    public AddBudgetModel(Budget budget, Context context) {
        this.budget = budget;
        if (this.budget == null)
        {
            this.budget = new Budget();
            newBudget = true;
        }

        currentAccounts = DatabaseManager.getInstance(context).GetAllAccounts();
        currentBudgets = DatabaseManager.getInstance(context).GetAllBudgets();
        currentCategories = new ArrayList<Category>();
		
		ArrayList<Category> allCategories = DatabaseManager.getInstance(context)
			.GetAllCategories();
		allCategories = Misc.getCategoriesInGroupOrder(allCategories);
		for (Category category : allCategories) {
			if (!category.income)
				currentCategories.add(category);
		}

		notifyTypeOptions = new ArrayList<String>();
		notifyTypeOptions.add("None");
		notifyTypeOptions.add("Daily");
		notifyTypeOptions.add("Weekly");
		notifyTypeOptions.add("Monthly");
    }

    public String getBudgetName() {
        return budget.name;
    }

    public void setBudgetName(String budgetName) {
        budget.name = budgetName;
        notifyObservers(this);
    }

    public Double getBudgetValue() {
        return budget.value;
    }

    public void setBudgetValue(Double value) {
        budget.value = value;
        notifyObservers(this);
    }
	
	public ArrayList<String> getNotifyTypeOptions()
	{
		return notifyTypeOptions;
	}
	
	public ArrayList<Account> getSelectedAccounts()
	{
		return budget.accounts;
	}
	
	public void setSelectedAccounts(ArrayList<Account> accounts)
	{
		budget.accounts = accounts;
		notifyObservers(this);
	}

	public ArrayList<Category> getSelectedCategories()
	{
		return budget.categories;
	}

	public void setSelectedCategories(ArrayList<Category> categories)
	{
		budget.categories = categories;
		notifyObservers(this);
	}

	public ArrayList<Account> getCurrentAccounts()
	{
		return currentAccounts;
	}

	public ArrayList<Category> getCurrentCategories()
	{
		return currentCategories;
	}
	
	public int getNotifyType()
	{
		return budget.notifyType;
	}
	
	public void setNotifyType(int notifyType)
	{
		budget.notifyType = notifyType;
		notifyObservers(this);
	}
	
    public boolean isNewBudget()
    {
        return newBudget;
    }

    public String validate(Context context)
    {
        if (budget.name.trim().equals("")) {
            return "Please enter a name.";
        }

        if (budget.value < 0) {
            return "Please enter a positive budget value.";
        }

        for (Budget currentBudget : currentBudgets) {
            if (currentBudget.id == budget.id)
                continue;

            if (budget.name.trim().equals(currentBudget.name.trim())) {
                return "A budget with this name already exists.";
            }
        }

        return null;
    }

    public void commit(Context context)
    {
        if (isNewBudget())
        {
            DatabaseManager.getInstance(context).AddBudget(budget);
        }
        else
        {
            DatabaseManager.getInstance(context).UpdateBudget(budget);
        }
    }
}
