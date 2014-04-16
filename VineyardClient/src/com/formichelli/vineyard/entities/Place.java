package com.formichelli.vineyard.entities;

import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;

import android.location.Location;

public class Place implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	private Location location;
	private Place[] children;

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		PutField o = out.putFields();

		o.put("name", name);
		o.put("description", description);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		GetField i = in.readFields();

		name = i.get("name", "defaultName").toString();
		description = i.get("name", "defaultName").toString();
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Place[] getChildren() {
		return this.children;
	}

	public void setChildren(Place[] children) {
		this.children = children;
	}
}