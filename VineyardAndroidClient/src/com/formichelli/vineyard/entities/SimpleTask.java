package com.formichelli.vineyard.entities;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.location.Location;

public class SimpleTask implements Task {
	private final static String CREATE_TIME = "create_time";
	private final static String ASSIGN_TIME = "assign_time";
	private final static String DUE_TIME = "due_time";
	private final static String ASSIGNEE = "assignee";
	private final static String STATUS = "status";
	private final static String PRIORITY = "priority";
	private final static String PLACE = "place";
	private final static String TITLE = "title";
	private final static String DESCRIPTION = "description";
	private final static String LOCATION = "location";
	private final static String ASSIGNED_WORKER = "assigned_worker";
	private final static String ASSIGNED_GROUP = "assigned_group";
	
	private final static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private Date createTime;
	private Date assignTime;
	private Date dueTime;
	private Worker assignee;
	private Status status;
	private Priority priority;
	private Place place;
	private String title;
	private String description;
	private Location location;
	private Worker assignedWorker;
	private WorkGroup assignedGroup;

	public SimpleTask() {
	}

	@SuppressLint("SimpleDateFormat")
	public SimpleTask(JSONObject jsonObject) throws JSONException {
		try {
			createTime = new SimpleDateFormat(dateFormat).parse(jsonObject.getString(CREATE_TIME));
			assignTime = new SimpleDateFormat(dateFormat).parse(jsonObject.getString(ASSIGN_TIME));
			dueTime = new SimpleDateFormat(dateFormat).parse(jsonObject.getString(DUE_TIME));
		} catch (ParseException e) {
			throw new JSONException("Invalid date format");
		}
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getAssignTime() {
		return assignTime;
	}

	public void setAssignTime(Date assignTime) {
		this.assignTime = assignTime;
	}

	public Date getDueTime() {
		return dueTime;
	}

	public void setDueTime(Date dueTime) {
		this.dueTime = dueTime;
	}

	public Worker getAssignee() {
		return assignee;
	}

	public void setAssignee(Worker assignee) {
		this.assignee = assignee;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Worker getAssignedWorker() {
		return assignedWorker;
	}

	public void setAssignedWorker(Worker assignedWorker) {
		this.assignedWorker = assignedWorker;
	}

	public WorkGroup getAssignedGroup() {
		return assignedGroup;
	}

	public void setAssignedGroup(WorkGroup assignedGroup) {
		this.assignedGroup = assignedGroup;
	}
}