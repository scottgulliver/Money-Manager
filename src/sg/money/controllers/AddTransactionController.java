package sg.money.controllers;
import sg.money.activities.*;
import sg.money.domainobjects.Category;
import sg.money.models.*;
import com.actionbarsherlock.view.*;
import sg.money.*;
import android.content.*;
import sg.money.fragments.*;
import android.widget.*;
import android.preference.*;
import java.util.*;

public class AddTransactionController
{
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

	private AddTransactionActivity view;
	private AddTransactionModel model;

    private ArrayList<String> typeChoices;
    private ArrayList<String> categoryNames;
    public static final String ADD_CATEGORY_STRING = "< Add new category >";

	public AddTransactionController(AddTransactionActivity view, AddTransactionModel model)
	{
		this.view = view;
		this.model = model;

        if (model.isNewTransaction())
        {
            //go to last selected category

            /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view);
            int lastSelectedCategoryId = model.getCategory().income ? sharedPref.getInt("lastIncomeCategoryId", -1) : sharedPref.getInt("lastExpenseCategoryId", 
			);

            if (lastSelectedCategoryId != -1)
            {
                Category category = null;
                for(Category cat : model.getAllCategories())
                {
                    if (cat.id == lastSelectedCategoryId)
                    {
                        category = cat;
                        break;
                    }
                }

                if (category != null)
                {
                    model.setCategory(category);
                }
            }*/
        }
	}

	public void onDateChange(Date date)
	{
		model.setDate(date);
	}

	public void onTransactionValueChange(double value)
	{
		model.setValue(value);
	}

	public void onTransactionDescriptionChange(String description)
	{
		model.setDescription(description);
	}

	public void onHideFromReportsChange(boolean checked)
	{
		model.setDontReport(checked);
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
                Intent intent = new Intent(view, ParentActivity.class);
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Transactions);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                view.startActivity(intent);
                break;
	    }
	    return true;
	}

    private void OkClicked()
    {
        view.cancelFocus();

        String validationError = model.validate();
        if (validationError != null)
        {
            Toast.makeText(view, validationError, Toast.LENGTH_SHORT).show();
        }
        else
        {
            model.commit(view);

    		//save selections to preference, for loading these by default next time
    		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view);
    		SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt((model.getCategory().isIncome() ? "lastIncomeCategoryId" : "lastExpenseCategoryId"), model.getCategory().getId());
    		editor.commit();

            view.setResult(view.RESULT_OK, new Intent());
            view.finish();
        }
    }

    private void CancelClicked()
    {
        view.setResult(view.RESULT_CANCELED, new Intent());
        view.finish();
    }

    public void onCategoryChange(int position) {

        String selectedCategoryName = categoryNames.get(position);

        boolean useNewCategory = selectedCategoryName.equals(ADD_CATEGORY_STRING);
        model.setUseNewCategory(useNewCategory);

        if (!useNewCategory)
        {
            model.setCategory(selectedCategoryName);
        }

    }

    public void onTypeChange(int position)
    {
        double transactionValue = model.getValue();

        TransactionType type = TransactionType.fromInteger(position);

        model.setIncomeType(type == TransactionType.Income);
        model.setIsTransfer(type == TransactionType.Transfer);

        model.setValue(transactionValue); // now that the type may have changed, refresh the value
    }

    public ArrayList<String> getCategoryNames() {

        ArrayList<String> categoryNames = model.getValidCategoryNames();
        categoryNames.add(ADD_CATEGORY_STRING);

        this.categoryNames = categoryNames;
        return categoryNames;
    }

    public ArrayList<String> getTypeChoices() {

        ArrayList<String> typeChoices = new ArrayList<String>();
        typeChoices.add(TransactionType.Expense.name());
        typeChoices.add(TransactionType.Income.name());
        if (model.getAccounts().length > 1)
        {
            typeChoices.add(TransactionType.Transfer.name());
        }

        this.typeChoices = typeChoices;
        return typeChoices;
    }

}
