package sg.money;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class AddAccountActivity extends Activity
{    
    EditText txtName;
    Account editAccount;
    ArrayList<Account> currentAccounts;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_acount);
        
        txtName = (EditText)findViewById(R.id.txtName);
        
        //check if we are editing
        editAccount = null;
        int editId = getIntent().getIntExtra("ID", -1);
        if (editId != -1)
        {
        	editAccount = DatabaseManager.getInstance(AddAccountActivity.this).GetAccount(editId);
        	txtName.setText(editAccount.name);
        	setTitle("Edit Account");
        }
        
        currentAccounts = DatabaseManager.getInstance(this).GetAllAccounts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_account, menu);
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
	    }
	    return true;
	}
    
    private boolean Validate()
    {
    	if (txtName.getText().toString().trim().isEmpty())
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
