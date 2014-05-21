package com.formichelli.vineyard.utilities;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.VineyardMainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";
	public static final int NOTIFICATION_ID = 1;
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
				for (String key : extras.keySet())
					Log.i(TAG, "Received: " + key);
				sendNotification(extras.getString(TITLE),
						extras.getString(DESCRIPTION));
			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);

	}

	private void sendNotification(String title, String description) {
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, VineyardMainActivity.class), 0);

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
	}
}