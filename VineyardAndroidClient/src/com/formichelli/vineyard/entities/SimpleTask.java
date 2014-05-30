package com.formichelli.vineyard.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

public class SimpleTask implements Task, Comparable<SimpleTask> {
	public final static String ID = "id";
	public final static String MODIFIER = "modifier";
	public final static String ASSIGNEE = "assignee";
	public final static String CREATE_TIME = "create_time";
	public final static String ASSIGN_TIME = "assign_time";
	public final static String DUE_TIME = "due_time";
	public final static String STATUS = "status";
	public final static String PRIORITY = "priority";
	public final static String PLACE = "place";
	public final static String TITLE = "title";
	public final static String DESCRIPTION = "description";
	public final static String LATITUDE = "latitude";
	public final static String LONGITUDE = "longitude";
	public final static String ASSIGNED_WORKER = "assigned_worker";
	public final static String ASSIGNED_GROUP = "assigned_group";

	private final static String dateFormat = "yyyy-MM-dd HH:mm:ss";

	private int id;
	private int assignerId;
	private int modifierId;
	private Date createTime;
	private Date assignTime;
	private Date dueTime;
	private Status status;
	private Priority priority;
	private Place place;
	private String title;
	private String description;
	private Double latitude;
	private Double longitude;
	private Worker assignedWorker;
	private WorkGroup assignedGroup;

	public SimpleTask() {
	}

	@SuppressLint("SimpleDateFormat")
	public SimpleTask(JSONObject jsonObject) throws JSONException {

		setId(jsonObject.getInt(ID));

		if (!jsonObject.isNull(ASSIGNEE))
			setAssignerId(jsonObject.getInt(ASSIGNEE));

		if (!jsonObject.isNull(MODIFIER))
			setModifierId(jsonObject.getInt(MODIFIER));

		try {
			if (!jsonObject.isNull(CREATE_TIME))
				setCreateTime(new SimpleDateFormat(dateFormat).parse(jsonObject
						.getString(CREATE_TIME)));

			if (!jsonObject.isNull(ASSIGN_TIME))
				setAssignTime(new SimpleDateFormat(dateFormat).parse(jsonObject
						.getString(ASSIGN_TIME)));

			if (!jsonObject.isNull(DUE_TIME))
				setDueTime(new SimpleDateFormat(dateFormat).parse(jsonObject
						.getString(DUE_TIME)));
		} catch (ParseException e1) {
			throw new JSONException("Required date format: " + dateFormat);
		}

		setStatus(jsonObject.getString(STATUS));

		if (jsonObject.isNull(PRIORITY))
			setPriority(null);
		else
			setPriority(Priority.fromString(jsonObject.getString(PRIORITY)));

		Place p = new Place();
		p.setId(jsonObject.getInt(PLACE));
		setPlace(p);

		setTitle(jsonObject.getString(TITLE));

		if (!jsonObject.isNull(DESCRIPTION))
			setDescription(jsonObject.getString(DESCRIPTION));
		else
			setDescription(null);

		if (!jsonObject.isNull(LATITUDE) && !jsonObject.isNull(LONGITUDE)) {
			setLatitude(jsonObject.getDouble(LATITUDE));
			setLongitude(jsonObject.getDouble(LONGITUDE));
		} else {
			setLatitude(null);
			setLongitude(null);
		}

		if (!jsonObject.isNull(ASSIGNED_WORKER)) {
			Worker w = new Worker();
			w.setId(jsonObject.getInt(ASSIGNED_WORKER));
			setAssignedWorker(w);
		}

		if (!jsonObject.isNull(ASSIGNED_GROUP)) {
			WorkGroup wg = new WorkGroup();
			wg.setId(jsonObject.getInt(ASSIGNED_GROUP));
			setAssignedGroup(wg);
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getAssignerId() {
		return assignerId;
	}

	@Override
	public void setAssignerId(int assignerId) {
		if (assignerId < 0)
			throw new IllegalArgumentException("assignerId cannot be negative");

		this.assignerId = assignerId;
	}

	public int getModifierId() {
		return modifierId;
	}

	public void setModifierId(int modifierId) {
		if (modifierId < 0)
			throw new IllegalArgumentException("modifierId cannot be negative");

		this.modifierId = modifierId;
	}

	@Override
	public Date getCreateTime() {
		return createTime;
	}

	@Override
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public Date getAssignTime() {
		return assignTime;
	}

	@Override
	public void setAssignTime(Date assignTime) {
		this.assignTime = assignTime;
	}

	@Override
	public Date getDueTime() {
		return dueTime;
	}

	@Override
	public void setDueTime(Date dueTime) {
		this.dueTime = dueTime;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		if (status == null)
			throw new IllegalArgumentException("status cannot be null");

		this.status = status;
	}

	@Override
	public void setStatus(String status) {
		this.status = Status.valueOf(status.toUpperCase(Locale.ENGLISH));
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	@Override
	public Place getPlace() {
		return place;
	}

	@Override
	public void setPlace(Place place) {
		if (place == null)
			throw new IllegalArgumentException("place cannot be null");

		this.place = place;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		if (title == null || title.compareTo("") == 0)
			throw new IllegalArgumentException(
					"title cannot be neither null nor empty");

		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	};

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public Worker getAssignedWorker() {
		return assignedWorker;
	}

	@Override
	public void setAssignedWorker(Worker assignedWorker) {
		this.assignedWorker = assignedWorker;
	}

	@Override
	public WorkGroup getAssignedGroup() {
		return assignedGroup;
	}

	@Override
	public void setAssignedGroup(WorkGroup assignedGroup) {
		this.assignedGroup = assignedGroup;
	}

	/**
	 * returns the list of parameters needed for a post request to create a new
	 * task
	 */
	public List<NameValuePair> getParams() {

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (getPriority() != null)
			params.add(new BasicNameValuePair(PRIORITY, getPriority().toString()));
		else
			params.add(new BasicNameValuePair(PRIORITY, ""));

		params.add(new BasicNameValuePair(PLACE, String.valueOf(getPlace()
				.getId())));

		params.add(new BasicNameValuePair(TITLE, title));

		params.add(new BasicNameValuePair(DESCRIPTION, description));

		if (getLatitude() != null && getLongitude() != null) {
			params.add(new BasicNameValuePair(LATITUDE, String
					.valueOf(getLatitude())));

			params.add(new BasicNameValuePair(LONGITUDE, String
					.valueOf(getLongitude())));
		} else {
			params.add(new BasicNameValuePair(LATITUDE, ""));

			params.add(new BasicNameValuePair(LONGITUDE, ""));
		}

		return params;
	}

	@Override
	public int compareTo(SimpleTask another) {
		return Priority.getIndex(another.priority)
				- Priority.getIndex(priority);
	}

}