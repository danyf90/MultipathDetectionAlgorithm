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
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";

	public static final int NOTIFICATION_ID = 1;
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String PLACE_ID = "placeId";
	private static final String TASK_ID = "id";

	private enum NotificationType {
		ISSUE_INSERTION, ISSUE_MODIFICATION, ISSUE_RESOLVED, TASK_INSERTION, TASK_MODIFICATION, TASK_RESOLVED, UNKNOWN
	};

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		Log.i(TAG, "GCM message received: " + extras.toString());

		if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)
				&& !extras.isEmpty())
			sendNotification(extras);

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(Bundle extras) {
		NotificationType notificationType;
		String title = extras.getString(TITLE);
		String description = extras.getString(DESCRIPTION);
		int placeId = Integer.valueOf(extras.getString(PLACE_ID));
		int taskId = Integer.valueOf(extras.getString(TASK_ID));
		boolean showIssuesNotifications, showTasksNotifications;
		Context context = getApplicationContext();

		showIssuesNotifications = PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean(
						context.getString(R.string.prefs_issues_notifications),
						true);

		showTasksNotifications = PreferenceManager.getDefaultSharedPreferences(
				context).getBoolean(
				context.getString(R.string.prefs_tasks_notifications), true);

		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, VineyardMainActivity.class);
		intent.putExtra(VineyardMainActivity.PLACE_ID, placeId);

		notificationType = getNotificationType(title);

		switch (notificationType) {
		case ISSUE_INSERTION:
			if (!showIssuesNotifications)
				return;
			title = getResources().getString(R.string.notification_issue_insertion);
			intent.putExtra(VineyardMainActivity.ISSUE_ID, taskId);
			break;

		case ISSUE_MODIFICATION:
			if (!showIssuesNotifications)
				return;
			title = getResources().getString(R.string.notification_issue_modification);
			intent.putExtra(VineyardMainActivity.ISSUE_ID, taskId);
			break;
		case ISSUE_RESOLVED:
			if (!showIssuesNotifications)
				return;
			title = getResources().getString(R.string.notification_issue_resolved);
			break;
		case TASK_INSERTION:
			if (!showTasksNotifications)
				return;
			title = getResources().getString(R.string.notification_task_insertion);
			intent.putExtra(VineyardMainActivity.TASK_ID, taskId);
			break;
		case TASK_MODIFICATION:
			if (!showTasksNotifications)
				return;
			title = getResources().getString(R.string.notification_task_modification);
			intent.putExtra(VineyardMainActivity.TASK_ID, taskId);
			break;
		case TASK_RESOLVED:
			if (!showTasksNotifications)
				return;
			title = getResources().getString(R.string.notification_task_resolved);
			break;
		case UNKNOWN:
			title = "unexpected notification type: " + title;
			Log.e(TAG, "notification type: " + title);
			break;
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

	private NotificationType getNotificationType(String title) {

		if (title.compareTo("issue-insertion") == 0)
			return NotificationType.ISSUE_INSERTION;

		else if (title.compareTo("issue-modification") == 0)
			return NotificationType.ISSUE_MODIFICATION;

		else if (title.compareTo("issue-resolved") == 0)
			return NotificationType.ISSUE_RESOLVED;

		else if (title.compareTo("task-insertion") == 0)
			return NotificationType.TASK_INSERTION;

		else if (title.compareTo("task-modification") == 0)
			return NotificationType.TASK_MODIFICATION;

		else if (title.compareTo("task-resolved") == 0)
			return NotificationType.TASK_RESOLVED;
		
		return NotificationType.UNKNOWN;
	}
}