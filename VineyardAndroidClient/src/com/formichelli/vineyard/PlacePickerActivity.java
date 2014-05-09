package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;
import com.formichelli.vineyard.utilities.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity which allow to choose a place and return its id and its ancestors id
 * as activity result
 */
public class PlacePickerActivity extends ActionBarActivity {
	public final static String HIERARCHY = "placeHierarchy";
	public final static String ANCESTORS = "selectedPlaceAncestors";

	ListView currentLevelPlacesListView;
	PlaceAdapter placeAdapter;
	Place selectedPlace;
	ViewGroup ancestorsList;
	int currentLevel = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_place_picker);

		ancestorsList = (ViewGroup) findViewById(R.id.place_picker_ancestors_list);
		placeAdapter = new PlaceAdapter(this, R.layout.place_list_item, null);
		currentLevelPlacesListView = (ListView) findViewById(R.id.place_picker_current_level_places);
		currentLevelPlacesListView.setAdapter(placeAdapter);
		currentLevelPlacesListView.setOnItemClickListener(onChildClickListener);
		getSupportActionBar().setTitle(R.string.activity_place_picker);

		Place rootPlace;
		try {
			// generate places hierarchy from places JSON and select rootPlace
			rootPlace = new Place(new JSONObject(getIntent().getExtras()
					.getString(HIERARCHY)));
			selectPlace(rootPlace);
		} catch (JSONException e) {
			setResult(Activity.RESULT_FIRST_USER);
			finish();
		}

		// get the selected place ancestors id array from the extras and select
		// the place
		ArrayList<Integer> ids = getIntent().getExtras().getIntegerArrayList(
				ANCESTORS);
		// Navigate from root to selected place, root is already selected
		for (int i = ids.size() - 2; i >= 0; i--) {
			for (Place p : selectedPlace.getChildren())
				if (p.getId() == ids.get(i)) {
					selectPlace(p);
					break;
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.place_picker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.place_picker_action_done:
			// Put the ancestors id array in the result intent and terminate
			Intent resultIntent = new Intent();

			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (; selectedPlace != null; selectedPlace = selectedPlace
					.getParent())
				ids.add(selectedPlace.getId());

			resultIntent.putExtra(ANCESTORS, ids);
			setResult(RESULT_OK, resultIntent);
			finish();
			break;
		case R.id.place_picker_action_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		default:
			return false;
		}

		return true;
	}

	private void selectPlace(Place place) {
		TextView t;

		if (place == null)
			throw new IllegalArgumentException("place cannot be null");
		if (selectedPlace == place)
			return;

		selectedPlace = place;

		// if the place is in the ancestors list remove it and its offsprings
		for (int i = 0, l = ancestorsList.getChildCount(); i < l; i++)
			if (ancestorsList.getChildAt(i).getTag() == selectedPlace) {
				removeItemsAfter(ancestorsList, ancestorsList.getChildAt(i));
				break;
			}

		// put the selected place in the ancestors list
		t = (TextView) getLayoutInflater()
				.inflate(R.layout.ancestors_list_item,
						currentLevelPlacesListView, false);
		t.setText(getStringForLevel(selectedPlace.getName(), currentLevel));
		t.setTag(selectedPlace);
		t.setOnClickListener(PlacePickerActivity.this.onAncestorClickListener);
		ancestorsList.addView(t);

		// show place children
		placeAdapter.replaceItems(selectedPlace.getChildren());
		Util.fixListHeight(currentLevelPlacesListView);
		currentLevel++;
	}

	private void removeItemsAfter(ViewGroup ancestorsList, View v) {
		// find TextView v and delete all its successors
		for (int i = 0, l = ancestorsList.getChildCount(); i < l; i++)
			if (ancestorsList.getChildAt(i) == v) {
				currentLevel = i;
				for (int j = i; i < l; i++)
					ancestorsList.removeViewAt(j);
				break;
			}
	}

	// Returns a string that contains the placeName but shows also an indication
	// about the place level in the hierarchy
	private String getStringForLevel(String placeName, int level) {
		String ret = "";
		for (int i = 0; i < level; i++)
			ret += "-";

		ret += ((level > 0) ? " " : "") + placeName;

		return ret;
	}

	private OnItemClickListener onChildClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectPlace((Place) view.getTag());
		}
	};

	OnClickListener onAncestorClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			selectPlace((Place) v.getTag());
		}
	};

}
