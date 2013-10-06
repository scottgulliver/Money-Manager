package sg.money.models;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import sg.money.DatabaseManager;
import sg.money.activities.AddTransactionActivity;
import sg.money.controllers.AddTransactionController;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;

public class AddCategoryModel extends SimpleObservable implements Parcelable {

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
            this.category.setColor(Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
        }

        currentCategories = DatabaseManager.getInstance(context).GetAllCategories();
    }

	public void setIsIncome(boolean incomeSelected)
	{
        if (category.isIncome() != incomeSelected)
        {
            category.setIncome(incomeSelected);
            notifyObservers(this);
        }
	}

	public boolean getIsPermanent()
	{
		return category.isPermanent();
	}

    public String getCategoryName()
    {
        return category.getName();
    }

    public void setCategoryName(String name)
    {
        if (category.getName() == null || !category.getName().equals(name))
        {
            category.setName(name);
            notifyObservers(this);
        }
    }

    public Category getParentCategory()
    {
		if (cachedParentCategory != null 
			&& cachedParentCategory.getId() == category.getParentCategoryId())
		{
			return cachedParentCategory;
		}
		
		cachedParentCategory = null;
		for(Category category : currentCategories)
		{
			if (category.getId() == category.getParentCategoryId())
			{
				cachedParentCategory = category;
			}
		}
		
        return cachedParentCategory;
    }

	
    public void setParentCategory(Category parent)
    {
        if (category.getParentCategoryId() != (parent != null ? parent.getId() : -1))
        {
            category.setParentCategoryId(parent != null ? parent.getId() : -1);
            cachedParentCategory = parent;
            notifyObservers(this);
        }
    }

    public int getCurrentColor() {
        return category.getParentCategoryId();
    }

    public ArrayList<Category> getCurrentCategories() {
        return currentCategories;
    }

    public void setCurrentColor(int color) {
        category.setColor(color);
		notifyObservers(this);
    }

    public String validate()
    {
        if (category.getName().trim().equals(""))
        {
            return "Please enter a name.";
        }

        if (category.getName().trim().equals(AddTransactionController.ADD_CATEGORY_STRING))
        {
            return "This name is not valid.";
        }

        if (newCategory || !category.isPermanent())
        {
            for(Category currentCategory : currentCategories)
            {
                if ((currentCategory.getId() == category.getId()))

                {
                    continue;
                }

                if (category.getName().trim().equals(currentCategory.getName().trim())
                        && currentCategory.isIncome() == category.isIncome())
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
        return category.isIncome();
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
        category = in.readParcelable(Category.class.getClassLoader());
        currentCategories = new ArrayList<Category>(Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader())));
        options = in.createStringArrayList();
        parentOptions = in.createStringArrayList();
        newCategory = in.readInt() == 1;
        cachedParentCategory = in.readParcelable(Category.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(category, flags);
        parcel.writeParcelableArray((Parcelable[])currentCategories.toArray(), flags);
        parcel.writeStringList(options);
        parcel.writeStringList(parentOptions);
        parcel.writeInt(newCategory ? 1 : 0);
        parcel.writeParcelable(cachedParentCategory, flags);
    }

    /* End Implementation of Parcelable */
}
