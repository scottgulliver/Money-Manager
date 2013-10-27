package sg.money.activities;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.content.DialogInterface.OnClickListener;
import android.content.*;
import sg.money.common.DatabaseManager;
import sg.money.utils.DialogButtons;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.domainobjects.Transaction;

public class SettingsActivity extends SherlockPreferenceActivity {

	private Preference m_enablePinPref;
	private Preference m_changePinPref;
	private Preference m_disablePinPref;
	
	
	/* Activity overrides */
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		m_enablePinPref = findPreference(resString(R.string.pref_enablepin_key));
		m_changePinPref = findPreference(resString(R.string.pref_changepin_key));
		m_disablePinPref = findPreference(resString(R.string.pref_disablepin_key));

		conditionPreferences();

		m_enablePinPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(SettingsActivity.this, PinEntryActivity.class);
						intent.putExtra(PinEntryActivity.PINENTRYTYPE,
								PinEntryActivity.PINENTRYTYPE_CREATE);
						startActivityForResult(intent, 0);
						return false;
					}
				});

		m_changePinPref
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

		m_disablePinPref
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
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 0) {
			conditionPreferences();
		}
	}
	
	
	/* Methods */

	private void conditionPreferences() {
		m_enablePinPref.setEnabled(!PinEntryActivity.PinIsSet(SettingsActivity.this));
		m_changePinPref.setEnabled(PinEntryActivity.PinIsSet(SettingsActivity.this));
		m_disablePinPref.setEnabled(PinEntryActivity.PinIsSet(SettingsActivity.this));
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
                            if (!transaction.isReconciled()) {
                                transaction.setReconciled(true);
                                DatabaseManager.getInstance(SettingsActivity.this).UpdateTransaction(transaction);
                            }
                        }
                    }
                });
	}
	
}
