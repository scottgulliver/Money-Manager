package sg.money;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.TypedValue;

public class Misc
{	
	public static String formatValue(Activity activity, double value)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String currencyCode = sharedPref.getString(activity.getString(R.string.pref_currency_key), "GBP");
		boolean useBrackets = sharedPref.getBoolean(activity.getString(R.string.pref_negativebrackets_key), false);
		
		//put value into a raw formatted value string
		String formattedValue = String.format(Locale.ENGLISH, "%.2f", value);
		boolean isNegative = (value < 0);
		
		//remove any negative chars - these go before the symbol
		if (isNegative)
			formattedValue = formattedValue.replace("-", "");
		
		if (currencyCode.equals("USD"))
			formattedValue = "$" + formattedValue;
		else if (currencyCode.equals("CAD"))
			formattedValue = "$" + formattedValue;
		else if (currencyCode.equals("EUR"))
			formattedValue = "€" + formattedValue;
		else if (currencyCode.equals("GBP"))
			formattedValue = "£" + formattedValue;
		else if (currencyCode.equals("CHF"))
			formattedValue = "Fr " + formattedValue;
		else if (currencyCode.equals("NZD"))
			formattedValue = "$" + formattedValue;
		else if (currencyCode.equals("AUD"))
			formattedValue = "$" + formattedValue;
		else if (currencyCode.equals("JPY"))
			formattedValue = "¥" + formattedValue;
		else if (currencyCode.equals("KRW"))
			formattedValue = "W" + formattedValue;
		else if (currencyCode.equals("BRL"))
			formattedValue = "R$" + formattedValue;
		else if (currencyCode.equals("TWD"))
			formattedValue = "$" + formattedValue;
		else if (currencyCode.equals("INR"))
			formattedValue = "p" + formattedValue;
		else if (currencyCode.equals("VND"))
			formattedValue = "d" + formattedValue;
		
		//add a negative back on if required
		if (isNegative)
		{
			if (useBrackets)
				formattedValue = "(" + formattedValue + ")";
			else
				formattedValue = "-" + formattedValue;
		}
		
		return formattedValue;
	}
	
	public static String formatDate(Activity activity, Date date) throws Exception
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String datePref = sharedPref.getString(activity.getString(R.string.pref_dateformat_key), "dd/mm/yy");
		
		SimpleDateFormat dateFormat;
		if (datePref.equals("dd/mm/yy"))
			dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
		else if (datePref.equals("mm/dd/yy"))
			dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH);
		else
			throw new Exception("Unsupported date format - " + datePref);
    	
    	return dateFormat.format(date);
	}
    
    static float dipsToPixels(Resources resources, float dipValue)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
    }
    
    static void showConfirmationDialog(Context context, String message, OnClickListener okClick)
    {
    	showConfirmationDialog(context, message, okClick, 
    			new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
    }
    
    static void showConfirmationDialog(Context context, String message, OnClickListener okClick, OnClickListener cancelClick)
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setMessage(message);
	    builder.setPositiveButton("OK", okClick);
	    builder.setNegativeButton("Cancel", cancelClick);
	    builder.create().show();
    }
}