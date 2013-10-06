package sg.money.models;

import android.content.*;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.*;
import sg.money.*;
import sg.money.domainobjects.*;
import sg.money.utils.*;

public class AddBudgetModel extends SimpleObservable implements Parcelable {

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
            this.budget.setNotifyType(Budget.NotificationType.None);
            newBudget = true;
        }

        currentAccounts = DatabaseManager.getInstance(context).GetAllAccounts();
        currentBudgets = DatabaseManager.getInstance(context).GetAllBudgets();
        currentCategories = new ArrayList<Category>();
		
		ArrayList<Category> allCategories = DatabaseManager.getInstance(context)
			.GetAllCategories();
		allCategories = Misc.getCategoriesInGroupOrder(allCategories);
		for (Category category : allCategories) {
			if (!category.isIncome())
				currentCategories.add(category);
		}

		notifyTypeOptions = new ArrayList<String>();
		notifyTypeOptions.add("None");
		notifyTypeOptions.add("Daily");
		notifyTypeOptions.add("Weekly");
		notifyTypeOptions.add("Monthly");
    }

    public String getBudgetName() {
        return budget.getName();
    }

    public void setBudgetName(String budgetName) {
        budget.setName(budgetName);
        notifyObservers(this);
    }

    public Double getBudgetValue() {
        return budget.getValue();
    }

    public void setBudgetValue(Double value) {
        budget.setValue(value);
        notifyObservers(this);
    }
	
	public ArrayList<String> getNotifyTypeOptions()
	{
		return notifyTypeOptions;
	}
	
	public ArrayList<Account> getSelectedAccounts()
	{
		return budget.getAccounts();
	}
	
	public void setSelectedAccounts(ArrayList<Account> accounts)
	{
		budget.setAccounts(accounts);
		notifyObservers(this);
	}

	public ArrayList<Category> getSelectedCategories()
	{
		return budget.getCategories();
	}

	public void setSelectedCategories(ArrayList<Category> categories)
	{
		budget.setCategories(categories);
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
	
	public Budget.NotificationType getNotifyType()
	{
		return budget.getNotifyType();
	}
	
	public void setNotifyType(Budget.NotificationType notifyType)
	{
        if (budget.getNotifyType() != notifyType)
        {
            budget.setNotifyType(notifyType);
            notifyObservers(this);
        }
	}
	
    public boolean isNewBudget()
    {
        return newBudget;
    }

    public String validate(Context context)
    {
        if (budget.getName().trim().equals("")) {
            return "Please enter a name.";
        }

        if (budget.getValue() < 0) {
            return "Please enter a positive budget value.";
        }

        for (Budget currentBudget : currentBudgets) {
            if (currentBudget.getId() == budget.getId())
                continue;

            if (budget.getName().trim().equals(currentBudget.getName().trim())) {
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

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<AddBudgetModel> CREATOR = new Parcelable.Creator<AddBudgetModel>() {
        public AddBudgetModel createFromParcel(Parcel in) {
            return new AddBudgetModel(in);
        }

        public AddBudgetModel[] newArray(int size) {
            return new AddBudgetModel[size];
        }
    };

    private AddBudgetModel(Parcel in) {
        budget = in.readParcelable(Budget.class.getClassLoader());
        currentAccounts = new ArrayList<Account>(Arrays.asList((Account[]) in.readParcelableArray(Account.class.getClassLoader())));
        currentCategories = new ArrayList<Category>(Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader())));
        currentBudgets = new ArrayList<Budget>(Arrays.asList((Budget[]) in.readParcelableArray(Budget.class.getClassLoader())));
        notifyTypeOptions = in.createStringArrayList();
        newBudget = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(budget, flags);
        parcel.writeParcelableArray((Parcelable[])currentAccounts.toArray(), flags);
        parcel.writeParcelableArray((Parcelable[])currentCategories.toArray(), flags);
        parcel.writeParcelableArray((Parcelable[])currentBudgets.toArray(), flags);
        parcel.writeStringList(notifyTypeOptions);
        parcel.writeInt(newBudget ? 1 : 0);
    }

    /* End Implementation of Parcelable */
}
