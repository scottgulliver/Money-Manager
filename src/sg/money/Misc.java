package sg.money;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.TypedValue;

public class Misc
{	
	public static String formatValue(Activity activity, double value)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String currencyCode = sharedPref.getString(activity.getString(R.string.pref_currency_key), "GBP");
		
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
			formattedValue = "-" + formattedValue;
		
		return formattedValue;
	}
    
    static float dipsToPixels(Resources resources, float dipValue)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
    }
}
