package sg.money.models;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import sg.money.common.DatabaseManager;
import sg.money.controllers.AddTransactionController;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Category;

public class AddCategoryModel extends Observable implements Parcelable {

    private ArrayList<Category> m_currentCategories;
    private Category m_category;
    private ArrayList<String> m_options;
    private ArrayList<String> m_parentOptions;
    private boolean m_newCategory;
	private Category m_cachedParentCategory;
	
	
	/* Constructor */

    public AddCategoryModel(Category category, Context context) {
        m_category = category;
        if (m_category == null)
        {
            m_category = new Category();
            m_newCategory = true;

            Random rnd = new Random(System.currentTimeMillis());
            m_category.setColor(Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
        }

        m_currentCategories = DatabaseManager.getInstance(context).GetAllCategories();
    }
	
	
	/* Getters / setters */

	public void setIsIncome(boolean incomeSelected)
	{
        if (m_category.isIncome() != incomeSelected)
        {
            m_category.setIncome(incomeSelected);
            notifyObservers(this);
        }
	}

	public boolean getIsPermanent()
	{
		return m_category.isPermanent();
	}

    public String getCategoryName()
    {
        return m_category.getName();
    }

    public void setCategoryName(String name)
    {
        if (m_category.getName() == null || !m_category.getName().equals(name))
        {
            m_category.setName(name);
            notifyObservers(this);
        }
    }

    public Category getParentCategory()
    {
		if (m_cachedParentCategory != null 
			&& m_cachedParentCategory.getId() == m_category.getParentCategoryId())
		{
			return m_cachedParentCategory;
		}
		
		m_cachedParentCategory = null;
		for(Category category : m_currentCategories)
		{
			if (category.getId() == m_category.getParentCategoryId())
			{
				m_cachedParentCategory = category;
			}
		}
		
        return m_cachedParentCategory;
    }

	
    public void setParentCategory(Category parent)
    {
        if (m_category.getParentCategoryId() != (parent != null ? parent.getId() : -1))
        {
            m_category.setParentCategoryId(parent != null ? parent.getId() : -1);
            m_cachedParentCategory = parent;
            notifyObservers(this);
        }
    }

    public int getCurrentColor() {
        return m_category.getColor();
    }

    public ArrayList<Category> getCurrentCategories() {
        return m_currentCategories;
    }

    public void setCurrentColor(int color) {
        m_category.setColor(color);
		notifyObservers(this);
    }

    public boolean isNewCategory() {
        return m_newCategory;
    }

    public boolean getIsIncome() {
        return m_category.isIncome();
    }
	
	
	/* Methods */

    public String validate()
    {
        if (m_category.getName().trim().equals(""))
        {
            return "Please enter a name.";
        }

        if (m_category.getName().trim().equals(AddTransactionController.ADD_CATEGORY_STRING))
        {
            return "This name is not valid.";
        }

        if (m_newCategory || !m_category.isPermanent())
        {
            for(Category currentCategory : m_currentCategories)
            {
                if ((currentCategory.getId() == m_category.getId()))

                {
                    continue;
                }

                if (m_category.getName().trim().equals(currentCategory.getName().trim())
                        && currentCategory.isIncome() == m_category.isIncome())
                {
                    return "A category with this name already exists.";
                }
            }
        }

        return null;
    }

    public void commit(Context context)
    {
        if (m_newCategory)
        {
            DatabaseManager.getInstance(context).AddCategory(m_category);
        }
        else
        {
            DatabaseManager.getInstance(context).UpdateCategory(m_category);
        }
    }
	

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<AddCategoryModel> CREATOR = new Parcelable.Creator<AddCategoryModel>() {
        public AddCategoryModel createFromParcel(Parcel in) {
            return new AddCategoryModel(in);
        }

        public AddCategoryModel[] newArray(int size) {
            return new AddCategoryModel[size];
        }
    };

    private AddCategoryModel(Parcel in) {
        m_category = in.readParcelable(Category.class.getClassLoader());
        m_currentCategories = new ArrayList<Category>(Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader())));
        m_options = in.createStringArrayList();
        m_parentOptions = in.createStringArrayList();
        m_newCategory = in.readInt() == 1;
        m_cachedParentCategory = in.readParcelable(Category.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(m_category, flags);
        parcel.writeParcelableArray(m_currentCategories.toArray(new Category[m_currentCategories.size()]), flags);
        parcel.writeStringList(m_options);
        parcel.writeStringList(m_parentOptions);
        parcel.writeInt(m_newCategory ? 1 : 0);
        parcel.writeParcelable(m_cachedParentCategory, flags);
    }

    /* End Implementation of Parcelable */
}
