package sg.money;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class BudgetsActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener
{
	static final int REQUEST_ADDBUDGET = 0;
	static final int REQUEST_SETTINGS = 1;

	ListView budgetsList;
	TextView txtMonth;
	ArrayList<Budget> budgets;
	BudgetListAdapter adapter;
	SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
	String currentMonth;
	ArrayList<Transaction> transactions;
	ActionMode actionMode;
	
	//Bundle State Data
	static final String STATE_MONTH = "stateMonth";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_budgets);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		budgetsList = (ListView) findViewById(R.id.budgetsList);
        txtMonth = (TextView)findViewById(R.id.txtMonth);
        
        View emptyView = findViewById(android.R.id.empty);
    	((TextView)findViewById(R.id.empty_text)).setText("No budgets");
    	((TextView)findViewById(R.id.empty_hint)).setText("Use the add button to create one.");
    	budgetsList.setEmptyView(emptyView);

        actionMode = null;
        budgetsList.setItemsCanFocus(false);
        budgetsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        budgetsList.setOnItemClickListener(this);
        budgetsList.setOnItemLongClickListener(this);
		
		if (savedInstanceState != null)
    	{
    		currentMonth = savedInstanceState.getString(STATE_MONTH);
        	setData(currentMonth);
    	}
    	else
    	{
        	setData("");
    	}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (actionMode == null)
		{
			onListItemClick(parent, view, position, id);
		}
		else
		{
			changeItemCheckState(position, budgetsList.isItemChecked(position));
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (actionMode == null) {
        	actionMode = startActionMode(new ModeCallback());
        }

		budgetsList.setItemChecked(position, !budgetsList.isItemChecked(position));
		changeItemCheckState(position, budgetsList.isItemChecked(position));
        
		return true;
	}
	
	public void changeItemCheckState(int position, boolean checked) {
        adapter.SetSelected(position, checked);
        adapter.notifyDataSetChanged();
    	final int checkedCount = adapter.GetSelectedItems().size();
        switch (checkedCount) {
            case 0:
                actionMode.setSubtitle(null);
                break;
            case 1:
            	actionMode.getMenu().clear();
            	actionMode.setSubtitle("" + checkedCount + " selected");
            	actionMode.getMenuInflater().inflate(R.menu.standard_cab, actionMode.getMenu());
                break;
            default:
            	actionMode.getMenu().clear();
            	actionMode.getMenuInflater().inflate(R.menu.standard_cab_multiple, actionMode.getMenu());
            	actionMode.setSubtitle("" + checkedCount + " selected");
                break;
        }
        
        if (adapter.GetSelectedItems().size() == 0)
        	actionMode.finish();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_MONTH, currentMonth);
        
        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_budgets, menu);
		return true;
	}

	private void setData(String month) {
		if (month.equals(""))
    	{ 
    		Calendar currentDate = Calendar.getInstance();
    	    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 1);
    	    
        	month = monthYearFormat.format(currentDate.getTime());
    	}
		
		txtMonth.setText(month);
    	currentMonth = month;
    	
    	Calendar startDate = new GregorianCalendar();
    	try {
			startDate.setTime(monthYearFormat.parse(month));
		} catch (ParseException e) {
			e.printStackTrace(); 
		}
    	Calendar endDate = (Calendar)startDate.clone();
    	endDate.add(Calendar.MONTH, 1);
		endDate.add(Calendar.SECOND, -1);
    	
    	transactions = DatabaseManager.getInstance(BudgetsActivity.this).GetAllTransactions(startDate.getTime(), endDate.getTime());
		budgets = DatabaseManager.getInstance(BudgetsActivity.this).GetAllBudgets();
		adapter = new BudgetListAdapter(this, budgets, transactions);
		budgetsList.setAdapter(adapter);
	}

	public class DateComparator implements Comparator<Transaction> {
	    public int compare(Transaction o1, Transaction o2) {
	        return o1.dateTime.compareTo(o2.dateTime);
	    }
	}
	
	private Dialog createDialog()
	{
		transactions = DatabaseManager.getInstance(BudgetsActivity.this).GetAllTransactions();
		Collections.sort(transactions, new DateComparator());
    	Collections.reverse(transactions);

    	final ArrayList<String> months = new ArrayList<String>();
    	if (transactions.size() == 0)
    	{
    		Calendar currentDate = Calendar.getInstance();
		    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    months.add(monthYearFormat.format(currentDate.getTime()));
    	}
    	else
    	{
	    	Transaction oldestTransaction = transactions.get(transactions.size()-1);
	    	Calendar oldestDate = Calendar.getInstance();
	    	oldestDate.setTime(oldestTransaction.dateTime);
	    	oldestDate.set(oldestDate.get(Calendar.YEAR), oldestDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    
		    Calendar currentDate = Calendar.getInstance();
		    currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1, 0, 0, 0);
		    //oldestDate.set(2012, 1, 1);
	    	while(true)
	    	{
	    		months.add(monthYearFormat.format(currentDate.getTime()));
	    		currentDate.add(Calendar.MONTH, -1);
	    		if (currentDate.before(oldestDate))
	    			break;
	    	}
    	}
    	
		AlertDialog.Builder builder = new AlertDialog.Builder(BudgetsActivity.this);
	    builder.setTitle("Select a month")
	           .setItems(months.toArray(new CharSequence[months.size()]), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int position) {
	               setData(months.get(position));
	           }
	    }).setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	    return builder.create();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

	    case android.R.id.home:
            Intent parentActivityIntent = new Intent(this, TransactionsActivity.class);
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
            
		case R.id.menu_addbudget: {
			Intent intent = new Intent(this, AddBudgetActivity.class);
			startActivityForResult(intent, REQUEST_ADDBUDGET);
			break;
			}
        
		case R.id.menu_viewcategories: {
			Intent intent = new Intent(this, CategoriesActivity.class);
			startActivity(intent);
			break;
			}
		
		case R.id.menu_month:{
    		createDialog().show();
    		break;
    		}

		case R.id.menu_settings: { 
        	startActivityForResult(new Intent(BudgetsActivity.this, SettingsActivity.class), REQUEST_SETTINGS);
			break;
			}
		}
		return true;
	}

	protected void onListItemClick(AdapterView<?> l, View v, int position, long id) {
    	EditItem(budgets.get(position));
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ADDBUDGET: {
			if (resultCode == RESULT_OK) {
				setData(currentMonth);
			}
			break;
		}
		case REQUEST_SETTINGS:
			setData(currentMonth);
			break;
		}
	}
	
	private void EditItem(Budget selectedItem)
	{			
		Intent intent = new Intent(this, AddBudgetActivity.class);
		intent.putExtra("ID", selectedItem.id);
		startActivityForResult(intent, REQUEST_ADDBUDGET);
	}
	
	private void confirmDeleteItems(final ActionMode mode)
	{
		Misc.showConfirmationDialog(this, 
				adapter.GetSelectedItems().size() == 1 
					? "Delete 1 budget?"
					: "Delete " + adapter.GetSelectedItems().size() + " budgets?", 
				new OnClickListener() { public void onClick(DialogInterface dialog, int which) {
						DeleteItems();
	                    mode.finish();
					}
				},
				new OnClickListener() { public void onClick(DialogInterface dialog, int which) {
                    mode.finish();
				}
			});
	}
	
	private void DeleteItems()
	{
		ArrayList<Budget> selectedItems = adapter.GetSelectedItems();
		for(Budget selectedItem : selectedItems)
		{
			DatabaseManager.getInstance(BudgetsActivity.this).DeleteBudget(selectedItem);
		}
		setData(currentMonth);
	}
	
	private final class ModeCallback implements ActionMode.Callback {
   	 
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.standard_cab, menu);
            return true;
        }
 
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
 
        public void onDestroyActionMode(ActionMode mode) {
			adapter.ClearSelected();
	        adapter.notifyDataSetChanged();
	        budgetsList.clearChoices();
 
            if (mode == actionMode) {
            	actionMode = null;
            }
        }
 
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.cab_edit:
            	EditItem(adapter.GetSelectedItems().get(0));
                mode.finish();
                return true;
            case R.id.cab_delete:
            	confirmDeleteItems(mode);
                return true;
            default:
                return false;
        }
        }
    };
}
