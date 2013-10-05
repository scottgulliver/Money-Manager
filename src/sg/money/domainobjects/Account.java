package sg.money.domainobjects;

import java.util.ArrayList;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import sg.money.DatabaseManager;

public class Account implements Parcelable
{  
	public int id;
	public String name;
	public double value; 
	
	public Account(String name)
	{
		this.name = name;
	}
	
	public Account(Context context, int id, String name)
	{
		this.id = id;
		this.name = name;
		
		if (id > -1)
		{
			ArrayList<Transaction> transactions = DatabaseManager.getInstance(context).GetAllTransactions(id);
			for(Transaction transaction : transactions)
				value += transaction.value;
		}
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
        id = in.readInt();
        name = in.readString();
        value = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeDouble(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */
}

