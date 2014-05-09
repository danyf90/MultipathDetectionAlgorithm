package com.formichelli.vineyard.utilities;

public class VineyardServer {
	public final static String PLACES_HIERARCHY_API = "/api/place/hierarchy/";
	public final static String PLACES_STATS_API = "/api/place/stats/";
	public final static String PLACE_ISSUES_API = "/api/place/%d/issues/";
	public final static String PLACE_TASKS_API = "/api/place/%d/tasks/";
	public static final String PHOTO_API = "/api/photo/%s?w=%d&h=%d";
	public static final String LOGIN_API = "/api/worker/login/";
	public static final String ADD_ISSUE_API = "/api/task/";
	public static final String EDIT_ISSUE_API = "/api/task/";
	public static final String ISSUES_AND_TASKS_API = "/api/task/";

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
}
