package com.formichelli.vineyard.gcm;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.VineyardMainActivity;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.VineyardServer;
import com.formichelli.vineyard.utilities.AsyncHttpRequest.Type;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class GcmClient {
	private static final String TAG = "GcmClient";
	private static final String SENDER_ID = "1088911131042";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	private static final String NOTIFICATION_ID = "notification_id";

	VineyardMainActivity activity;
	String regId;
	GoogleCloudMessaging gcm;

	public String getRegId() {
		return regId;
	}

	public GcmClient(VineyardMainActivity activity) {
		this.activity = activity;

		if (checkGooglePlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(activity);
			regId = getRegistrationId(activity);

			// regId not stored locally, obtain a new one
			if (regId == null)
				registerInBackground();
		}
	}

	public boolean checkGooglePlayServices() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity) == ConnectionResult.SUCCESS;
	}

	/**
	 * Gets the current registration ID for application on GCM service. If
	 * result is empty, the application needs to register.
	 * 
	 * @param context
	 *            application context
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return null;
		}

		// Check if application was updated; if so, it must clear the
		// registration ID
		// since the existing regID is not guaranteed to work with the new
		// application version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		if (registeredVersion != getAppVersion(context)) {
			Log.i(TAG, "App version changed.");
			return null;
		}

		return registrationId;
	}

	/**
	 * Remove the current registration ID from shared preference so next time it
	 * will be requested from GCM server
	 * 
	 * @param context
	 *            application context
	 */
	public void unregister(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		prefs.edit().remove(PROPERTY_REG_ID).apply();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		return activity.getSharedPreferences(
				VineyardMainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (gcm == null)
					gcm = GoogleCloudMessaging.getInstance(activity);

				if (regId != null)
					return true;

				try {
					regId = gcm.register(SENDER_ID);

					storeRegistrationId(activity, regId);
					return true;
				} catch (IOException e) {
					Log.e(TAG, e.getLocalizedMessage());
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result == false) {
					Toast.makeText(
							activity,
							activity.getString(R.string.gcm_error_no_registration_id),
							Toast.LENGTH_LONG).show();
					return;
				}

				sendRegistrationId();
			}

			private void sendRegistrationId() {
				if (regId != null) {
					ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair(NOTIFICATION_ID, regId));
					(new AsyncHttpRequest(
							activity.getServer().getUrl()
									+ VineyardServer.WORKERS_API
									+ activity.getUserId(), Type.PUT, params) {
						@Override
						protected void onPostExecute(
								Pair<Integer, String> response) {
							if (response == null || response.first != HttpStatus.SC_ACCEPTED) {
								Toast.makeText(
										activity,
										activity.getString(R.string.gcm_error_no_registration_id),
										Toast.LENGTH_LONG).show();
								Log.e(TAG, response.first + ": " + response.second);
							}
						}
					}).execute();
				}
			}

			private void storeRegistrationId(Context context, String regId) {
				getGCMPreferences(context).edit()
						.putString(PROPERTY_REG_ID, regId)
						.putInt(PROPERTY_APP_VERSION, getAppVersion(context))
						.apply();
			}
		}.execute();
	}
}
