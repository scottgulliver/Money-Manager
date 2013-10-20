package sg.money.activities;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import sg.money.domainobjects.Account;
import sg.money.common.DatabaseManager;
import sg.money.R;
import sg.money.models.AddAccountModel;
import sg.money.models.OnChangeListener;
import android.view.View.*;

import sg.money.controllers.*;
import sg.money.utils.Misc;

public class AddAccountActivity extends BaseActivity implements OnChangeListener<AddAccountModel>
{
    private final String SIS_KEY_MODEL = "Model";

	private View m_baseView;
    private EditText m_txtName;
    private EditText m_txtStartingBalance;
    private TextView m_tvStartingBalance;
    private AddAccountModel m_model;
	private AddAccountController m_controller;
	
	
	/* Activity Overrides */
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_account);

		m_baseView = findViewById(R.id.addactivitybase);
        m_txtName = (EditText)findViewById(R.id.txtName);
        m_txtStartingBalance = (EditText)findViewById(R.id.txtStartBalance);
        m_tvStartingBalance = (TextView)findViewById(R.id.textView2);

        m_txtName = (EditText)findViewById(R.id.txtName);
        m_txtStartingBalance = (EditText)findViewById(R.id.txtStartBalance);
        m_tvStartingBalance = (TextView)findViewById(R.id.textView2);

        if (savedInstanceState != null)
        {
            m_model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
        }
        else
        {
            Account account = null;
            int editId = getIntent().getIntExtra("ID", -1);
            if (editId != -1)
            {
                account = DatabaseManager.getInstance(AddAccountActivity.this).GetAccount(editId);
            }

            m_model = new AddAccountModel(account);
        }

        m_model.addListener(this);
		m_controller = new AddAccountController(this, m_model);
		
        m_txtStartingBalance.setVisibility(m_model.isNewAccount() ? View.VISIBLE : View.GONE);
        m_tvStartingBalance.setVisibility(m_model.isNewAccount() ? View.VISIBLE : View.GONE);

    	setTitle(m_model.isNewAccount() ? "Add Account" : "Edit Account");
    	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        m_txtStartingBalance.setText("0.00");
		
		m_txtName.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
						m_controller.onAccountNameChange(m_txtName.getText().toString());
					}
				}			
			});

		m_txtStartingBalance.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
                        if (Misc.stringNullEmptyOrWhitespace(m_txtStartingBalance.getText().toString()))
                            m_txtStartingBalance.setText("0");

						m_controller.onStartingBalanceChange(Double.parseDouble(m_txtStartingBalance.getText().toString()));
					}
				}			
			});

        updateUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_account, menu);
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return m_controller.onOptionsItemSelected(item);
    }
	
	
	/* Methods */
	
	private void updateUi()
	{
		m_txtName.setText(m_model.getAccountName());
		m_txtStartingBalance.setText(String.valueOf(m_model.getStartingBalance()));
	}

    public void cancelFocus()
    {
        m_txtName.clearFocus();
        m_txtStartingBalance.clearFocus();
    }


	/* Implementation of OnChangeListener */

	@Override
	public void onChange(AddAccountModel model)
	{
		runOnUiThread(new Runnable() {
				public void run() {
					updateUi();
				}
			});
	}
}
