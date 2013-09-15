package sg.money.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import sg.money.R;
import sg.money.activities.PinEntryActivity;

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment
{
	CheckBoxPreference usePinProtectionPreference;
	EditTextPreference pinNumberPreference;
	Preference enablePinPref;
	Preference changePinPref;
	Preference disablePinPref;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        enablePinPref = (Preference)findPreference(resString(R.string.pref_enablepin_key));
        changePinPref = (Preference)findPreference(resString(R.string.pref_changepin_key));
        disablePinPref = (Preference)findPreference(resString(R.string.pref_disablepin_key));
        
        conditionPreferences();
        
        enablePinPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), PinEntryActivity.class);
				intent.putExtra(PinEntryActivity.PINENTRYTYPE, PinEntryActivity.PINENTRYTYPE_CREATE);
				startActivityForResult(intent, 0);
				return false;
			}
		});
        
        changePinPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), PinEntryActivity.class);
				intent.putExtra(PinEntryActivity.PINENTRYTYPE, PinEntryActivity.PINENTRYTYPE_CHANGE);
				startActivityForResult(intent, 0);
				return false;
			}
		});
        
        disablePinPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), PinEntryActivity.class);
				intent.putExtra(PinEntryActivity.PINENTRYTYPE, PinEntryActivity.PINENTRYTYPE_REMOVE);
				startActivityForResult(intent, 0);
				return false;
			}
		});
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == 0)
    	{
    		conditionPreferences();
    	}
    }
    
    private void conditionPreferences()
    {
        enablePinPref.setEnabled(!PinEntryActivity.PinIsSet(getActivity()));
        changePinPref.setEnabled(PinEntryActivity.PinIsSet(getActivity()));
        disablePinPref.setEnabled(PinEntryActivity.PinIsSet(getActivity()));
    }
    
    private String resString(int resId)
    {
    	return getResources().getString(resId);
    }
}
