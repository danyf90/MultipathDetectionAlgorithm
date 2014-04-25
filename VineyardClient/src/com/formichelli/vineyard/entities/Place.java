package com.formichelli.vineyard.entities;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.SparseArray;

public class Place {
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String DESCRIPTION = "description";
	private final static String PARENT = "parent";

	private int id;
	private String name;
	private String description;
	private Location location;
	private Place parent;
	private ArrayList<Place> children;

	public Place() {
		children = new ArrayList<Place>();
	}

	public Place(int id, String name, String description, Location location,
			Place parent, ArrayList<Place> children) {
		setId(id);
		setName(name);
		setDescription(description);
		setLocation(location);
		setParent(parent);
		setChildren(children);
	}

	public Place(String placesString) throws JSONException {
		JSONObject placeObject;
		SparseArray<Place> places = new SparseArray<Place>();

		JSONArray placeObjects = new JSONArray(placesString);

		// First place is root
		placeObject = placeObjects.getJSONObject(0);
		setId(placeObject.getInt(ID));
		setName(placeObject.getString(NAME));
		setDescription(placeObject.getString(DESCRIPTION));
		children = new ArrayList<Place>();
		places.append(getId(), this);

		for (int i = 1, l = placeObjects.length(); i < l; i++) {
			placeObject = placeObjects.getJSONObject(i);

			Place p = new Place();

			p.setId(placeObject.getInt(ID));

			p.setName(placeObject.getString(NAME));

			p.setDescription(placeObject.getString(DESCRIPTION));

			// TODO Location

			int parentId = placeObject.getInt(PARENT);
			p.setParent(places.get(parentId));
			places.get(parentId).addChild(p);

			places.append(p.getId(), p);
		}
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
		if (children == null)
			this.children = new ArrayList<Place>();
		else
			this.children = children;
	}

	public void addChild(Place child) {
		if (children == null)
			children = new ArrayList<Place>();

		children.add(child);
	}
}