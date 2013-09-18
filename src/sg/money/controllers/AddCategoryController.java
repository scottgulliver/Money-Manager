package sg.money.controllers;
import android.content.*;
import android.widget.*;
import com.actionbarsherlock.view.*;
import java.util.*;
import sg.money.*;
import sg.money.activities.*;
import sg.money.domainobjects.*;
import sg.money.fragments.*;
import sg.money.models.*;

public class AddCategoryController {

	private AddCategoryActivity view;
	private AddCategoryModel model;
	
	ArrayList<String> parentOptions;
	
	public AddCategoryController(AddCategoryActivity view, AddCategoryModel model)
	{
		this.view = view;
		this.model = model;
	}

	public void onCategoryNameChange(String name)
	{
		model.setCategoryName(name);
	}

	public void onTypeChange(boolean incomeSelected)
	{
		model.setIsIncome(incomeSelected);
	}

	public void onParentChange(int position)
	{
		String parentName = parentOptions.get(position);
		
		Category parentCategory = null;
		for(Category category : model.getCurrentCategories())
		{
			if (category.name.equals(parentName))
			{
				parentCategory = category;
				break;
			}
		}
		
		if (parentCategory == null)
		{
			throw new RuntimeException("Selected category does not exist");
		}
		
		model.setParentCategory(parentCategory);
	}

	public void colorChanged(int color) {
		model.setCurrentColor(color);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	    	case R.id.menu_acceptcategory:{
					OkClicked();
					break;
	    		}

	    	case R.id.menu_rejectcategory:{
					CancelClicked();
					break;
	    		}	    	

	        case R.id.menu_settings:{
					break;
            	}

	        case android.R.id.home:
                Intent intent = new Intent(view, ParentActivity.class);
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Categories);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                view.startActivity(intent);
                break;
	    }
	    return true;
	}

    private void OkClicked()
    {
        view.cancelFocus();

        String validationError = model.validate();
        if (validationError != null)
        {
            Toast.makeText(view, validationError, Toast.LENGTH_SHORT).show();
        }
        else
        {
            model.commit(view);

            view.setResult(view.RESULT_OK, new Intent());
            view.finish();
        }
    }

    private void CancelClicked()
    {
        view.setResult(view.RESULT_CANCELED, new Intent());
        view.finish();
    }

    public ArrayList<String> getParentCategoryOptions()
    {
    	parentOptions = new ArrayList<String>();
    	parentOptions.add("< None >");
    	for(Category category : model.getCurrentCategories())
    	{
    		if (category.parentCategoryId == -1 && category.income == model.getIsIncome() && !category.isPermanent)
    		{
    			parentOptions.add(category.name);
    		}
    	}
		
		return parentOptions;
    }
}
