package sg.money.domainobjects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class Category implements Parcelable
{
	public int id;
	public String name;
	public int color;
	public boolean income;
	public boolean isPermanent = false;
	public boolean useInReports = true;
	public int parentCategoryId;
	
	public Category()
	{
	}
	
	public Category(String name, int color, boolean income, boolean isPermanent, boolean useInReports, int parentCategoryId)
	{
		this.name = name;
		this.color = color;
		this.income = income;
		this.isPermanent = isPermanent;
		this.useInReports = useInReports;
		this.parentCategoryId = parentCategoryId;
	}

    /* Implementation of Parcelable */

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    private Category(Parcel in) {
        id = in.readInt();
        name = in.readString();
        color = in.readInt();
        income = in.readInt() == 1;
        isPermanent = in.readInt() == 1;
        useInReports = in.readInt() == 1;
        parentCategoryId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(color);
        parcel.writeInt(income ? 1 : 0);
        parcel.writeInt(isPermanent ? 1 : 0);
        parcel.writeInt(useInReports ? 1 : 0);
        parcel.writeInt(parentCategoryId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */
}
