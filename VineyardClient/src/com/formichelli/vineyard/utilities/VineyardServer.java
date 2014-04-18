package com.formichelli.vineyard.utilities;

import java.util.ArrayList;
import java.util.Random;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.Worker;

public class VineyardServer {

	static public Place getRootPlace() {
		// TODO get place hierarchy from server

		// generate fake place hierarchy
		Place root = new Place();
		root.setName("root");
		root.setDescription("This is the root description");
		ArrayList<Place> children = root.getChildren();
		int N = 10;

		for (int i = 0; i < N; i++) {
			Place p = new Place();
			p.setName("P" + i);
			p.setDescription("Place #" + i);
			p.setParent(root);
			ArrayList<Place> childChildren = p.getChildren();

			for (int j = 0; j < i; j++) {
				Place cp = new Place();
				cp.setName(p.getName() + j);
				cp.setDescription(p.getDescription() + j);
				cp.setParent(p);
				childChildren.add(cp);
			}
			p.setChildren(childChildren);

			children.add(p);
		}

		root.setChildren(children);

		return root;
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

	static public ArrayList<IssueTask> getIssues(Place p){
		ArrayList<IssueTask> issues = new ArrayList<IssueTask>();
		Worker w = new Worker();
		IssueTask i = new IssueTask();
		
		w.setEmail("asd@asd.asd");
		w.setName("Employee #1");
		
		i.setAssignedWorker(w);
		i.setAssignedGroup(null);
		i.setTitle("Problem");
		i.setDescription("There is a big problem!");
		i.setPlaceId(3);
		i.setPriority(Task.Priority.HIGH);

		issues.add(i);
		issues.add(i);
		
		return issues;
	};

	static public ArrayList<SimpleTask> getTasks(Place p){
		ArrayList<SimpleTask> issues = new ArrayList<SimpleTask>();
		return issues;
	};
}
