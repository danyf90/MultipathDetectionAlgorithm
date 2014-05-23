package com.formichelli.vineyard.gcm;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.VineyardMainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String PLACE_ID = "placeId";
	private static final String TASK_ID = "id";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)
				&& !extras.isEmpty())
			sendNotification(extras);

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(Bundle extras) {
		String title = extras.getString(TITLE);
		String description = extras.getString(DESCRIPTION);
		int placeId = Integer.valueOf(extras.getString(PLACE_ID));
		int taskId = Integer.valueOf(extras.getString(TASK_ID));
		int issueOrTask;

		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, VineyardMainActivity.class);
		intent.putExtra(VineyardMainActivity.PLACE_ID, placeId);

		if (title.compareTo("issue") == 0)
			issueOrTask = 0;
		else if (title.compareTo("task") == 0)
			issueOrTask = 1;
		else
			issueOrTask = -1;

		switch (issueOrTask) {
		case 0:
			title = getResources().getString(R.string.notification_new_issue);
			intent.putExtra(VineyardMainActivity.ISSUE_ID, taskId);
			break;
		case 1:
			title = getResources().getString(R.string.notification_new_task);
			intent.putExtra(VineyardMainActivity.TASK_ID, taskId);
			break;
		default:
			title = "unexpected notification type: " + title;
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setAutoCancel(true)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(title)
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText(description))
				.setContentText(description).setContentIntent(contentIntent);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if (v != null) {
			final long pattern[] = { 0, 100, 100, 100, 100, 100 };
			v.vibrate(pattern, -1);
		}
	}
}