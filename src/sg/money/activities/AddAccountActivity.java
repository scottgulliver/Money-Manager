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
import sg.money.utils.Misc;
import android.util.*;

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

        model = new AddAccountModel(account);
        model.addListener(this);
		
		controller = new AddAccountController(this, model);
		

        txtStartingBalance.setVisibility(model.isNewAccount() ? View.VISIBLE : View.GONE);
        textView2.setVisibility(model.isNewAccount() ? View.VISIBLE : View.GONE);

    	setTitle(model.isNewAccount() ? "Add Account" : "Edit Account");
    	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
						Log.i("sg.money", Log.getStackTraceString(new Throwable()));
                        if (Misc.stringNullEmptyOrWhitespace(txtStartingBalance.getText().toString()))
                            txtStartingBalance.setText("0");

						controller.onStartingBalanceChange(Double.parseDouble(txtStartingBalance.getText().toString()));
					}
				}			
			});

        updateUi();
    }
	
	@Override
	public void onChange(AddAccountModel model)
	{
		runOnUiThread(new Runnable() {
            public void run() {
                updateUi();
            }
        });
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
		cancelFocus();
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return controller.onOptionsItemSelected(item);
    }

    public void cancelFocus()
    {
        txtName.clearFocus();
        txtStartingBalance.clearFocus();
    }
}
