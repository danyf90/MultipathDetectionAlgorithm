package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import java.util.List;

import com.formichelli.vineyard.entities.Worker;

public class WorkGroup {
	private int id;
	private String name;
	private String description;
	private List<Worker> workers;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
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
			this.workers = new ArrayList<Worker>();
		else
			this.workers = workers;
	}

	public void addWorker(Worker worker) {
		if (worker == null)
			workers.add(worker);
	}

	public void removeWorker(Worker worker) {
		if (worker == null)
			workers.remove(worker);
	}

}