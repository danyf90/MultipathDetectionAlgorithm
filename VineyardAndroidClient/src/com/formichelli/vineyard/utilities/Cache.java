package com.formichelli.vineyard.utilities;

import android.content.SharedPreferences;

public class Cache {
	public final static String PLACES_HIERARCHY = "places";
	public final static String PLACES_STATS = "stats";
	public final static String PLACES_ISSUES = "places";
	public final static String PLACES_TASKS = "places";

	SharedPreferences sp;

	public Cache(SharedPreferences sp) {
		this.sp = sp;
	}

	public void setRootPlaceJSON(String rootPlaceJSON) {
		sp.edit().putString(PLACES_HIERARCHY, rootPlaceJSON).apply();
	}

	public String getRootPlaceJSON() {
		return sp.getString(PLACES_HIERARCHY, null);
	}

	public void setPlacesStatsJSON(String placesStatsJSON) {
		sp.edit().putString(PLACES_STATS, placesStatsJSON).apply();
	}

	public String getPlacesStatsJSON() {
		return sp.getString(PLACES_STATS, null);
	}

	public void setPlacesIssuesJSON(String placesIssuesJSON) {
		sp.edit().putString(PLACES_ISSUES, placesIssuesJSON).apply();
	}

	public void setPlaceIssuesJSON(int placeId, String placeIssuesJSON) {
		sp.edit().putString(PLACES_ISSUES + placeId, placeIssuesJSON).apply();
	}

	public String getPlacesIssuesJSON() {
		return sp.getString(PLACES_ISSUES, null);
	}

	public String getPlaceIssuesJSON(int placeId) {
		return sp.getString(PLACES_ISSUES + placeId, null);
	}

	public void setPlacesTasksJSON(String placesTasksJSON) {
		sp.edit().putString(PLACES_TASKS, placesTasksJSON).apply();
	}

	public void setPlaceTasksJSON(int placeId, String placeTasksJSON) {
		sp.edit().putString(PLACES_TASKS + placeId, placeTasksJSON).apply();
	}

	public String getPlacesTasksJSON() {
		return sp.getString(PLACES_TASKS, null);
	}

	public String getPlaceTasksJSON(int placeId) {
		return sp.getString(PLACES_TASKS + placeId, null);
	}

}