package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.List;

public class Worker {
	private int id;
	private String username;
	private String name;
	private String email;
	private String password;
	private int role;
	private List<WorkGroup> groups;

	public Worker() {
		groups = new ArrayList<WorkGroup>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getRole() {
		return this.role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public List<WorkGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<WorkGroup> groups) {
		if (groups == null)
			this.groups = new ArrayList<WorkGroup>();
		
		this.groups = groups;
	}
	public void addGroup(WorkGroup group) {
		groups.add(group);
	}

}