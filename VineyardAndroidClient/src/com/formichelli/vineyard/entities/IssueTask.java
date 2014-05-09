package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Class which represents an issue
 */
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

		setPhotos(new ArrayList<String>());
		if (!jsonObject.isNull(PHOTOS))
			setPhotos(jsonObject.getJSONArray(PHOTOS));
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
			this.photos.clear();
	}

	public void setPhotos(JSONArray photos) {
		this.photos.clear();

		for (int i = 0, l = photos.length(); i < l; i++) {
			try {
				this.photos.add(photos.getString(i));
			} catch (JSONException e) {
				Log.e("setChildren", "Error parsing photo: " + e.getLocalizedMessage());
			}
		}
	}

	public void addPhoto(String photo) {
		photos.add(photo);
	}

	public void removePhoto(String photo) {
		photos.remove(photo);
	}

	/**
	 * returns the list of parameters needed for a post request to create a new issue
	 */
	@Override
	public List<NameValuePair> getParams() {
		List<NameValuePair> params = super.getParams();
		params.add(new BasicNameValuePair(ISSUER, String.valueOf(getIssuer())));
		return params;
	}
}