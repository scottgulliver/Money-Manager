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

    private ArrayList<DrawerArrayItem> m_items;
    private static LayoutInflater m_inflater;
    private ArrayList<Account> m_selectedItems;
    private Activity m_activity;
	
    final int COLOR_SELECTED = Color.rgb(133, 194, 215);
	
	
	/* Constructor */

    public DrawerArrayAdapter(Activity activity, ArrayList<DrawerArrayItem> items) {
    	m_activity = activity;
        m_items = items;
        m_inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_selectedItems = new ArrayList<Account>();
		items = new ArrayList<DrawerArrayItem>();
    }
	
	
	/* Methods */
 
    public int getCount() {
        return m_items.size();
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
		{
            vi = m_inflater.inflate(R.layout.drawer_item_layout, null);
		}

        TextView name = (TextView)vi.findViewById(R.id.drawer_item_name);
        ImageView icon = (ImageView)vi.findViewById(R.id.drawer_item_icon);

        DrawerArrayItem item = m_items.get(position);
 
        //set values
        name.setText(item.getText());
        icon.setImageResource(item.getImageId());

        return vi;
    }
}
