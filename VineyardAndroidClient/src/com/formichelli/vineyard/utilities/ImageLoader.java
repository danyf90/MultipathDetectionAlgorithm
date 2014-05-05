package com.formichelli.vineyard.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.formichelli.vineyard.VineyardMainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
	VineyardMainActivity activity;
	ViewGroup container;
	ProgressBar progress;
	String filename;

	public ImageLoader(VineyardMainActivity activity, ViewGroup container,
			ProgressBar progress) {
		this.activity = activity;
		this.container = container;
		this.progress = progress;

		filename = activity.getExternalCacheDir().getAbsolutePath() + "/";
	}

	@Override
	protected void onPreExecute() {
		progress.setVisibility(View.VISIBLE);

	}

	@Override
	protected Bitmap doInBackground(String... params) {
		filename += params[0];

		if ((new File(filename)).exists())
			return BitmapFactory.decodeFile(filename);


		try {
			final String request = String.format(Locale.US, activity.getServer()
					.getUrl() + String.format(Locale.US, VineyardServer.PHOTO_API, params[0], container.getMeasuredWidth(),
							container.getMeasuredHeight()));
			android.util.Log.e("ASD",request);
			HttpResponse response = new DefaultHttpClient()
					.execute(new HttpGet(request));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK)
				return BitmapFactory.decodeStream(response.getEntity()
						.getContent());
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostExecute(Bitmap photo) {
		progress.setVisibility(View.INVISIBLE);

		if (photo == null) {
			android.util.Log.e("ASD", "null");
			// TODO set no image
			return;
		}

		saveBitmap(photo, filename);
		container.setBackgroundDrawable(new BitmapDrawable(photo));
	}

	private void saveBitmap(Bitmap b, String filename) {
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename);
			b.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}