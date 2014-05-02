package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONException;

import com.formichelli.vineyard.utilities.AsyncHttpRequests;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
		inflater.inflate(R.menu.tasks, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_tasks_up:
			activity.setCurrentPlace(activity.getCurrentPlace().getParent());
			return true;
		default:
			return false;
		}
	}

	class PlaceTasksAsyncHttpRequest extends AsyncHttpRequests {
		private static final String TAG = "PlaceTasksAsyncHttpRequest";

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.getLoadingFragment());
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			String tasksJSON;

			try {
				if (result != null && result.size() == 1
						&& result.get(0) != null) {
					// request OK, parse JSON to get tasks, cache data and show
					// retrieved tasks
					tasksJSON = result.get(0);

					activity.getCache().setPlaceTasksJSON(
							activity.getCurrentPlace().getId(), tasksJSON);

				} else {
					tasksJSON = activity.getCache().getPlaceTasksJSON(
							activity.getCurrentPlace().getId());

					Toast.makeText(activity,
							activity.getString(R.string.cache_data_used),
							Toast.LENGTH_SHORT).show();
				}

				activity.getCurrentPlace().setTasks(tasksJSON);
				activity.switchFragment();

			} catch (JSONException e) {
				android.util.Log.e(TAG, e.getLocalizedMessage());
				activity.getLoadingFragment().setError();
			}
		}
	}
}
