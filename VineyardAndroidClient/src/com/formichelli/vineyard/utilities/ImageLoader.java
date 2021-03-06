package com.formichelli.vineyard.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.formichelli.vineyard.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Class which allows to retrieve an image from a server.
 * 
 */
public class ImageLoader extends AsyncTask<Void, Void, BitmapDrawable> {
	private static final String TAG = "ImageLoader";
	Context context;
	ViewGroup container;
	ProgressBar progress;
	String imageUrl;
	String localName;

	protected ImageLoader() {
	}

	/**
	 * @param context
	 * @param container
	 *            the image will be set as its background
	 * @param progress
	 *            progress bar that will be shown during the loading
	 */
	public ImageLoader(Context context, ViewGroup container,
			ProgressBar progress, String imageUrl) {
		this.context = context;
		this.container = container;
		this.progress = progress;
		this.imageUrl = imageUrl;
	}

	@Override
	protected void onPreExecute() {
		if (progress != null)
			progress.setVisibility(View.VISIBLE);
	}

	/**
	 * Sends an HTTP GET request and returns the image bitmap
	 */
	@Override
	protected BitmapDrawable doInBackground(Void... params) {
		if (context == null || container == null || imageUrl == null)
			return null;

		// get the filename only
		localName = context.getExternalCacheDir().getAbsolutePath()
				+ imageUrl.substring(imageUrl.lastIndexOf('/'),
						imageUrl.lastIndexOf('?'));

		Bitmap imageBitmap;

		if ((new File(localName)).exists()) {
			// the image is already present locally
			imageBitmap = BitmapFactory.decodeFile(localName);
			if (imageBitmap != null)
				return new BitmapDrawable(context.getResources(), imageBitmap);

			Log.e(TAG, localName
					+ " is not valid, downloading it from server...");
		}

		// the image must be retrieved from the server
		try {
			HttpResponse response = new DefaultHttpClient()
					.execute(new HttpGet(imageUrl));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK)
				imageBitmap = BitmapFactory.decodeStream(response.getEntity()
						.getContent());
			else
				imageBitmap = null;
		} catch (IllegalArgumentException | IllegalStateException | IOException e) {
			e.printStackTrace();
			imageBitmap = null;
		}

		if (imageBitmap != null) {
			saveBitmap(imageBitmap, localName);
			return new BitmapDrawable(context.getResources(), imageBitmap);
		}

		return null;
	}

	/*
	 * Hide the progress bar and set the image (if not null) as background of
	 * the container
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostExecute(BitmapDrawable photo) {
		if (progress != null)
			progress.setVisibility(View.GONE);
		
		if (photo == null)
			container.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.action_issue_dark));
		else
			container.setBackgroundDrawable(photo);
	}

	protected void saveBitmap(Bitmap b, String filename) {
		FileOutputStream out;
		try {
			out = new FileOutputStream(filename);
			// use PNG since JPEG loses quality
			b.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}