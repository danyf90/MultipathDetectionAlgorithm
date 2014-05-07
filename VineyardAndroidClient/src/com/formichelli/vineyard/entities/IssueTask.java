package com.formichelli.vineyard.entities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IssueTask extends SimpleTask {
	private final static String ISSUER = "issuer";
	private final static String PHOTOS = "photos";

	private int issuer;
	private List<URL> photos;

	public IssueTask() {
		photos = new ArrayList<URL>();
	}

	public IssueTask(JSONObject jsonObject) throws JSONException {
		super(jsonObject);

		setIssuer(jsonObject.getInt(ISSUER));

		if (jsonObject.has(PHOTOS))
			setPhotos(jsonObject.getJSONArray(PHOTOS));
		else
			setPhotos(new ArrayList<URL>());
	}

	public int getIssuer() {
		return this.issuer;
	}

	public void setIssuer(int issuer) {
		this.issuer = issuer;
	}

	public List<URL> getPhotos() {
		return this.photos;
	}

	public void setPhotos(List<URL> photos) {
		if (photos != null)
			this.photos = photos;
		else
			this.photos = new ArrayList<URL>();
	}

	public void setPhotos(JSONArray photos) {
		this.photos = new ArrayList<URL>();

		for (int i = 0, l = photos.length(); i < l; i++) {
			try {
				this.photos.add(new URL(photos.getString(i)));
			} catch (MalformedURLException | JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void addPhoto(URL photo) {
		photos.add(photo);
	}

	public void removePhoto(URL photo) {
		photos.remove(photo);
	}

	public void removeAllPhotos() {
		photos.clear();
	}
	
	@Override
	public List<NameValuePair> getParams() {
		List<NameValuePair> params = super.getParams();

		params.add(new BasicNameValuePair(ISSUER, String.valueOf(getIssuer())));

		return params;
	}
}