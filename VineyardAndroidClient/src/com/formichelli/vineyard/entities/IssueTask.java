package com.formichelli.vineyard.entities;

import java.net.URL;
import java.util.ArrayList;

public class IssueTask extends SimpleTask {
	private int issuer;
	private ArrayList<URL> photos;

	public IssueTask() {
		photos = new ArrayList<URL>();
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

	public void addPhoto(URL photo) {
		photos.add(photo);
	}

	public void removePhoto(URL photo) {
		photos.remove(photo);
	}
}