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
import android.view.View.*;
import android.location.*;
import sg.money.controllers.*;

public class AddAccountActivity extends BaseActivity implements OnChangeListener<AddAccountModel>
{
	View baseView;
    EditText txtName;
    EditText txtStartingBalance;
    TextView textView2;
    private AddAccountModel model;
	private AddAccountController controller;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_account);

		baseView = findViewById(R.id.addactivitybase);
        txtName = (EditText)findViewById(R.id.txtName);
        txtStartingBalance = (EditText)findViewById(R.id.txtStartBalance);
        textView2 = (TextView)findViewById(R.id.textView2);

        txtName = (EditText)findViewById(R.id.txtName);
        txtStartingBalance = (EditText)findViewById(R.id.txtStartBalance);
        textView2 = (TextView)findViewById(R.id.textView2);

        Account account = null;
        int editId = getIntent().getIntExtra("ID", -1);
        if (editId != -1)
        {
            account = DatabaseManager.getInstance(AddAccountActivity.this).GetAccount(editId);
        }

        model = new AddAccountModel();
        model.addListener(this);
		
		controller = new AddAccountController(this, model);
		

        txtStartingBalance.setVisibility(model.isNewAccount() ? View.VISIBLE : View.GONE);
        textView2.setVisibility(model.isNewAccount() ? View.VISIBLE : View.GONE);

    	setTitle(model.isNewAccount() ? "Add Account" : "Edit Account");
    	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
<<<<<<< HEAD
        
=======

>>>>>>> branch 'Lexington' of https://sgulliver@bitbucket.org/pennypresssoftware/money-manager.git
        txtStartingBalance.setText("0.00");
		
		txtName.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
						controller.onAccountNameChange(txtName.getText().toString());
					}
				}			
			});

		txtStartingBalance.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
						controller.onStartingBalanceChange(Double.parseDouble(txtStartingBalance.getText().toString()));
					}
				}			
			});
    }
	
	@Override
	public void onChange(AddAccountModel model)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				updateUi();
			}
		});
	}

	private void updateModel()
	{
		model.setAccountName(txtName.getText().toString());
		model.setStartingBalance(Double.parseDouble(txtStartingBalance.getText().toString()));
	}
	
	private void updateUi()
	{
		txtName.setText(model.getAccountName());
		txtStartingBalance.setText(String.valueOf(model.getStartingBalance()));
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
    
    private void OkClicked()
    {
		cancelFocus();
		
		String validationError = model.validate(AddAccountActivity.this);
    	if (validationError != null)
		{
    		Toast.makeText(AddAccountActivity.this, validationError, Toast.LENGTH_SHORT).show();
		}
		else
		{
			model.commit(AddAccountActivity.this);
			
			setResult(RESULT_OK, new Intent());
			finish();
		}
    }
	
	private void cancelFocus()
	{
		baseView.requestFocus();
	}

    private void CancelClicked()
    {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }
}
