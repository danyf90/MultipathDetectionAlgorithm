package com.formichelli.vineyard.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Sends an http POST request and asynchronously returns the response
 */
public class AsyncHttpPostRequest extends AsyncTask<Void, Void, HttpResponse> {
	protected final static String TAG = "AsyncHttpPostRequest";

	protected String serverUrl;
	protected List<NameValuePair> params;

	public AsyncHttpPostRequest() {
		this.params = new ArrayList<NameValuePair>();
	}

	public AsyncHttpPostRequest(String serverUrl, List<NameValuePair> params) {
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
	protected HttpResponse doInBackground(Void... params) {
		try {
			if (serverUrl == null || this.params == null)
				return null;

			Log.i(TAG, "Sending POST request to " + serverUrl);

			for (NameValuePair param : this.params)
				Log.i(TAG, param.getName() + " = " + param.getValue());

			HttpPost httpPost = new HttpPost(serverUrl);
			httpPost.setEntity(new UrlEncodedFormEntity(this.params));

			return new DefaultHttpClient().execute(httpPost);
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
			return null;
		}
	}
}