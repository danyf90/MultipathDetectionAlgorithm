package com.formichelli.vineyard.utilities;

import android.content.SharedPreferences;

public class Cache {
	public final static String PLACES = "places";
	public final static String ISSUES_AND_TASKS = "places";

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

}