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
	private AddAccountActivity m_view;
	private AddAccountModel m_model;
	
	
	/* Constructor */
	
	public AddAccountController(AddAccountActivity view, AddAccountModel model)
	{
		m_model = model;
		m_view = view;
	}
	
	
	/* Methods */

	public void onAccountNameChange(String name)
	{
		m_model.setAccountName(name);
	}

	public void onStartingBalanceChange(Double balance)
	{
		m_model.setStartingBalance(balance);
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
                intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Accounts);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                m_view.startActivity(intent);
                break;
        }
        return true;
    }

    private void OkClicked()
    {
        m_view.cancelFocus();

        String validationError = m_model.validate(m_view);
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
}
