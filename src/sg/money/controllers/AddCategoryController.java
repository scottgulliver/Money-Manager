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
import sg.money.widgets.ColorPickerDialog;

public class AddCategoryController 
{
	private AddCategoryActivity m_view;
	private AddCategoryModel m_model;
	private ArrayList<String> m_parentOptions;
	
	
	/* Constructor */
	
	public AddCategoryController(AddCategoryActivity view, AddCategoryModel model)
	{
		m_view = view;
		m_model = model;
	}
	
	
	/* Methods */

	public void onCategoryNameChange(String name)
	{
		m_model.setCategoryName(name);
	}

	public void onTypeChange(boolean incomeSelected)
	{
		m_model.setIsIncome(incomeSelected);
	}

	public void onParentChange(int position)
	{
		String parentName = m_parentOptions.get(position);
		
		Category parentCategory = null;
		for(Category category : m_model.getCurrentCategories())
		{
			if (category.getName().equals(parentName))
			{
				parentCategory = category;
				break;
			}
		}
		
		m_model.setParentCategory(parentCategory);
	}

	public void onColorChange(int color)
	{
        m_view.cancelFocus();
		m_model.setCurrentColor(color);
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
                Intent intent = new Intent(m_view, ParentActivity.class);
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Categories);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                m_view.startActivity(intent);
                break;
	    }
	    return true;
	}

    private void OkClicked()
    {
        m_view.cancelFocus();

        String validationError = m_model.validate();
        if (validationError != null)
        {
            Toast.makeText(m_view, validationError, Toast.LENGTH_SHORT).show();
        }
        else
        {
            m_model.commit(m_view);

            m_view.setResult(m_view.RESULT_OK, new Intent());
            m_view.finish();
        }
    }

    private void CancelClicked()
    {
        m_view.setResult(m_view.RESULT_CANCELED, new Intent());
        m_view.finish();
    }

    public ArrayList<String> getParentCategoryOptions()
    {
    	m_parentOptions = new ArrayList<String>();
    	m_parentOptions.add("< None >");
    	for(Category category : m_model.getCurrentCategories())
    	{
    		if (category.getParentCategoryId() == -1
                    && category.isIncome() == m_model.getIsIncome()
                    && !category.isPermanent()
                    && (m_model.isNewCategory() || m_model.getId() != category.getId()))
    		{
    			m_parentOptions.add(category.getName());
    		}
    	}
		
		return m_parentOptions;
    }

    public void changeColor() {
        new ColorPickerDialog(m_view, m_view, m_model.getCurrentColor()).show();
    }
}
