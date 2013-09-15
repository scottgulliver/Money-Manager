package sg.money.activities;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.os.Bundle;

import sg.money.adapters.BudgetNotificationReciever;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
