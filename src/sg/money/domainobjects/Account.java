package sg.money.domainobjects;

import java.util.ArrayList;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import sg.money.common.DatabaseManager;
import java.io.*;

/**
* An account holds a value, which is affected by transactions recorded against it.
*/
public class Account implements Parcelable, Serializable
{  
	private int m_id;
	private String m_name;
	private double m_value;
	
	
	/* Constructors */
	
	public Account(String name)
	{
		m_name = name;
	}
	
	public Account(Context context, int id, String name)
	{
		m_id = id;
		m_name = name;
		
		if (id > -1)
		{
			ArrayList<Transaction> transactions = DatabaseManager.getInstance(context).GetAllTransactions(id);
			for(Transaction transaction : transactions)
				m_value += transaction.getValue();
		}
	}
	
	
	/* Getters and setters */

	public void setId(int id)
	{
		m_id = id;
	}

	public int getId()
	{
		return m_id;
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setValue(double value)
	{
		m_value = value;
	}

	public double getValue()
	{
		return m_value;
	}

	
    /* Implementation of Parcelable */

    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    private Account(Parcel in) {
        m_id = in.readInt();
        m_name = in.readString();
        m_value = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(m_id);
        parcel.writeString(m_name);
        parcel.writeDouble(m_value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */
	
}

