package sg.money.domainobjects;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import sg.money.DatabaseManager;

/**
 * TODO add summary
 */
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
	
	private int m_id;
    private String m_name;
    private double m_value;
    private NotificationType m_notifyType;
    private ArrayList<Account> m_accounts;
    private ArrayList<Category> m_categories;


    /* Constructor */

    public Budget()
    {
        m_accounts = new ArrayList<Account>();
        m_categories = new ArrayList<Category>();
    }


    /* Getters / setters */

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public double getValue() {
        return m_value;
    }

    public void setValue(double value) {
        m_value = value;
    }

    public NotificationType getNotifyType() {
        return m_notifyType;
    }

    public void setNotifyType(NotificationType notifyType) {
        m_notifyType = notifyType;
    }

    public ArrayList<Account> getAccounts() {
        return m_accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        m_accounts = accounts;
    }

    public ArrayList<Category> getCategories() {
        return m_categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        m_categories = categories;
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
        m_id = in.readInt();
        m_name = in.readString();
        m_value = in.readDouble();
        m_notifyType = NotificationType.fromInteger(in.readInt());
        m_accounts = new ArrayList<Account>(Arrays.asList((Account[]) in.readParcelableArray(Account.class.getClassLoader())));
        m_categories = new ArrayList<Category>(Arrays.asList((Category[]) in.readParcelableArray(Category.class.getClassLoader())));
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(m_id);
        parcel.writeString(m_name);
        parcel.writeDouble(m_value);
        parcel.writeInt(m_notifyType.getValue());
        parcel.writeParcelableArray((Parcelable[]) m_accounts.toArray(), flags);
        parcel.writeParcelableArray((Parcelable[]) m_categories.toArray(), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */


    /* Methods */

	public double getSpent(Context context, Calendar startDate, Calendar endDate)
	{
		ArrayList<Transaction> transactions = DatabaseManager.getInstance(context).GetAllTransactions(startDate.getTime(), endDate.getTime());
		double spending = 0;
        for(Transaction transaction : transactions)
        {
        	if (!DatabaseManager.getInstance(context).GetCategory(transaction.getCategory()).isUseInReports())
        		continue;
        	
        	if (!m_accounts.isEmpty())
        	{
            	boolean isAccout = false;
            	for(Account account : m_accounts)
            	{
            		if (transaction.getAccount() == account.getId())
        			{
        				isAccout = true;
        				break;
        			}
            	}
            	if (!isAccout)
            		continue;
        	}
        	if (!m_categories.isEmpty())
        	{
            	boolean isCategory = false;
            	for(Category category : m_categories)
            	{
            		if (transaction.getCategory() == category.getId())
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
		double percentageConversion = 100.0 / m_value;
		percentageConversion *= spent;
		return df.format(percentageConversion) + "%";
	}
}
