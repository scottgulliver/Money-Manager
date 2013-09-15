package sg.money.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    static final String SETTING_DEFAULTACCOUNTID = "SETTING_DEFAULTACCOUNTID";
    static final String SETTING_FIRSTTIMEDRAWEROPENED = "SETTING_FIRSTTIMEDRAWEROPENED";

    public static void setDefaultAccount(Context context, int accountId)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SETTING_DEFAULTACCOUNTID, accountId);
        editor.commit();
    }

    public static int getDefaultAccount(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(SETTING_DEFAULTACCOUNTID, -1);
    }

    public static void setFirstTimeDrawerOpened(Context context, boolean opened)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SETTING_FIRSTTIMEDRAWEROPENED, opened);
        editor.commit();
    }

    public static boolean getFirstTimeDrawerOpened(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_FIRSTTIMEDRAWEROPENED, false);
    }
}
