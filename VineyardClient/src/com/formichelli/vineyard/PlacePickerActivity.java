package com.formichelli.vineyard;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;
import com.formichelli.vineyard.utilities.Util;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class PlacePickerActivity extends ActionBarActivity {
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

		selectPlace(VineyardServer.getRootPlace());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_place_picker, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_issues_up:
			selectedPlace = selectedPlace.getParent();
			placeAdapter.replaceItems(selectedPlace.getChildren());
			return true;
		case R.id.place_picker_action_done:
			Intent resultIntent = new Intent();
			resultIntent.putExtra("placeid", selectedPlace.getId());
			resultIntent.putExtra("placename", selectedPlace.getName());
			setResult(RESULT_OK, resultIntent);
			finish();
			this.setResult(RESULT_OK);
			finish();
			return true;
		case R.id.place_picker_action_cancel:
			this.setResult(RESULT_CANCELED);
			finish();
			return true;
		default:
			this.setResult(RESULT_CANCELED);
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

		Log.e("TAG", "children #: " + selectedPlace.getChildren().size());

		if (placeAdapter != null)
			placeAdapter.replaceItems(selectedPlace.getChildren());
		else {
			placeAdapter = new PlaceAdapter(this, R.layout.drawer_list_item,
					selectedPlace.getChildren());
			currentLevelPlacesListView.setAdapter(placeAdapter);
			currentLevelPlacesListView
					.setOnItemClickListener(onChildClickListener);
		}

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
