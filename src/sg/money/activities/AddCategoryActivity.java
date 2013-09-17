package sg.money.activities;

import java.util.ArrayList;
import java.util.Random;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import sg.money.controllers.AddBudgetController;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.models.AddBudgetModel;
import sg.money.models.AddCategoryModel;
import sg.money.models.OnChangeListener;
import sg.money.widgets.ColorPickerDialog;
import sg.money.DatabaseManager;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.R;

public class AddCategoryActivity extends BaseActivity implements ColorPickerDialog.OnColorChangedListener, OnChangeListener<AddCategoryModel>
{
	EditText txtName;
	Spinner spnType;
	Spinner spnParent;
	ImageView imgColor;

    ArrayList<String> parentOptions; //TODO FIX THIS

    AddCategoryModel model;
	
	//Bundle State Data
	static final String STATE_COLOUR = "stateColour";
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_category);

    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          
        txtName = (EditText)findViewById(R.id.txtName);
        spnType = (Spinner)findViewById(R.id.spnType1);
        imgColor = (ImageView)findViewById(R.id.imgColor);
        spnParent = (Spinner)findViewById(R.id.spnParent);


        // check if we are editing
        Category editCategory = null;
        int editId = getIntent().getIntExtra("ID", -1);
        if (editId != -1) {
            editCategory = DatabaseManager.getInstance(AddCategoryActivity.this).GetCategory(editId);
        }


        model = new AddCategoryModel(editCategory, this);
        model.addListener(this);

        setTitle(model.isNewCategory() ? "Add Category" : "Edit Category");

        imgColor.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) { ColorClicked(); } });

        ArrayList<String> options = new ArrayList<String>();
        options.add("Expense");
        options.add("Income");
        
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item, options);

		spnType.setAdapter(arrayAdapter);
    	
    	spnType.post(new Runnable() {
			public void run() {
				spnType.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						PopulateCategories();
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			}
		});
    	
    	PopulateCategories();

        //check if we are editing
        if (!model.isNewCategory())
        {
        	txtName.setText(editCategory.name);
        	
        	if (editCategory.isPermanent)
        	{
        		txtName.setFocusable(false);
        		txtName.setFocusableInTouchMode(false);
        		txtName.setClickable(false);
        		spnType.setFocusable(false);
        		spnType.setFocusableInTouchMode(false);
        		spnType.setClickable(false);
        	}
        	
        	if (editCategory.parentCategoryId != -1)
        	{
        		Category selectedCategory = null;
        		for(Category category : model.getCurrentCategories())
        		{
        			if (category.id == editCategory.parentCategoryId)
        			{
        				selectedCategory = category;
        			}
        		}
        		
        		if (selectedCategory != null)
        		{
        			spnType.setSelection(selectedCategory.income ? 0 : 1);
        			for(int i = 0; i < parentOptions.size(); i++)
        			{
        				if (parentOptions.get(i).equals(selectedCategory.name))
        				{
        					spnParent.setSelection(i);
        					break;
        				}
        			}
        		}
        	}
    		else
    		{
    			if (editCategory.isPermanent)
    				spnParent.setEnabled(false);
    			else
    			{
	    			for(Category category : model.getCurrentCategories())
	    			{
	    				if (category.parentCategoryId == editCategory.id)
	    				{
	    					spnParent.setEnabled(false);
	    				}
	    			}
    			}
    		}
        }
        
        if (savedInstanceState != null)
        {
        	model.setCurrentColor(savedInstanceState.getInt(STATE_COLOUR));
        }

    	imgColor.setBackgroundColor(model.getCurrentColor());
    	
    }

    @Override
    public void onChange(AddCategoryModel model)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                updateUi();
            }
        });
    }

    public void updateUi()
    {
        txtName.setText(model.getCategoryName());
        spnType.setSelection(model.getIsIncome() ? 1 : 0);
        imgColor.setBackgroundColor(model.getCurrentColor());
        spnParent.setSelection(0); //TODO THIS
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_COLOUR, model.getCurrentColor());
        
        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_category, menu);
        return true;
    }
    
    @Override
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
                Intent intent = new Intent(this, ParentActivity.class);
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Categories);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
	    }
	    return true;
	}
    
    private void PopulateCategories()
    {
    	boolean isIncome = (spnType.getSelectedItemId() == 1);
    	
    	parentOptions = new ArrayList<String>();
    	parentOptions.add("< None >");
    	for(Category category : model.getCurrentCategories())
    	{
    		if (category.parentCategoryId == -1 && category.income == isIncome && !category.isPermanent)
    		{
    			parentOptions.add(category.name);
    		}
    	}
    	ArrayAdapter<String> parentArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, parentOptions);
    	spnParent.setAdapter(parentArrayAdapter);
    }
    
    private void ColorClicked()
    {
    	//launch the color picker
    	new ColorPickerDialog(this, this, model.getCurrentColor()).show();
    }
    
    private int getSelectedParentId()
    {
    	String selectedName = (String)spnParent.getSelectedItem();
    	boolean isIncome = (spnType.getSelectedItemId() == 1);
    	
    	for(Category category : model.getCurrentCategories())
    	{
    		if (category.name.equals(selectedName) && category.income == isIncome)
    			return category.id;
    	}

    	return -1; // todo - throw exception here..
    }
    
    private void OkClicked()
    {
        cancelFocus();

        String validationError = model.validate();
        if (validationError != null)
        {
            Toast.makeText(this, validationError, Toast.LENGTH_SHORT).show();
        }
        else
        {
            model.commit(this);

            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

    public void cancelFocus()
    {
        txtName.clearFocus();
    }

    private void CancelClicked()
    {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

	public void colorChanged(int color) {
		model.setCurrentColor(color);
		imgColor.setBackgroundColor(color);
	}
}
