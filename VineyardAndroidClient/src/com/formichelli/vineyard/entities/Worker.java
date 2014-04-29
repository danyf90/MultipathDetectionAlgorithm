package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import com.formichelli.vineyard.entities.WorkGroup;

public class Worker {
	private String username;
	private String name;
	private String email;
	private String password;
	private int role;
	public ArrayList<WorkGroup> unnamed_WorkGroup_ = new ArrayList<WorkGroup>();

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
}