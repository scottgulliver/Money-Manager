package sg.money;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
