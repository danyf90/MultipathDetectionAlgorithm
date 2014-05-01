package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

public class Place {
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String DESCRIPTION = "description";
	private final static String ATTRIBUTES = "attributes";
	private final static String CHILDREN = "children";

	private int id;
	private String name;
	private String description;
	private Location location;
	private Place parent;
	private ArrayList<Place> children;
	private HashMap<String, String> attributes;
	private int issuesCount;
	private int tasksCount;

	public Place() {
		attributes = new HashMap<String, String>();
		children = new ArrayList<Place>();
	}

	public Place(int id, String name, String description, Location location,
			Place parent, ArrayList<Place> children,
			HashMap<String, String> attributes, int issuesCount, int tasksCount) {
		setId(id);
		setName(name);
		setDescription(description);
		setLocation(location);
		setParent(parent);
		setChildren(children);
		setAttributes(attributes);
		setIssuesCount(issuesCount);
		setTasksCount(tasksCount);
	}

	public Place(JSONObject rootPlaceObject) throws JSONException {

		setId(rootPlaceObject.getInt(ID));

		setName(rootPlaceObject.getString(NAME));

		setDescription(rootPlaceObject.getString(DESCRIPTION));

		try {
			setAttributes(rootPlaceObject.getJSONObject(ATTRIBUTES));
		} catch (JSONException e) {
			attributes = new HashMap<String, String>();

		}

		try {
			setChildren(rootPlaceObject.getJSONArray(CHILDREN));
		} catch (JSONException e) {
			children = new ArrayList<Place>();
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

	public void setChildren(JSONArray childrenArray) {
		children = new ArrayList<Place>();

		if (childrenArray == null)
			return;

		for (int i = 0, l = childrenArray.length(); i < l; i++) {
			Place p;
			try {
				p = new Place(childrenArray.getJSONObject(i));
				p.setParent(this);
			} catch (JSONException e) {
				Log.e("setChildren", "Error parsing children " + i);
				break;
			}
			children.add(p);
		}

	}

	public void addChild(Place child) {
		children.add(child);
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {
		if (attributes == null)
			this.attributes = new HashMap<String, String>();
		this.attributes = attributes;
	}

	public void setAttributes(JSONObject attributesObject) {
		attributes = new HashMap<String, String>();

		if (attributesObject == null)
			return;

		Iterator<?> i = attributesObject.keys();
		while (i.hasNext()) {
			String key = (String) i.next();

			try {
				attributes.put(key, attributesObject.getString(key));
			} catch (JSONException e) {
				Log.e("Place.setAttributes", "This should never happen");
			}
		}
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public int getIssuesCount() {
		return issuesCount;
	}

	public void setIssuesCount(int issuesCount) {
		this.issuesCount = issuesCount;
	}

	public int getTasksCount() {
		return tasksCount;
	}

	public void setTasksCount(int tasksCount) {
		this.tasksCount = tasksCount;
	}

}