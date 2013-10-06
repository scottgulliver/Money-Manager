package sg.money.domainobjects;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;

/**
* TODO add summary
*/
public class Category implements Parcelable
{
	private int m_id;
	private String m_name;
	private int m_color;
	private boolean m_income;
	private boolean m_isPermanent;
	private boolean m_useInReports;
	private int m_parentCategoryId;
	
	
	/* Constructors */
	
	public Category()
	{
		m_useInReports = true;
	}
	
	public Category(String name, int color, boolean income, boolean isPermanent, boolean useInReports, int parentCategoryId)
	{
		m_name = name;
		m_color = color;
		m_income = income;
		m_isPermanent = isPermanent;
		m_useInReports = useInReports;
		m_parentCategoryId = parentCategoryId;
	}
	
	
	/* Getters / setters */

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

	public void setColor(int color)
	{
		m_color = color;
	}

	public int getColor()
	{
		return m_color;
	}

	public void setIncome(boolean income)
	{
		m_income = income;
	}

	public boolean isIncome()
	{
		return m_income;
	}

	public void setIsPermanent(boolean isPermanent)
	{
		m_isPermanent = isPermanent;
	}

	public boolean isPermanent()
	{
		return m_isPermanent;
	}

	public void setUseInReports(boolean useInReports)
	{
		m_useInReports = useInReports;
	}

	public boolean isUseInReports()
	{
		return m_useInReports;
	}

	public void setParentCategoryId(int parentCategoryId)
	{
		m_parentCategoryId = parentCategoryId;
	}

	public int getParentCategoryId()
	{
		return m_parentCategoryId;
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
        m_id = in.readInt();
        m_name = in.readString();
        m_color = in.readInt();
        m_income = in.readInt() == 1;
        m_isPermanent = in.readInt() == 1;
        m_useInReports = in.readInt() == 1;
        m_parentCategoryId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(m_id);
        parcel.writeString(m_name);
        parcel.writeInt(m_color);
        parcel.writeInt(m_income ? 1 : 0);
        parcel.writeInt(m_isPermanent ? 1 : 0);
        parcel.writeInt(m_useInReports ? 1 : 0);
        parcel.writeInt(m_parentCategoryId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* End Implementation of Parcelable */
}
