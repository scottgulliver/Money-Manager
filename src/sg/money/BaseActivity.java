package sg.money;

import com.actionbarsherlock.app.SherlockActivity;
import android.os.Bundle;

public abstract class BaseActivity extends SherlockActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
