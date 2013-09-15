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
	
	TextView txtTitle;
	EditText txtPinNumber;
	Button okButton;
	Button cancelButton;
	
	int pinEntryType;
	String currentPinNumber;
	String initialEnterText;
	boolean enteringNewPin = false;
	int iteration;
	
	public static boolean PinIsSet(Context context)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return !sharedPref.getString(context.getResources().getString(R.string.pref_pinnumber_key), "").equals("");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		pinEntryType = getIntent().getExtras() != null ? getIntent().getExtras().getInt(PINENTRYTYPE, PINENTRYTYPE_ENTER) : PINENTRYTYPE_ENTER;
		if (pinEntryType == PINENTRYTYPE_ENTER && !PinIsSet(PinEntryActivity.this))
		{
			Intent newIntent = new Intent(PinEntryActivity.this, ParentActivity.class);
			startActivity(newIntent);
			finish();
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pin_entry);
		
		txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtPinNumber = (EditText)findViewById(R.id.txtPinNumber);
		okButton = (Button)findViewById(R.id.ok_button);
		cancelButton = (Button)findViewById(R.id.cancel_button);
		
		txtPinNumber.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			public void afterTextChanged(Editable s) {
				pinNumberTextChanged();
			}
		});
		
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				okClicked();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cancelClicked();
			}
		});
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		currentPinNumber = sharedPref.getString(getString(R.string.pref_pinnumber_key), "");

		switch(pinEntryType)
		{
			case PINENTRYTYPE_CREATE:
				initialEnterText = "Enter a PIN";
				enteringNewPin = true;
				break;
			case PINENTRYTYPE_ENTER:
				initialEnterText = "Enter your PIN";
				break;
			case PINENTRYTYPE_CHANGE:
				initialEnterText = "Enter your current PIN";
				break;
			case PINENTRYTYPE_REMOVE:
				initialEnterText = "Enter your PIN";
			break;
			default:
		        finish();
		        break;
		}
		
		txtTitle.setText(initialEnterText);
		
		okButton.setEnabled(false);
		iteration = 0;
	}
	
	/**
	 * This method sets the UI back into a state where it appears new again
	 */
	private void softReset()
	{
		txtTitle.setText(initialEnterText);
		txtPinNumber.setText("");
		okButton.setEnabled(false);
	}

	private void pinNumberTextChanged()
	{
		int length = txtPinNumber.getText().length();
		if (length == 0 && enteringNewPin)
		{
			txtTitle.setText(initialEnterText);
		}
		else if (length < 4)
		{	
			if (okButton.isEnabled())
				okButton.setEnabled(false);
			
			if (enteringNewPin)
				txtTitle.setText("PIN must contain at least 4 digits");
		}
		else
		{
			if (!okButton.isEnabled())
				okButton.setEnabled(true);
			
			if (enteringNewPin)
				txtTitle.setText("Tap Continue when finished");
		}
	}
	
	/**
	 * Change the 'state' of the UI. This code is AWFUL I know..
	 * Please don't be too harsh at the code mocking (http://dilbert.com/strips/comic/2013-02-24/)
	 */
	private void okClicked()
	{
		switch(pinEntryType)
		{
			case PINENTRYTYPE_CREATE:
				if (iteration == 0)
				{ // entered once, so confirm
					currentPinNumber = txtPinNumber.getText().toString();
					iteration++;
					initialEnterText = "Confirm PIN";
					enteringNewPin = false;
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
				if (iteration == 0)
				{
					if (checkPinsMatch(false))
					{
						pinEntryType = PINENTRYTYPE_CREATE;
						iteration = 0;
						initialEnterText = "Enter new PIN";
						enteringNewPin = true;
						softReset();
					}
				}
				break;
			case PINENTRYTYPE_REMOVE:
				checkPinsMatch(true);
				break;
			default:
		        finish(); // no idea why we are here..
		        break;
		}
	}
	
	private boolean checkPinsMatch(boolean exitWhenCorrect)
	{
		if (txtPinNumber.getText().toString().equals(currentPinNumber)) // :D YAY!
		{
			if (exitWhenCorrect)
			{
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
				Editor editor = sharedPref.edit();
				
				switch(pinEntryType)
				{
					case PINENTRYTYPE_CREATE:
					case PINENTRYTYPE_CHANGE:
						editor.putString(getString(R.string.pref_pinnumber_key), txtPinNumber.getText().toString());
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
			txtPinNumber.setText("");
			initialEnterText = "PIN was incorrect";
			softReset();
			return false;
		}
	}
	
	private void cancelClicked()
	{
        finish();
	}
}
