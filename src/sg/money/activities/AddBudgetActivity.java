package sg.money.activities;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import sg.money.domainobjects.Budget;
import sg.money.common.DatabaseManager;
import sg.money.models.AddBudgetModel;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.models.*;
import sg.money.controllers.*;
import android.view.View.*;
import android.widget.AdapterView.*;
import android.widget.*;

public class AddBudgetActivity extends BaseActivity implements OnChangeListener<AddBudgetModel> {

    private final String SIS_KEY_MODEL = "Model";

	private EditText m_txtName;
	private EditText m_txtValue;
	private Button m_btnCategories;
	private Button m_btnAccounts;
	private Spinner m_spnNotifyType;
    private AddBudgetModel m_model;
	private AddBudgetController m_controller;

	// Bundle State Data
	static final String STATE_SELECTED_ACCOUNTS = "stateSelectedAccounts";
	static final String STATE_SELECTED_CATEGORIES = "stateSelectedCategories";

	
	/* Activity overrides */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_budget);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		m_txtName = (EditText) findViewById(R.id.txtName);
		m_txtValue = (EditText) findViewById(R.id.txtValue);
		m_btnCategories = (Button) findViewById(R.id.btnCategories);
		m_btnAccounts = (Button) findViewById(R.id.btnAccounts);
		m_spnNotifyType = (Spinner) findViewById(R.id.spnNotifyType);

        if (savedInstanceState != null)
        {
            m_model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
        }
        else
        {
            // check if we are editing
            Budget editBudget = null;
            int editId = getIntent().getIntExtra("ID", -1);
            if (editId != -1) {
                editBudget = DatabaseManager.getInstance(AddBudgetActivity.this).GetBudget(editId);
            }

            m_model = new AddBudgetModel(editBudget, this);
        }

		m_model.addListener(this);
		m_controller = new AddBudgetController(this, m_model);
		
		setTitle(m_model.isNewBudget() ? "Add Budget" : "Edit Budget");

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                                                     android.R.layout.simple_spinner_dropdown_item,
                                                     m_controller.getNotificationOptions());
		m_spnNotifyType.setAdapter(arrayAdapter);

		m_btnCategories.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					m_controller.CategoriesClicked();
				}
			});

		m_btnAccounts.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					m_controller.AccountsClicked();
				}
			});

		m_txtName.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
                        m_controller.onNameChange(m_txtName.getText().toString());
					}
				}			
			});

		m_txtValue.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
                        if (Misc.stringNullEmptyOrWhitespace(m_txtValue.getText().toString()))
                            m_txtValue.setText("0");

						m_controller.onValueChange(Double.parseDouble(m_txtValue.getText().toString()));
					}
				}			
			});
			
		m_spnNotifyType.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				m_controller.onNotifyTypeSelected(Budget.NotificationType.fromInteger(position));
			}

			public void onNothingSelected(AdapterView<?> parent)
			{
                //do nothing
			}
		});

        updateUi();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
        cancelFocus();
        savedInstanceState.putParcelable(SIS_KEY_MODEL, m_model);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_add_budget, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return m_controller.onOptionsItemSelected(item);
	}
	
	
	/* Methods */
	
	public void updateUi()
	{
		m_txtName.setText(m_model.getBudgetName());
		m_txtValue.setText(Misc.formatValue(m_model.getBudgetValue()));
		m_spnNotifyType.setSelection(m_model.getNotifyType().getValue());
	}

    public void cancelFocus()
    {
        m_txtName.clearFocus();
        m_txtValue.clearFocus();
    }
	
	
	/* Implementation of OnChangeListener */

    @Override
    public void onChange(AddBudgetModel model)
    {
        runOnUiThread(new Runnable() {
				public void run() {
					updateUi();
				}
			});
    }
}
