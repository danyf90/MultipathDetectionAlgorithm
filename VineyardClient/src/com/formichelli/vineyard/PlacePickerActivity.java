package com.formichelli.vineyard;

import java.util.ArrayList;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.PlaceAdapter;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PlacePickerActivity extends ActionBarActivity {
	ListView currentLevelPlacesListView;
	PlaceAdapter placeAdapter;
	Place selectedPlace;
	ViewGroup linearLayout;
	int currentLevel = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_place_picker);

		linearLayout = (ViewGroup) findViewById(R.id.place_picker_linear_layout);
		currentLevelPlacesListView = (ListView) findViewById(R.id.place_picker_current_level_places);

		selectPlace(getRootPlace());
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

	private Place getRootPlace() {
		// TODO get place hierarchy from server

		// generate fake place hierarchy
		Place root = new Place();
		root.setName("root");
		ArrayList<Place> children = root.getChildren();
		int N = 10;

		for (int i = 0; i < N; i++) {
			Place p = new Place();
			p.setName("p" + i);
			ArrayList<Place> childChildren = p.getChildren();

			for (int j = 0; j < i; j++) {
				Place cp = new Place();
				cp.setName(p.getName() + j);
				childChildren.add(cp);
			}
			p.setChildren(childChildren);

			children.add(p);
		}

		root.setChildren(children);

		return root;
	}

	// Set the right height for the ListView since it collapse if it is placed
	// inside a ScrollView
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

	private void selectPlace(Place p) {
		TextView t;

		if (p == null || selectedPlace == p)
			return;

		// check if the place is in the ancestors list
		for (int i = 0, l = linearLayout.getChildCount(); i < l; i++)
			if (linearLayout.getChildAt(i).getTag() == p) {
				removeItemsAfter(linearLayout, linearLayout.getChildAt(i));
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

		fixListHeight(currentLevelPlacesListView);

		t = (TextView) getLayoutInflater()
				.inflate(R.layout.ancestors_list_item,
						currentLevelPlacesListView, false);
		t.setText(getStringForLevel(p.getName(), currentLevel));
		t.setTag(selectedPlace);
		t.setOnClickListener(PlacePickerActivity.this.onAncestorClickListener);

		linearLayout.addView(t, currentLevel);
		currentLevel++;
	}

	private void removeItemsAfter(ViewGroup linearLayout, View v) {
		// -1 because the last item is the listview
		int i, childCount = linearLayout.getChildCount() - 1;

		// find TextView v and delete all its successors
		for (i = 0; i < childCount; i++)
			if (linearLayout.getChildAt(i) == v) {
				currentLevel = i;
				break;
			}
		for (int j = i; i < childCount; i++)
			linearLayout.removeViewAt(j);
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
