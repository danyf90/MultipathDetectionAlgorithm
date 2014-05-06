package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Sends n http GET requests and asynchronously returns the responses
 */
public class AsyncHttpGetRequests extends
		AsyncTask<String, Void, ArrayList<String>> {
	private final static String TAG = "AsyncHttpGetRequests";

	/**
	 * Sends a GET request params[i] for each element i of @p params
	 */
	@Override
	protected ArrayList<String> doInBackground(String... params) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			for (String request : params) {
				if (request == null)
					return null;

				Log.i(TAG, "Sending GET request to " + request);

				HttpResponse response = new DefaultHttpClient()
						.execute(new HttpGet(request));

				StatusLine statusLine = response.getStatusLine();
				switch (statusLine.getStatusCode()) {
				case HttpStatus.SC_OK:
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					results.add(out.toString());
					break;
				case HttpStatus.SC_NOT_FOUND:
					Log.e("http error", "not found");
					results.add("[]");
					break;
				default:
					// Closes the connection.
					response.getEntity().getContent().close();
					Log.e("http error", statusLine.getReasonPhrase());
					results.add(null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return results;
	}
}