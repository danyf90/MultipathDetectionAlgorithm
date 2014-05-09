package com.formichelli.vineyard.utilities;

import android.content.SharedPreferences;

/**
 * Class that allows to cache and retrieve strings
 */
public class Cache {
	public final static String PLACES = "cache_places";
	public final static String ISSUES_AND_TASKS = "cache_issuesAndTasks";
	public final static String WORKERS = "cache_workers";
	public final static String WORKER_GROUPS = "cache_workerGroups";

	SharedPreferences sp;

	public Cache(SharedPreferences sp) {
		this.sp = sp;
	}

	public void putPlaces(String placesJSON) {
		sp.edit().putString(PLACES, placesJSON).apply();
	}

	public String getPlaces() {
		return sp.getString(PLACES, null);
	}


	public void putIssuesAndTasks(String issuesAndTasksJSON) {
		sp.edit().putString(ISSUES_AND_TASKS, issuesAndTasksJSON).apply();		
	}

	public String getIssuesAndTasks() {
		return sp.getString(ISSUES_AND_TASKS, null);		
	}

	public void putWorkers(String workersJSON) {
		sp.edit().putString(WORKERS, workersJSON).apply();		
	}

	public String getWorkers() {
		return sp.getString(WORKERS, null);		
	}
	public void putWorkerGroups(String workerGroupsJSON) {
		sp.edit().putString(WORKER_GROUPS, workerGroupsJSON).apply();		
	}

	public String getWorkerGroups() {
		return sp.getString(WORKER_GROUPS, null);		
	}

}