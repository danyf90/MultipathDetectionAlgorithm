package com.formichelli.vineyard.entities;

import java.util.Date;

import com.formichelli.vineyard.R;

import android.location.Location;

public interface Task {

	public enum Status {
		NEW, ASSIGNED, DONE;
		public int getStringId() {
			switch (this) {
			case NEW:
				return R.string.status_new;
			case ASSIGNED:
				return R.string.status_assigned;
			case DONE:
				return R.string.status_done;
			}
			return -1;
		}
	};

	public enum Priority {
		LOW, MEDIUM, HIGH;
		public int getStringId() {
			switch (this) {
			case LOW:
				return R.string.priority_low;
			case MEDIUM:
				return R.string.priority_medium;
			case HIGH:
				return R.string.priority_high;
			}
			return -1;
		}

		public int toInt() {
			switch (this) {
			case LOW:
				return 0;
			case MEDIUM:
				return 1;
			case HIGH:
				return 2;
			}
			return -1;
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