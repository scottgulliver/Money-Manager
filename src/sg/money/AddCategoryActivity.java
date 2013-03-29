package sg.money;

import java.util.ArrayList;
import java.util.Random;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class AddCategoryActivity extends BaseActivity implements ColorPickerDialog.OnColorChangedListener
{
	ArrayList<Category> currentCategories;
	EditText txtName;
	Spinner spnType;
	ImageView imgColor;
	ArrayList<String> options;
	Category editCategory; 
	int currentColor;
	
	//Bundle State Data
	static final String STATE_COLOUR = "stateColour";
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_add_category);
    	setTitle("Add Category");

    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          
        txtName = (EditText)findViewById(R.id.txtName);
        spnType = (Spinner)findViewById(R.id.spnType1);
        imgColor = (ImageView)findViewById(R.id.imgColor);
        
        imgColor.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) { ColorClicked(); } });
        
        options = new ArrayList<String>();
        options.add("Expense");
        options.add("Income");
        
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this,android.R.layout.simple_spinner_dropdown_item, options);

		spnType.setAdapter(arrayAdapter);
		

		if (savedInstanceState != null)
    	{
			currentColor = savedInstanceState.getInt(STATE_COLOUR);
    	}
    	else
    	{
    		Random rnd = new Random(System.currentTimeMillis());
    		currentColor = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
    	}
        
        //check if we are editing
		editCategory = null;
        int editId = getIntent().getIntExtra("ID", -1);
        if (editId != -1)
        {
        	editCategory = DatabaseManager.getInstance(AddCategoryActivity.this).GetCategory(editId);
        	txtName.setText(editCategory.name);
        	spnType.setSelection(editCategory.income?1:0);
        	currentColor = editCategory.color;
        	setTitle("Edit Category");
        	
        	if (editCategory.isPermanent)
        	{
        		txtName.setFocusable(false);
        		txtName.setFocusableInTouchMode(false);
        		txtName.setClickable(false);
        		spnType.setFocusable(false);
        		spnType.setFocusableInTouchMode(false);
        		spnType.setClickable(false);
        	}
        }

    	imgColor.setBackgroundColor(currentColor);
    	
    	currentCategories = DatabaseManager.getInstance(this).GetAllCategories();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_COLOUR, currentColor);
        
        super.onSaveInstanceState(savedInstanceState);
    }
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_add_category, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	    	case R.id.menu_acceptcategory:{
	    		OkClicked();
	    		break;
	    		}

	    	case R.id.menu_rejectcategory:{
	    		CancelClicked();
	    		break;
	    		}	    	
	    	
	        case R.id.menu_settings:{
                break;
            	}

	        case android.R.id.home: // up button
	            Intent intent = new Intent(this, CategoriesActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	    }
	    return true;
	}
    
    private void ColorClicked()
    {
    	//launch the color picker
    	new ColorPickerDialog(this, this, currentColor).show();
    }
    
    private boolean Validate()
    {
    	if (txtName.getText().toString().trim().equals(""))
    	{
    		Toast.makeText(AddCategoryActivity.this, "Please enter a name.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if (txtName.getText().toString().trim().equals(AddTransactionActivity.ADD_CATEGORY_STRING))
    	{
    		Toast.makeText(AddCategoryActivity.this, "This name is not valid.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	
    	if (editCategory == null || !editCategory.isPermanent)
    	{
	    	for(Category currentCategory : currentCategories)
	    	{
	    		if (editCategory != null && (currentCategory.id == editCategory.id)) continue;
	    		
	    		if (txtName.getText().toString().trim().equals(currentCategory.name.trim())
					&& currentCategory.income == (spnType.getSelectedItemId() == 1))
	        	{
	        		Toast.makeText(AddCategoryActivity.this, "A category with this name already exists.", Toast.LENGTH_SHORT).show();
	        		return false;
	        	}
	    	}
    	}
    	
    	return true;
    }
    
    private void OkClicked()
    {
    	if (!Validate())
    		return;
    	
		if (editCategory == null)
    	{
			//create the category
	    	Category newCategory = new Category();
	    	newCategory.name = txtName.getText().toString().trim();
	    	newCategory.income = (spnType.getSelectedItemId() == 1);
	    	newCategory.color = currentColor;
	    	
			DatabaseManager.getInstance(AddCategoryActivity.this).AddCategory(newCategory);
    	}
    	else
    	{
	    	//edit the category	    	
    		editCategory.name = txtName.getText().toString().trim();
    		editCategory.income = (spnType.getSelectedItemId() == 1);
    		editCategory.color = currentColor;
			DatabaseManager.getInstance(AddCategoryActivity.this).UpdateCategory(editCategory);
    	}
    	
        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void CancelClicked()
    {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

	public void colorChanged(int color) {
		currentColor = color;
		imgColor.setBackgroundColor(color);
	}
}
