package com.formichelli.vineyard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class TasksFragment extends Fragment {
	VineyardMainActivity activity;
	MenuItem upItem;
	boolean first;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_tasks, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		activity = (VineyardMainActivity) getActivity();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tasks, menu);

		upItem = menu.findItem(R.id.action_tasks_up);

		if (first) {
			// init() must be called just once after that both onActivityCreated
			// and onCreateOptionMenu are called
			init();
			first = false;
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	private void init() {
		if (activity.getCurrentPlace().getParent() != null)
			upItem.setVisible(true);
		else
			upItem.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_tasks_up:
			activity.setCurrentPlace(activity.getCurrentPlace().getParent());
			init();
			return true;
		default:
			return false;
		}
	}

}
