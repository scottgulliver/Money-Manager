package sg.money.adapters;

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
import java.util.ArrayList;

import sg.money.R;
import sg.money.domainobjects.Account;

public class DrawerArrayAdapter extends BaseAdapter {

    ArrayList<DrawerArrayItem> items = new ArrayList<DrawerArrayItem>();
    private static LayoutInflater inflater=null;
    private ArrayList<Account> selectedItems;
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
    private Activity activity;

    public DrawerArrayAdapter(Activity activity, ArrayList<DrawerArrayItem> items) {
    	this.activity = activity;
        this.items = items;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItems = new ArrayList<Account>();
    }
 
    public int getCount() {
        return items.size();
    }
    
    public void ClearSelected()
    {
    	selectedItems.clear();
    }
    
    public void SetSelected(int position, boolean selected)
    {
    	/*Account item = entries.get(position);
    	if (selected && !selectedItems.contains(item))
    		selectedItems.add(item);
    	else if (!selected && selectedItems.contains(item))
    		selectedItems.remove(item);*/
    }
    
    public ArrayList<Account> GetSelectedItems()
    {
    	return selectedItems;
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.drawer_item_layout, null);

        TextView name = (TextView)vi.findViewById(R.id.drawer_item_name);
        ImageView icon = (ImageView)vi.findViewById(R.id.drawer_item_icon);

        DrawerArrayItem item = items.get(position);
 
        //set values
        name.setText(item.getText());
        icon.setImageResource(item.getImageId());

        return vi;
    }
}