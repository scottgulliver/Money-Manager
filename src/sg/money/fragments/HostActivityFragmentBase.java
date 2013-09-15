package sg.money.fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import sg.money.activities.ParentActivity;

public abstract class HostActivityFragmentBase extends Fragment {

    public HostActivityFragmentBase()
    {
    }

    protected ParentActivity getParentActivity()
    {
        return (ParentActivity)super.getActivity();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu, boolean drawerIsOpen) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected ActionMode getActionMode()
    {
        return getParentActivity().getActionMode();
    }

    protected void setActionMode(ActionMode actionMode)
    {
        getParentActivity().setActionMode(actionMode);
    }
}
