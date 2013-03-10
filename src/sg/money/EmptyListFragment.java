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
	String bundleHint;
	TextView emptyText;
	TextView emptyHint;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bundleText = getArguments().getString("EmptyText");
        bundleHint = getArguments().getString("EmptyHint", "");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	View v = inflater.inflate(R.layout.empty_listview, null);
    	
        bundleText = getArguments().getString("EmptyText");
        bundleHint = getArguments().getString("EmptyHint", "");
    	
    	emptyText = (TextView)v.findViewById(R.id.empty_text);
    	emptyHint = (TextView)v.findViewById(R.id.empty_hint);
    	
        bundleText = getArguments().getString("EmptyText");
        bundleHint = getArguments().getString("EmptyHint", "");
    	
    	if (bundleText != null)
    		setEmptyText(bundleText);
    	
    	if (bundleHint != null)
    		setEmptyHint(bundleHint);
    	
        return v;
    }
    
    @Override
    public void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    
    public void setEmptyText(String text)
    {
    	emptyText.setText(text);
    }
    
    public void setEmptyHint(String text)
    {
    	emptyHint.setText(text);
    }
}
