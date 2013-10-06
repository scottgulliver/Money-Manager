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
import sg.money.controllers.*;
import android.view.View.*;

public class AddCategoryActivity extends BaseActivity implements ColorPickerDialog.OnColorChangedListener, OnChangeListener<AddCategoryModel>
{
    private final String SIS_KEY_MODEL = "Model";

	EditText txtName;
	Spinner spnType;
	Spinner spnParent;
	ImageView imgColor;

    AddCategoryModel model;
	AddCategoryController controller;
	
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


        ArrayList<String> options = new ArrayList<String>();
        options.add("Expense");
        options.add("Income");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, options);

        spnType.setAdapter(arrayAdapter);

        if (savedInstanceState != null)
        {
            model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
        }
        else
        {
            // check if we are editing
            Category editCategory = null;
            int editId = getIntent().getIntExtra("ID", -1);
            if (editId != -1) {
                editCategory = DatabaseManager.getInstance(AddCategoryActivity.this).GetCategory(editId);
            }

            model = new AddCategoryModel(editCategory, this);
        }

        model.addListener(this);
		controller = new AddCategoryController(this, model);

        setTitle(model.isNewCategory() ? "Add Category" : "Edit Category");

        imgColor.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) { ColorClicked(); } });


		txtName.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
                    {
						controller.onCategoryNameChange(txtName.getText().toString());
					}
				}			
			});
    	
    	spnType.post(new Runnable() {
			public void run() {
				spnType.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cancelFocus();
                        controller.onTypeChange(position == 1);
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
			}
			});

    	spnParent.post(new Runnable() {
				public void run() {
					spnParent.setOnItemSelectedListener(new OnItemSelectedListener() {
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                cancelFocus();
                                controller.onParentChange(position);
							}

							public void onNothingSelected(AdapterView<?> parent) {
							}
						});
				}
			});
		
		updateUi();
    }

	public void colorChanged(int color)
	{
        cancelFocus();
        controller.colorChanged(color);
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
		
		//load the valid options for the parent

    	ArrayAdapter<String> parentArrayAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_dropdown_item, 
			controller.getParentCategoryOptions());
    	spnParent.setAdapter(parentArrayAdapter);
		
		//set the parent category selection

		if (model.getParentCategory() != null)
		{
			//spnType.setSelection(model.getParentCategory().income ? 1 : 0);
			ArrayList<String> parentOptions = controller.getParentCategoryOptions();
            boolean selectedParent = false;
			for(int i = 0; i < parentOptions.size(); i++)
			{
				if (parentOptions.get(i).equals(model.getParentCategory().getName()))
				{
					spnParent.setSelection(i);
                    selectedParent = true;
					break;
				}
			}
            if(!selectedParent)
            {
                spnParent.setSelection(0);
            }
		}
		
		//optionally disable some controls
		
		boolean enableControls = !model.getIsPermanent();
		txtName.setFocusable(enableControls);
		txtName.setFocusableInTouchMode(enableControls);
		txtName.setClickable(enableControls);
        spnType.setEnabled(enableControls);
		spnParent.setEnabled(enableControls);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(SIS_KEY_MODEL, model);
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
	    return controller.onOptionsItemSelected(item);
	}
    
	//TODO move to controller
    private void ColorClicked()
    {
    	//launch the color picker
    	new ColorPickerDialog(this, this, model.getCurrentColor()).show();
    }

    public void cancelFocus()
    {
        txtName.clearFocus();
    }
}
