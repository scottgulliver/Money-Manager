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
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;

public class AddCategoryModel extends SimpleObservable {

    ArrayList<Category> currentCategories;
    Category category;
    ArrayList<String> options;
    ArrayList<String> parentOptions;
    int currentColor;
    boolean newCategory;

    public AddCategoryModel(Category category, Context context) {
        this.category = category;
        if (this.category == null)
        {
            this.category = new Category();
            newCategory = true;


            Random rnd = new Random(System.currentTimeMillis());
            currentColor = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        }

        currentCategories = DatabaseManager.getInstance(context).GetAllCategories();
    }

    public String getCategoryName()
    {
        return category.name;
    }

    public void setCategoryName(String value)
    {
        category.name = value;
    }

    public int getParentCategory()
    {
        return category.parentCategoryId;
    }

    public void setParentCategory(Category parent)
    {
        category.parentCategoryId = parent.id;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public ArrayList<Category> getCurrentCategories() {
        return currentCategories;
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }

    public String validate()
    {
        if (category.name.trim().equals(""))
        {
            return "Please enter a name.";
        }

        if (category.name.trim().equals(AddTransactionActivity.ADD_CATEGORY_STRING))
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
