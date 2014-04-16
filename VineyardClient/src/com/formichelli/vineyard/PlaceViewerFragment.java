package com.formichelli.vineyard;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PlaceViewerFragment extends Fragment {
	VineyardMainActivity activity;
	TextView desc, issuesCount, tasksCount;
	ViewGroup issues, tasks;
	PlaceAdapter placeAdapter;
	ListView children;
	MenuItem upItem;
	Place currentPlace;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_place_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		desc = (TextView) activity.findViewById(R.id.place_view_description);
		issues = (ViewGroup) activity.findViewById(R.id.place_view_issues);
		issuesCount = (TextView) activity
				.findViewById(R.id.place_view_issues_count);
		tasks = (ViewGroup) activity.findViewById(R.id.place_view_tasks);
		tasksCount = (TextView) activity
				.findViewById(R.id.place_view_tasks_count);
		children = (ListView) activity.findViewById(R.id.place_view_children);

		issues.setOnClickListener(startIssuesFragment);

		tasks.setOnClickListener(startTasksFragment);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_place_viewer, menu);

		upItem = menu.findItem(R.id.action_place_viewer_up);
		loadPlace(VineyardServer.getRootPlace());
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_place_viewer_up:
			loadPlace(currentPlace.getParent());
			break;
		default:
			return false;
		}

		return true;
	}

	private void loadPlace(Place p) {
		currentPlace = p;
		
		if (p.getParent() == null)
			upItem.setVisible(false);
		else
			upItem.setVisible(true);

		this.desc.setText(p.getDescription());

		if (placeAdapter != null)
			placeAdapter.replaceItems(p.getChildren());
		else {
			placeAdapter = new PlaceAdapter(activity,
					R.layout.drawer_list_item, p.getChildren());
			children.setAdapter(placeAdapter);
			children.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					loadPlace((Place) view.getTag());
				}
			});
		}

		issuesCount.setText(VineyardServer.getIssuesCount(p));
		tasksCount.setText(VineyardServer.getTasksCount(p));
	}

	OnClickListener startIssuesFragment = new OnClickListener() {

		@Override
		public void onClick(View v) {
			activity.issuesFragment.setPlace(currentPlace);
			getActivity().getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, activity.issuesFragment).commit();
		}
	};

	OnClickListener startTasksFragment = new OnClickListener() {

		@Override
		public void onClick(View v) {
			activity.tasksFragment.setPlace(currentPlace);
			getActivity().getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, activity.tasksFragment).commit();
		}
	};

}
