package sg.money.activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.os.Bundle;
import sg.money.common.BudgetNotificationReciever;

/**
 * Forms the base of all fragment activities in the app.
 */
public abstract class BaseFragmentActivity extends SherlockFragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
