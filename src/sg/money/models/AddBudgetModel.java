package sg.money.models;

import android.content.*;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.*;

import sg.money.common.DatabaseManager;
import sg.money.domainobjects.*;

public class AddBudgetModel extends Observable implements Parcelable {

    private Budget m_budget;
    private ArrayList<Account> m_currentAccounts;
    private ArrayList<Category> m_currentCategories;
    private ArrayList<Budget> m_currentBudgets;
    private ArrayList<String> m_notifyTypeOptions;
    private boolean m_newBudget;
	
	
	/* Constructor */

    public AddBudgetModel(Budget budget, Context context) {
        m_budget = budget;
        if (m_budget == null)
        {
            m_budget = new Budget();
            m_budget.setNotifyType(Budget.NotificationType.None);
            m_newBudget = true;
        }

        m_currentAccounts = DatabaseManager.getInstance(context).GetAllAccounts();
        m_currentBudgets = DatabaseManager.getInstance(context).GetAllBudgets();
        m_currentCategories = new ArrayList<Category>();
		
		ArrayList<Category> allCategories = DatabaseManager.getInstance(context)
			.GetAllCategories();
		allCategories = Category.getCategoriesInGroupOrder(allCategories);
		for (Category category : allCategories) {
			if (!category.isIncome())
				m_currentCategories.add(category);
		}

		m_notifyTypeOptions = new ArrayList<String>();
		m_notifyTypeOptions.add("None");
		m_notifyTypeOptions.add("Daily");
		m_notifyTypeOptions.add("Weekly");
		m_notifyTypeOptions.add("Monthly");
    }
	
	
	/* Getters / setters */

    public String getBudgetName() {
        return m_budget.getName();
    }

    public void setBudgetName(String budgetName) {
        m_budget.setName(budgetName);
        notifyObservers(this);
    }

    public Double getBudgetValue() {
        return m_budget.getValue();
    }

    public void setBudgetValue(Double value) {
        m_budget.setValue(value);
        notifyObservers(this);
    }
	
	public ArrayList<String> getNotifyTypeOptions()
	{
		return m_notifyTypeOptions;
	}
	
	public ArrayList<Account> getSelectedAccounts()
	{
		return m_budget.getAccounts();
	}
	
	public void setSelectedAccounts(ArrayList<Account> accounts)
	{
		m_budget.setAccounts(accounts);
		notifyObservers(this);
	}

	public ArrayList<Category> getSelectedCategories()
	{
		return m_budget.getCategories();
	}

	public void setSelectedCategories(ArrayList<Category> categories)
	{
		m_budget.setCategories(categories);
		notifyObservers(this);
	}

	public ArrayList<Account> getCurrentAccounts()
	{
		return m_currentAccounts;
	}

	public ArrayList<Category> getCurrentCategories()
	{
		return m_currentCategories;
	}
	
	public Budget.NotificationType getNotifyType()
	{
		return m_budget.getNotifyType();
	}
	
	public void setNotifyType(Budget.NotificationType notifyType)
	{
        if (m_budget.getNotifyType() != notifyType)
        {
            m_budget.setNotifyType(notifyType);
            notifyObservers(this);
        }
	}
	
    public boolean isNewBudget()
    {
        return m_newBudget;
    }
	
	
	/* Methods */

    public String validate(Context context)
    {
        if (m_budget.getName().trim().equals("")) {
            return "Please enter a name.";
        }

        if (m_budget.getValue() < 0) {
            return "Please enter a positive budget value.";
        }

        for (Budget currentBudget : m_currentBudgets) {
            if (currentBudget.getId() == m_budget.getId())
                continue;

            if (m_budget.getName().trim().equals(currentBudget.getName().trim())) {
                return "A budget with this name already exists.";
            }
        }

        return null;
    }

    public void commit(Context context)
    {
        if (isNewBudget())
        {
            DatabaseManager.getInstance(context).AddBudget(m_budget);
        }
        else
        {
            DatabaseManager.getInstance(context).UpdateBudget(m_budget);
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
        m_budget = in.readParcelable(Budget.class.getClassLoader());
        m_currentAccounts = new ArrayList<Account>(Arrays.asList((Account[]) in.readParcelableArray(Account.class.getClassLoader())));
        m_currentCategories = new ArrayList<Category>(Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader())));
        m_currentBudgets = new ArrayList<Budget>(Arrays.asList((Budget[]) in.readParcelableArray(Budget.class.getClassLoader())));
        m_notifyTypeOptions = in.createStringArrayList();
        m_newBudget = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(m_budget, flags);
        parcel.writeParcelableArray((Parcelable[])m_currentAccounts.toArray(), flags);
        parcel.writeParcelableArray((Parcelable[])m_currentCategories.toArray(), flags);
        parcel.writeParcelableArray((Parcelable[])m_currentBudgets.toArray(), flags);
        parcel.writeStringList(m_notifyTypeOptions);
        parcel.writeInt(m_newBudget ? 1 : 0);
    }

    /* End Implementation of Parcelable */
}
