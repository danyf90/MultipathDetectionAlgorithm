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
 * Sends an http request an asynchronously returns the response
 */
public class AsyncHttpRequests extends
		AsyncTask<String, Void, ArrayList<String>> {

	/**
	 * Sends a request to params[i] for each element i of @p params
	 */
	@Override
	protected ArrayList<String> doInBackground(String... params) {
		ArrayList<String> results = new ArrayList<String>();
		try {
			for (String request : params) {
				HttpResponse response = new DefaultHttpClient()
						.execute(new HttpGet(request));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					results.add(out.toString());
				} else {
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