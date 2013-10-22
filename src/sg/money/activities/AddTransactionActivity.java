package sg.money.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;

import sg.money.domainobjects.Category;
import sg.money.common.DatabaseManager;
import sg.money.models.AddTransactionModel;
import sg.money.models.OnChangeListener;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.domainobjects.Transaction;
import sg.money.controllers.*;
import android.util.*;
import android.view.View.*;
import android.widget.CompoundButton.*;

public class AddTransactionActivity extends BaseFragmentActivity implements OnChangeListener<AddTransactionModel> {


    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();

			c.setTime(m_model.getDate());

			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);

			((AddTransactionActivity)getActivity()).updateDate(c.getTime());
		}
    }
	
	
    private final String SIS_KEY_MODEL = "Model";

	private EditText m_txtValue;
	private EditText m_txtDesc; 
	private TextView m_txtCategory;
	private Spinner m_spnCategory;
	private Spinner m_spnType;
	private EditText m_txtNewCatName; 
	private TextView m_tvCategoryName;
	private TextView m_txtHideFromReports;
	private CheckBox m_chkHideFromReports;
	private TextView m_txtTransferAccount;
	private Spinner m_spnTransferAccount;
	private static Button m_btnDate;
    private AddTransactionModel m_model;
	private AddTransactionController m_controller;
	
	
	/* Activity overrides */
	
    @Override 
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        m_txtValue = (EditText)findViewById(R.id.txtValue);
        m_txtDesc = (EditText)findViewById(R.id.txtDesc1);
        m_txtCategory = (TextView)findViewById(R.id.textView3);
        m_spnCategory = (Spinner)findViewById(R.id.spnCategory1);
        m_spnType = (Spinner)findViewById(R.id.spnType1);
        m_btnDate = (Button)findViewById(R.id.btnDate);
        m_txtNewCatName = (EditText)findViewById(R.id.txtNewCatName);
        m_tvCategoryName = (TextView)findViewById(R.id.textView4);
        m_chkHideFromReports = (CheckBox)findViewById(R.id.chkHideFromReports);
        m_txtHideFromReports = (TextView)findViewById(R.id.textView6);
        m_txtTransferAccount = (TextView)findViewById(R.id.textView7);
        m_spnTransferAccount = (Spinner)findViewById(R.id.spnTransferAccount);

        m_txtNewCatName.setVisibility(View.GONE);
        m_tvCategoryName.setVisibility(View.GONE);

        if (savedInstanceState != null)
        {
            m_model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
        }
        else
        {
            int accountId = getIntent().getIntExtra("AccountID", -1);
            if (accountId == -1)
            {
                throw new RuntimeException("AccountID is -1");
            }

            // check if we are editing
            Transaction editTransaction = null;
            int editId = getIntent().getIntExtra("ID", -1);
            if (editId != -1) {
                editTransaction = DatabaseManager.getInstance(AddTransactionActivity.this).GetTransaction(editId);
            }

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            int lastSelectedCategoryId = sharedPref.getInt("lastExpenseCategoryId", -1);

            if (lastSelectedCategoryId == -1)
            {
                for(Category category : DatabaseManager.getInstance(this).GetAllCategories())
                {
                    lastSelectedCategoryId = category.getId();
                }
            }

            m_model = new AddTransactionModel(editTransaction, accountId, this);
        }

        m_model.addListener(this);
        m_controller = new AddTransactionController(this, m_model);

        setTitle(m_model.isNewTransaction() ? "Add Transaction" : "Edit Transaction");

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_dropdown_item, m_controller.getTypeChoices());

        m_spnType.setAdapter(arrayAdapter2);

        updateUi();

    	m_btnDate.setOnClickListener(new OnClickListener() {
    		 
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}
 
		});
		
		m_spnType.post(new Runnable() { // set up as a post to stop the initial call on initialising UI
			public void run() {
				m_spnType.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cancelFocus();
						m_controller.onTypeChange(position);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			}});
		
		m_spnCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cancelFocus();
						m_controller.onCategoryChange(position);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
			});

        m_spnTransferAccount.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cancelFocus();
                m_controller.onTransferAccountChange(position);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

		m_txtValue.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{

						m_controller.onTransactionValueChange(Double.parseDouble(m_txtValue.getText().toString()));
					}
				}			
			});

		m_txtDesc.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
						m_controller.onTransactionDescriptionChange(m_txtDesc.getText().toString());
					}
				}			
			});
			
		m_chkHideFromReports.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton view, boolean checked)
				{
                    cancelFocus();
					m_controller.onHideFromReportsChange(checked);
				}
			});
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	cancelFocus();
        savedInstanceState.putParcelable(SIS_KEY_MODEL, m_model);
        super.onSaveInstanceState(savedInstanceState);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_transaction, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    return m_controller.onOptionsItemSelected(item);
	}
	
	
	/* Methods */

    public void updateUi()
    {
		m_txtDesc.setText(m_model.getDescription());
		m_chkHideFromReports.setChecked(m_model.getDontReport());

		m_txtValue.setText(String.valueOf(m_model.getValue()));

		String str = m_txtValue.getText().toString();
		if (str.contains(".") && str.substring(str.indexOf(".")+1).length() == 1)
			m_txtValue.setText(str + 0);
		
		try
		{
			m_btnDate.setText(Misc.formatDate(AddTransactionActivity.this, m_model.getDate()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
			this,android.R.layout.simple_spinner_dropdown_item, m_model.getAccountNames());
		m_spnTransferAccount.setAdapter(arrayAdapter1);

    	boolean isTransfer = m_model.getIsTransfer();
        boolean isExistingTransfer = isTransfer && !m_model.isNewTransaction();
    	m_txtTransferAccount.setVisibility(isTransfer && (!isExistingTransfer || !m_model.isReceivingParty())
                ? View.VISIBLE : View.GONE);
    	m_spnTransferAccount.setVisibility(isTransfer && (!isExistingTransfer || !m_model.isReceivingParty())
                ? View.VISIBLE : View.GONE);
    	m_chkHideFromReports.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
    	m_txtHideFromReports.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
    	m_txtCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
    	m_spnCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
        m_spnType.setEnabled(!isExistingTransfer);

        boolean addNewCategory = m_model.getUseNewCategory();
        m_txtNewCatName.setVisibility(addNewCategory ? View.VISIBLE : View.GONE);
        m_tvCategoryName.setVisibility(addNewCategory ? View.VISIBLE : View.GONE);
		
        m_spnType.setSelection(m_model.getIsTransfer()
                ? m_controller.getTypeChoices().indexOf(AddTransactionController.TransactionType.Transfer.name())
                : m_model.isIncomeType()
                	? m_controller.getTypeChoices().indexOf(AddTransactionController.TransactionType.Income.name())
                	: m_controller.getTypeChoices().indexOf(AddTransactionController.TransactionType.Expense.name()));

		if (!m_model.getIsTransfer())
		{
            ArrayList<String> categoryNames = m_controller.getCategoryNames();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, categoryNames);
            m_spnCategory.setAdapter(arrayAdapter);

            m_spnCategory.setSelection(categoryNames.indexOf(m_model.getCategory() != null ? m_model.getCategory().getName() : AddTransactionController.ADD_CATEGORY_STRING));
		}
		else
		{
			boolean isTransferFrom = !m_model.isReceivingParty();

			if (isTransferFrom)
				m_model.setValue(m_model.getValue()); //ensure that the value is positive

			m_spnTransferAccount.setEnabled(isTransferFrom);
            m_spnTransferAccount.setSelection(Arrays.asList(m_model.getAccountNames()).indexOf(m_model.getTransferAccount(this).getName()));
		}
    }
    
    public void updateDate(Date date)
    {
        cancelFocus();
		m_controller.onDateChange(date);
   	}
	
	public void cancelFocus()
	{
		m_txtValue.clearFocus();
		m_txtDesc.clearFocus();
	}
	
	
	/* Implementation of OnChangeListener */
	
    @Override
    public void onChange(AddTransactionModel model)
    {
        runOnUiThread(new Runnable() {
				public void run() {
					updateUi();
				}
			});
    }
}
