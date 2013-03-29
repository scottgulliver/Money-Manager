package sg.money;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.os.Bundle;
 
public abstract class BaseFragmentActivity extends SherlockFragmentActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BudgetNotificationReciever.setUpEvents(this, false);
	}
}
