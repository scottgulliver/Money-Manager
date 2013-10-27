package sg.money.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import sg.money.R;

public class EmptyListFragment extends Fragment
{
	private String m_bundleText;
    private String m_bundleHint;
    private TextView m_emptyText;
    private TextView m_emptyHint;


    /* Fragment overrides */
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        m_bundleText = getArguments().getString("EmptyText");
        m_bundleHint = getArguments().getString("EmptyHint");
        if (m_bundleHint == null)
        	m_bundleHint = "";
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	View v = inflater.inflate(R.layout.empty_listview, null);
    	
        m_bundleText = getArguments().getString("EmptyText");
        m_bundleHint = getArguments().getString("EmptyHint");
        if (m_bundleHint == null)
        	m_bundleHint = "";
    	
    	m_emptyText = (TextView)v.findViewById(R.id.empty_text);
    	m_emptyHint = (TextView)v.findViewById(R.id.empty_hint);
    	
        m_bundleText = getArguments().getString("EmptyText");
        m_bundleHint = getArguments().getString("EmptyHint");
        if (m_bundleHint == null)
        	m_bundleHint = "";
    	
    	if (m_bundleText != null)
    		setEmptyText(m_bundleText);
    	
    	if (m_bundleHint != null)
    		setEmptyHint(m_bundleHint);
    	
        return v;
    }


    /* Methods */
    
    public void setEmptyText(String text)
    {
    	m_emptyText.setText(text);
    }
    
    public void setEmptyHint(String text)
    {
    	m_emptyHint.setText(text);
    }
}
