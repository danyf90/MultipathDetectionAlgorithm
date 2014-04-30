package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
public class AsyncHttpRequest extends AsyncTask<String, Void, String> {

	/**
	 * Sends a request to host @p params[0] to port @p params[1] at path @p params[2] 
	 */
	@Override
	protected String doInBackground(String... params) {

		try {
			String requestString = params[0] + ":" + params[1] + params[2];

			HttpResponse response = new DefaultHttpClient().execute(new HttpGet(
					requestString));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				return out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				Log.e("http error", statusLine.getReasonPhrase());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}