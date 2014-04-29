package com.formichelli.vineyard.entities;

import java.util.Date;

import android.location.Location;

public class SimpleTask implements Task {

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