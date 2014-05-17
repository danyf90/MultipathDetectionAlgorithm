package com.formichelli.vineyard.utilities;

import android.content.SharedPreferences;

/**
 * Class that allows to cache and retrieve strings
 */
public class Cache {
	public final static String PLACES = "cache_places";
	public final static String ISSUES_AND_TASKS = "cache_issuesAndTasks";
	public final static String WORKERS = "cache_workers";
	public final static String WORK_GROUPS = "cache_workGroups";
	private final static String LAST_MODIFIED = "_last_modified";

	SharedPreferences sp;

	public Cache(SharedPreferences sp) {
		this.sp = sp;
	}

	public void putPlaces(String placesJSON, String lastModified) {
		put(PLACES, placesJSON, lastModified);
	}

	public String getPlaces() {
		return sp.getString(PLACES, null);
	}
	public String getPlacesLastModified() {
		return sp.getString(PLACES + LAST_MODIFIED, null);
	}

	public void putIssuesAndTasks(String issuesAndTasksJSON, String lastModified) {
		put(ISSUES_AND_TASKS, issuesAndTasksJSON, lastModified);
	}

	public String getIssuesAndTasks() {
		return sp.getString(ISSUES_AND_TASKS, null);
	}

	public String getIssuesAndTasksLastModified() {
		return sp.getString(ISSUES_AND_TASKS + LAST_MODIFIED, null);
	}

	public void putWorkers(String workersJSON, String lastModified) {
		put(WORKERS, workersJSON, lastModified);
	}

	public String getWorkers() {
		return sp.getString(WORKERS, null);
	}

	public String getWorkersLastModified() {
		return sp.getString(WORKERS + LAST_MODIFIED, null);
	}

	public void putWorkGroups(String workGroupsJSON, String lastModified) {
		put(WORK_GROUPS, workGroupsJSON, lastModified);
	}

	public String getWorkGroups() {
		return sp.getString(WORK_GROUPS, null);
	}

	public String getWorkGroupsLastModified() {
		return sp.getString(WORK_GROUPS + LAST_MODIFIED, null);
	}

	private void put(String key, String value, String lastModified) {
		sp.edit().putString(key, value).putString(key + LAST_MODIFIED, lastModified).commit();
	}
}