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
	private int issuesCount;
	private int tasksCount;

	public Place() {
		attributes = new HashMap<String, String>();
		children = new ArrayList<Place>();
	}

	public Place(int id, String name, String description, String photo,
			Location location, Place parent, List<Place> children,
			HashMap<String, String> attributes, int issuesCount,
			int tasksCount, List<IssueTask> issues,
			List<SimpleTask> tasks) {
		setId(id);
		setName(name);
		setDescription(description);
		setPhoto(photo);
		setLocation(location);
		setParent(parent);
		setChildren(children);
		setAttributes(attributes);
		setIssues(issues);
		setTasks(tasks);
		setIssuesCount(issuesCount);
		setTasksCount(tasksCount);

	}

	public Place(JSONObject rootPlaceObject) throws JSONException {

		setId(rootPlaceObject.getInt(ID));

		setName(rootPlaceObject.getString(NAME));

		setDescription(rootPlaceObject.getString(DESCRIPTION));


		if (rootPlaceObject.has(PHOTO))
			setPhoto(rootPlaceObject.getString(PHOTO));
		else
			attributes = new HashMap<String, String>();
		
		if (rootPlaceObject.has(ATTRIBUTES))
			setAttributes(rootPlaceObject.getJSONObject(ATTRIBUTES));
		else
			attributes = new HashMap<String, String>();
		
		if (rootPlaceObject.has(CHILDREN))
			setChildren(rootPlaceObject.getJSONArray(CHILDREN));
		else
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
		this.children = children;
	}

	public void setChildren(JSONArray childrenArray) {

		if (childrenArray == null) {
			children = null;
			return;
		}

		children = new ArrayList<Place>();
		for (int i = 0, l = childrenArray.length(); i < l; i++) {
			Place p;
			try {
				p = new Place(childrenArray.getJSONObject(i));
				p.setParent(this);
				children.add(p);
			} catch (JSONException e) {
				Log.e("setChildren", "Error parsing children " + i);
			}
		}
	}

	public void addChild(Place child) {
		if (children == null)
			this.children = new ArrayList<Place>();
		else
			children.add(child);
	}

	public List<IssueTask> getIssues() {
		return issues;
	}

	public void setIssues(List<IssueTask> issues) {
		this.issues = issues;
	}

	public void setIssues(String issuesJSON) throws JSONException {
		JSONArray issuesArray = new JSONArray(issuesJSON);

		setIssues(new ArrayList<IssueTask>());

		for (int i = 0, l = issuesArray.length(); i < l; i++) {
			try {
				addIssue(new IssueTask(issuesArray.getJSONObject(i)));
			} catch (JSONException e) {
				android.util.Log.e("Place.setIsues", e.getLocalizedMessage());
			}
		}
	}

	public void addIssue(IssueTask issue) {
		if (issues == null)
			issues = new ArrayList<IssueTask>();
		else
			issues.add(issue);
	}

	public List<SimpleTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<SimpleTask> tasks) {
		this.tasks = tasks;
	}

	public void setTasks(String tasksJSON) throws JSONException {
		JSONArray tasksArray = new JSONArray(tasksJSON);

		setTasks(new ArrayList<SimpleTask>());

		for (int i = 0, l = tasksArray.length(); i < l; i++) {
			try {
				addTask(new SimpleTask(tasksArray.getJSONObject(i)));
			} catch (JSONException e) {
			}
		}
	}

	public void addTask(SimpleTask task) {
		if (tasks == null)
			tasks = new ArrayList<SimpleTask>();
		else
			tasks.add(task);
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {
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
		if (attributes == null)
			attributes = new HashMap<String, String>();
		
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

	public int getChildrenIssuesCount() {
		int issuesCount = this.issuesCount;

		for (Place p : children)
			issuesCount += p.getChildrenIssuesCount();

		return issuesCount;
	}

	public int getChildrenTasksCount() {
		int tasksCount = this.tasksCount;

		for (Place p : children)
			tasksCount += p.getChildrenTasksCount();

		return tasksCount;
	}
}