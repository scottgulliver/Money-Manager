package sg.money.controllers;
import android.content.Intent;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

import sg.money.R;
import sg.money.activities.*;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.models.*;

public class AddAccountController
{
	private AddAccountActivity view;
	private AddAccountModel model;
	
	
	public AddAccountController(AddAccountActivity view, AddAccountModel model)
	{
		this.model = model;
		this.view = view;
	}

	public void onAccountNameChange(String name)
	{
		model.setAccountName(name);
	}

	public void onStartingBalanceChange(Double balance)
	{
		model.setStartingBalance(balance);
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
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Accounts);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                view.startActivity(intent);
                break;
        }
        return true;
    }

    private void OkClicked()
    {
        view.cancelFocus();

        String validationError = model.validate(view);
        if (validationError != null)
        {
            Toast.makeText(view, validationError, Toast.LENGTH_SHORT).show();
        }
        else
        {
            model.commit(view);

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
