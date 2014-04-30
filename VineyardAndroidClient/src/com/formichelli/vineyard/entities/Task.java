package com.formichelli.vineyard.entities;

import java.util.Date;

import android.location.Location;

public interface Task {

	public enum Status {
		NEW, ASSIGNED, DONE;
	};

	public enum Priority {
		LOW, MEDIUM, HIGH;

		public int toInt() {
			switch (this) {
			case LOW:
				return 1;
			case MEDIUM:
				return 2;
			case HIGH:
				return 3;
			}
			return 0;
		}
	};

	public abstract String getDescription();

	public void setDescription(String description);

	public Worker getAssignee();

	public void setAssignee(Worker assignee);

	public Status getStatus();

	public void setStatus(Status status);

	public Priority getPriority();

	public void setPriority(Priority priority);

	public Place getPlace();

	public void setPlace(Place p);

	public String getTitle();

	public void setTitle(String title);

	public Location getLocation();

	public void setLocation(Location location);

	public WorkGroup getAssignedGroup();

	public void setAssignedGroup(WorkGroup assignedGroup);

	public Worker getAssignedWorker();

	public void setAssignedWorker(Worker assignedWorker);

	public Date getCreateTime();

	public Date getAssignTime();

	public Date getDueTime();

	public void setDueTime(Date dueTime);
}