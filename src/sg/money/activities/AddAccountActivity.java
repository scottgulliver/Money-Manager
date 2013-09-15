package sg.money.activities;

import java.util.ArrayList;
import java.util.Calendar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import sg.money.domainobjects.Account;
import sg.money.domainobjects.Category;
import sg.money.DatabaseManager;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.R;
import sg.money.domainobjects.Transaction;
import sg.money.models.AddAccountModel;
import sg.money.models.OnChangeListener;

public class AddAccountActivity extends BaseActivity implements OnChangeListener
{    
    EditText txtName;
    EditText txtStartingBalance;
    TextView textView2;
    private AddAccountModel model;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_account);

        Account account = null;
        int editId = getIntent().getIntExtra("ID", -1);
        if (editId != -1)
        {
            account = DatabaseManager.getInstance(AddAccountActivity.this).GetAccount(editId);
        }

        model = new AddAccountModel();
        model.addListener(this);

        txtStartingBalance.setVisibility(model.isNewAccount() ? View.VISIBLE : View.GONE);
        textView2.setVisibility(model.isNewAccount() ? View.VISIBLE : View.GONE);

    	setTitle(model.isNewAccount() ? "Add Account" : "Edit Account");
    	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        txtName = (EditText)findViewById(R.id.txtName);
        txtStartingBalance = (EditText)findViewById(R.id.txtStartBalance);
        textView2 = (TextView)findViewById(R.id.textView2);
        
        txtStartingBalance.setText("0.00");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_account, menu);
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
	            Intent intent = new Intent(this, ParentActivity.class);
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Accounts);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            break;
	    }
	    return true;
	}
    
    private boolean Validate()
    {
    	if (txtName.getText().toString().trim().equals(""))
    	{
    		Toast.makeText(AddAccountActivity.this, "Please enter a name.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	for(Account currentAccount : currentAccounts)
    	{
    		if (editAccount != null && (currentAccount.id == editAccount.id)) continue;
    		
    		if (txtName.getText().toString().trim().equals(currentAccount.name.trim()))
        	{
        		Toast.makeText(AddAccountActivity.this, "An account with this name already exists.", Toast.LENGTH_SHORT).show();
        		return false;
        	}
    	}
    	
    	return true;
    }
    
    private void OkClicked()
    {
    	if (!Validate())
    		return;
    	
    	if (editAccount == null)
    	{
	    	//create the account
	    	Account newAccount = new Account(txtName.getText().toString().trim());
	    	newAccount.value = 0;
	    	
			DatabaseManager.getInstance(AddAccountActivity.this).AddAccount(newAccount);
			
			double startBalance = Double.valueOf(txtStartingBalance.getText().toString());
			if (startBalance != 0.0)
			{
				Transaction transaction = new Transaction();
				transaction.account = newAccount.id;
				transaction.description = "Starting balance for account";
				transaction.dateTime = Calendar.getInstance().getTime();
				transaction.value = startBalance;
				
				ArrayList<Category> categories = DatabaseManager.getInstance(AddAccountActivity.this).GetAllCategories();
				for(Category category : categories)
				{
					if (category.name.equals("Starting Balance") && category.income == startBalance > 0)
					{
						transaction.category = category.id;
						break;
					}
				}
				
				DatabaseManager.getInstance(AddAccountActivity.this).InsertTransaction(transaction);
			}
    	}
    	else
    	{
	    	//edit the account	    	
    		editAccount.name = txtName.getText().toString().trim();
			DatabaseManager.getInstance(AddAccountActivity.this).UpdateAccount(editAccount);
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
