package sg.money.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import sg.money.R;

public class PinEntryActivity extends BaseActivity
{
	public static final int PINENTRYTYPE_CREATE = 1;
	public static final int PINENTRYTYPE_ENTER = 2;
	public static final int PINENTRYTYPE_CHANGE = 3;
	public static final int PINENTRYTYPE_REMOVE = 4;
	public static final String PINENTRYTYPE = "PINENTRYTYPE";
	public static final int RESULT_PINOK = 1001;
	
	private TextView m_txtTitle;
	private EditText m_txtPinNumber;
	private Button m_okButton;
	private Button m_cancelButton;
	private int m_pinEntryType;
	private String m_currentPinNumber;
	private String m_initialEnterText;
	private boolean m_enteringNewPin;
	private int m_iteration;
	
	
	/* Activity overrides */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		m_enteringNewPin = false;
		
		m_pinEntryType = getIntent().getExtras() != null ? getIntent().getExtras().getInt(PINENTRYTYPE, PINENTRYTYPE_ENTER) : PINENTRYTYPE_ENTER;
		if (m_pinEntryType == PINENTRYTYPE_ENTER && !PinIsSet(PinEntryActivity.this))
		{
			Intent newIntent = new Intent(PinEntryActivity.this, ParentActivity.class);
			startActivity(newIntent);
			finish();
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pin_entry);
		
		m_txtTitle = (TextView)findViewById(R.id.txtTitle);
		m_txtPinNumber = (EditText)findViewById(R.id.txtPinNumber);
		m_okButton = (Button)findViewById(R.id.ok_button);
		m_cancelButton = (Button)findViewById(R.id.cancel_button);
		
		m_txtPinNumber.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			public void afterTextChanged(Editable s) {
				pinNumberTextChanged();
			}
		});
		
		m_okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				okClicked();
			}
		});
		
		m_cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cancelClicked();
			}
		});
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		m_currentPinNumber = sharedPref.getString(getString(R.string.pref_pinnumber_key), "");

		switch(m_pinEntryType)
		{
			case PINENTRYTYPE_CREATE:
				m_initialEnterText = "Enter a PIN";
				m_enteringNewPin = true;
				break;
			case PINENTRYTYPE_ENTER:
				m_initialEnterText = "Enter your PIN";
				break;
			case PINENTRYTYPE_CHANGE:
				m_initialEnterText = "Enter your current PIN";
				break;
			case PINENTRYTYPE_REMOVE:
				m_initialEnterText = "Enter your PIN";
			break;
			default:
		        finish();
		        break;
		}
		
		m_txtTitle.setText(m_initialEnterText);
		
		m_okButton.setEnabled(false);
		m_iteration = 0;
	}
	
	
	/* Static methods */

	public static boolean PinIsSet(Context context)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return !sharedPref.getString(context.getResources().getString(R.string.pref_pinnumber_key), "").equals("");
	}
	
	
	/* Other methods */
	
	/**
	 * This method sets the UI back into a state where it appears new again
	 */
	private void softReset()
	{
		m_txtTitle.setText(m_initialEnterText);
		m_txtPinNumber.setText("");
		m_okButton.setEnabled(false);
	}

	private void pinNumberTextChanged()
	{
		int length = m_txtPinNumber.getText().length();
		if (length == 0 && m_enteringNewPin)
		{
			m_txtTitle.setText(m_initialEnterText);
		}
		else if (length < 4)
		{	
			if (m_okButton.isEnabled())
				m_okButton.setEnabled(false);
			
			if (m_enteringNewPin)
				m_txtTitle.setText("PIN must contain at least 4 digits");
		}
		else
		{
			if (!m_okButton.isEnabled())
				m_okButton.setEnabled(true);
			
			if (m_enteringNewPin)
				m_txtTitle.setText("Tap Continue when finished");
		}
	}
	
	/**
	 * Change the 'state' of the UI. This code is AWFUL I know..
	 * Please don't be too harsh at the code mocking (http://dilbert.com/strips/comic/2013-02-24/)
	 */
	private void okClicked()
	{
		switch(m_pinEntryType)
		{
			case PINENTRYTYPE_CREATE:
				if (m_iteration == 0)
				{ // entered once, so confirm
					m_currentPinNumber = m_txtPinNumber.getText().toString();
					m_iteration++;
					m_initialEnterText = "Confirm PIN";
					m_enteringNewPin = false;
					softReset();
				}
				else
				{
					checkPinsMatch(true);
				}
				break;
			case PINENTRYTYPE_ENTER:
				checkPinsMatch(true);
				break;
			case PINENTRYTYPE_CHANGE:
				if (m_iteration == 0)
				{
					if (checkPinsMatch(false))
					{
						m_pinEntryType = PINENTRYTYPE_CREATE;
						m_iteration = 0;
						m_initialEnterText = "Enter new PIN";
						m_enteringNewPin = true;
						softReset();
					}
				}
				break;
			case PINENTRYTYPE_REMOVE:
				checkPinsMatch(true);
				break;
			default:
		        finish();
		        break;
		}
	}
	
	private boolean checkPinsMatch(boolean exitWhenCorrect)
	{
		if (m_txtPinNumber.getText().toString().equals(m_currentPinNumber))
		{
			if (exitWhenCorrect)
			{
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
				Editor editor = sharedPref.edit();
				
				switch(m_pinEntryType)
				{
					case PINENTRYTYPE_CREATE:
					case PINENTRYTYPE_CHANGE:
						editor.putString(getString(R.string.pref_pinnumber_key), m_txtPinNumber.getText().toString());
						editor.commit();
						break;
					case PINENTRYTYPE_REMOVE:
						editor.putString(getString(R.string.pref_pinnumber_key), "");
						editor.commit();
						break;
					case PINENTRYTYPE_ENTER:
						Intent newIntent = new Intent(PinEntryActivity.this, ParentActivity.class);
						startActivity(newIntent);
						break;
				}
				
				setResult(RESULT_PINOK);
		        finish();
			}
			return true;
		}
		else
		{ 
			m_txtPinNumber.setText("");
			m_initialEnterText = "PIN was incorrect";
			softReset();
			return false;
		}
	}
	
	private void cancelClicked()
	{
        finish();
	}
}
