package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.Random;

import com.formichelli.vineyard.entities.Place;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PlacePickerFragment extends Fragment {
	Activity activity;
	ListView currentLevelPlacesListView;
	ArrayAdapter<Place> arrayAdapter;
	Place selectedPlace;
	ArrayList<Place> currentLevelPlaces;
	ViewGroup linearLayout;
	int currentLevel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_place_picker, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();

		linearLayout = (ViewGroup) activity
				.findViewById(R.id.place_picker_linear_layout);
		currentLevelPlacesListView = (ListView) activity
				.findViewById(R.id.place_picker_current_level_places);
		currentLevel = 0;

		selectedPlace = getChildrenPlaces(null).get(0);

		arrayAdapter = new ArrayAdapter<Place>(activity,
				R.layout.drawer_list_item, R.id.drawer_list_item,
				getChildrenPlaces(selectedPlace));
		currentLevelPlacesListView.setAdapter(arrayAdapter);
		currentLevelPlacesListView
				.setOnItemClickListener(listViewOnItemClickListener);

		fixListHeight(currentLevelPlacesListView);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_place_picker, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_done:
			// TODO
			break;
		default:
			return false;
		}

		return true;
	}

	private ArrayList<Place> getChildrenPlaces(Place currentPlace) {
		// TODOt
		Random r = new Random();
		int N = 10;
		ArrayList<Place> ret = new ArrayList<Place>();

		for (int i = 0; i < N; i++) {
			Place p = new Place();
			p.setName(String.valueOf(r.nextInt(1000)));
			ret.add(p);
		}

		return ret;
	}

	// ListView collapse if it is put in a ScrollView
	private void fixListHeight(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	private OnItemClickListener listViewOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.e("Place", ((TextView) view).getText().toString());
			arrayAdapter.clear();
			arrayAdapter.addAll(getChildrenPlaces(null));

			TextView t = (TextView) activity.getLayoutInflater().inflate(
					R.layout.drawer_list_item, currentLevelPlacesListView,
					false);

			t.setText(getStringForLevel(view, currentLevel));
			t.setBackgroundColor(getResources().getColor(R.color.wine_light));
			linearLayout.addView(t, currentLevel);
			currentLevel++;
		}

		private String getStringForLevel(View v, int level) {

			String s = "";
			for (int i = 0; i < level; i++)
				s += "-";

			s += ((level > 0) ? " " : "") + ((TextView) v).getText();

			return s;
		}

	};
}
