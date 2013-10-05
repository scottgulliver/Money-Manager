package sg.money.domainobjects;

import java.util.Date;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import sg.money.DatabaseManager;

public class Transaction implements Parcelable
{
	public int id;
	public double value;
	public String description;
	public int category;
	public Date dateTime;
	public int account;
	public boolean dontReport;
	public boolean isTransfer;
	public int transferToTransaction;
	public int transferFromTransaction;
	public boolean reconciled;

    public Transaction()
    {

    }
	
	public double getRealValue(Context context)
	{
		Category category = DatabaseManager.getInstance(context).GetCategory(this.category);
		return (category.income ? value : value * -1.0);
	}
	
	public Transaction getRelatedTransferTransaction(Context context)
	{
		if (!isTransfer)
			return null; //todo throw exception here.
		
		return DatabaseManager.getInstance(context).GetTransaction(transferFromTransaction != -1 
																	? transferFromTransaction 
																	: transferToTransaction);
	}
	
	public boolean isReceivingParty()
	{
		if (!isTransfer)
			return false; //todo throw exception here.
		
		return transferFromTransaction != -1;
	}
	
	public Account getAccount(Context context)
	{
		return DatabaseManager.getInstance(context).GetAccount(account);
	}
	
	public String getTransferDescription(Context context)
	{
		return "Transfer "+(isReceivingParty() ? "from " : "to ")+getRelatedTransferTransaction(context).getAccount(context).name;
	}

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    private Transaction(Parcel in) {
        id = in.readInt();
        value = in.readDouble();
        description = in.readString();
        category = in.readInt();
        dateTime = (Date)in.readSerializable();
        account = in.readInt();
        dontReport = in.readInt() == 1;
        isTransfer = in.readInt() == 1;
        transferToTransaction = in.readInt();
        transferFromTransaction = in.readInt();
        reconciled = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeDouble(value);
        parcel.writeString(description);
        parcel.writeInt(category);
        parcel.writeSerializable(dateTime);
        parcel.writeInt(account);
        parcel.writeInt(dontReport ? 1 : 0);
        parcel.writeInt(isTransfer ? 1 : 0);
        parcel.writeInt(transferToTransaction);
        parcel.writeInt(transferFromTransaction);
        parcel.writeInt(reconciled ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */
}
