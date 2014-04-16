package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.Random;

import com.formichelli.vineyard.entities.Place;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

// TODO use customadapter to bind views and places

public class PlacePickerActivity extends ActionBarActivity {
	ListView currentLevelPlacesListView;
	ArrayAdapter<Place> arrayAdapter;
	Place selectedPlace;
	ArrayList<Place> currentLevelPlaces;
	ViewGroup linearLayout;
	int currentLevel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_place_picker);

		linearLayout = (ViewGroup) findViewById(R.id.place_picker_linear_layout);
		currentLevelPlacesListView = (ListView) findViewById(R.id.place_picker_current_level_places);
		currentLevel = 0;

		selectedPlace = getChildrenPlaces(null).get(0);
		currentLevelPlaces = getChildrenPlaces(selectedPlace);

		arrayAdapter = new ArrayAdapter<Place>(this, R.layout.drawer_list_item,
				R.id.drawer_list_item, currentLevelPlaces);
		currentLevelPlacesListView.setAdapter(arrayAdapter);
		currentLevelPlacesListView
				.setOnItemClickListener(listViewOnItemClickListener);

		fixListHeight(currentLevelPlacesListView);
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
		case R.id.place_picker_action_done:
			Intent resultIntent = new Intent();
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

	private ArrayList<Place> getChildrenPlaces(Place currentPlace) {
		// TODO
		Random r = new Random();
		int N = 10;
		ArrayList<Place> ret = new ArrayList<Place>();

		if (currentPlace == null) {
			Place p = new Place();
			p.setName(String.valueOf(r.nextInt(1000)));
			ret.add(p);
			return ret;
		}

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
			String text = ((TextView) view).getText().toString();

			for (Place p : currentLevelPlaces)
				if (p.getName().compareTo(text) == 0)
					selectedPlace = p;
			currentLevelPlaces = getChildrenPlaces(selectedPlace);

			arrayAdapter.clear();
			arrayAdapter.addAll(currentLevelPlaces);

			TextView t = (TextView) getLayoutInflater().inflate(
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
