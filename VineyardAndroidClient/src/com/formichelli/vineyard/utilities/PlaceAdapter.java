package com.formichelli.vineyard.utilities;

import java.util.ArrayList;
import java.util.List;

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
	List<Place> objects;
	int resource;

	/**
	 * Create a PlaceAdapter
	 * 
	 * @param context
	 *            Activity context
	 * @param resource
	 *            item layout. It must contain a TextView with id
	 *            place_list_item_label that will contain the Place name, a
	 *            TextView with id place_list_item_issues that will contain the
	 *            count of the issues of the place, a TextView with id
	 *            place_list_item_tasks that will contain the count of the tasks
	 *            of the place
	 * @param objects
	 *            places to be added to the ListView
	 */
	public PlaceAdapter(Context context, int resource, List<Place> objects) {
		super(context, resource, objects);

		this.context = context;

		this.resource = resource;

		if (objects != null)
			this.objects = objects;
		else
			this.objects = new ArrayList<Place>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Place p = objects.get(position);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View item = inflater.inflate(resource, parent, false);

		item.setTag(p);

		// set name
		((TextView) item.findViewById(R.id.place_list_item_label)).setText(p
				.getName());

		// set issues count
		int childrenCount = p.getChildrenIssuesCount();
		if (childrenCount == 0)
			((TextView) item.findViewById(R.id.place_list_item_issues))
					.setVisibility(View.GONE);
		else
			((TextView) item.findViewById(R.id.place_list_item_issues))
					.setText(String.valueOf(childrenCount));

		// set tasks count
		childrenCount = p.getChildrenTasksCount();
		if (childrenCount == 0)
			((TextView) item.findViewById(R.id.place_list_item_tasks))
					.setWidth(0);
		else
			((TextView) item.findViewById(R.id.place_list_item_tasks))
					.setText(String.valueOf(childrenCount));

		return item;
	}

	@Override
	public int getCount() {
		return objects.size();
	}

	/**
	 * Replace all items of the ListView
	 * 
	 * @param objects
	 *            new objects of the ListView
	 */
	public void replaceItems(List<Place> objects) {
		if (objects != null)
			this.objects = objects;
		else
			this.objects.clear();

		notifyDataSetChanged();
	}
}
