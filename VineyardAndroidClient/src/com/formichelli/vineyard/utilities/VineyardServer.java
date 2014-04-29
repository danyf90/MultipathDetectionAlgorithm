package com.formichelli.vineyard.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.Worker;

public class VineyardServer {
	private final static String placeHierarchyAPI = "/api/place/hierarchy";

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

	public String getUrl(){
		return url;
	}

	public void setUrl(String url) {
		// serverUrl must not end with '/'
		if (url.endsWith("/"))
			this.url = url.substring(0, url .length()-1);
		else
			this.url = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Obtain the entire tree of the places
	 * 
	 * @return root place
	 */
	public Place getRootPlace() {
		try {
			new asyncHttpRequest().execute(url + placeHierarchyAPI,
					String.valueOf(port)).get();
		} catch (InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			JSONObject rootPlaceObject = new JSONObject();
			return new Place(rootPlaceObject);
		} catch (JSONException e) {
			return null;
		}
	}

	/**
	 * Obtain the entire tree of the places
	 * 
	 * @return JSON representation of the root place
	 */
	public String getRootPlaceJSON() {
		try {
			return new asyncHttpRequest()
					.execute(url + placeHierarchyAPI).get();
		} catch (InterruptedException | ExecutionException e) {
			return null;
		}
	}

	public void sendIssue(IssueTask i) {
		// TODO
	};

	public int getIssuesCount(Place p) {
		// TODO
		return new Random().nextInt(10);
	};

	public int getTasksCount(Place p) {
		// TODO
		return new Random().nextInt(10);
	};

	/**
	 * Get the list of issues associated with place @p p
	 * 
	 * @param p
	 *            query place
	 * @return list of issues
	 */
	public ArrayList<IssueTask> getIssues(Place p) {
		ArrayList<IssueTask> issues = new ArrayList<IssueTask>();
		Worker w = new Worker();
		IssueTask i = new IssueTask();

		w.setEmail("asd@asd.asd");
		w.setName("Employee #1");

		i.setAssignedWorker(w);
		i.setAssignedGroup(null);
		i.setTitle("Problem");
		i.setDescription("There is a big problem!");
		i.setPlace(p);
		i.setPriority(Task.Priority.HIGH);

		issues.add(i);
		issues.add(i);

		return issues;
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

	public static class asyncHttpRequest extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				String requestString = params[0] + ":" + params[1] + placeHierarchyAPI;

				HttpResponse response = new DefaultHttpClient().execute(new HttpGet(
						requestString));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					return out.toString();
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					Log.e("http error", statusLine.getReasonPhrase());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
	};
}
