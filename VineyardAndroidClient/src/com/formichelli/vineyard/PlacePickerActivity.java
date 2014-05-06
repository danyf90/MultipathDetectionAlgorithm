package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;
import com.formichelli.vineyard.utilities.Util;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PlacePickerActivity extends ImmersiveActivity {
	public final static String HIERARCHY= "placeHierarchy";
	public final static String ANCESTORS= "selectedPlaceAncestors";
	
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
		currentLevelPlacesListView = (ListView) findViewById(R.id.place_picker_current_level_places);
		placeAdapter = new PlaceAdapter(this, R.layout.place_list_item,
				null);
		currentLevelPlacesListView.setAdapter(placeAdapter);
		currentLevelPlacesListView
				.setOnItemClickListener(onChildClickListener);

		Place rootPlace;
		try {
			rootPlace = new Place(new JSONObject(getIntent().getExtras()
					.getString(HIERARCHY)));
			selectPlace(rootPlace);
		} catch (JSONException e) {
			setResult(RESULT_CANCELED); // TODO distinguish between json
										// exception and cancel by user
			finish();
		}

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

		getSupportActionBar().setTitle(R.string.title_place_picker);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.place_picker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.place_picker_action_done:
			Intent resultIntent = new Intent();

			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (; selectedPlace != null; selectedPlace = selectedPlace
					.getParent())
				ids.add(selectedPlace.getId());

			resultIntent.putExtra(ANCESTORS, ids);

			setResult(RESULT_OK, resultIntent);
			finish();
			return true;
		case R.id.place_picker_action_cancel:
		default:
			setResult(RESULT_CANCELED);
			finish();
			return false;
		}
	}

	private void selectPlace(Place p) {
		TextView t;

		if (p == null || selectedPlace == p)
			return;

		// check if the place is in the ancestors list
		for (int i = 0, l = ancestorsList.getChildCount(); i < l; i++)
			if (ancestorsList.getChildAt(i).getTag() == p) {
				removeItemsAfter(ancestorsList, ancestorsList.getChildAt(i));
				break;
			}

		selectedPlace = p;

		placeAdapter.replaceItems(selectedPlace.getChildren());

		Util.fixListHeight(currentLevelPlacesListView);

		t = (TextView) getLayoutInflater()
				.inflate(R.layout.ancestors_list_item,
						currentLevelPlacesListView, false);
		t.setText(getStringForLevel(p.getName(), currentLevel));
		t.setTag(selectedPlace);
		t.setOnClickListener(PlacePickerActivity.this.onAncestorClickListener);

		ancestorsList.addView(t);
		currentLevel++;
	}

	private void removeItemsAfter(ViewGroup ancestorsList, View v) {
		// -1 because the last item is the listview
		int i, childCount = ancestorsList.getChildCount();

		// find TextView v and delete all its successors
		for (i = 0; i < childCount; i++)
			if (ancestorsList.getChildAt(i) == v) {
				currentLevel = i;
				break;
			}
		for (int j = i; i < childCount; i++)
			ancestorsList.removeViewAt(j);
	}

	private String getStringForLevel(String s, int level) {

		String ret = "";
		for (int i = 0; i < level; i++)
			ret += "-";

		ret += ((level > 0) ? " " : "") + s;

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
