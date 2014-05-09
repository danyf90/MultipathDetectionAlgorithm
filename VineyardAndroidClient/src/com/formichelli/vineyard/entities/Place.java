package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.List;
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
	private final static String PHOTO = "photo";
	private final static String ATTRIBUTES = "attributes";
	private final static String CHILDREN = "children";

	private int id;
	private String name;
	private String description;
	private String photo;
	private Location location;
	private Place parent;
	private List<Place> children;
	private List<IssueTask> issues;
	private List<SimpleTask> tasks;
	private HashMap<String, String> attributes;

	public Place() {
		children = new ArrayList<Place>();
		issues = new ArrayList<IssueTask>();
		tasks = new ArrayList<SimpleTask>();
		attributes = new HashMap<String, String>();
	}

	public Place(JSONObject rootPlaceObject) throws JSONException {

		setId(rootPlaceObject.getInt(ID));

		setName(rootPlaceObject.getString(NAME));

		setDescription(rootPlaceObject.getString(DESCRIPTION));

		attributes = new HashMap<String, String>();
		if (!rootPlaceObject.isNull(PHOTO))
			setPhoto(rootPlaceObject.getString(PHOTO));

		children = new ArrayList<Place>();
		if (!rootPlaceObject.isNull(CHILDREN))
			setChildren(rootPlaceObject.getJSONArray(CHILDREN));

		attributes = new HashMap<String, String>();
		if (!rootPlaceObject.isNull(ATTRIBUTES))
			setAttributes(rootPlaceObject.getJSONObject(ATTRIBUTES));

		// issues and tasks are not present in places JSON
		setIssues(new ArrayList<IssueTask>());
		setTasks(new ArrayList<SimpleTask>());
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
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

	public List<Place> getChildren() {
		return this.children;
	}

	public void setChildren(List<Place> children) {
		if (children == null)
			this.children.clear();
		else
			this.children = children;
	}

	public void setChildren(JSONArray childrenArray) {
		children.clear();

		for (int i = 0, l = childrenArray.length(); i < l; i++) {
			Place p;
			try {
				p = new Place(childrenArray.getJSONObject(i));
				p.setParent(this);
				children.add(p);
			} catch (JSONException e) {
				Log.e("setChildren",
						"Error parsing children: " + e.getLocalizedMessage());
			}
		}
	}

	public void addChild(Place child) {
		if (child != null)
			children.add(child);
	}

	public void removeChild(Place child) {
		children.remove(child);
	}

	public List<IssueTask> getIssues() {
		return issues;
	}

	public void setIssues(List<IssueTask> issues) {
		if (issues == null)
			this.issues.clear();
		else
			this.issues = issues;
	}

	public void setIssues(JSONArray issuesArray) throws JSONException {
		this.issues.clear();

		for (int i = 0, l = issuesArray.length(); i < l; i++) {
			try {
				addIssue(new IssueTask(issuesArray.getJSONObject(i)));
			} catch (JSONException e) {
				android.util.Log.e("Place.setIsues", e.getLocalizedMessage());
			}
		}
	}

	public void addIssue(IssueTask issue) {
		if (issue != null)
			issues.add(issue);
	}

	public void removeIssue(IssueTask issue) {
		issues.remove(issue);
	}

	public List<SimpleTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<SimpleTask> tasks) {
		if (tasks == null)
			this.tasks.clear();
		else
			this.tasks = tasks;
	}

	public void setTasks(JSONArray tasksArray) throws JSONException {
		tasks.clear();

		for (int i = 0, l = tasksArray.length(); i < l; i++) {
			try {
				addTask(new SimpleTask(tasksArray.getJSONObject(i)));
			} catch (JSONException e) {
			}
		}
	}

	public void addTask(SimpleTask task) {
		if (task != null)
			tasks.add(task);
	}

	public void removeTask(SimpleTask task) {
		tasks.remove(task);
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {
		if (attributes != null)
			this.attributes.clear();
		else
			this.attributes = attributes;
	}

	public void setAttributes(JSONObject attributesObject) {
		this.attributes.clear();

		if (attributesObject == null)
			return;

		Iterator<?> i = attributesObject.keys();
		while (i.hasNext()) {
			String key = (String) i.next();

			try {
				addAttribute(key, attributesObject.getString(key));
			} catch (JSONException e) {
				Log.e("Place.setAttributes", "This should never happen");
			}
		}
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	public int getIssuesCount() {
		return issues.size();
	}

	public int getTasksCount() {
		return tasks.size();
	}

	public int getChildrenIssuesCount() {
		int issuesCount = issues.size();

		for (Place p : children)
			issuesCount += p.getChildrenIssuesCount();

		return issuesCount;
	}

	public int getChildrenTasksCount() {
		int tasksCount = tasks.size();

		for (Place p : children)
			tasksCount += p.getChildrenTasksCount();

		return tasksCount;
	}

}