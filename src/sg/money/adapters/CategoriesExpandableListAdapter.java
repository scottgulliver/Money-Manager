package sg.money.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import sg.money.R;
import sg.money.domainobjects.Category;
import sg.money.utils.Misc;

public class CategoriesExpandableListAdapter extends BaseExpandableListAdapter 
{

    private ArrayList<Category> categories;
    private HashMap<Category, ArrayList<Category>> groupings;
    private ArrayList<String> customStrings; 
    private static LayoutInflater inflater=null;
    private ArrayList<Category> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
    private Activity activity;
 
    public CategoriesExpandableListAdapter(Activity activity, ArrayList<Category> categories) {
    	this.activity = activity;
        this.categories = categories;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Category>();
        buildGroupings();
    }
 
    public CategoriesExpandableListAdapter(Activity activity, ArrayList<Category> categories, ArrayList<String> customStrings) {
    	this.activity = activity;
        this.categories = categories;
        this.customStrings = customStrings;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Category>();
        buildGroupings();
    }
    
    private void buildGroupings()
    {
    	groupings = new HashMap<Category, ArrayList<Category>>();
    	for(Category category : categories)
    	{
    		if (category.parentCategoryId == -1)
    			groupings.put(category, new ArrayList<Category>());
    	}
    	for(Category category : categories)
    	{
    		if (category.parentCategoryId != -1)
    		{
    			for(Category parentCategory : groupings.keySet())
    			{
    				if (parentCategory.id == category.parentCategoryId)
    				{
    					groupings.get(parentCategory).add(category);
    					break;
    				}
    			}
    		}
    	}
    }
    
    private ArrayList<Category> getOrderedGroups()
    {
    	ArrayList<Category> arrayList = new ArrayList<Category>(groupings.keySet());
    	Collections.sort(arrayList, new CategoryComparator());
    	return arrayList;
    }
    
    private class CategoryComparator implements Comparator<Category> {
	    public int compare(Category o1, Category o2) {
	        return Double.compare(o1.id, o2.id);
	    }
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
    	int gpPos = getGroupPosition(position);
    	int chPos = getChildPosition(position);
    	
    	Category item = (Category)(chPos == -1 ? getGroup(gpPos) : getChild(gpPos, chPos));
    	if (selected && !selectedItems.contains(item))
    		selectedItems.add(item);
    	else if (!selected && selectedItems.contains(item))
    		selectedItems.remove(item);
    }
    
    public ArrayList<Category> GetSelectedItems()
    {
    	return selectedItems;
    }

	public Object getChild(int groupPosition, int childPosition) {
		Category parentCategory = getOrderedGroups().get(groupPosition);
		return groupings.get(parentCategory).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		Category parentCategory = getOrderedGroups().get(groupPosition);
		return groupings.get(parentCategory).get(childPosition).id;
	}

	public int getChildrenCount(int groupPosition) {
		Category parentCategory = getOrderedGroups().get(groupPosition);
		return groupings.get(parentCategory).size();
	}

	public Object getGroup(int groupPosition) {
		return getOrderedGroups().get(groupPosition);
	}

	public int getGroupCount() {
		return groupings.size();
	}

	public long getGroupId(int groupPosition) {
		return getOrderedGroups().get(groupPosition).id;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Category category = (Category)getGroup(groupPosition);
		return getView(category, convertView, parent, isExpanded);
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Category category = (Category)getChild(groupPosition, childPosition);
		return getView(category, convertView, parent, false);
	}
	
	public View getView(Category category, View convertView, ViewGroup parent, boolean isExpanded) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.category_item_layout, null);

        TextView nameText = (TextView)vi.findViewById(R.id.category_name);
        TextView typeText = (TextView)vi.findViewById(R.id.category_type);
        ImageView colorField = (ImageView)vi.findViewById(R.id.category_color);

        //set values
        nameText.setText(category.name);
        typeText.setText(category.income ? "Income" : "Expense");
        
        colorField.setBackgroundColor(category.color);
        
    	RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)colorField.getLayoutParams();
    	int leftMargin = category.parentCategoryId == -1 ? params.rightMargin : (int) Misc.dipsToPixels(activity.getResources(), 40);
    	params.setMargins(leftMargin, params.topMargin, params.bottomMargin, params.rightMargin);
    	colorField.setLayoutParams(params);
    
        if (selectedItems.contains(category))
        	vi.setBackgroundColor(COLOR_SELECTED);
        else
        	vi.setBackgroundColor(Color.TRANSPARENT);

        return vi;
    }

	public int getPosition(int groupPosition, int childPosition) {
		int position = 0;//groupPosition*(CHILDREN + 1) + childPosition + 1;
		for(int i = 0; i < groupPosition; i++)
		{
			position++;
			position += getChildrenCount(i);
		}
		position++;
		position += (childPosition+1);
		return position-1;
	}
	
	public int getGroupPosition(int position)
	{
		int upTo = -1;
		for(int i = 0; i < getGroupCount(); i++)
		{
			upTo++;
			upTo += getChildrenCount(i);
			if (upTo >= position)
				return i;
		}
		return -1;
	}
	
	public int getChildPosition(int position)
	{
		int upTo = -1;
		for(int i = 0; i < getGroupCount(); i++)
		{
			upTo++;
			if (upTo == position)
				return -1;
			for(int c = 0; c < getChildrenCount(i); c++)
			{
				upTo++;
				if (upTo == position)
					return c;
			}
		}
		return -1;
	}
}
