package sg.money.domainobjects;

import java.util.Date;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import sg.money.DatabaseManager;

/**
 * An instance of an expense, income, or transfer.
 */
public class Transaction implements Parcelable
{
	public int m_id;
	public double m_value;
	public String m_description;
	public int m_category;
	public Date m_dateTime;
	public int m_account;
	public boolean m_dontReport;
	public boolean m_isTransfer;
	public int m_transferToTransaction;
	public int m_transferFromTransaction;
	public boolean m_reconciled;


    /* Constructor */

    public Transaction()
    {
    }


    /* Getters / setters */

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public double getValue() {
        return m_value;
    }

    public void setValue(double value) {
        m_value = value;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public int getCategory() {
        return m_category;
    }

    public void setCategory(int category) {
        m_category = category;
    }

    public Date getDateTime() {
        return m_dateTime;
    }

    public void setDateTime(Date dateTime) {
        m_dateTime = dateTime;
    }

    public int getAccount() {
        return m_account;
    }

    public void setAccount(int account) {
        m_account = account;
    }

    public boolean isDontReport() {
        return m_dontReport;
    }

    public void setDontReport(boolean dontReport) {
        m_dontReport = dontReport;
    }

    public boolean isTransfer() {
        return m_isTransfer;
    }

    public void setTransfer(boolean transfer) {
        m_isTransfer = transfer;
    }

    public int getTransferToTransaction() {
        return m_transferToTransaction;
    }

    public void setTransferToTransaction(int transferToTransaction) {
        m_transferToTransaction = transferToTransaction;
    }

    public int getTransferFromTransaction() {
        return m_transferFromTransaction;
    }

    public void setTransferFromTransaction(int transferFromTransaction) {
        m_transferFromTransaction = transferFromTransaction;
    }

    public boolean isReconciled() {
        return m_reconciled;
    }

    public void setReconciled(boolean reconciled) {
        m_reconciled = reconciled;
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
        m_id = in.readInt();
        m_value = in.readDouble();
        m_description = in.readString();
        m_category = in.readInt();
        m_dateTime = (Date)in.readSerializable();
        m_account = in.readInt();
        m_dontReport = in.readInt() == 1;
        m_isTransfer = in.readInt() == 1;
        m_transferToTransaction = in.readInt();
        m_transferFromTransaction = in.readInt();
        m_reconciled = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(m_id);
        parcel.writeDouble(m_value);
        parcel.writeString(m_description);
        parcel.writeInt(m_category);
        parcel.writeSerializable(m_dateTime);
        parcel.writeInt(m_account);
        parcel.writeInt(m_dontReport ? 1 : 0);
        parcel.writeInt(m_isTransfer ? 1 : 0);
        parcel.writeInt(m_transferToTransaction);
        parcel.writeInt(m_transferFromTransaction);
        parcel.writeInt(m_reconciled ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */


    /* Methods */

    public double getRealValue(Context context)
    {
        Category category = DatabaseManager.getInstance(context).GetCategory(m_category);
        return (category.isIncome() ? m_value : m_value * -1.0);
    }

    public Transaction getRelatedTransferTransaction(Context context)
    {
        if (!m_isTransfer)
        {
            throw new RuntimeException("getRelatedTransferTransaction called, although the" +
                    " transaction is not a transfer.");
        }

        return DatabaseManager.getInstance(context).GetTransaction(m_transferFromTransaction != -1
                ? m_transferFromTransaction
                : m_transferToTransaction);
    }

    public boolean isReceivingParty()
    {
        if (!m_isTransfer)
        {
            throw new RuntimeException("isReceivingParty called, although the" +
                    " transaction is not a transfer.");
        }

        return m_transferFromTransaction != -1;
    }

    public Account getAccount(Context context)
    {
        return DatabaseManager.getInstance(context).GetAccount(m_account);
    }

    public String getTransferDescription(Context context)
    {
        return "Transfer "+(isReceivingParty() ? "from " : "to ")+getRelatedTransferTransaction(context).getAccount(context).getName();
    }
}
