package sg.money.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import sg.money.DatabaseManager;
import sg.money.R;
import sg.money.domainobjects.Budget;
import sg.money.fragments.BudgetsFragment;

public class BudgetNotificationReciever extends BroadcastReceiver {
	private static final int DAILY_NOTIFICATION   = 843254;
	
	public static void setUpEvents(Context context, boolean deleteExisting)
	{
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, BudgetNotificationReciever.class);
		intent.putExtra("alarmType", DAILY_NOTIFICATION);
		
		if (!deleteExisting)
		{
			if (PendingIntent.getBroadcast(context, DAILY_NOTIFICATION, intent, PendingIntent.FLAG_NO_CREATE) != null)
				return;
		}
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DAILY_NOTIFICATION, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		//set start to be 1:00am tomorrow
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, 1);
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000*60*60*24 , pendingIntent);	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			Integer alarmType = bundle.getInt("alarmType");
			
			if (alarmType == DAILY_NOTIFICATION)
			{
				Calendar today = Calendar.getInstance();
				int highestNotifyType = 0;
				String title = null;
				
				Calendar startDate = Calendar.getInstance();
				Calendar endDate = Calendar.getInstance();
				startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), 1, 0, 0, 0);
				
				if (today.get(Calendar.DAY_OF_MONTH) == 1) //monthly notification
				{
					highestNotifyType = 3;
					title = "Monthly budget overview";
					startDate.add(Calendar.MONTH, -1);
				}
				else if (today.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) //weekly notification
				{
					highestNotifyType = 2;
					title = "Weekly budget overview";
				}
				else //daily notification
				{
					highestNotifyType = 1;
					title = "Daily budget overview";
				}
				
				endDate = (Calendar)startDate.clone();
				endDate.add(Calendar.MONTH, 1);
				endDate.add(Calendar.SECOND, -1);

				ArrayList<Budget> budgets = DatabaseManager.getInstance(context).GetAllBudgets();
				ArrayList<Budget> budgetsToShow = new ArrayList<Budget>();
				
				for(Budget budget : budgets)
				{
					if (budget.notifyType > 0 && budget.notifyType <= highestNotifyType)
						budgetsToShow.add(budget);
				}

				boolean onlyDailyBudgets = true;
				ArrayList<String> linesToShow = new ArrayList<String>();
				for(Budget budget : budgetsToShow)
				{
					linesToShow.add(budget.name + " - " + budget.getCompletePercentage(context, startDate, endDate));
					
					if (budget.notifyType > 1)
						onlyDailyBudgets = false;
				}
				
				if (onlyDailyBudgets)
					title = "Daily budget overview";
				
				String smallText = "Expand to see budgets";
				
				if (!budgetsToShow.isEmpty())
					createNotification(context, title, smallText, linesToShow);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("InlinedApi")
	private void createNotification(Context context, String title, String smallText, ArrayList<String> linesToShow) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.statusbar_icon)
			.setContentTitle(title)
			.setContentText(smallText)
			.setContentInfo(String.valueOf(linesToShow.size()))
			.setAutoCancel(true);
		
		if (Build.VERSION.RELEASE.compareTo("4.1") >= 0)
			mBuilder.setPriority(Notification.PRIORITY_LOW);
		
		if (linesToShow.size() == 1)
		{
			mBuilder.setContentText(linesToShow.get(0));
			mBuilder.setContentInfo("");
		}
		else
		{
			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			for (int i=0; i < linesToShow.size(); i++) {
			    inboxStyle.addLine(linesToShow.get(i));
			} 
			mBuilder.setStyle(inboxStyle);
		}

		Intent resultIntent = new Intent(context, BudgetsFragment.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(BudgetsFragment.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		int mId = 0; // mId allows you to update the notification later on.
		mNotificationManager.notify(mId, mBuilder.build());
	}
}
