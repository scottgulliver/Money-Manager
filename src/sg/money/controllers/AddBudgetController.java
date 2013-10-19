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
	/**
	* Different types of dialog may be presented to the user.
	*/
    private enum DialogType
	{
		Accounts,
		Categories
	};
	
	
	private AddBudgetActivity m_view;
	private AddBudgetModel m_model;
	
	
	/* Constructor */

	public AddBudgetController(AddBudgetActivity view, AddBudgetModel model)
	{
		m_model = model;
		m_view = view;
	}
	
	
	/* Methods */
	
	public void onNameChange(String name)
	{
		m_model.setBudgetName(name);
	}

	public void onValueChange(Double value)
	{
		m_model.setBudgetValue(value);
	}

    public void onNotifyTypeSelected(Budget.NotificationType notificationType) {
        m_model.setNotifyType(notificationType);
    }

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
				Intent intent = new Intent(m_view, ParentActivity.class);
				intent.putExtra(ParentActivity.INTENTEXTRA_CONTENTTYPE, HostActivityFragmentTypes.Budgets);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				m_view.startActivity(intent);
				break;
		}
		return true;
	}

    public List<String> getNotificationOptions()
    {
        return Misc.toStringList(Budget.NotificationType.class);
    }

	public void CategoriesClicked() {
		boolean[] checkedItems = new boolean[m_model.getCurrentCategories().size() + 1];
		checkedItems[0] = m_model.getSelectedCategories().size() == 0;
		for (int i = 0; i < m_model.getCurrentCategories().size(); i++) {
			boolean alreadySelected = false;
			for (Category selectedCategory : m_model.getSelectedCategories()) {
				if (selectedCategory.getId() == m_model.getCurrentCategories().get(i).getId()) {
					alreadySelected = true;
					break;
				}
			}
			checkedItems[i + 1] = alreadySelected;
		}

		ArrayList<String> items = new ArrayList<String>();
		items.add("All Categories");
		for (Category category : m_model.getCurrentCategories()) {
			if (!category.isUseInReports())
				continue;

			items.add(Category.getCategoryName(category, m_model.getCurrentCategories()));
		}

		createDialog("Categories", items, checkedItems, DialogType.Categories).show();
	}

	public void AccountsClicked() {
		boolean[] checkedItems = new boolean[m_model.getCurrentAccounts().size() + 1];
		checkedItems[0] = m_model.getSelectedAccounts().size() == 0;
		for (int i = 0; i < m_model.getCurrentAccounts().size(); i++) {
			boolean alreadySelected = false;
			for (Account selectedAccount : m_model.getSelectedAccounts()) {
				if (selectedAccount.getId() == m_model.getCurrentAccounts().get(i).getId()) {
					alreadySelected = true;
					break;
				}
			}
			checkedItems[i + 1] = alreadySelected;
		}

		ArrayList<String> items = new ArrayList<String>();
		items.add("All Accounts");
		for (Account account : m_model.getCurrentAccounts())
			items.add(account.getName());

		createDialog("Accounts", items, checkedItems, DialogType.Accounts).show();
	}

	private Dialog createDialog(String title, final ArrayList<String> items,
								final boolean[] checkedItems, final DialogType type) {
		final ArrayList<Object> mSelectedItems = new ArrayList<Object>(); // Where
		
		for (int i = 1; i < checkedItems.length; i++) {
			if (checkedItems[i] == true)
				mSelectedItems.add(i);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(m_view);
		// Set the dialog title
		builder.setTitle(title)
			// choices..
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
						m_model.getSelectedAccounts().clear();
					else if (type == DialogType.Categories)
						m_model.getSelectedCategories().clear();

					for (Object item : mSelectedItems) {
						if (type == DialogType.Accounts) {
							for (Account account : m_model.getCurrentAccounts()) {
								if (account.getName().equals(items.get(Integer
												  .parseInt(String.valueOf(item))))) {
									m_model.getSelectedAccounts().add(account);
									break;
								}
							}
						} else if (type == DialogType.Categories) {
							for (Category category : m_model.getCurrentCategories()) {
								if (Category.getCategoryName(category, m_model.getCurrentCategories()).equals(items.get(Integer
																									   .parseInt(String.valueOf(item))))) {
									m_model.getSelectedCategories().add(category);
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

	private void CancelClicked() {
		m_view.setResult(m_view.RESULT_CANCELED, new Intent());
		m_view.finish();
	}
}
