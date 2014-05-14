package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
		ArrayList<String> images = intent.getExtras().getStringArrayList(
				IMAGE);

		Log.e(TAG, "starting...");
		
		for (String path : images) {
			Log.i(TAG, "Sending image " + path + " to the server...");

			Bitmap image = BitmapFactory.decodeFile(path);
			if (image == null)
				throw new IllegalArgumentException(path
						+ " cannot be decoded");

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 90, out);
			String imageString = Base64.encodeToString(out.toByteArray(),
					Base64.DEFAULT);

			ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
			postParams.add(new BasicNameValuePair("photo", imageString));

			HttpPost request = new HttpPost(serverUrl);
			try {
				request.setEntity(new UrlEncodedFormEntity(postParams));
				HttpResponse result = new DefaultHttpClient()
						.execute(request);
				if (result.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED)
					Toast.makeText(this, "Image sent!", Toast.LENGTH_LONG)
							.show();
				else {
					Toast.makeText(this,
							"An error occurred while sending the image",
							Toast.LENGTH_LONG).show();
					Log.e(TAG,
							"An error occurred while sending the image: "
									+ result.getStatusLine()
											.getStatusCode());
				}

				File f = new File(path);
				if (f != null)
					f.delete();

				return;
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("SendImageIntent", e.getLocalizedMessage());
			}

			Toast.makeText(
					this,
					"An error occurred while sending an image to the server...",
					Toast.LENGTH_LONG).show();

			File f = new File(path);
			if (f != null)
				f.delete();
		}
	}
}
