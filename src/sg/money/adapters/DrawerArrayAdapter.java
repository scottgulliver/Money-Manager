package sg.money.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import sg.money.R;
import sg.money.domainobjects.DrawerArrayItem;

public class DrawerArrayAdapter extends BaseListAdapter<DrawerArrayItem> {

	/* Constructor */

    public DrawerArrayAdapter(Activity activity, ArrayList<DrawerArrayItem> items) {
    	super(activity, items);
    }
	
	
	/* Methods */

    @Override
    protected int getLayoutResourceId() {
        return R.layout.drawer_item_layout;
    }

    @Override
    protected void buildView(View view, DrawerArrayItem item) {
        TextView name = (TextView)view.findViewById(R.id.drawer_item_name);
        ImageView icon = (ImageView)view.findViewById(R.id.drawer_item_icon);

        //set values
        name.setText(item.getText());
        icon.setImageResource(item.getImageId());
    }
}
