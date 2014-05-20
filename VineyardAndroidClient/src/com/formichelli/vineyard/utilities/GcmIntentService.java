package com.formichelli.vineyard.utilities;

import org.json.JSONException;
import org.json.JSONObject;

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
				Log.i(TAG, "Received: " + extras.toString());
				sendNotification(extras.toString());
			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg) {
		String title, description;
		JSONObject msgObject;

		try {
			msgObject = new JSONObject(msg);
			title = msgObject.getString(TITLE);
			description = msgObject.getString(DESCRIPTION);
		} catch (JSONException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return;
		}

		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, VineyardMainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(description).setContentIntent(contentIntent);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}