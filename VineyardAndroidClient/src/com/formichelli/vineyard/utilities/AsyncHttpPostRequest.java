package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Sends an http POST request and asynchronously returns the response
 */
public class AsyncHttpPostRequest extends AsyncTask<Void, Void, String> {
	protected final static String TAG = "AsyncHttpPostRequest";

	protected String serverUrl;
	protected List<NameValuePair> pairs;

	public AsyncHttpPostRequest() {
		this.pairs = new ArrayList<NameValuePair>();
	}

	public AsyncHttpPostRequest(String serverUrl, List<NameValuePair> pairs) {
		this.serverUrl = serverUrl;
		if (pairs != null)
			this.pairs = pairs;
		else
			this.pairs = new ArrayList<NameValuePair>();
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public List<NameValuePair> getPairs() {
		return pairs;
	}

	public void setPairs(List<NameValuePair> pairs) {
		if (pairs != null)
			this.pairs = pairs;
		else
			this.pairs = new ArrayList<NameValuePair>();
	}

	public void addPair(NameValuePair pair) {
		if (pair != null)
			pairs.add(pair);
	}

	/**
	 * Sends a POST request to serverUrl with nameValuePair values
	 */
	@Override
	protected String doInBackground(Void... params) {
		try {
			if (serverUrl == null || pairs == null)
				return null;

			Log.i(TAG, "Sending POST request to " + serverUrl);

			HttpPost httpPost = new HttpPost(serverUrl);
			httpPost.setEntity(new UrlEncodedFormEntity(pairs));

			HttpResponse response = new DefaultHttpClient().execute(httpPost);

			StatusLine statusLine = response.getStatusLine();
			switch (statusLine.getStatusCode()) {
			case HttpStatus.SC_ACCEPTED:
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				Log.e(TAG, "Response: " + out.toString());
				return out.toString();
			default:
				// Closes the connection.
				response.getEntity().getContent().close();
				Log.e("http error", statusLine.getReasonPhrase());
				return null;
			}
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
			return null;
		}
	}
}