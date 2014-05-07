package com.formichelli.vineyard.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
	Activity activity;
	ViewGroup container;
	ProgressBar progress;
	String localName;

	public ImageLoader(Activity activity) {

	}

	public ImageLoader(Activity activity, ViewGroup container,
			ProgressBar progress) {
		this.activity = activity;
		this.container = container;
		this.progress = progress;
	}

	@Override
	protected void onPreExecute() {
		progress.setVisibility(View.VISIBLE);

	}

	@Override
	protected Bitmap doInBackground(String... params) {
		if (container == null || progress == null || params == null
				|| params[0] == null)
			return null;

		final String request = params[0];
		
		// get the filename only
		localName = activity.getExternalCacheDir().getAbsolutePath()
				+ request.substring(request.lastIndexOf('/'), request.lastIndexOf('?'));

		if ((new File(localName)).exists())
			return BitmapFactory.decodeFile(localName);

		try {
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
			android.util.Log.e("ImageLoader", "null");
			// TODO set no image
			return;
		}

		saveBitmap(photo, localName);
		container.setBackgroundDrawable(new BitmapDrawable(photo));
	}

	private void saveBitmap(Bitmap b, String filename) {
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename);
			// JPEG loses quality
			b.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}