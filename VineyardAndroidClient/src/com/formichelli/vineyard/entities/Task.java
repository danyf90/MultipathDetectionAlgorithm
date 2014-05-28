package com.formichelli.vineyard.entities;

import java.util.Date;
import java.util.Locale;

import org.json.JSONException;

public interface Task {

	public enum Status {
		NEW, ASSIGNED, RESOLVED;

		public static int getIndex(Status status) {
			if (status == null)
				return 0;

			switch (status) {
			case NEW:
				return 1;
			case ASSIGNED:
				return 2;
			case RESOLVED:
				return 3;
			default:
				return -1;
			}
		}
	};

	public enum Priority {
		LOW, MEDIUM, HIGH;

		public static int getIndex(Priority priority) {
			if (priority == null)
				return 0;

			switch (priority) {
			case LOW:
				return 1;
			case MEDIUM:
				return 2;
			case HIGH:
				return 3;
			default:
				return -1;
			}
		}

		public static Priority fromString(String priority) throws JSONException {
			if (priority == null)
				return null;
			if (priority.toUpperCase(Locale.US).compareTo("LOW") == 0)
				return LOW;
			if (priority.toUpperCase(Locale.US).compareTo("MEDIUM") == 0)
				return MEDIUM;
			if (priority.toUpperCase(Locale.US).compareTo("HIGH") == 0)
				return HIGH;

			// throw new JSONException("Invalid priority: " + priority); // TODO
			// uncomment when server is updated

			return null;
		}
	};

	public int getId();

	public void setId(int id);

	public int getAssignerId();

	public void setAssignerId(int assignerId);

	public int getModifierId();

	public void setModifierId(int assignerId);

	public Date getCreateTime();

	public void setCreateTime(Date createTime);

	public Date getAssignTime();

	public void setAssignTime(Date assignTime);

	public Date getDueTime();

	public void setDueTime(Date dueTime);

	public abstract String getDescription();

	public void setDescription(String description);

	public Status getStatus();

	public void setStatus(Status status);

	public void setStatus(String priority);

	public Priority getPriority();

	public void setPriority(Priority priority);

	public Place getPlace();

	public void setPlace(Place place);

	public String getTitle();

	public void setTitle(String title);

	public Double getLatitude();

	public void setLatitude(Double latitude);

	public Double getLongitude();

	public void setLongitude(Double longitude);

	public Worker getAssignedWorker();

	public void setAssignedWorker(Worker assignedWorker);

	public WorkGroup getAssignedGroup();

	public void setAssignedGroup(WorkGroup assignedGroup);

}