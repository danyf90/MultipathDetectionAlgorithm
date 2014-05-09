package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IssueTask extends SimpleTask {
	public final static String ISSUER = "issuer";
	public final static String PHOTOS = "photos";

	private int issuer;
	private List<String> photos;

	public IssueTask() {
		photos = new ArrayList<String>();
	}

	public IssueTask(JSONObject jsonObject) throws JSONException {
		super(jsonObject);

		setIssuer(jsonObject.getInt(ISSUER));

		if (jsonObject.has(PHOTOS))
			setPhotos(jsonObject.getJSONArray(PHOTOS));
		else
			setPhotos(new ArrayList<String>());
	}

	public int getIssuer() {
		return this.issuer;
	}

	public void setIssuer(int issuer) {
		this.issuer = issuer;
	}

	public List<String> getPhotos() {
		return this.photos;
	}

	public void setPhotos(List<String> photos) {
		if (photos != null)
			this.photos = photos;
		else
			this.photos = new ArrayList<String>();
	}

	public void setPhotos(JSONArray photos) {
		this.photos = new ArrayList<String>();

		for (int i = 0, l = photos.length(); i < l; i++) {
			try {
				this.photos.add(photos.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void addPhoto(String photo) {
		photos.add(photo);
	}

	public void removePhoto(String photo) {
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