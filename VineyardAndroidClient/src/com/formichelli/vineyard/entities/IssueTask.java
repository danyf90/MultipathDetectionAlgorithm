package com.formichelli.vineyard.entities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IssueTask extends SimpleTask {
	private final static String ISSUER = "issuer";
	private final static String PHOTOS = "photos";

	private int issuer;
	private ArrayList<URL> photos;

	public IssueTask() {
		photos = new ArrayList<URL>();
	}

	public IssueTask(JSONObject jsonObject) throws JSONException {
		super(jsonObject);

		setIssuer(jsonObject.getInt(ISSUER));
		
		if (jsonObject.has(PHOTOS))
			setPhotos(jsonObject.getJSONArray(PHOTOS));
	}

	public int getIssuer() {
		return this.issuer;
	}

	public void setIssuer(int issuer) {
		this.issuer = issuer;
	}

	public ArrayList<URL> getPhotos() {
		return this.photos;
	}

	public void setPhotos(ArrayList<URL> photos) {
		this.photos = photos;
	}

	public void setPhotos(JSONArray photos) {
		for (int i = 0, l = photos.length(); i < l; i++) {
			try {
				this.photos.add(new URL(photos.getString(i)));
			} catch (MalformedURLException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addPhoto(URL photo) {
		if (photos == null)
			photos = new ArrayList<URL>();

		photos.add(photo);
	}

	public void removePhoto(URL photo) {
		photos.remove(photo);
	}

	public void removeAllPhotos() {
		photos = new ArrayList<URL>();
	}
}