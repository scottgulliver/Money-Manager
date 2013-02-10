package sg.money;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddBudgetActivity extends Activity
{
	ArrayList<Budget> currentBudgets;
	ArrayList<Category> currentCategories;
	ArrayList<Account> currentAccounts;

	ArrayList<Category> selectedCategories;
	ArrayList<Account> selectedAccounts;
	
	EditText txtName;
	EditText txtValue;
	Button btnCategories;
	Button btnAccounts;
	
	Budget editBudget;
	
	//todo change these - just horrible!
	int viewingDialog = 0;
	static final int ACCOUNTSLIST = 1;
	static final int CATEGORIESLIST = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_budget);
        
		txtName = (EditText)findViewById(R.id.txtName);
		txtValue = (EditText)findViewById(R.id.txtValue);
		btnCategories = (Button)findViewById(R.id.btnCategories);
      	btnAccounts = (Button)findViewById(R.id.btnAccounts);
      
      	btnCategories.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) { CategoriesClicked(); } });
      
      	btnAccounts.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) { AccountsClicked(); } });
      	
      	currentBudgets = DatabaseManager.getInstance(this).GetAllBudgets();
      	currentCategories = new ArrayList<Category>();
      	ArrayList<Category> allCategories = DatabaseManager.getInstance(this).GetAllCategories();
      	for(Category category : allCategories)
      	{
      		if (!category.income)
      			currentCategories.add(category);
      	}
      	currentAccounts = DatabaseManager.getInstance(this).GetAllAccounts();
      	selectedCategories = new ArrayList<Category>();
    	selectedAccounts = new ArrayList<Account>();
    	
      	//check if we are editing
      	editBudget = null;
      	int editId = getIntent().getIntExtra("ID", -1);
      	if (editId != -1)
      	{	
      		editBudget = DatabaseManager.getInstance(AddBudgetActivity.this).GetBudget(editId);
      		txtName.setText(editBudget.name);
      		txtValue.setText(String.valueOf(editBudget.value));
      		selectedCategories = editBudget.categories;
      		selectedAccounts = editBudget.accounts;
      		
      		this.setTitle("Edit Budget");
      	}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_add_budget, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	    	case R.id.menu_acceptbudget:{
	    		OkClicked();
	    		break;
	    		}

	    	case R.id.menu_rejectbudget:{
	    		CancelClicked();
	    		break;
	    		}	    	
	    	
	        case R.id.menu_settings:{
                break;
            	}
	    }
	    return true;
	}
    
    private void CategoriesClicked()
    {
    	boolean[] checkedItems = new boolean[currentCategories.size()+1];
    	checkedItems[0] = selectedCategories.size() == 0;
    	for(int i = 0; i < currentCategories.size(); i++)
    	{
    		boolean alreadySelected = false;
    		for(Category selectedCategory : selectedCategories)
    		{
    			if (selectedCategory.id == currentCategories.get(i).id)
    			{
    				alreadySelected = true;
    				break;
    			}
    		}
    		checkedItems[i+1] = alreadySelected;
    	}
    	
    	ArrayList<String> items = new ArrayList<String>();
    	items.add("All Categories");
    	for(Category category : currentCategories)
    		items.add(category.name);

    	viewingDialog = CATEGORIESLIST;
    	createDialog("Categories", items, checkedItems).show();
    }
    
    private void AccountsClicked()
    {
    	boolean[] checkedItems = new boolean[currentAccounts.size()+1];
    	checkedItems[0] = selectedAccounts.size() == 0;
    	for(int i = 0; i < currentAccounts.size(); i++)
    	{
    		boolean alreadySelected = false;
    		for(Account selectedAccount : selectedAccounts)
    		{
    			if (selectedAccount.id == currentAccounts.get(i).id)
    			{
    				alreadySelected = true;
    				break;
    			}
    		}
    		checkedItems[i+1] = alreadySelected;
    	}
    	
    	ArrayList<String> items = new ArrayList<String>();
    	items.add("All Accounts");
    	for(Account account : currentAccounts)
    		items.add(account.name);
    	
    	viewingDialog = ACCOUNTSLIST;
    	createDialog("Accounts", items, checkedItems).show();
    }
    
    private Dialog createDialog(String title, final ArrayList<String> items, final boolean[] checkedItems) {
    	final ArrayList<Object> mSelectedItems = new ArrayList<Object>();  // Where we track the selected items
    	for(int i = 1; i < checkedItems.length; i++)
    	{
    		if (checkedItems[i] == true)
        		mSelectedItems.add(i);
    	}
        AlertDialog.Builder builder = new AlertDialog.Builder(AddBudgetActivity.this);
        // Set the dialog title
        builder.setTitle(title)
        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
               .setMultiChoiceItems(items.toArray(new CharSequence[items.size()]),
            		   				checkedItems,
            		   				new DialogInterface.OnMultiChoiceClickListener() {
                   public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                       if (isChecked) {
                           if (which == 0)
                           {
                        	   mSelectedItems.clear();
                        	   for(int i = 1; i < items.size(); i++)
                        	   {
                        		   checkedItems[i] = false;
                        		   ((AlertDialog)dialog).getListView().setItemChecked(i, false);
                        	   }
                        	   ((BaseAdapter)((AlertDialog)dialog).getListView().getAdapter()).notifyDataSetChanged();
                           }
                           else
                           {
                               mSelectedItems.add(which);
                               if (checkedItems[0] == true)
                        	   {
                        		   checkedItems[0] = false;
                        		   ((AlertDialog)dialog).getListView().setItemChecked(0, false);
                            	   ((BaseAdapter)((AlertDialog)dialog).getListView().getAdapter()).notifyDataSetChanged();
                        	   }
                           }
                       } else if (which == 0) {
                    	   if (mSelectedItems.size() == 0)
                    	   {
                    		   checkedItems[0] = true;
                    		   ((AlertDialog)dialog).getListView().setItemChecked(0, true);
                        	   ((BaseAdapter)((AlertDialog)dialog).getListView().getAdapter()).notifyDataSetChanged();
                    	   }
                       } else if (mSelectedItems.contains(which)) {
                    	   mSelectedItems.remove(Integer.valueOf(which));
                    	   if (mSelectedItems.size() == 0)
                    	   {
                    		   checkedItems[0] = true;
                    		   ((AlertDialog)dialog).getListView().setItemChecked(0, true);
                        	   ((BaseAdapter)((AlertDialog)dialog).getListView().getAdapter()).notifyDataSetChanged();
                    	   }
                       }
                   }
               })
               // Set the action buttons
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
        			   if (viewingDialog == ACCOUNTSLIST)
                		   selectedAccounts.clear();
        			   else if (viewingDialog == CATEGORIESLIST)
                		   selectedCategories.clear();
        				    
            		   for(Object item : mSelectedItems)
            		   {
            			   if (viewingDialog == ACCOUNTSLIST)
                    	   {
                			   for(Account account : currentAccounts)
                			   {
                				   if (account.name.equals(items.get(Integer.parseInt(String.valueOf(item)))))
        						   {
                        			   selectedAccounts.add(account);
                					   break;
        						   }
                			   }
                    	   }
            			   else if (viewingDialog == CATEGORIESLIST)
                    	   {
                			   for(Category category : currentCategories)
                			   {
                				   if (category.name.equals(items.get(Integer.parseInt(String.valueOf(item)))))
        						   {
                        			   selectedCategories.add(category);
                					   break;
        						   }
                			   }
                    	   }
            		   }
               
            		   dialog.cancel();
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.cancel();
                   }
               });

        return builder.create();
    }

    private boolean Validate()
    {
    	if (txtName.getText().toString().trim().isEmpty())
    	{
    		Toast.makeText(AddBudgetActivity.this, "Please enter a name.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if (txtValue.getText().toString().trim().isEmpty())
    	{
    		Toast.makeText(AddBudgetActivity.this, "Please enter a value.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if (Double.parseDouble(txtValue.getText().toString().trim()) < 0)
    	{
    		Toast.makeText(AddBudgetActivity.this, "Please enter a positive budget value.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	for(Budget currentBudget : currentBudgets)
    	{
    		if (editBudget != null && (currentBudget.id == editBudget.id)) continue;
    		
    		if (txtName.getText().toString().trim().equals(currentBudget.name.trim()))
        	{
        		Toast.makeText(AddBudgetActivity.this, "A budget with this name already exists.", Toast.LENGTH_SHORT).show();
        		return false;
        	}
    	}
    	
    	return true;
    }
    
    private void OkClicked()
    {
    	if (!Validate())
    		return;
    	
    	boolean creatingNew = false;
		if (editBudget == null)
    	{
			editBudget = new Budget();
			creatingNew = true;
    	}
		
		//set up values
		editBudget.name = txtName.getText().toString().trim();
		editBudget.value = Double.parseDouble(txtValue.getText().toString().trim());
		editBudget.categories = selectedCategories;
		editBudget.accounts = selectedAccounts;
		
    	//commit to the db
    	if (creatingNew)
			DatabaseManager.getInstance(AddBudgetActivity.this).AddBudget(editBudget);
    	else
			DatabaseManager.getInstance(AddBudgetActivity.this).UpdateBudget(editBudget);
    	
        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void CancelClicked()
    {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }
}
