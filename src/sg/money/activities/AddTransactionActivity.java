package sg.money.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;

import sg.money.controllers.AddCategoryController;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Category;
import sg.money.DatabaseManager;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.models.AddCategoryModel;
import sg.money.models.AddTransactionModel;
import sg.money.models.OnChangeListener;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.domainobjects.Transaction;
import sg.money.controllers.*;
import android.util.*;
import android.view.View.*;
import android.widget.CompoundButton.*;

public class AddTransactionActivity extends BaseFragmentActivity implements OnChangeListener<AddTransactionModel>
{
    private final String SIS_KEY_MODEL = "Model";

	EditText txtValue;
	EditText txtDesc; 
	TextView txtCategory;
	Spinner spnCategory;
	Spinner spnType;
	EditText txtNewCatName; 
	TextView textView4;
	TextView txtHideFromReports;
	CheckBox chkHideFromReports;
	static Button btnDate;
	public static Date buttonDate;
	TextView txtTransferAccount;
	Spinner spnTransferAccount;

    AddTransactionModel model;
	AddTransactionController controller;
		
	//Bundle State Data
	static final String STATE_DATE = "stateDate";
	static final String STATE_CATEGORY = "stateCategory";
	static final String STATE_TYPE = "stateType";
	
    @Override 
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtValue = (EditText)findViewById(R.id.txtValue);
        txtDesc = (EditText)findViewById(R.id.txtDesc1);
        txtCategory = (TextView)findViewById(R.id.textView3);
        spnCategory = (Spinner)findViewById(R.id.spnCategory1);
        spnType = (Spinner)findViewById(R.id.spnType1);
        btnDate = (Button)findViewById(R.id.btnDate);
        txtNewCatName = (EditText)findViewById(R.id.txtNewCatName);
        textView4 = (TextView)findViewById(R.id.textView4);
        chkHideFromReports = (CheckBox)findViewById(R.id.chkHideFromReports);
        txtHideFromReports = (TextView)findViewById(R.id.textView6);
        txtTransferAccount = (TextView)findViewById(R.id.textView7);
        spnTransferAccount = (Spinner)findViewById(R.id.spnTransferAccount);

        txtNewCatName.setVisibility(View.GONE);
        textView4.setVisibility(View.GONE);

        if (savedInstanceState != null)
        {
            model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
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
            //int lastSelectedCategoryId = model.getCategory().income ? sharedPref.getInt("lastIncomeCategoryId", -1) : sharedPref.getInt("lastExpenseCategoryId", -1);

            int lastSelectedCategoryId = sharedPref.getInt("lastExpenseCategoryId", -1);

            if (lastSelectedCategoryId == -1)
            {
                for(Category category : DatabaseManager.getInstance(this).GetAllCategories())
                {
                    lastSelectedCategoryId = category.id;
                }
            }

            model = new AddTransactionModel(editTransaction, accountId, lastSelectedCategoryId, this);
        }

        model.addListener(this);
        controller = new AddTransactionController(this, model);

        setTitle(model.isNewTransaction() ? "Add Transaction" : "Edit Transaction");

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_dropdown_item, controller.getTypeChoices());

        spnType.setAdapter(arrayAdapter2);

    	btnDate.setOnClickListener(new OnClickListener() {
    		 
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}
 
		});
		
		spnType.post(new Runnable() { // set up as a post to stop the initial call on initialising UI
			public void run() {
				spnType.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cancelFocus();
						controller.onTypeChange(position);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			}});
		
		spnCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cancelFocus();
						controller.onCategoryChange(position);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
			});

		txtValue.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
						controller.onTransactionValueChange(Double.parseDouble(txtValue.getText().toString()));
					}
				}			
			});

		txtDesc.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
						controller.onTransactionDescriptionChange(txtDesc.getText().toString());
					}
				}			
			});
			
		chkHideFromReports.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton view, boolean checked)
				{
                    cancelFocus();
					controller.onHideFromReportsChange(checked);
				}
			});

        updateUi();
    }

    @Override
    public void onChange(AddTransactionModel model)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                updateUi();
            }
        });
    }

    public void updateUi()
    {
		txtDesc.setText(model.getDescription());
		chkHideFromReports.setChecked(model.getDontReport());

		txtValue.setText(String.valueOf(model.getValue()));

		//gah! - change this! do this properly.
		String str = txtValue.getText().toString();
		if (str.contains(".") && str.substring(str.indexOf(".")+1).length() == 1)
			txtValue.setText(str + 0);
		
		try
		{
			btnDate.setText(Misc.formatDate(AddTransactionActivity.this, model.getDate()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
			this,android.R.layout.simple_spinner_dropdown_item, model.getAccountNames());
		spnTransferAccount.setAdapter(arrayAdapter1);

    	boolean isTransfer = model.getIsTransfer();
    	txtTransferAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
    	spnTransferAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
    	chkHideFromReports.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
    	txtHideFromReports.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
    	txtCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
    	spnCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        boolean addNewCategory = model.getUseNewCategory();
        txtNewCatName.setVisibility(addNewCategory ? View.VISIBLE : View.GONE);
        textView4.setVisibility(addNewCategory ? View.VISIBLE : View.GONE);

		Log.e("sg.money", model.isIncomeType() ? "Income spn" : "Not income spn");
        spnType.setSelection(model.getIsTransfer()
                ? controller.getTypeChoices().indexOf(AddTransactionController.TransactionType.Transfer.name())
                : model.isIncomeType()
                	? controller.getTypeChoices().indexOf(AddTransactionController.TransactionType.Income.name())
                	: controller.getTypeChoices().indexOf(AddTransactionController.TransactionType.Expense.name()));

        //spnType.setEnabled(model.getIsTransfer());
		
		if (!isTransfer)
		{
			//category stuff

			
		}

		if (!model.getIsTransfer())
		{
			//if (!model.getCategory().income)
			//	model.setValue(model.getValue() * -1.0);

            ArrayList<String> categoryNames = controller.getCategoryNames();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, categoryNames);
            spnCategory.setAdapter(arrayAdapter);

            spnCategory.setSelection(categoryNames.indexOf(model.getCategory() != null ? model.getCategory().name : AddTransactionController.ADD_CATEGORY_STRING));
		}
		else
		{
			boolean isTransferFrom = !model.isReceivingParty();

			if (isTransferFrom)
				model.setValue(model.getValue() * -1.0); //ensure that the value is positive

			spnTransferAccount.setEnabled(isTransferFrom);

			int accountPosition = (new ArrayList<String>(Arrays.asList(model.getAccountNames()))).indexOf(model.getRelatedTransferTransaction(this).getAccount(this).name);
			spnTransferAccount.setSelection(accountPosition);
		}
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	cancelFocus();
        savedInstanceState.putParcelable(SIS_KEY_MODEL, model);
        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    public void updateDate(Date date)
    {
        cancelFocus();
		controller.onDateChange(date);
   	}
    
    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			
			c.setTime(model.getDate());
			
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_transaction, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    return controller.onOptionsItemSelected(item);
	}
	
	public void cancelFocus()
	{
		txtValue.clearFocus();
		txtDesc.clearFocus();
	}
}
