package sg.money.utils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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

import sg.money.DatabaseManager;
import sg.money.R;
import sg.money.domainobjects.Category;

public class Misc
{	
	public static String formatValue(Activity activity, double value)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
		String currencyCode = sharedPref.getString(activity.getString(R.string.pref_currency_key), "GBP");
		boolean useBrackets = sharedPref.getBoolean(activity.getString(R.string.pref_negativebrackets_key), false);
		
		//put value into a raw formatted value string
		DecimalFormat decimalFormatter = new DecimalFormat("###,##0.00");
	    String formattedValue = decimalFormatter.format(value);
		boolean isNegative = (value < 0);
		
		//remove any negative chars - these go before the symbol
		if (isNegative)
			formattedValue = formattedValue.replace("-", "");

        formattedValue = getCurrencySymbol(currencyCode) + formattedValue;
		
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

    public static String getCurrencySymbol(String currencyCode)
    {
        if (currencyCode.equals("AED")) return "د.إ";
        if (currencyCode.equals("AFN")) return "؋";
        if (currencyCode.equals("ALL")) return "L";
        if (currencyCode.equals("ANG")) return "ƒ";
        if (currencyCode.equals("AOA")) return "Kz";
        if (currencyCode.equals("ARS")) return "$";
        if (currencyCode.equals("AUD")) return "$";
        if (currencyCode.equals("AWG")) return "ƒ";
        if (currencyCode.equals("BAM")) return "KM";
        if (currencyCode.equals("BBD")) return "$";
        if (currencyCode.equals("BDT")) return "৳";
        if (currencyCode.equals("BGN")) return "лв";
        if (currencyCode.equals("BHD")) return ".د.ب";
        if (currencyCode.equals("BIF")) return "Fr";
        if (currencyCode.equals("BMD")) return "$";
        if (currencyCode.equals("BND")) return "$";
        if (currencyCode.equals("BOB")) return "Bs.";
        if (currencyCode.equals("BRL")) return "R$";
        if (currencyCode.equals("BSD")) return "$";
        if (currencyCode.equals("BTN")) return "Nu.";
        if (currencyCode.equals("BWP")) return "P";
        if (currencyCode.equals("BYR")) return "Br";
        if (currencyCode.equals("BZD")) return "$";
        if (currencyCode.equals("CAD")) return "$";
        if (currencyCode.equals("CDF")) return "Fr";
        if (currencyCode.equals("CHF")) return "Fr";
        if (currencyCode.equals("CLP")) return "$";
        if (currencyCode.equals("CNY")) return "¥";
        if (currencyCode.equals("COP")) return "$";
        if (currencyCode.equals("CRC")) return "₡";
        if (currencyCode.equals("CUC")) return "$";
        if (currencyCode.equals("CUP")) return "$";
        if (currencyCode.equals("CVE")) return "$";
        if (currencyCode.equals("CZK")) return "Kč";
        if (currencyCode.equals("DJF")) return "Fr";
        if (currencyCode.equals("DKK")) return "kr";
        if (currencyCode.equals("DOP")) return "$";
        if (currencyCode.equals("DZD")) return "د.ج";
        if (currencyCode.equals("EGP")) return "£";
        if (currencyCode.equals("ERN")) return "Nfk";
        if (currencyCode.equals("ETB")) return "Br";
        if (currencyCode.equals("EUR")) return "€";
        if (currencyCode.equals("FJD")) return "$";
        if (currencyCode.equals("FKP")) return "£";
        if (currencyCode.equals("GBP")) return "£";
        if (currencyCode.equals("GEL")) return "ლ";
        if (currencyCode.equals("GGP")) return "£";
        if (currencyCode.equals("GHS")) return "₵";
        if (currencyCode.equals("GIP")) return "£";
        if (currencyCode.equals("GMD")) return "D";
        if (currencyCode.equals("GNF")) return "Fr";
        if (currencyCode.equals("GTQ")) return "Q";
        if (currencyCode.equals("GYD")) return "$";
        if (currencyCode.equals("HKD")) return "$";
        if (currencyCode.equals("HNL")) return "L";
        if (currencyCode.equals("HRK")) return "kn";
        if (currencyCode.equals("HTG")) return "G";
        if (currencyCode.equals("HUF")) return "Ft";
        if (currencyCode.equals("IDR")) return "Rp";
        if (currencyCode.equals("ILS")) return "₪";
        if (currencyCode.equals("IMP")) return "£";
        if (currencyCode.equals("INR")) return "₹";
        if (currencyCode.equals("IQD")) return "ع.د";
        if (currencyCode.equals("IRR")) return "﷼";
        if (currencyCode.equals("ISK")) return "kr";
        if (currencyCode.equals("JEP")) return "£";
        if (currencyCode.equals("JMD")) return "$";
        if (currencyCode.equals("JOD")) return "د.ا";
        if (currencyCode.equals("JPY")) return "¥";
        if (currencyCode.equals("KES")) return "Sh";
        if (currencyCode.equals("KHR")) return "៛";
        if (currencyCode.equals("KMF")) return "Fr";
        if (currencyCode.equals("KPW")) return "₩";
        if (currencyCode.equals("KRW")) return "₩";
        if (currencyCode.equals("KWD")) return "د.ك";
        if (currencyCode.equals("KYD")) return "$";
        if (currencyCode.equals("KZT")) return "₸";
        if (currencyCode.equals("LAK")) return "₭";
        if (currencyCode.equals("LBP")) return "ل.ل";
        if (currencyCode.equals("LKR")) return "Rs";
        if (currencyCode.equals("LRD")) return "$";
        if (currencyCode.equals("LSL")) return "L";
        if (currencyCode.equals("LTL")) return "Lt";
        if (currencyCode.equals("LVL")) return "Ls";
        if (currencyCode.equals("LYD")) return "ل.د";
        if (currencyCode.equals("MAD")) return "د.م.";
        if (currencyCode.equals("MDL")) return "L";
        if (currencyCode.equals("MGA")) return "Ar";
        if (currencyCode.equals("MKD")) return "ден";
        if (currencyCode.equals("MMK")) return "Ks";
        if (currencyCode.equals("MNT")) return "₮";
        if (currencyCode.equals("MOP")) return "P";
        if (currencyCode.equals("MRO")) return "UM";
        if (currencyCode.equals("MUR")) return "₨";
        if (currencyCode.equals("MVR")) return ".ރ";
        if (currencyCode.equals("MWK")) return "MK";
        if (currencyCode.equals("MXN")) return "$";
        if (currencyCode.equals("MYR")) return "RM";
        if (currencyCode.equals("MZN")) return "MT";
        if (currencyCode.equals("NAD")) return "$";
        if (currencyCode.equals("NGN")) return "₦";
        if (currencyCode.equals("NIO")) return "C$";
        if (currencyCode.equals("NOK")) return "kr";
        if (currencyCode.equals("NPR")) return "₨";
        if (currencyCode.equals("NZD")) return "$";
        if (currencyCode.equals("OMR")) return "ر.ع.";
        if (currencyCode.equals("PAB")) return "B/.";
        if (currencyCode.equals("PEN")) return "S/.";
        if (currencyCode.equals("PGK")) return "K";
        if (currencyCode.equals("PHP")) return "₱";
        if (currencyCode.equals("PKR")) return "₨";
        if (currencyCode.equals("PLN")) return "zł";
        if (currencyCode.equals("PRB")) return "р.";
        if (currencyCode.equals("PYG")) return "₲";
        if (currencyCode.equals("QAR")) return "ر.ق";
        if (currencyCode.equals("RON")) return "L";
        if (currencyCode.equals("RSD")) return "дин.";
        if (currencyCode.equals("RUB")) return "р.";
        if (currencyCode.equals("RWF")) return "Fr";
        if (currencyCode.equals("SAR")) return "ر.س";
        if (currencyCode.equals("SBD")) return "$";
        if (currencyCode.equals("SCR")) return "₨";
        if (currencyCode.equals("SDG")) return "£";
        if (currencyCode.equals("SEK")) return "kr";
        if (currencyCode.equals("SGD")) return "$";
        if (currencyCode.equals("SHP")) return "£";
        if (currencyCode.equals("SLL")) return "Le";
        if (currencyCode.equals("SOS")) return "Sh";
        if (currencyCode.equals("SRD")) return "$";
        if (currencyCode.equals("SSP")) return "£";
        if (currencyCode.equals("STD")) return "Db";
        if (currencyCode.equals("SVC")) return "₡";
        if (currencyCode.equals("SYP")) return "£";
        if (currencyCode.equals("SZL")) return "L";
        if (currencyCode.equals("TJS")) return "ЅМ";
        if (currencyCode.equals("TMT")) return "m";
        if (currencyCode.equals("TND")) return "د.ت";
        if (currencyCode.equals("TOP")) return "T$";
        if (currencyCode.equals("TTD")) return "$";
        if (currencyCode.equals("TWD")) return "$";
        if (currencyCode.equals("TZS")) return "Sh";
        if (currencyCode.equals("UAH")) return "₴";
        if (currencyCode.equals("UGX")) return "Sh";
        if (currencyCode.equals("USD")) return "$";
        if (currencyCode.equals("UYU")) return "$";
        if (currencyCode.equals("VEF")) return "Bs F";
        if (currencyCode.equals("VND")) return "₫";
        if (currencyCode.equals("VUV")) return "Vt";
        if (currencyCode.equals("WST")) return "T";
        if (currencyCode.equals("XAF")) return "Fr";
        if (currencyCode.equals("XCD")) return "$";
        if (currencyCode.equals("XOF")) return "Fr";
        if (currencyCode.equals("XPF")) return "Fr";
        if (currencyCode.equals("YER")) return "﷼";
        if (currencyCode.equals("ZAR")) return "R";
        if (currencyCode.equals("ZMW")) return "ZK";
        if (currencyCode.equals("ZWL")) return "$";

        return null;
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

    public static float dipsToPixels(Resources resources, float dipValue)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
    }

    public static void showConfirmationDialog(Context context, String message, DialogButtons buttons, OnClickListener okClick)
    {
    	showConfirmationDialog(context, message, buttons, okClick, 
    			new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
    }
    
    public static void showConfirmationDialog(Context context, String message, DialogButtons buttons, OnClickListener okClick, OnClickListener cancelClick)
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setMessage(message);
		switch(buttons)
		{
			case OkCancel:
				builder.setPositiveButton("OK", okClick);
				builder.setNegativeButton("Cancel", cancelClick);
				break;
			case YesNo:
				builder.setPositiveButton("Yes", okClick);
				builder.setNegativeButton("No", cancelClick);
				break;
		}
	    builder.create().show();
    }

    public static ArrayList<Category> getCategoriesInGroupOrder(ArrayList<Category> categories)
    {
    	ArrayList<Category> orderedList = new ArrayList<Category>();
    	
    	Collections.sort(categories, new CategoryComparator());
    	
    	for(Category category : categories)
    	{
    		if (category.getParentCategoryId() == -1)
    		{
        		orderedList.add(category);
    			for(Category subCategory : categories)
    			{
    				if (subCategory.getParentCategoryId() == category.getId())
    					orderedList.add(subCategory);
    			}
    		}
    	}
    	
    	return orderedList;
    }

    public static String getCategoryName(Category category, Context context)
    {
    	ArrayList<Category> categories = DatabaseManager.getInstance(context).GetAllCategories();
    	return getCategoryName(category, categories);
    }

    public static String getCategoryName(Category category, ArrayList<Category> categories)
    {
    	String name = category.getName();
		if (category.getParentCategoryId() != -1)
		{
			for(Category parentCategory : categories)
			{
				if (parentCategory.getId() == category.getParentCategoryId())
				{
					name = parentCategory.getName() + " >> " + name;
					break;
				}
			}
		}
		return name;
    }

    public  static class CategoryComparator implements Comparator<Category> {
	    public int compare(Category o1, Category o2) {
	        return Double.compare(o1.getId(), o2.getId());
	    }
	}

    public static <T extends Enum<T>> List<String> toStringList(Class<T> clz) {
        try {
            List<String> res = new LinkedList<String>();
            Method getDisplayValue = clz.getMethod("name");

            for (Object e : clz.getEnumConstants()) {
                res.add((String) getDisplayValue.invoke(e));

            }

            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean stringNullEmptyOrWhitespace(final String string)
    {
        return string == null || string.isEmpty() || string.trim().isEmpty();
    }
}
