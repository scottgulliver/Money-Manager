package sg.money.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
* Provides a common way to access preferences.
*/
public class Settings {

    static final String SETTING_DEFAULTACCOUNTID = "SETTING_DEFAULTACCOUNTID";
    static final String SETTING_FIRSTTIMEDRAWEROPENED = "SETTING_FIRSTTIMEDRAWEROPENED";

	/**
	 * Sets the default account to open in the transactions view.
	 */
    public static void setDefaultAccount(Context context, int accountId)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SETTING_DEFAULTACCOUNTID, accountId);
        editor.commit();
    }

	/**
	 * Gets the default account to open in the transactions view.
	 */
    public static int getDefaultAccount(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SETTING_DEFAULTACCOUNTID, -1);
    }

	/**
	 * Sets whether the navigation drawer has been shown to the user yet.
	 */
    public static void setFirstTimeDrawerOpened(Context context, boolean opened)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SETTING_FIRSTTIMEDRAWEROPENED, opened);
        editor.commit();
    }

	/**
	 * Sets whether the navigation drawer has been shown to the user yet.
	 */
    public static boolean getFirstTimeDrawerOpened(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_FIRSTTIMEDRAWEROPENED, false);
    }
}
