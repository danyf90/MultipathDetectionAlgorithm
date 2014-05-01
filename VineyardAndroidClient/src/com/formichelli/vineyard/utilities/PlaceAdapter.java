package com.formichelli.vineyard.utilities;

import java.util.ArrayList;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.entities.Place;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter that shows a list of Places
 */
public class PlaceAdapter extends ArrayAdapter<Place> {
	private final Context context;
	ArrayList<Place> objects;
	int resource;

	/**
	 * Create a PlaceAdapter
	 * 
	 * @param context
	 *            Activity context
	 * @param resource
	 *            item layout, it must contain a TextView with id
	 *            drawer_list_item_label that will contain the Place name
	 * @param objects
	 *            places to be added to the ListView
	 */
	public PlaceAdapter(Context context, int resource, ArrayList<Place> objects) {
		super(context, resource, objects);

		this.context = context;
		this.resource = resource;
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView t;
		Place p = objects.get(position);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View item = inflater.inflate(resource, parent, false);

		item.findViewById(R.id.place_list_item).setTag(p);
		
		
		t = (TextView) item.findViewById(R.id.place_list_item_label);
		t.setText(p.getName());		

		t = (TextView) item.findViewById(R.id.place_list_item_stats);

		t.setText("(" + p.getIssuesCount() + ", " + p.getTasksCount() + ")");

		return item;
	}

	/**
	 * Replace all items of the ListView
	 * @param objects new objects of the ListView
	 */
	public void replaceItems(ArrayList<Place> objects) {
		this.objects.clear();
		this.objects.addAll(objects);
		notifyDataSetChanged();
	}
}
