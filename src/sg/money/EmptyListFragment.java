package sg.money;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
 
public class EmptyListFragment extends Fragment
{
	String bundleText;
	TextView emptyText;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundleText = getArguments().getString("EmptyText");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	View v = inflater.inflate(R.layout.empty_listview, null);
    	
    	emptyText = (TextView)v.findViewById(R.id.empty_text);
    	
    	if (bundleText != null)
    		setEmptyText(bundleText);
    	
        return v;
    }
    
    public void setEmptyText(String text)
    {
    	emptyText.setText(text);
    }
}
