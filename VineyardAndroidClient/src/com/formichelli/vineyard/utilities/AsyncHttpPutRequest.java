package com.formichelli.vineyard.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Sends an http PUT request and asynchronously returns the response code
 */
public class AsyncHttpPutRequest extends AsyncTask<Void, Void, Integer> {
	protected final static String TAG = "AsyncHttpPutRequest";

	protected String serverUrl;
	protected List<NameValuePair> params;

	public AsyncHttpPutRequest() {
		this.params = new ArrayList<NameValuePair>();
	}

	public AsyncHttpPutRequest(String serverUrl, List<NameValuePair> params) {
		this.serverUrl = serverUrl;
		if (params != null)
			this.params = params;
		else
			this.params = new ArrayList<NameValuePair>();
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public List<NameValuePair> getParams() {
		return params;
	}

	public void setParams(List<NameValuePair> params) {
		if (params != null)
			this.params = params;
		else
			this.params = new ArrayList<NameValuePair>();
	}

	public void addPair(NameValuePair param) {
		if (param != null)
			params.add(param);
	}

	/**
	 * Sends a POST request to serverUrl with nameValuePair values
	 */
	@Override
	protected Integer doInBackground(Void... params) {
		try {
			if (serverUrl == null || this.params == null)
				return null;

			Log.i(TAG, "Sending PUT request to " + serverUrl);

			for (NameValuePair param : this.params)
				Log.i(TAG, param.getName() + " = " + param.getValue());

			HttpPut httpPut = new HttpPut(serverUrl);
			httpPut.setEntity(new UrlEncodedFormEntity(this.params));

			return new DefaultHttpClient().execute(httpPut).getStatusLine().getStatusCode();
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
			return -1;
		}
	}
}