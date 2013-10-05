package sg.money.activities;

import java.util.ArrayList;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.DatabaseManager;
import sg.money.fragments.HostActivityFragmentTypes;
import sg.money.models.AddBudgetModel;
import sg.money.utils.Misc;
import sg.money.R;
import sg.money.models.*;
import sg.money.controllers.*;
import android.view.View.*;
import android.widget.AdapterView.*;
import android.widget.*;

public class AddBudgetActivity extends BaseActivity implements OnChangeListener<AddBudgetModel> {

    private final String SIS_KEY_MODEL = "Model";

	EditText txtName;
	EditText txtValue;
	Button btnCategories;
	Button btnAccounts;
	Spinner spnNotifyType;

	// todo change these - just horrible!
	int viewingDialog = 0;
	static final int ACCOUNTSLIST = 1;
	static final int CATEGORIESLIST = 2;

	// Bundle State Data
	static final String STATE_SELECTED_ACCOUNTS = "stateSelectedAccounts";
	static final String STATE_SELECTED_CATEGORIES = "stateSelectedCategories";

    AddBudgetModel model;
	AddBudgetController controller;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_budget);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		txtName = (EditText) findViewById(R.id.txtName);
		txtValue = (EditText) findViewById(R.id.txtValue);
		btnCategories = (Button) findViewById(R.id.btnCategories);
		btnAccounts = (Button) findViewById(R.id.btnAccounts);
		spnNotifyType = (Spinner) findViewById(R.id.spnNotifyType);

        if (savedInstanceState != null)
        {
            model = savedInstanceState.getParcelable(SIS_KEY_MODEL);
        }
        else
        {
            // check if we are editing
            Budget editBudget = null;
            int editId = getIntent().getIntExtra("ID", -1);
            if (editId != -1) {
                editBudget = DatabaseManager.getInstance(AddBudgetActivity.this).GetBudget(editId);
            }

            model = new AddBudgetModel(editBudget, this);
        }

		model.addListener(this);
		controller = new AddBudgetController(this, model);
		
		setTitle(model.isNewBudget() ? "Add Budget" : "Edit Budget");

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                                                     android.R.layout.simple_spinner_dropdown_item,
                                                     controller.getNotificationOptions());
		spnNotifyType.setAdapter(arrayAdapter);

		btnCategories.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					controller.CategoriesClicked();
				}
			});

		btnAccounts.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					controller.AccountsClicked();
				}
			});

		txtName.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
                        controller.onNameChange(txtName.getText().toString());
					}
				}			
			});

		txtValue.setOnFocusChangeListener(new OnFocusChangeListener()
			{
				public void onFocusChange(View view, boolean hasFocus)
				{
					if (!hasFocus)
					{
                        if (Misc.stringNullEmptyOrWhitespace(txtValue.getText().toString()))
                            txtValue.setText("0");

						controller.onValueChange(Double.parseDouble(txtValue.getText().toString()));
					}
				}			
			});
			
		spnNotifyType.setOnItemSelectedListener(new OnItemSelectedListener()
			{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				controller.onNotifyTypeSelected(Budget.NotificationType.fromInteger(position));
			}

			public void onNothingSelected(AdapterView<?> parent)
			{
                //do nothing
			}
		});

        updateUi();
	}

    @Override
    public void onChange(AddBudgetModel model)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                updateUi();
            }
        });
    }
	
	public void updateUi()
	{
		txtName.setText(model.getBudgetName());
		txtValue.setText(String.valueOf(model.getBudgetValue()));
		spnNotifyType.setSelection(model.getNotifyType().getValue());
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
        cancelFocus();
        savedInstanceState.putParcelable(SIS_KEY_MODEL, model);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		/*
		ArrayList<Integer> selectedAccountIds = savedInstanceState
				.getIntegerArrayList(STATE_SELECTED_ACCOUNTS);
		ArrayList<Integer> selectedCategoryIds = savedInstanceState
				.getIntegerArrayList(STATE_SELECTED_CATEGORIES);

		selectedAccounts.clear();
		for (int accountId : selectedAccountIds) {
			for (Account account : currentAccounts) {
				if (account.id == accountId) {
					selectedAccounts.add(account);
					break;
				}
			}
		}

		selectedCategories.clear();
		for (int categoryId : selectedCategoryIds) {
			for (Category category : currentCategories) {
				if (category.id == categoryId) {
					selectedCategories.add(category);
					break;
				}
			}
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_add_budget, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return controller.onOptionsItemSelected(item);
	}

    public void cancelFocus()
    {
        txtName.clearFocus();
        txtValue.clearFocus();
    }
}
