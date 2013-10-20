package sg.money.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

public abstract class BaseListAdapter<TModel> extends BaseAdapter
{
    protected ArrayList<TModel> m_items;
    private static LayoutInflater m_inflater;
    protected ArrayList<TModel> m_selectedItems;
    protected Activity m_activity;

    protected final int COLOR_SELECTED = Color.rgb(133, 194, 215);


    /* Constructor */

    public BaseListAdapter(Activity activity, ArrayList<TModel> items) {
        m_activity = activity;
        m_items = items;
        m_inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_selectedItems = new ArrayList<TModel>();
    }


    /* Abstract methods */

    protected abstract int getLayoutResourceId();

    protected abstract void buildView(View view, TModel item);


    /* Methods */

    public int getCount() {
        return m_items.size();
    }

    public void clearSelected()
    {
        m_selectedItems.clear();
    }

    public void setSelected(int position, boolean selected)
    {
        TModel item = m_items.get(position);
        if (selected && !m_selectedItems.contains(item))
        {
            m_selectedItems.add(item);
        }
        else if (!selected && m_selectedItems.contains(item))
        {
            m_selectedItems.remove(item);
        }
    }

    public ArrayList<TModel> getSelectedItems()
    {
        return m_selectedItems;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null)
        {
            view = m_inflater.inflate(getLayoutResourceId(), null);
        }

        buildView(view, m_items.get(position));
        return view;
    }
}