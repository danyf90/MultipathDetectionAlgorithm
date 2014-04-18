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

public class PlaceAdapter extends ArrayAdapter<Place> {
	private final Context context;
	ArrayList<Place> objects;
	int resource;

	public PlaceAdapter(Context context, int resource, ArrayList<Place> objects) {
		super(context, resource, objects);

		this.context = context;
		this.resource = resource;
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView t;
		Place object = objects.get(position);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View item = inflater.inflate(resource, parent, false);

		t = (TextView) item.findViewById(R.id.drawer_list_item_label);
		t.setText(object.getName());
		t.setTag(object);

		return item;
	}
	

	public void replaceItems(ArrayList<Place> objects) {
		this.objects.clear();
		this.objects.addAll(objects);
		notifyDataSetChanged();
	}
}
