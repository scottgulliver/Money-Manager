package sg.money.activities;

import com.actionbarsherlock.app.SherlockActivity;
import android.os.Bundle;
import sg.money.common.BudgetNotificationReciever;

/**
* Forms the base of all activities in the app.
*/
public abstract class BaseActivity extends SherlockActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
