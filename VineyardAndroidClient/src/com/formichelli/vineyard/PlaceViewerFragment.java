package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.HashMap;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
	TextView description, issuesCount, tasksCount, childrenIssuesCount,
			childrenTasksCount, childrenLabel;
	ViewGroup attributesLabels, attributesValues, issues, tasks;
	PlaceAdapter placeAdapter;
	ListView childrenList;
	MenuItem upItem;
	Drawable redBorder, whiteBorder;

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

		issues = (ViewGroup) activity.findViewById(R.id.place_view_issues);
		issuesCount = (TextView) activity
				.findViewById(R.id.place_view_issues_count);
		childrenIssuesCount = (TextView) activity
				.findViewById(R.id.place_view_children_issues_count);

		tasks = (ViewGroup) activity.findViewById(R.id.place_view_tasks);
		tasksCount = (TextView) activity
				.findViewById(R.id.place_view_tasks_count);
		childrenTasksCount = (TextView) activity
				.findViewById(R.id.place_view_children_tasks_count);

		description = (TextView) activity
				.findViewById(R.id.place_view_description);

		attributesLabels = (ViewGroup) activity
				.findViewById(R.id.place_view_attributes_labels);
		attributesValues = (ViewGroup) activity
				.findViewById(R.id.place_view_attributes_values);

		childrenLabel = (TextView) activity
				.findViewById(R.id.place_view_children_label);
		childrenList = (ListView) activity
				.findViewById(R.id.place_view_children_list);

		redBorder = getResources()
				.getDrawable(R.drawable.white_with_red_border);
		whiteBorder = getResources().getDrawable(
				R.drawable.white_with_wine_border);

		issues.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchFragment(activity.issuesFragment);
			}
		});

		tasks.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.switchFragment(activity.tasksFragment);
			}
		});

		if (upItem != null)
			init();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_place_viewer, menu);

		upItem = menu.findItem(R.id.action_place_viewer_up);

		if (activity != null)
			init();

		super.onCreateOptionsMenu(menu, inflater);
	}

	// This function must be called after both onActivityCreated and
	// onCreateOptionMenu, their calling order is different in different version
	// of android
	private void init() {
		loadPlace(activity.getCurrentPlace());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_place_viewer_up:
			loadPlace(activity.getCurrentPlace().getParent());
			break;
		case R.id.action_place_viewer_refresh:
			activity.sendRootPlaceRequest();
			break;
		default:
			return false;
		}

		return true;
	}

	// setBackgroundDrawable needed for compatibility with API 8
	@SuppressWarnings("deprecation")
	public void loadPlace(Place p) {
		int c;

		activity.setCurrentPlace(p);

		if (p.getParent() == null)
			upItem.setVisible(false);
		else
			upItem.setVisible(true);

		// set photo TODO

		// set issues count
		c = p.getIssuesCount();
		issuesCount.setText(String.valueOf(c));
		childrenIssuesCount.setText("(" + (p.getChildrenIssuesCount() - c)
				+ ")");
		if (c != 0)
			issues.setBackgroundDrawable(redBorder);
		else
			issues.setBackgroundDrawable(whiteBorder);

		// set tasks count
		c = p.getTasksCount();
		tasksCount.setText(String.valueOf(c));
		childrenTasksCount.setText("(" + (p.getChildrenTasksCount() - c) + ")");
		if (c != 0)
			tasks.setBackgroundDrawable(redBorder);
		else
			tasks.setBackgroundDrawable(whiteBorder);

		// set description
		description.setText(p.getDescription());

		// set attributes
		attributesLabels.removeAllViews();
		attributesValues.removeAllViews();
		HashMap<String, String> attributes = p.getAttributes();
		if (attributes.size() != 0)
			for (String key : p.getAttributes().keySet()) {
				TextView t = new TextView(activity);
				t.setTypeface(null, Typeface.BOLD_ITALIC);
				t.setText(key + ":");
				attributesLabels.addView(t);

				t = new TextView(activity);
				t.setText(attributes.get(key));
				attributesValues.addView(t);
			}

		ArrayList<Place> children = p.getChildren();

		if (children.size() == 0) {
			childrenLabel.setVisibility(View.INVISIBLE);
			childrenList.setVisibility(View.INVISIBLE);
		} else {
			// set children
			if (placeAdapter != null) {
				placeAdapter.replaceItems(children);
			} else {
				placeAdapter = new PlaceAdapter(activity,
						R.layout.place_list_item, children);
				childrenList.setAdapter(placeAdapter);
			}

			if (childrenList.getAdapter() == null)
				childrenList.setAdapter(placeAdapter);

			if (childrenList.getOnItemClickListener() == null)
				childrenList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						loadPlace((Place) view.getTag());
					}
				});
			childrenLabel.setVisibility(View.VISIBLE);
			childrenList.setVisibility(View.VISIBLE);
		}
	}
}
