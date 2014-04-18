package com.formichelli.vineyard.utilities;

import java.util.ArrayList;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.entities.IssueTask;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class IssueExpandableAdapter extends BaseExpandableListAdapter {
	private final Context context;
	ArrayList<IssueTask> objects;
	int groupResource, childResource;

	public IssueExpandableAdapter(Activity context, int groupResource,
			int childResource, ArrayList<IssueTask> objects) {

		this.context = context;
		this.groupResource = groupResource;
		this.childResource = childResource;
		this.objects = objects;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView t;
		IssueTask object = objects.get(groupPosition);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(groupResource, parent, false);

		t = (TextView) item.findViewById(R.id.drawer_list_item_label);
		if (t == null)
			Log.e("ASD","IS NULL!");
		t.setText(object.getTitle());
		t.setTag(object);

		return item;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		IssueTask object = objects.get(groupPosition);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View childView = inflater.inflate(childResource, parent, false);

		((TextView) childView.findViewById(R.id.issue_view_description))
				.setText(object.getDescription());
		((TextView) childView.findViewById(R.id.issue_view_priority_label))
				.setText(object.getPriority().toString());
		((TextView) childView
				.findViewById(R.id.issue_view_assigned_worker_label))
				.setText(object.getAssignedWorker().getName());

		// TODO add photos

		return childView;
	}

	@Override
	public int getGroupCount() {
		return objects.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return objects.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (childPosition > 0)
			return null;

		return objects.get(groupPosition);
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
}