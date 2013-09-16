package sg.money.models;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import sg.money.DatabaseManager;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;

public class AddBudgetModel extends SimpleObservable {

    private Budget budget;
    private ArrayList<Account> currentAccounts;
    private ArrayList<Category> currentCategories;
    private ArrayList<Budget> currentBudgets;
    private ArrayList<Account> selectedAccounts;
    private ArrayList<Category> selectedCategories;
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
        currentCategories = DatabaseManager.getInstance(context).GetAllCategories();
        currentBudgets = DatabaseManager.getInstance(context).GetAllBudgets();
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