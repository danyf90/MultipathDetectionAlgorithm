package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkGroup {
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String DESCRIPTION = "description";
	public final static String WORKERS = "workers";

	private int id;
	private String name;
	private String description;
	private List<Worker> workers;

	public WorkGroup() {
		workers = new ArrayList<Worker>();
	}

	public WorkGroup(JSONObject jsonObject) throws JSONException {

		setId(jsonObject.getInt(ID));

		setName(jsonObject.getString(NAME));

		setDescription(jsonObject.getString(DESCRIPTION));

		workers = new ArrayList<Worker>();
		if (!jsonObject.isNull(WORKERS))
			setWorkers(jsonObject.getJSONArray(WORKERS));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (id < 0)
			throw new IllegalArgumentException("id cannot be negative");

		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.compareTo("") == 0)
			throw new IllegalArgumentException(
					"name cannot be neither null nor empty");

		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Worker> getWorkers() {
		return workers;
	}

	public void setWorkers(List<Worker> workers) {
		if (workers == null)
			this.workers.clear();
		else
			this.workers = workers;
	}

	public void setWorkers(JSONArray workersArray) {
		this.workers.clear();

		for (int i = 0, l = workersArray.length(); i < l; i++)
			try {
				Worker w = new Worker();
				w.setId(workersArray.getInt(i));
				addWorker(w);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}

	public void addWorker(Worker worker) {
		if (worker != null)
			workers.add(worker);
	}

	public void removeWorker(Worker worker) {
		workers.remove(worker);
	}

}