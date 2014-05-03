package com.formichelli.vineyard.utilities;

import java.util.ArrayList;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;

public class VineyardServer {
	public final static String PLACES_HIERARCHY_API = "/api/place/hierarchy";
	public final static String PLACES_STATS_API = "/api/place/stats";
	public final static String PLACE_ISSUES_API = "/api/place/%d/issues";
	public final static String PLACE_TASKS_API = "/api/place/%d/tasks";

	private String url;

	public VineyardServer(String serverUrl) {
		setUrl(serverUrl);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		// serverUrl must begin with http://
		if (!url.startsWith("http://"))
			url = "http://" + url;
		
		// serverUrl must not end with '/'
		if (url.endsWith("/"))
			this.url = url.substring(0, url.length() - 1);
		
		this.url = url;
	}

	public void sendIssue(IssueTask i) {
		// TODO
	};

	/**
	 * Get the list of tasks associated with place @p p
	 * 
	 * @param p
	 *            query place
	 * @return list of tasks
	 */
	public ArrayList<SimpleTask> getTasks(Place p) {
		ArrayList<SimpleTask> issues = new ArrayList<SimpleTask>();
		return issues;
	};
}
