package sg.money.models;

import android.content.Context;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import sg.money.DatabaseManager;
import sg.money.activities.AddTransactionActivity;
import sg.money.controllers.AddTransactionController;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;

public class AddCategoryModel extends SimpleObservable {

    ArrayList<Category> currentCategories;
    Category category;
    ArrayList<String> options;
    ArrayList<String> parentOptions;
    boolean newCategory;
	private Category cachedParentCategory;

    public AddCategoryModel(Category category, Context context) {
        this.category = category;
        if (this.category == null)
        {
            this.category = new Category();
            newCategory = true;

            Random rnd = new Random(System.currentTimeMillis());
            this.category.color = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        }

        currentCategories = DatabaseManager.getInstance(context).GetAllCategories();
    }

	public void setIsIncome(boolean incomeSelected)
	{
        if (category.income != incomeSelected)
        {
            category.income = incomeSelected;
            notifyObservers(this);
        }
	}

	public boolean getIsPermanent()
	{
		return category.isPermanent;
	}

    public String getCategoryName()
    {
        return category.name;
    }

    public void setCategoryName(String name)
    {
        category.name = name;
		notifyObservers(this);
    }

    public Category getParentCategory()
    {
		if (cachedParentCategory != null 
			&& cachedParentCategory.id == category.parentCategoryId)
		{
			return cachedParentCategory;
		}
		
		cachedParentCategory = null;
		for(Category category : currentCategories)
		{
			if (category.id == category.parentCategoryId)
			{
				cachedParentCategory = category;
			}
		}
		
        return cachedParentCategory;
    }

	
    public void setParentCategory(Category parent)
    {
        if (category.parentCategoryId != (parent != null ? parent.id : -1))
        {
            category.parentCategoryId = parent != null ? parent.id : -1;
            cachedParentCategory = parent;
            notifyObservers(this);
        }
    }

    public int getCurrentColor() {
        return category.color;
    }

    public ArrayList<Category> getCurrentCategories() {
        return currentCategories;
    }

    public void setCurrentColor(int color) {
        category.color = color;
		notifyObservers(this);
    }

    public String validate()
    {
        if (category.name.trim().equals(""))
        {
            return "Please enter a name.";
        }

        if (category.name.trim().equals(AddTransactionController.ADD_CATEGORY_STRING))
        {
            return "This name is not valid.";
        }

        if (newCategory || !category.isPermanent)
        {
            for(Category currentCategory : currentCategories)
            {
                if ((currentCategory.id == category.id))

                {
                    continue;
                }

                if (category.name.trim().equals(currentCategory.name.trim())
                        && currentCategory.income == category.income)
                {
                    return "A category with this name already exists.";
                }
            }
        }

        return null;
    }

    public void commit(Context context)
    {
        if (newCategory)
        {
            DatabaseManager.getInstance(context).AddCategory(category);
        }
        else
        {
            DatabaseManager.getInstance(context).UpdateCategory(category);
        }
    }

    public boolean isNewCategory() {
        return newCategory;
    }

    public boolean getIsIncome() {
        return category.income;
    }
}
