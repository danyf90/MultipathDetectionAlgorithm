package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.utilities.AsyncHttpRequests;

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
    	inflater.inflate(R.menu.menu_tasks, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_issues_up:
			activity.setCurrentPlace(activity.getCurrentPlace().getParent());
			return true;
		default:
			return false;
		}
	}

	private class PlaceTasksAsyncHttpRequest extends AsyncHttpRequests {

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.loadingFragment);
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (result != null && result.size() == 1 && result.get(0) != null) {
				JSONArray tasksArray;
				ArrayList<SimpleTask> tasks = new ArrayList<SimpleTask>();

				try {
					tasksArray = new JSONArray(result.get(0));

				} catch (JSONException e) {
					return;
				}

				for (int i = 0, l = tasksArray.length(); i < l; i++) {
					try {
						tasks.add(new SimpleTask(tasksArray.getJSONObject(i)));
					} catch (JSONException e) {
					}
				}

				activity.getCurrentPlace().setTasks(tasks);

				activity.switchFragment(activity.lastFragment);
			} else {
				// TODO
			}
		}
	}
}
