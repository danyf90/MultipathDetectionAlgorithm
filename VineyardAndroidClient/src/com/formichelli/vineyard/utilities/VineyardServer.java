package com.formichelli.vineyard.utilities;

public class VineyardServer {
	public final static String PLACES_HIERARCHY_API = "/api/place/hierarchy/";
	public static final String PHOTO_API = "/api/photo/%s?w=%d&h=%d";
	public static final String LOGIN_API = "/api/worker/login/";
	public static final String ISSUES_AND_TASKS_API = "/api/task/";
	public static final String OPEN_ISSUES_AND_TASKS_API = "/api/task/open/";
	public static final String WORKERS_API = "/api/worker/";
	public static final String WORKGROUPS_API = "/api/group/";

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
