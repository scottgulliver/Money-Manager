package sg.money.domainobjects;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import sg.money.DatabaseManager;

public class Budget implements Parcelable
{
	public enum NotificationType{
		None(0),
		Daily(1),
		Weekly(2),
		Monthly(3);

        // access to values() for casting is expensive, so use this instead..
        public static NotificationType fromInteger(int x) {
            switch(x) {
                case 0:
                    return None;
                case 1:
                    return Daily;
                case 2:
                    return Weekly;
                case 3:
                    return Monthly;
            }
            return null;
        }

        private final int value;
        private NotificationType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
	}
	
	public int id;
	public String name;
	public double value;
	public NotificationType notifyType;
	public ArrayList<Account> accounts = new ArrayList<Account>();
	public ArrayList<Category> categories = new ArrayList<Category>();

    public Budget()
    {

    }
	
	/* test comment for budget class */
	
	public double getSpent(Context context, Calendar startDate, Calendar endDate)
	{
		ArrayList<Transaction> transactions = DatabaseManager.getInstance(context).GetAllTransactions(startDate.getTime(), endDate.getTime());
		double spending = 0;
        for(Transaction transaction : transactions)
        {
        	if (!DatabaseManager.getInstance(context).GetCategory(transaction.category).useInReports)
        		continue;
        	
        	if (!accounts.isEmpty())
        	{
            	boolean isAccout = false;
            	for(Account account : accounts)
            	{
            		if (transaction.account == account.id)
        			{
        				isAccout = true;
        				break;
        			}
            	}
            	if (!isAccout)
            		continue;
        	}
        	if (!categories.isEmpty())
        	{
            	boolean isCategory = false;
            	for(Category category : categories)
            	{
            		if (transaction.category == category.id)
            		{
            			isCategory = true;
        				break;
        			}
            	}
            	if (!isCategory)
            		continue;
        	}
        	spending += transaction.getRealValue(context);
        }
        
        return spending;
	}
	
	public String getCompletePercentage(Context context, Calendar startDate, Calendar endDate)
	{
		return getCompletePercentage(getSpent(context, startDate, endDate));
	}
	
	public String getCompletePercentage(double spent)
	{
		DecimalFormat df = new DecimalFormat("#");
		double percentageConversion = 100.0 / value;
		percentageConversion *= spent;
		return df.format(percentageConversion) + "%";
	}

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<Budget> CREATOR = new Parcelable.Creator<Budget>() {
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };

    private Budget(Parcel in) {
        id = in.readInt();
        name = in.readString();
        value = in.readDouble();
        notifyType = NotificationType.fromInteger(in.readInt());
        accounts = new ArrayList<Account>(Arrays.asList((Account[]) in.readParcelableArray(Account.class.getClassLoader())));
        categories = new ArrayList<Category>(Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader())));
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeDouble(value);
        parcel.writeInt(notifyType.getValue());
        parcel.writeParcelableArray((Parcelable[])accounts.toArray(), flags);
        parcel.writeParcelableArray((Parcelable[])categories.toArray(), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */
}
