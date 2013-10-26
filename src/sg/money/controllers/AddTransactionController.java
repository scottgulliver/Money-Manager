package sg.money.controllers;

import android.content.*;
import android.widget.*;
import com.actionbarsherlock.view.*;
import java.util.*;
import sg.money.*;
import sg.money.activities.*;
import sg.money.common.DatabaseManager;
import sg.money.domainobjects.*;
import sg.money.fragments.*;
import sg.money.models.*;
import sg.money.utils.*;

public class AddTransactionController
{
    /**
	* Available options for 'type' of transaction.
	*/
    public enum TransactionType
    {
        Expense(0),
        Income(1),
        Transfer(2);

        // access to values() for casting is expensive, so use this instead..
        public static TransactionType fromInteger(int x) {
            switch(x) {
                case 0:
                    return Expense;
                case 1:
                    return Income;
                case 2:
                    return Transfer;
            }
            return null;
        }

        private final int value;
        private TransactionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

	private AddTransactionActivity m_view;
	private AddTransactionModel m_model;
    private ArrayList<String> m_typeChoices;
    private ArrayList<String> m_categoryNames;
	
    public static final String ADD_CATEGORY_STRING = "< Add new category >";
	
	
	/* Constructor */

	public AddTransactionController(AddTransactionActivity view, AddTransactionModel model)
	{
		m_view = view;
		m_model = model;
	}
	
	
	/* Methods */

	public void onDateChange(Date date)
	{
		m_model.setDate(date);
	}

	public void onTransactionValueChange(double value)
	{
		m_model.setValue(value);
	}

	public void onTransactionDescriptionChange(String description)
	{
		m_model.setDescription(description);
	}

	public void onHideFromReportsChange(boolean checked)
	{
		m_model.setDontReport(checked);
	}
    public void onNewCategoryNameChange(String name) {
        m_model.setNewCategoryName(name);
    }

	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	    	case R.id.menu_accepttransaction:{
					OkClicked();
					break;
	    		}

	    	case R.id.menu_rejecttransaction:{
					CancelClicked();
					break;
	    		}	    	

	        case R.id.menu_settings:{
					break;
            	}

	        case android.R.id.home: // up button
                Intent intent = new Intent(m_view, ParentActivity.class);
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Transactions);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                m_view.startActivity(intent);
                break;
	    }
	    return true;
	}
	
	public void setSavedCategorySelection()
	{
		int savedId = Settings.getLastUsedCategoryId(m_view, m_model.isIncomeType());

        if (savedId == -1)
        {
            m_model.setCategory(getDefaultCategory());
        }
        else
        {
            for(Category category : m_model.getAllCategories())
            {
                if (category.getId() == savedId)
                {
                    m_model.setCategory(category);
                    break;
                }
            }
        }
	}

    private Category getDefaultCategory()
    {
        return DatabaseManager.getInstance(m_view).GetAllCategories().get(0);
    }

    private void OkClicked()
    {
        m_view.cancelFocus();

        String validationError = m_model.validate();
        if (validationError != null)
        {
            Toast.makeText(m_view, validationError, Toast.LENGTH_SHORT).show();
        }
        else
        {
            m_model.commit(m_view);
            m_view.setResult(m_view.RESULT_OK, new Intent());
            m_view.finish();
        }
    }

    private void CancelClicked()
    {
        m_view.setResult(m_view.RESULT_CANCELED, new Intent());
        m_view.finish();
    }

    public void onCategoryChange(int position)
    {
        String selectedCategoryName = m_categoryNames.get(position);

        boolean useNewCategory = selectedCategoryName.equals(ADD_CATEGORY_STRING);
        m_model.setUseNewCategory(useNewCategory);

        if (!useNewCategory)
        {
            m_model.setCategory(selectedCategoryName);
        }
		
		Settings.setLastUsedCategoryId(m_view, m_model.getCategory().getId(),
			m_model.getCategory().isIncome());
    }

    public void onTransferAccountChange(int position)
    {
        m_model.setTransferAccount(m_model.getAccounts()[position]);
    }

    public void onTypeChange(int position)
    {
        double transactionValue = m_model.getValue();

        TransactionType type = TransactionType.fromInteger(position);

        m_model.setIncomeType(type == TransactionType.Income);
        m_model.setIsTransfer(type == TransactionType.Transfer);

        m_model.setValue(transactionValue); // now that the type may have changed, refresh the value
		
		setSavedCategorySelection();
    }

    public ArrayList<String> getCategoryNames() {

        ArrayList<String> categoryNames = m_model.getValidCategoryNames();
        categoryNames.add(ADD_CATEGORY_STRING);

        this.m_categoryNames = categoryNames;
        return categoryNames;
    }

    public ArrayList<String> getTypeChoices() {

        ArrayList<String> typeChoices = new ArrayList<String>();
        typeChoices.add(TransactionType.Expense.name());
        typeChoices.add(TransactionType.Income.name());
        if (m_model.getAccounts().length > 0 &&
                (m_model.isNewTransaction() || m_model.getIsTransfer()))
        {
            typeChoices.add(TransactionType.Transfer.name());
        }

        this.m_typeChoices = typeChoices;
        return typeChoices;
    }
}
