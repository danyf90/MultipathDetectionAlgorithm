package com.formichelli.vineyard.entities;

import java.util.ArrayList;
import com.formichelli.vineyard.entities.Worker;

public class WorkGroup {
	private String name;
	private String description;
	private Worker[] workers;
	public ArrayList<Worker> unnamed_Worker_ = new ArrayList<Worker>();

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public Worker[] getWorkers() {
		return this.workers;
	}
}