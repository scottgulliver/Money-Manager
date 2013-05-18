package sg.money;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryListAdapter extends BaseAdapter {
 
    private ArrayList<Category> categories; 
    private ArrayList<String> customStrings; 
    private static LayoutInflater inflater=null;
    private ArrayList<Category> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
    private Activity activity;
 
    public CategoryListAdapter(Activity activity, ArrayList<Category> categories) {
    	this.activity = activity;
        this.categories = categories;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Category>();
    }
 
    public CategoryListAdapter(Activity activity, ArrayList<Category> categories, ArrayList<String> customStrings) {
        this.categories = categories;
        this.customStrings = customStrings;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Category>();
    }
 
    public int getCount() {
        return categories.size();
    }
    
    public void ClearSelected()
    {
    	selectedItems.clear();
    }
    
    public void SetSelected(int position, boolean selected)
    {
    	Category item = (Category)getItem(position);
    	if (selected && !selectedItems.contains(item))
    		selectedItems.add(item);
    	else if (!selected && selectedItems.contains(item))
    		selectedItems.remove(item);
    }
    
    public ArrayList<Category> GetSelectedItems()
    {
    	return selectedItems;
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    private String getCategoryName(Category category)
    {
    	return Misc.getCategoryName(category, activity);
    }
    
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.category_item_layout, null);

        TextView nameText = (TextView)vi.findViewById(R.id.category_name);
        TextView typeText = (TextView)vi.findViewById(R.id.category_type);
        ImageView colorField = (ImageView)vi.findViewById(R.id.category_color);
 
        Category category = categories.get(position);

        //set values
        nameText.setText(getCategoryName(category));
 
        if (customStrings == null)
        	typeText.setText(category.income ? "Income" : "Expense");
        else
        	typeText.setText(customStrings.get(position));
        
        colorField.setBackgroundColor(category.color);
        
        if (selectedItems.contains(category))
        	vi.setBackgroundColor(COLOR_SELECTED);
        else
        	vi.setBackgroundColor(Color.TRANSPARENT);

        return vi;
    }
}