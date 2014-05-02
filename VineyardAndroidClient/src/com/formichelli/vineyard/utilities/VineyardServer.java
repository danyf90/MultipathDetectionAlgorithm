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
	private int port;

	public VineyardServer(String serverUrl) {
		setUrl(serverUrl);
		setPort(80);
	}

	public VineyardServer(String serverUrl, int port) {
		setUrl(serverUrl);
		setPort(port);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		// serverUrl must not end with '/'
		if (url.endsWith("/"))
			this.url = url.substring(0, url.length() - 1);
		else
			this.url = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void sendIssue(IssueTask i) {
		// TODO
	};

	/**
	 * Get the list of issues associated with place @p p
	 * 
	 * @param p
	 *            query place
	 * @return list of issues
	 */
//	public ArrayList<IssueTask> getIssues(Place p) {
//		ArrayList<IssueTask> issues = new ArrayList<IssueTask>();
//		Worker w = new Worker();
//		IssueTask i = new IssueTask();
//
//		w.setEmail("asd@asd.asd");
//		w.setName("Employee #1");
//
//		i.setAssignedWorker(w);
//		i.setAssignedGroup(null);
//		i.setTitle("Problem");
//		i.setDescription("There is a big problem!");
//		i.setPlace(p);
//		i.setPriority(Task.Priority.HIGH);
//
//		issues.add(i);
//		issues.add(i);
//
//		return issues;
//	};

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
