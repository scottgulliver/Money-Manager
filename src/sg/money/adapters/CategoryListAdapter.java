package sg.money.adapters;

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
import sg.money.R;
import sg.money.domainobjects.Category;
import sg.money.utils.Misc;

public class CategoryListAdapter extends BaseAdapter {
 
    private ArrayList<Category> m_categories;
    private ArrayList<String> m_customStrings; 
    private static LayoutInflater m_inflater;
    private ArrayList<Category> m_selectedItems;
    private Activity m_activity;
	
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
	
	
	/* Constructors */
 
    public CategoryListAdapter(Activity activity, ArrayList<Category> categories) {
    	m_activity = activity;
        m_categories = categories;
        m_inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_selectedItems = new ArrayList<Category>();
    }
 
    public CategoryListAdapter(Activity activity, ArrayList<Category> categories, ArrayList<String> customStrings) {
        m_categories = categories;
        m_customStrings = customStrings;
        m_inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_selectedItems = new ArrayList<Category>();
    }
	
	
	/* Methods */
 
    public int getCount() {
        return m_categories.size();
    }
    
    public void ClearSelected()
    {
    	m_selectedItems.clear();
    }
    
    public void SetSelected(int position, boolean selected)
    {
    	Category item = (Category)getItem(position);
    	if (selected && !m_selectedItems.contains(item))
		{
    		m_selectedItems.add(item);
		}
    	else if (!selected && m_selectedItems.contains(item))
		{
    		m_selectedItems.remove(item);
		}
    }
    
    public ArrayList<Category> GetSelectedItems()
    {
    	return m_selectedItems;
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    private String getCategoryName(Category category)
    {
    	return Category.getCategoryName(category, m_activity);
    }
    
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
		{
            vi = m_inflater.inflate(R.layout.category_item_layout, null);
		}

        TextView nameText = (TextView)vi.findViewById(R.id.category_name);
        TextView typeText = (TextView)vi.findViewById(R.id.category_type);
        ImageView colorField = (ImageView)vi.findViewById(R.id.category_color);
 
        Category category = m_categories.get(position);

        //set values
        nameText.setText(getCategoryName(category));
 
        if (m_customStrings == null)
		{
        	typeText.setText(category.isIncome() ? "Income" : "Expense");
		}
        else
		{
        	typeText.setText(m_customStrings.get(position));
		}
        
        colorField.setBackgroundColor(category.getColor());
        
        if (m_selectedItems.contains(category))
		{
        	vi.setBackgroundColor(COLOR_SELECTED);
		}
        else
		{
        	vi.setBackgroundColor(Color.TRANSPARENT);
		}

        return vi;
    }
}
