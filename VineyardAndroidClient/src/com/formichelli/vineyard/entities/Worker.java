package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class Worker {
	public final static String ID = "id";
	public final static String USERNAME = "username";
	public final static String NAME = "name";
	public final static String EMAIL = "email";
	public final static String ROLES = "role";

	private int id;
	private String username;
	private String name;
	private String email;
	private Set<Role> roles;
	private List<WorkGroup> groups;

	public enum Role {
		OPERATOR, ADMIN;
	}

	public Worker() {
		groups = new ArrayList<WorkGroup>();
	}

	public Worker(JSONObject jsonObject) throws JSONException {

		setId(jsonObject.getInt(ID));

		setUsername(jsonObject.getString(USERNAME));
		
		setEmail(jsonObject.getString(EMAIL));
		
		setName(jsonObject.getString(NAME));

		roles = new HashSet<Role>();
		setRoles(jsonObject.getString(ROLES));
		
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

	public Set<Role> getRole() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		if (roles != null)
			this.roles = roles;
		else
			this.roles.clear();
	}

	public void addRole(Role role) {
		if (role != null)
			roles.add(role);
	}

	public void setRoles(String rolesJSON) {
		this.roles.clear();

		if (rolesJSON != null) {
			String[] roles = rolesJSON.split(",");
			for (String role : roles)
				this.roles.add(Role.valueOf(role.toUpperCase(Locale.US)));
		}
	}

	public List<WorkGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<WorkGroup> groups) {
		if (groups == null)
			this.groups.clear();
		else
			this.groups = groups;
	}

	public void addGroup(WorkGroup group) {
		if (group != null)
			groups.add(group);
	}

	public void removeGroup(WorkGroup group) {
		groups.remove(group);
	}

}