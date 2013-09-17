package sg.money.controllers;

import sg.money.activities.*;
import sg.money.models.*;
import android.content.*;
import com.actionbarsherlock.view.*;
import sg.money.*;
import sg.money.fragments.*;
import sg.money.domainobjects.*;
import java.util.*;
import sg.money.utils.*;
import android.app.*;
import android.widget.*;
import android.view.*;

public class AddBudgetController
{
	private enum DialogType
	{
		Accounts,
		Categories
	};
	
	private AddBudgetActivity view;
	private AddBudgetModel model;

	public AddBudgetController(AddBudgetActivity view, AddBudgetModel model)
	{
		this.model = model;
		this.view = view;
	}
	
	public void onNameChange(String name)
	{
		model.setBudgetName(name);
	}

	public void onValueChange(Double value)
	{
		model.setBudgetValue(value);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_acceptbudget: {
					OkClicked();
					break;
				}

			case R.id.menu_rejectbudget: {
					CancelClicked();
					break;
			}
			case R.id.menu_settings: {
					break;
				}

			case android.R.id.home:
				Intent intent = new Intent(view, ParentActivity.class);
				intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Budgets);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				view.startActivity(intent);
				break;
		}
		return true;
	}

	public void CategoriesClicked() {
		boolean[] checkedItems = new boolean[model.getCurrentCategories().size() + 1];
		checkedItems[0] = model.getSelectedCategories().size() == 0;
		for (int i = 0; i < model.getCurrentCategories().size(); i++) {
			boolean alreadySelected = false;
			for (Category selectedCategory : model.getSelectedCategories()) {
				if (selectedCategory.id == model.getCurrentCategories().get(i).id) {
					alreadySelected = true;
					break;
				}
			}
			checkedItems[i + 1] = alreadySelected;
		}

		ArrayList<String> items = new ArrayList<String>();
		items.add("All Categories");
		for (Category category : model.getCurrentCategories()) {
			if (!category.useInReports)
				continue;

			items.add(Misc.getCategoryName(category, model.getCurrentCategories()));
		}

		createDialog("Categories", items, checkedItems, DialogType.Categories).show();
	}

	public void AccountsClicked() {
		boolean[] checkedItems = new boolean[model.getCurrentAccounts().size() + 1];
		checkedItems[0] = model.getSelectedAccounts().size() == 0;
		for (int i = 0; i < model.getCurrentAccounts().size(); i++) {
			boolean alreadySelected = false;
			for (Account selectedAccount : model.getSelectedAccounts()) {
				if (selectedAccount.id == model.getCurrentAccounts().get(i).id) {
					alreadySelected = true;
					break;
				}
			}
			checkedItems[i + 1] = alreadySelected;
		}

		ArrayList<String> items = new ArrayList<String>();
		items.add("All Accounts");
		for (Account account : model.getCurrentAccounts())
			items.add(account.name);

		createDialog("Accounts", items, checkedItems, DialogType.Accounts).show();
	}

	private Dialog createDialog(String title, final ArrayList<String> items,
								final boolean[] checkedItems, final DialogType type) {
		final ArrayList<Object> mSelectedItems = new ArrayList<Object>(); // Where
		// we
		// track
		// the
		// selected
		// items
		for (int i = 1; i < checkedItems.length; i++) {
			if (checkedItems[i] == true)
				mSelectedItems.add(i);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(view);
		// Set the dialog title
		builder.setTitle(title)
			// Specify the list array, the items to be selected by default
			// (null for none),
			// and the listener through which to receive callbacks when
			// items are selected
			.setMultiChoiceItems(
			items.toArray(new CharSequence[items.size()]),
			checkedItems,
			new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
					if (isChecked) {
						if (which == 0) {
							mSelectedItems.clear();
							for (int i = 1; i < items.size(); i++) {
								checkedItems[i] = false;
								((AlertDialog) dialog)
									.getListView()
									.setItemChecked(i, false);
							}
							((BaseAdapter) ((AlertDialog) dialog)
								.getListView().getAdapter())
								.notifyDataSetChanged();
						} else {
							mSelectedItems.add(which);
							if (checkedItems[0] == true) {
								checkedItems[0] = false;
								((AlertDialog) dialog)
									.getListView()
									.setItemChecked(0, false);
								((BaseAdapter) ((AlertDialog) dialog)
									.getListView().getAdapter())
									.notifyDataSetChanged();
							}
						}
					} else if (which == 0) {
						if (mSelectedItems.size() == 0) {
							checkedItems[0] = true;
							((AlertDialog) dialog).getListView()
								.setItemChecked(0, true);
							((BaseAdapter) ((AlertDialog) dialog)
								.getListView().getAdapter())
								.notifyDataSetChanged();
						}
					} else if (mSelectedItems.contains(which)) {
						mSelectedItems.remove(Integer
											  .valueOf(which));
						if (mSelectedItems.size() == 0) {
							checkedItems[0] = true;
							((AlertDialog) dialog).getListView()
								.setItemChecked(0, true);
							((BaseAdapter) ((AlertDialog) dialog)
								.getListView().getAdapter())
								.notifyDataSetChanged();
						}
					}
				}
			})
			// Set the action buttons
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (type == DialogType.Accounts)
						model.getSelectedAccounts().clear();
					else if (type == DialogType.Categories)
						model.getSelectedCategories().clear();

					for (Object item : mSelectedItems) {
						if (type == DialogType.Accounts) {
							for (Account account : model.getCurrentAccounts()) {
								if (account.name.equals(items.get(Integer
																  .parseInt(String.valueOf(item))))) {
									model.getSelectedAccounts().add(account);
									break;
								}
							}
						} else if (type == DialogType.Categories) {
							for (Category category : model.getCurrentCategories()) {
								if (Misc.getCategoryName(category, model.getCurrentCategories()).equals(items.get(Integer
																									   .parseInt(String.valueOf(item))))) {
									model.getSelectedCategories().add(category);
									break;
								}
							}
						}
					}

					dialog.cancel();
				}
			})
			.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

		return builder.create();
	}

	private void OkClicked() {

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

		//editBudget.notifyType = spnNotifyType.getSelectedItemPosition();
	}

	private void CancelClicked() {
		view.setResult(view.RESULT_CANCELED, new Intent());
		view.finish();
	}
}
