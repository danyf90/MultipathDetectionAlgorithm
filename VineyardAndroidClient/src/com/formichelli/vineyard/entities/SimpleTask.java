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

public class SimpleTask implements Task {
	private final static String ID = "id";
	private final static String ASSIGNEE = "assignee";
	private final static String CREATE_TIME = "create_time";
	private final static String ASSIGN_TIME = "assign_time";
	private final static String DUE_TIME = "due_time";
	private final static String STATUS = "status";
	private final static String PRIORITY = "priority";
	private final static String PLACE = "place";
	private final static String TITLE = "title";
	private final static String DESCRIPTION = "description";
//	private final static String LATITUDE = "latitude";
//	private final static String LONGITUDE = "longitude";
	private final static String ASSIGNED_WORKER = "assigned_worker";
	private final static String ASSIGNED_GROUP = "assigned_group";

	private final static String dateFormat = "yyyy-MM-dd HH:mm:ss";

	private int id;
	private int assignerId;
	private Date createTime;
	private Date assignTime;
	private Date dueTime;
	private Status status;
	private Priority priority;
	private int placeId;
	private String title;
	private String description;
	private double latitude;
	private double longitude;
	private int assignedWorkerId;
	private int assignedGroupId;

	public SimpleTask() {
		priority = Priority.NOT_SET;
	}

	@SuppressLint("SimpleDateFormat")
	public SimpleTask(JSONObject jsonObject) throws JSONException {

		setId(jsonObject.getInt(ID));

		if (!jsonObject.isNull(ASSIGNEE))
			setAssignerId(jsonObject.getInt(ASSIGNEE));

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
			// TODO what to do?
		}

		setStatus(jsonObject.getString(STATUS));

		setPriority(jsonObject.getString(PRIORITY));

		setPlaceId(jsonObject.getInt(PLACE));

		setTitle(jsonObject.getString(TITLE));

		if (!jsonObject.isNull(DESCRIPTION))
			setDescription(jsonObject.getString(DESCRIPTION));
		else
			setDescription(null);

//		if (!jsonObject.isNull(LATITUDE))
//			setLatitude(jsonObject.getDouble(LATITUDE));
//		if (!jsonObject.isNull(LONGITUDE))
//			setLongitude(jsonObject.getDouble(LONGITUDE));

		if (!jsonObject.isNull(ASSIGNED_WORKER))
			setAssignedWorkerId(jsonObject.getInt(ASSIGNED_WORKER));

		if (!jsonObject.isNull(ASSIGNED_GROUP))
			setAssignedGroupId(jsonObject.getInt(ASSIGNED_GROUP));
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
		this.assignerId = assignerId;
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
	public void setPriority(String priority) {
		priority = priority.toUpperCase(Locale.ENGLISH).replace('-', '_');

		this.priority = Priority.valueOf(priority);
	}

	@Override
	public void setPriority(Priority priority) {
		if (priority == null)
			this.priority = Priority.NOT_SET;

		this.priority = priority;
	}

	@Override
	public int getPlaceId() {
		return placeId;
	}

	@Override
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
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

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	};

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public int getAssignedWorkerId() {
		return assignedWorkerId;
	}

	@Override
	public void setAssignedWorkerId(int assignedWorkerId) {
		this.assignedWorkerId = assignedWorkerId;
	}

	@Override
	public int getAssignedGroupId() {
		return assignedGroupId;
	}

	@Override
	public void setAssignedGroupId(int assignedGroupId) {
		this.assignedGroupId = assignedGroupId;
	}

	public List<NameValuePair> getParams() {

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair(PRIORITY, getPriority().toString()));
		params.add(new BasicNameValuePair(PLACE, String.valueOf(getPlaceId())));
		params.add(new BasicNameValuePair(TITLE, title));
		params.add(new BasicNameValuePair(DESCRIPTION, description));
//		params.add(new BasicNameValuePair(LATITUDE, String
//				.valueOf(getLatitude())));
//		params.add(new BasicNameValuePair(LONGITUDE, String
//				.valueOf(getLongitude())));

		return params;
	}

}