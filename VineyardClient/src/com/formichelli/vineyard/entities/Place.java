package com.formichelli.vineyard.entities;

import android.location.Location;

public class Place {
	private String name;
	private String description;
	private Location location;
	private Place[] children;
	public SimpleTask unnamed_SimpleTask_;

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