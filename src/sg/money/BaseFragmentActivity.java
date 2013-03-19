package sg.money;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
 
public abstract class BaseFragmentActivity extends FragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
