package sg.money.activities;

import java.util.ArrayList;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import sg.money.domainobjects.Category;
import sg.money.models.AddCategoryModel;
import sg.money.models.OnChangeListener;
import sg.money.widgets.ColorPickerDialog;
import sg.money.common.DatabaseManager;
import sg.money.R;
import sg.money.controllers.*;
import android.view.View.*;

public class AddCategoryActivity extends BaseActivity implements ColorPickerDialog.OnColorChangedListener, OnChangeListener<AddCategoryModel> {
    
	private final String SIS_KEY_MODEL = "Model";

	private EditText m_txtName;
	private Spinner m_spnType;
	private Spinner m_spnParent;
	private ImageView m_imgColor;
    private AddCategoryModel m_model;
	private AddCategoryController m_controller;
	
	
	/* Activity overrides */
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_category);

    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          
        m_txtName = (EditText)findViewById(R.id.txtName);
        m_spnType = (Spinner)findViewById(R.id.spnType1);
        m_imgColor = (ImageView)findViewById(R.id.imgColor);
        m_spnParent = (Spinner)findViewById(R.id.spnParent);


        ArrayList<String> options = new ArrayList<String>();
        options.add("Expense");
        options.add("Income");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, options);

        m_spnType.setAdapter(arrayAdapter);

        if (savedInstanceState != null)
        {
            m_model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
        }
        else
        {
            // check if we are editing
            Category editCategory = null;
            int editId = getIntent().getIntExtra("ID", -1);
            if (editId != -1) {
                editCategory = DatabaseManager.getInstance(AddCategoryActivity.this).GetCategory(editId);
            }

            m_model = new AddCategoryModel(editCategory, this);
        }

        m_model.addListener(this);
		m_controller = new AddCategoryController(this, m_model);

        setTitle(m_model.isNewCategory() ? "Add Category" : "Edit Category");

        m_imgColor.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) { m_controller.changeColor(); } });


		m_txtName.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
                    {
						m_controller.onCategoryNameChange(m_txtName.getText().toString());
					}
				}			
			});
    	
    	m_spnType.post(new Runnable() {
			public void run() {
				m_spnType.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cancelFocus();
                        m_controller.onTypeChange(position == 1);
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
			}
			});

    	m_spnParent.post(new Runnable() {
				public void run() {
					m_spnParent.setOnItemSelectedListener(new OnItemSelectedListener() {
							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                cancelFocus();
                                m_controller.onParentChange(position);
							}

							public void onNothingSelected(AdapterView<?> parent) {
							}
						});
				}
			});
		
		updateUi();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(SIS_KEY_MODEL, m_model);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_category, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    return m_controller.onOptionsItemSelected(item);
	}
	
	
	/* Methods */

    public void updateUi()
    {
        m_txtName.setText(m_model.getCategoryName());
        m_spnType.setSelection(m_model.getIsIncome() ? 1 : 0);
        m_imgColor.setBackgroundColor(m_model.getCurrentColor());

		//load the valid options for the parent

    	ArrayAdapter<String> parentArrayAdapter = new ArrayAdapter<String>(this,
																		   android.R.layout.simple_spinner_dropdown_item, 
																		   m_controller.getParentCategoryOptions());
    	m_spnParent.setAdapter(parentArrayAdapter);

		//set the parent category selection
		if (m_model.getParentCategory() != null)
		{
			ArrayList<String> parentOptions = m_controller.getParentCategoryOptions();
            boolean selectedParent = false;
			for(int i = 0; i < parentOptions.size(); i++)
			{
				if (parentOptions.get(i).equals(m_model.getParentCategory().getName()))
				{
					m_spnParent.setSelection(i);
                    selectedParent = true;
					break;
				}
			}
            if(!selectedParent)
            {
                m_spnParent.setSelection(0);
            }
		}

		//optionally disable some controls
		boolean enableControls = !m_model.getIsPermanent();
		m_txtName.setFocusable(enableControls);
		m_txtName.setFocusableInTouchMode(enableControls);
		m_txtName.setClickable(enableControls);
        m_spnType.setEnabled(enableControls);
		m_spnParent.setEnabled(enableControls);
    }

    public void cancelFocus()
    {
        m_txtName.clearFocus();
    }
	
	
	/* Implementation of OnChangeListener */

    @Override
    public void onChange(AddCategoryModel model)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                updateUi();
            }
        });
    }
	
	
	/* Implementation of ColorPickerDialog.OnColorChangedListener */

	public void colorChanged(int color)
	{
		m_controller.onColorChange(color);
	}
}
