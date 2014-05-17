package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import com.formichelli.vineyard.R;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class SendImagesIntent extends IntentService {
	private static final String TAG = "SendImageIntent";

	public static final String SERVER_URL = "serverUrl";
	public static final String IMAGES = "images";
	public static final String IMAGE_SENT = "sent";
	public static final String ISSUE_ID = "issueId";
	public static final String PHOTO_NAME = "photoName";

	public SendImagesIntent() {
		super("SendImagesIntent");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String serverUrl = intent.getExtras().getString(SERVER_URL);
		int issueId = intent.getIntExtra(ISSUE_ID, -1);
		ArrayList<String> images = intent.getExtras().getStringArrayList(IMAGES);
		boolean error = false;

		for (String path : images) {
			Log.i(TAG, "Sending image" + path + " to the server...");
			File image = new File(path);

			HttpPost request = new HttpPost(serverUrl);
			try {
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
						.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addBinaryBody("photo", image);
				HttpEntity entity = entityBuilder.build();
				request.setEntity(entity);

				HttpResponse response = new DefaultHttpClient().execute(request);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
					// Send broadcast to main activity
					Intent photoUploadedIntent = new Intent("IMAGE_SENT");
					photoUploadedIntent.putExtra(ISSUE_ID, issueId);
					photoUploadedIntent.putExtra(ISSUE_ID, getPhotoName(response));
					LocalBroadcastManager.getInstance(this).sendBroadcast(photoUploadedIntent);
					Log.i(TAG, "image " + path + " sent!");
				} else {
					error = true;
					Log.e(TAG, "An error occurred while sending the image: "
							+ response.getStatusLine().getStatusCode());
				}

			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, e.getLocalizedMessage());

				error = true;
			}

			File f = new File(path);
			if (f != null)
				f.delete();
		}

		if (!error)
			Toast.makeText(this,
					getString(R.string.issue_report_sending_images_completed),
					Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this,
					getString(R.string.issue_report_sending_images_error),
					Toast.LENGTH_LONG).show();

	}
	
	private String getPhotoName(HttpResponse response) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			response.getEntity().writeTo(out);
			out.close();
		} catch (IOException e) {
			return null;
		}

		return out.toString();
	}
}
