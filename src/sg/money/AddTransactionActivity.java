package sg.money;

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

public class AddTransactionActivity extends BaseFragmentActivity
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

	ArrayList<Category> categories;
	Map<String, Account> accountsMap = new HashMap<String, Account>();
	ArrayList<String> categoryNames;
	Transaction editTransaction;
	
	int accountID = -1; 
	
	public static final String ADD_CATEGORY_STRING = "< Add new category >";
	
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
    	setTitle("Add Transaction");

    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        accountID = getIntent().getIntExtra("AccountID", -1);
        
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

    	categories = DatabaseManager.getInstance(AddTransactionActivity.this).GetAllCategories();
    	
    	btnDate.setOnClickListener(new OnClickListener() {
    		 
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
			    newFragment.show(getSupportFragmentManager(), "datePicker");
			}
 
		});
    	
    	ArrayList<Account> accounts = DatabaseManager.getInstance(AddTransactionActivity.this).GetAllAccounts();
    	for(Account account : accounts)
    	{
    		if (account.id != accountID)
    			accountsMap.put(account.name, account);
    	}
		ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>(accountsMap.keySet()));
		spnTransferAccount.setAdapter(arrayAdapter1);
		
		ArrayList<String> typeChoices = new ArrayList<String>();
		typeChoices.add(TYPE_EXPENSE, "Expense");
		typeChoices.add(TYPE_INCOME, "Income");
		if (accounts.size() > 1)
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
        	setTitle("Edit Transaction");
        	editTransaction = DatabaseManager.getInstance(AddTransactionActivity.this).GetTransaction(editId);
        	
        	txtDesc.setText(editTransaction.description);
        	updateDateButtonText(editTransaction.dateTime);
			chkHideFromReports.setChecked(editTransaction.dontReport);
        	
        	if (!editTransaction.isTransfer)
        	{
            	Category editCategory = null;
                for(Category category : categories)
                {
                	if (category.id == editTransaction.category)
                	{
                		editCategory = category;
                		break;
                	}
                }
                
                if (!editCategory.income)
                	editTransaction.value *= -1.0;

                spnType.setSelection(editCategory.income ? TYPE_INCOME : TYPE_EXPENSE);
        		PopulateCategories();
                
                for(String name : categoryNames)
                {
                	if (name.equals(editCategory.name))
                	{
                		spnCategory.setSelection(categoryNames.indexOf(name));
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
                	editTransaction.value *= -1.0; //ensure that the value is positive
                
                spnTransferAccount.setEnabled(isTransferFrom);
                
                int accountPosition = (new ArrayList<String>(accountsMap.keySet())).indexOf(editTransaction.getRelatedTransferTransaction(this).getAccount(this).name);
                spnTransferAccount.setSelection(accountPosition);
        	}

        	txtValue.setText(String.valueOf(editTransaction.value));
        	
        	//gah! - change this! do this properly.
        	String str = txtValue.getText().toString();
        	if (str.contains(".") && str.substring(str.indexOf(".")+1).length() == 1)
        		txtValue.setText(str + 0);
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
    
    public void updateDateButtonText(Date date)
    {
    	try
        {
			btnDate.setText(Misc.formatDate(AddTransactionActivity.this, date));
	    	buttonDate = date;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
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
			
			((AddTransactionActivity)getActivity()).updateDateButtonText(c.getTime());
		}
    }
    
    private void PopulateCategories()
    {
    	setNewCategoryFieldsVisible(false);
    	
    	long selectedTypeIndex = spnType.getSelectedItemId();
    	
    	if (selectedTypeIndex == TYPE_TRANSFER)
    	{
    		txtTransferAccount.setVisibility(View.VISIBLE);
    		spnTransferAccount.setVisibility(View.VISIBLE);
    		chkHideFromReports.setVisibility(View.GONE);
    		txtHideFromReports.setVisibility(View.GONE);
    		txtCategory.setVisibility(View.GONE);
    		spnCategory.setVisibility(View.GONE);
    		return;
    	}
    	else
    	{
    		txtTransferAccount.setVisibility(View.GONE);
    		spnTransferAccount.setVisibility(View.GONE);
    		chkHideFromReports.setVisibility(View.VISIBLE);
    		txtHideFromReports.setVisibility(View.VISIBLE);
    		txtCategory.setVisibility(View.VISIBLE);
    		spnCategory.setVisibility(View.VISIBLE);
    	}
    	
    	boolean isIncome = (selectedTypeIndex == TYPE_INCOME);

    	categoryNames = new ArrayList<String>();
    	for(Category category : categories)
    	{
    		if (category.income == isIncome)
    			categoryNames.add(category.name);
    	}
    	
    	categoryNames.add(ADD_CATEGORY_STRING);
    	
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, categoryNames);

		spnCategory.setAdapter(arrayAdapter);
		
		
		
		//go to last selected category
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AddTransactionActivity.this);
		String lastSelectedCategory = isIncome ? sharedPref.getString("lastIncomeCategory", "") : sharedPref.getString("lastExpenseCategory", "");

		if (!lastSelectedCategory.equals("") && categoryNames.contains(lastSelectedCategory))
			spnCategory.setSelection(categoryNames.indexOf(lastSelectedCategory));
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
	    switch (item.getItemId())
	    {
	    	case R.id.menu_accepttransaction:{
	    		OkClicked();
	    		break;
	    		}

	    	case R.id.menu_rejecttransaction:{
	    		CancelClicked();
	    		break;
	    		}	    	
	    	
	        case R.id.menu_settings:{
                break;
            	}

	        case android.R.id.home: // up button
	            Intent intent = new Intent(this, TransactionsActivity.class);
	            if (accountID != -1)
	            	intent.putExtra("AccountID", accountID);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	    }
	    return true;
	}
    
    private boolean Validate()
    {
    	if (txtValue.getText().toString().trim().equals(""))
    	{
    		Toast.makeText(AddTransactionActivity.this, "Please enter a value.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if (txtDesc.getText().toString().trim().equals(""))
    	{
    		Toast.makeText(AddTransactionActivity.this, "Please enter a description.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if (txtNewCatName.getVisibility() == View.VISIBLE)
    	{
			boolean isIncome = (spnType.getSelectedItemId() == 1);
    		if (txtNewCatName.getText().toString().trim() == "")
    		{
	    		Toast.makeText(AddTransactionActivity.this, "Please enter a name for the new category.", Toast.LENGTH_SHORT).show();
	    		return false;
    		}
    		
    		for(Category currentCategory : categories)
        	{
        		if (txtNewCatName.getText().toString().trim().equals(currentCategory.name.trim())
					&& currentCategory.income == isIncome)
            	{
            		Toast.makeText(AddTransactionActivity.this, "A category with this name already exists.", Toast.LENGTH_SHORT).show();
            		return false;
            	}
        	}
    	}
    	
    	return true;
    }
    
    private Category getSelectedCategory()
    {
    	Category selectedCategory = null;
    	boolean isIncome = (spnType.getSelectedItemId() == 1);
    	String selectedCategoryName = categoryNames.get(spnCategory.getSelectedItemPosition());
    	for(Category category : categories)
    	{
    		if (category.name.equals(selectedCategoryName) && category.income == isIncome)
    		{
    			selectedCategory = category;
    			break;
    		}
    	}
    	
    	return selectedCategory;
    }
    
    private void OkClicked()
    {
    	if (!Validate())
    		return;
    	
    	boolean creatingNew = false;
    	
    	if (editTransaction == null)
    	{
    		creatingNew = true;
    		editTransaction = new Transaction();
    	}
    	
    	if (spnType.getSelectedItemPosition() != TYPE_TRANSFER)
    	{
    		Category selectedCategory = getSelectedCategory();

        	//create the category, if it is new
        	if (txtNewCatName.getVisibility() == View.VISIBLE)
        	{
        		Random rnd = new Random(System.currentTimeMillis());
        		Category newCategory = new Category();
        		newCategory.name = txtNewCatName.getText().toString().trim();
        		newCategory.income = (spnType.getSelectedItemId() == 1);
        		newCategory.color = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        		DatabaseManager.getInstance(this).AddCategory(newCategory);
        		selectedCategory = newCategory;
        	}
        	
    		//create the transaction object
        	editTransaction.category = selectedCategory.id;
        	editTransaction.value = Double.valueOf(txtValue.getText().toString());
        	editTransaction.description = txtDesc.getText().toString().trim();
        	Calendar c = Calendar.getInstance();
        	c.setTime(buttonDate);
        	editTransaction.dateTime = c.getTime();
    		editTransaction.account = accountID;
    		editTransaction.dontReport = chkHideFromReports.isChecked();
    		
    		if (!selectedCategory.income)
    			editTransaction.value *= -1.0f;
    		
    		if (creatingNew)
    			DatabaseManager.getInstance(AddTransactionActivity.this).InsertTransaction(editTransaction);
    		else
    			DatabaseManager.getInstance(AddTransactionActivity.this).UpdateTransaction(editTransaction);
    		
    		//save selections to preference, for loading these by default next time
    		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AddTransactionActivity.this);
    		Editor editor = sharedPref.edit();
    		
    		if (selectedCategory.income)
    			editor.putString("lastIncomeCategory", selectedCategory.name);
    		else
    			editor.putString("lastExpenseCategory", selectedCategory.name);
    					
    		editor.commit();
    	}
    	else
    	{
    		Transaction fromTransaction, toTransaction;
    		if (creatingNew)
    		{
    			fromTransaction = editTransaction;
        		toTransaction = new Transaction();
    		}
    		else
    		{
    			fromTransaction = editTransaction.isReceivingParty() 
    					? editTransaction.getRelatedTransferTransaction(this) 
						: editTransaction;
        		toTransaction = editTransaction.isReceivingParty() 
    					? editTransaction
						: editTransaction.getRelatedTransferTransaction(this);
    		}
    		
    		//transfer from transaction
    		fromTransaction.value = Double.valueOf(txtValue.getText().toString()) * -1.0f;
    		fromTransaction.description = txtDesc.getText().toString().trim();
        	Calendar c = Calendar.getInstance();
        	c.setTime(buttonDate);
        	fromTransaction.dateTime = c.getTime();
        	fromTransaction.account = accountID;
        	fromTransaction.dontReport = true;
        	fromTransaction.transferFromTransaction = -1;
        	fromTransaction.isTransfer = true;

    		//transfer to transaction
        	toTransaction.value = Double.valueOf(txtValue.getText().toString());
        	toTransaction.description = txtDesc.getText().toString().trim();
        	toTransaction.dateTime = c.getTime();
        	toTransaction.account = accountsMap.get(spnTransferAccount.getSelectedItem()).id;
        	toTransaction.dontReport = true;
    		toTransaction.transferToTransaction = -1;
    		toTransaction.isTransfer = true;
    		
    		if (creatingNew)
    		{
    			DatabaseManager.getInstance(AddTransactionActivity.this).InsertTransaction(fromTransaction);
    			DatabaseManager.getInstance(AddTransactionActivity.this).InsertTransaction(toTransaction);

    			fromTransaction.transferToTransaction = toTransaction.id;
    			toTransaction.transferFromTransaction = fromTransaction.id;
    			DatabaseManager.getInstance(AddTransactionActivity.this).UpdateTransaction(fromTransaction);
    			DatabaseManager.getInstance(AddTransactionActivity.this).UpdateTransaction(toTransaction);
    		}
    		else
    		{
    			DatabaseManager.getInstance(AddTransactionActivity.this).UpdateTransaction(fromTransaction);
    			DatabaseManager.getInstance(AddTransactionActivity.this).UpdateTransaction(toTransaction);
    		}
    	}

        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void CancelClicked()
    {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }
}
