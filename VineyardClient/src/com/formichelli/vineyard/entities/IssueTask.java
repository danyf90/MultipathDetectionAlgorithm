package com.formichelli.vineyard.entities;

import java.net.URL;

public class IssueTask extends SimpleTask {
	private int issuer;
	private URL[] photos;

	public int getIssuer() {
		return this.issuer;
	}

	public void setIssuer(int issuer) {
		this.issuer = issuer;
	}

	public URL[] getPhotos() {
		return this.photos;
	}

	public void addPhoto(URL photo) {
		throw new UnsupportedOperationException();
	}

	public void removePhoto(URL photo) {
		throw new UnsupportedOperationException();
	}
}