package com.formichelli.vineyard.utilities;

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
import android.util.Log;
import android.widget.Toast;

public class SendImagesIntent extends IntentService {
	private static final String TAG = "SendImageIntent";

	public static final String SERVER_URL = "serverUrl";
	public static final String IMAGE = "image";

	public SendImagesIntent() {
		super("SendImagesIntent");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String serverUrl = intent.getExtras().getString(SERVER_URL);
		ArrayList<String> images = intent.getExtras().getStringArrayList(IMAGE);

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

				HttpResponse result = new DefaultHttpClient().execute(request);
				if (result.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
					Log.i(TAG, "image " + image + " sent!");
				} else {
					Toast.makeText(
							this,
							getString(R.string.issue_report_sending_image_error),
							Toast.LENGTH_LONG).show();
					Log.e(TAG, "An error occurred while sending the image: "
							+ result.getStatusLine().getStatusCode());
				}

			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, e.getLocalizedMessage());

				Toast.makeText(this,
						getString(R.string.issue_report_sending_image_error),
						Toast.LENGTH_LONG).show();
			}

			File f = new File(path);
			if (f != null)
				f.delete();
		}

		Toast.makeText(this,
				getString(R.string.issue_report_sending_image_completed),
				Toast.LENGTH_LONG).show();

	}
}
