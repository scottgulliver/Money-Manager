package sg.money.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class AddTransactionActivity extends BaseFragmentActivity implements OnChangeListener<AddTransactionModel>
{
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
	
	
	
	
	private static final int TYPE_EXPENSE = 0;
	private static final int TYPE_INCOME = 1;
	private static final int TYPE_TRANSFER = 2;
		
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
		
		final Calendar c = Calendar.getInstance();
		updateDateButtonText(c.getTime());
		
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

        model = new AddTransactionModel(editTransaction, accountId, this);
        model.addListener(this);
        controller = new AddTransactionController(this, model);

        setTitle(model.isNewTransaction() ? "Add Transaction" : "Edit Transaction");
		
		
		
		
		
		
		
		
		
		//for controller
		Calendar c = Calendar.getInstance();
		c.setTime(buttonDate);
		editTransaction.dateTime = c.getTime();


    	btnDate.setOnClickListener(new OnClickListener() {
    		 
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}
 
		});
		
		ArrayList<String> typeChoices = new ArrayList<String>();
		typeChoices.add(TYPE_EXPENSE, "Expense");
		typeChoices.add(TYPE_INCOME, "Income");
		if (model.getAccounts().length > 1)
			typeChoices.add(TYPE_TRANSFER, "Transfer");
    	
		ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item, typeChoices);

		spnType.setAdapter(arrayAdapter2);
		
		spnType.post(new Runnable() { // set up as a post to stop the initial call on initialising UI
			public void run() {
				spnType.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						PopulateCategories();
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			}});
		
		PopulateCategories();
		
		spnCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
						setNewCategoryFieldsVisible(categoryNames.get(pos).equals(ADD_CATEGORY_STRING));
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
		});

        //check if we are editing
		editTransaction = null;
        int editId = getIntent().getIntExtra("ID", -1);
        if (editId != -1)
        {
        }
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
		
		if (!isTransfer)
		{
			//category stuff

			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, model.getCategoryNames());
			spnCategory.setAdapter(arrayAdapter);

			//go to last selected category

			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AddTransactionActivity.this);
			int lastSelectedCategoryId = isIncome ? sharedPref.getInt("lastIncomeCategoryId", -1) : sharedPref.getInt("lastExpenseCategoryId", -1);

			if (lastSelectedCategoryId != -1)
			{
				Category category = null;
				for(Category cat : categories)
				{
					if (cat.id == lastSelectedCategoryId)
					{
						category = cat;
						break;
					}
				}

				if (category != null)
				{
					spnCategory.setSelection(categoryNames.indexOf(getCategoryName(category)));
				}
			}
			else if (!lastSelectedCategory.equals("") && categoryNames.contains(lastSelectedCategory))
				spnCategory.setSelection(categoryNames.indexOf(lastSelectedCategory));
			
		}

		if (!model.getIsTransfer())
		{
			if (!model.getCategory().income)
				model.setValue(model.getValue() * -1.0);

			spnType.setSelection(model.getCategory().income ? TYPE_INCOME : TYPE_EXPENSE);
			PopulateCategories();

			for(String name : model.getCategoryNames())
			{
				if (name.equals(model.getCategory().name))
				{
					spnCategory.setSelection(model.getCategoryNames().indexOf(name));
					break; 
				}
			}
		}
		else
		{
			spnType.setSelection(TYPE_TRANSFER);
			spnType.setEnabled(false);
			PopulateCategories(); // handles the show/hide logic (but probably shouldn't..)

			boolean isTransferFrom = !editTransaction.isReceivingParty();

			if (isTransferFrom)
				model.setValue(model.getValue() * -1.0); //ensure that the value is positive

			spnTransferAccount.setEnabled(isTransferFrom);

			int accountPosition = (new ArrayList<String>(accountsMap.keySet())).indexOf(editTransaction.getRelatedTransferTransaction(this).getAccount(this).name);
			spnTransferAccount.setSelection(accountPosition);
		}
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
    	String sDate = dateFormat.format(buttonDate);
        savedInstanceState.putString(STATE_DATE, sDate);
        
        savedInstanceState.putInt(STATE_TYPE, spnType.getSelectedItemPosition());

    	Category selectedCategory = getSelectedCategory();
    	if (selectedCategory != null)
    		savedInstanceState.putString(STATE_CATEGORY, selectedCategory.name);

        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
    	String sDate = savedInstanceState.getString(STATE_DATE);
    	
    	try {
			updateDateButtonText(dateFormat.parse(sDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

        spnType.setSelection(savedInstanceState.getInt(STATE_TYPE));
        PopulateCategories();
    	
    	String categoryName = savedInstanceState.getString(STATE_CATEGORY);
    	if (categoryName != null)
    	{
    		for(String name : categoryNames)
            {
            	if (name.equals(categoryName))
            	{
            		spnCategory.setSelection(categoryNames.indexOf(name));
            		break; 
            	}
            }
    	}
    }
    
    public void updateDate(Date date)
    {
		controller.onDateChange(date);
   	}
    
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			
			c.setTime(buttonDate);
			
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
    
    private void PopulateCategories()
    {
    	setNewCategoryFieldsVisible(false);

    	
    }
    
    private void setNewCategoryFieldsVisible(boolean visible)
    {
        txtNewCatName.setVisibility(visible ? View.VISIBLE : View.GONE);
        textView4.setVisibility(visible ? View.VISIBLE : View.GONE);
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
    
    private Category getSelectedCategory()
    {
    	Category selectedCategory = null;
    	boolean isIncome = (spnType.getSelectedItemId() == 1);
    	String selectedCategoryName = categoryNames.get(spnCategory.getSelectedItemPosition());
    	for(Category category : categories)
    	{
    		if (getCategoryName(category).equals(selectedCategoryName) && category.income == isIncome)
    		{
    			selectedCategory = category;
    			break;
    		}
    	}
    	
    	return selectedCategory;
    }
	
	public void cancelFocus()
	{
		
	}
}
