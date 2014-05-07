package com.formichelli.vineyard.entities;

import java.util.Date;

public interface Task {

	public enum Status {
		NEW, ASSIGNED, DONE;
	};

	public enum Priority {
		NOT_SET, LOW, MEDIUM, HIGH;

		public int toInt() {
			switch (this) {
			case NOT_SET:
				return 0;
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

	public int getId();

	public void setId(int id);

	public int getAssignerId();

	public void setAssignerId(int assignerId);

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

	public void setPriority(String priority);

	public int getPlaceId();

	public void setPlaceId(int p);

	public String getTitle();

	public void setTitle(String title);

	public double getLatitude();

	public void setLatitude(double latitude);

	public double getLongitude();

	public void setLongitude(double longitude);

	public int getAssignedWorkerId();

	public void setAssignedWorkerId(int assignedWorkerId);

	public int getAssignedGroupId();

	public void setAssignedGroupId(int assignedGroupId);

}