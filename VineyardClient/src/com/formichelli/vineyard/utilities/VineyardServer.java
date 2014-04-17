package com.formichelli.vineyard.utilities;

import java.util.ArrayList;
import java.util.Random;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;

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
}
