package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.HashMap;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;

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
	TextView textView, issuesCount, tasksCount;
	ViewGroup issues, tasks;
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
		textView = (TextView) activity.findViewById(R.id.place_view_text);
		issues = (ViewGroup) activity.findViewById(R.id.place_view_issues);
		issuesCount = (TextView) activity
				.findViewById(R.id.place_view_issues_count);
		tasks = (ViewGroup) activity.findViewById(R.id.place_view_tasks);
		tasksCount = (TextView) activity
				.findViewById(R.id.place_view_tasks_count);
		childrenList = (ListView) activity.findViewById(R.id.place_view_children_list);
		redBorder = getResources()
				.getDrawable(R.drawable.white_with_red_border);
		whiteBorder = getResources().getDrawable(
				R.drawable.white_with_wine_border);

		activity.setTitle(activity.getCurrentPlace().getName());
		
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
	// onCreateOptionMenu, their calling order is different in different version of android
	private void init() {
		loadPlace(activity.getCurrentPlace());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_place_viewer_up:
			loadPlace(activity.getCurrentPlace().getParent());
			break;
		default:
			return false;
		}

		return true;
	}

	// setBackgroundDrawable needed for compatibility with API 8
	@SuppressWarnings("deprecation")
	public void loadPlace(Place p) {
		int i, t;

		activity.setCurrentPlace(p);

		if (p.getParent() == null)
			upItem.setVisible(false);
		else
			upItem.setVisible(true);

		String text = p.getDescription();
		
		HashMap<String,String> attributes =p.getAttributes(); 
		if (attributes.size() != 0) {
			text += "\n\n" + getString(R.string.attributes_label) + "\n\n";
			
			for (String key: p.getAttributes().keySet())
				text += key + ":   " + attributes.get(key) + "\n";
		}
		
		ArrayList<Place> children = p.getChildren();
		
		if (children.size() != 0){
			text += "\n\n" + getString(R.string.children_label);
		}
		
		this.textView.setText(text);

		if (placeAdapter != null) {
			placeAdapter.replaceItems(p.getChildren());
		} else {
			placeAdapter = new PlaceAdapter(activity,
					R.layout.drawer_list_item, p.getChildren());
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

		i = activity.getServer().getIssuesCount(p);
		issuesCount.setText(String.valueOf(i));
		if (i != 0)
			issues.setBackgroundDrawable(redBorder);
		else
			issues.setBackgroundDrawable(whiteBorder);

		t = activity.getServer().getTasksCount(p);
		tasksCount.setText(String.valueOf(t));
		if (t != 0)
			tasks.setBackgroundDrawable(redBorder);
		else
			tasks.setBackgroundDrawable(whiteBorder);
	}
}
