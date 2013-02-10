package sg.money;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	public static String ACTION_ACCOUNT = "ACTION_ACCOUNT";
	public static String ACTION_ADDTRANSACTION = "ACTION_ADDTRANSACTION";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			// Create some random data
			int number = (new Random().nextInt(100));

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			Log.w("WidgetExample", String.valueOf(number));
			// Set the text
			remoteViews.setTextViewText(R.id.accountName,
					String.valueOf(number));

			// Register an onClickListener
			Intent intent = new Intent(context, MyWidgetProvider.class);

			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			Intent intent1 = new Intent(context, MyWidgetProvider.class);
			intent1.setAction(ACTION_ACCOUNT);
			PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.accountName, pendingIntent1);

			Intent intent2 = new Intent(context, MyWidgetProvider.class);
			intent2.setAction(ACTION_ADDTRANSACTION);
			PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.addButton, pendingIntent2);

			//PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
			//		0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			// remoteViews.setOnClickPendingIntent(R.id.addButton,
			// pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
   
		if (intent.getAction().equals(ACTION_ACCOUNT)) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			remoteViews.setTextViewText(R.id.accountName, "ACTIONACCOUNT");
		}
		else if (intent.getAction().equals(ACTION_ADDTRANSACTION)) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			remoteViews.setTextViewText(R.id.accountName, "ACTIONADDTRANSACTION");
		}
	}
}