package sg.money.controllers;
import sg.money.activities.*;
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
	private AddTransactionActivity view;
	private AddTransactionModel model;

	public AddTransactionController(AddTransactionActivity view, AddTransactionModel model)
	{
		this.view = view;
		this.model = model;
	}

	public void onDateChange(Date date)
	{
		model.setDate(date);
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
			editor.putInt((model.getCategory().income ? "lastIncomeCategoryId" : "lastExpenseCategoryId"), model.getCategory().id);
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
}
