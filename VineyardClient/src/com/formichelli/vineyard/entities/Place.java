package com.formichelli.vineyard.entities;

import java.util.ArrayList;

import android.location.Location;

public class Place {
	private int id;
	private String name;
	private String description;
	private Location location;
	private Place parent;
	private ArrayList<Place> children;
	
	public Place() {
		children = new ArrayList<Place>();
	}
	
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
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

	public Place getParent() {
		return parent;
	}

	public void setParent(Place parent) {
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Place> getChildren() {
		return (ArrayList<Place>) this.children.clone();
	}

	public void setChildren(ArrayList<Place> children) {
		this.children = children;
	}

}