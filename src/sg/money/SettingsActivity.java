package sg.money;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;

public class SettingsActivity extends Activity
{	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
