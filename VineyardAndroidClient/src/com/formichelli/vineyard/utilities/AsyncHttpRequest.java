package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

/**
 * Sends an HTTP request and asynchronously returns the response
 */
public class AsyncHttpRequest extends
		AsyncTask<Void, Void, Pair<Integer, String>> {
	protected final static String TAG = "AsyncHttpRequest";

	public enum Type {
		GET, POST, PUT, DELETE
	};

	protected String serverUrl;
	protected String lastModified;
	protected List<NameValuePair> params;
	protected Type type;

	public AsyncHttpRequest() {
		this.serverUrl = null;
		this.type = null;
		this.params = new ArrayList<NameValuePair>();
	}

	public AsyncHttpRequest(String serverUrl) {
		this.serverUrl = serverUrl;
		this.type = null;
		this.params = new ArrayList<NameValuePair>();
	}

	public AsyncHttpRequest(Type type) {
		this.serverUrl = null;
		this.type = type;
		this.params = new ArrayList<NameValuePair>();
	}

	public AsyncHttpRequest(String serverUrl, Type type) {
		this.serverUrl = serverUrl;
		this.type = type;
		this.params = new ArrayList<NameValuePair>();
	}

	public AsyncHttpRequest(String serverUrl, Type type,
			List<NameValuePair> params) {
		this.serverUrl = serverUrl;
		this.type = type;
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

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
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

	public void addParam(NameValuePair param) {
		if (param != null)
			params.add(param);
	}

	public void deleteParam(NameValuePair param) {
		params.remove(param);
	}

	/**
	 * Sends a POST request to serverUrl with nameValuePair values
	 */
	@Override
	protected Pair<Integer, String> doInBackground(Void... params) {
		HttpRequestBase request;

		if (serverUrl == null || type == null)
			return new Pair<Integer,String>(-1,null);

		Log.i(TAG, "Sending " + type + " request to " + serverUrl);
		if (type != Type.GET)
			for (NameValuePair param : this.params)
				Log.i(TAG, param.getName() + " = " + param.getValue());

		// create the right request
		switch (type) {
		case DELETE:
			request = new HttpDelete(serverUrl);
			break;

		case GET:
			request = new HttpGet(serverUrl);
			
			if (lastModified != null)
				request.addHeader("If-Modified-Since", lastModified);
			break;

		case POST:
			request = new HttpPost(serverUrl);
			break;

		case PUT:
			request = new HttpPut(serverUrl);
			break;

		default:
			return new Pair<Integer,String>(-1,null);
		}

		// add the parameters for DELETE, POST and PUT requests
		if (type != Type.GET) {
			if (this.params != null)
				try {
					((HttpEntityEnclosingRequestBase) request)
							.setEntity(new UrlEncodedFormEntity(this.params, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "Error: " + e.getLocalizedMessage());
					return new Pair<Integer,String>(-1,null);
				}
		}

		// sends the request and returns response status code and body
		try {
			HttpResponse response = new DefaultHttpClient().execute(request);

			String body = getResponseBody(response);
			int statusCode = response.getStatusLine()
					.getStatusCode();
			
			if (type == Type.GET && statusCode == HttpStatus.SC_OK) {
				// set last modified date
				Header lastModifiedHeader = response.getFirstHeader("Last-Modified"); 
				if (lastModifiedHeader != null)
					this.setLastModified(lastModifiedHeader.getValue());
				else
					this.setLastModified(null);
			}
			
			return new Pair<Integer, String>(statusCode, body);
		} catch (IOException e) {
			Log.e(TAG, "Error: " + e.getLocalizedMessage());
			return new Pair<Integer,String>(-1,null);
		}

	}

	private String getResponseBody(HttpResponse response) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			HttpEntity entity = response.getEntity();
			if (entity != null)
				entity.writeTo(out);
			out.close();
		} catch (IOException e) {
			return null;
		}

		return out.toString();
	}
}