package sg.money.activities;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.content.DialogInterface.OnClickListener;
import android.content.*;

import sg.money.DatabaseManager;
import sg.money.utils.DialogButtons;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.domainobjects.Transaction;

public class SettingsActivity extends SherlockPreferenceActivity 
{
	CheckBoxPreference usePinProtectionPreference;
	CheckBoxPreference useReconcilePreference;
	EditTextPreference pinNumberPreference;
	Preference enablePinPref;
	Preference changePinPref;
	Preference disablePinPref;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// check deprecation?
		enablePinPref = findPreference(resString(R.string.pref_enablepin_key));
		changePinPref = findPreference(resString(R.string.pref_changepin_key));
		disablePinPref = findPreference(resString(R.string.pref_disablepin_key));
		useReconcilePreference = (CheckBoxPreference)findPreference(resString(R.string.pref_usereconcile_key));

		conditionPreferences();

		enablePinPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(SettingsActivity.this, PinEntryActivity.class);
						intent.putExtra(PinEntryActivity.PINENTRYTYPE,
								PinEntryActivity.PINENTRYTYPE_CREATE);
						startActivityForResult(intent, 0);
						return false;
					}
				});

		changePinPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(SettingsActivity.this,
								PinEntryActivity.class);
						intent.putExtra(PinEntryActivity.PINENTRYTYPE,
								PinEntryActivity.PINENTRYTYPE_CHANGE);
						startActivityForResult(intent, 0);
						return false;
					}
				});

		disablePinPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(SettingsActivity.this,
								PinEntryActivity.class);
						intent.putExtra(PinEntryActivity.PINENTRYTYPE,
								PinEntryActivity.PINENTRYTYPE_REMOVE);
						startActivityForResult(intent, 0);
						return false;
					}
				});
				
		useReconcilePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
			{

				public boolean onPreferenceChange(Preference p1, Object p2)
				{
					boolean setting = (Boolean)p2;
					if (setting)
						showReconcileTransactionsDialog();
					return true;
				}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0) {
			conditionPreferences();
		}
	}

	private void conditionPreferences() {
		enablePinPref.setEnabled(!PinEntryActivity.PinIsSet(SettingsActivity.this));
		changePinPref.setEnabled(PinEntryActivity.PinIsSet(SettingsActivity.this));
		disablePinPref.setEnabled(PinEntryActivity.PinIsSet(SettingsActivity.this));
	}

	private String resString(int resId) {
		return getResources().getString(resId);
	}

	private void showReconcileTransactionsDialog()
	{
		Misc.showConfirmationDialog(this,
                "Mark all existing transactions as reconciled?",
                DialogButtons.YesNo,
                new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        for (Transaction transaction : DatabaseManager.getInstance(SettingsActivity.this).GetAllTransactions()) {
                            if (!transaction.reconciled) {
                                transaction.reconciled = true;
                                DatabaseManager.getInstance(SettingsActivity.this).UpdateTransaction(transaction);
                            }
                        }
                    }
                });
	}
	
}
