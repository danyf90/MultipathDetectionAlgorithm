package com.formichelli.vineyard.utilities;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONException;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.Worker;

public class VineyardServer {

	/**
	 * Obtain the entire tree of the places
	 * 
	 * @return root place
	 */
	static public Place getRootPlace() throws JSONException {
		return new Place(
				"[{\"id\":\"13\",\"name\":\"Vigneto Acino Fresco\",\"parent\":null,\"description\":null,\"location\":null},{\"id\":\"1\",\"name\":\"Vigna A\",\"parent\":\"13\",\"description\":\"Prima vigna\",\"location\":\"\"},{\"id\":\"2\",\"name\":\"Vigna B\",\"parent\":\"13\",\"description\":\"Seconda vigna\",\"location\":\"\"},{\"id\":\"3\",\"name\":\"Vigna C\",\"parent\":\"13\",\"description\":\"Terza vigna\",\"location\":\"\"},{\"id\":\"14\",\"name\":\"Vigna E\",\"parent\":\"13\",\"description\":\"Ancora una vigna...\",\"location\":null},{\"id\":\"4\",\"name\":\"Filare A1\",\"parent\":\"1\",\"description\":\"Primo filare prima vigna\",\"location\":null},{\"id\":\"5\",\"name\":\"Filare A2\",\"parent\":\"1\",\"description\":\"Secondo filare prima vigna\",\"location\":null},{\"id\":\"6\",\"name\":\"Filare A3\",\"parent\":\"1\",\"description\":\"Terzo filare seconda vigna\",\"location\":null},{\"id\":\"7\",\"name\":\"Filare B1\",\"parent\":\"2\",\"description\":\"Primo filare seconda vigna\",\"location\":null},{\"id\":\"8\",\"name\":\"Filare C1\",\"parent\":\"3\",\"description\":\"Primo filare terza vigna\",\"location\":null},{\"id\":\"9\",\"name\":\"Filare C2\",\"parent\":\"3\",\"description\":\"Secondo filare terza vigna\",\"location\":null},{\"id\":\"10\",\"name\":\"Filare C3\",\"parent\":\"3\",\"description\":\"Terzo filare terza vigna\",\"location\":null},{\"id\":\"11\",\"name\":\"Fila C21\",\"parent\":\"8\",\"description\":\"Fila 1 Filare C2\",\"location\":null},{\"id\":\"12\",\"name\":\"Fila C22\",\"parent\":\"8\",\"description\":\"Fila 2 Filare C2\",\"location\":null}]");
	}

	static public void sendIssue(IssueTask i) {
		// TODO
	};

	static public int getIssuesCount(Place p) {
		// TODO
		return new Random().nextInt(10);
	};

	static public int getTasksCount(Place p) {
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
	static public ArrayList<IssueTask> getIssues(Place p) {
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
	static public ArrayList<SimpleTask> getTasks(Place p) {
		ArrayList<SimpleTask> issues = new ArrayList<SimpleTask>();
		return issues;
	};
}
