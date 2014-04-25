package com.formichelli.vineyard.utilities;

import java.net.URL;
import java.util.ArrayList;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.VineyardMainActivity;
import com.formichelli.vineyard.entities.IssueTask;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IssueExpandableAdapter extends BaseExpandableListAdapter {
	private final FragmentActivity context;
	ArrayList<IssueTask> objects;
	int groupResource, childResource;
	OnClickListener reportIssueOnClickListener, editOnClickListener,
			deleteOnClickListener;

	public IssueExpandableAdapter(Activity context, int groupResource,
			int childResource, ArrayList<IssueTask> objects,
			OnClickListener reportIssueOnClickListener,
			OnClickListener editOnClickListener,
			OnClickListener deleteOnClickListener) {

		this.context = (FragmentActivity) context;
		this.groupResource = groupResource;
		this.childResource = childResource;
		this.objects = objects;
		this.reportIssueOnClickListener = reportIssueOnClickListener;
		this.editOnClickListener = editOnClickListener;
		this.deleteOnClickListener = deleteOnClickListener;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView t;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(groupResource, parent, false);
		t = (TextView) item.findViewById(R.id.drawer_list_item_label);

		if (groupPosition == 0) {
			// First item is report issue
			t.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_add_dark, 0, 0, 0);
			t.setText(context.getString(R.string.action_report_issue));
			t.setOnClickListener(reportIssueOnClickListener);

		} else {
			IssueTask object = objects.get(groupPosition - 1);

			t.setText(object.getTitle());
			t.setTag(object);

			if (isExpanded)
				t.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.action_up_dark, 0, 0, 0);
			else
				t.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.action_expand_dark, 0, 0, 0);
		}

		return item;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		IssueTask object = objects.get(groupPosition - 1);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup childView = (ViewGroup) inflater.inflate(childResource,
				parent, false);

		((TextView) childView.findViewById(R.id.issue_view_description))
				.setText(object.getDescription());
		((TextView) childView.findViewById(R.id.issue_view_priority_value))
				.setText(context.getString(object.getPriority().getStringId()));
		((TextView) childView
				.findViewById(R.id.issue_view_assigned_worker_name))
				.setText(object.getAssignedWorker().getName());

		ArrayList<URL> photos = object.getPhotos();
		if (photos == null || photos.size() == 0) {
			childView.removeView(childView
					.findViewById(R.id.issue_view_gallery_container));
		} else {
			LinearLayout galleryLayout = (LinearLayout) childView
					.findViewById(R.id.issue_view_gallery);
			MenuItem deleteItem = ((VineyardMainActivity) context).getMenu()
					.findItem(R.id.action_issues_up);

			Gallery gallery = new Gallery(context, deleteItem, galleryLayout,
					false);

			for (URL p : photos) {
				gallery.addImage(p.getPath());
			}
		}

		childView.findViewById(R.id.issue_view_edit).setOnClickListener(
				editOnClickListener);
		childView.findViewById(R.id.issue_view_delete).setOnClickListener(
				deleteOnClickListener);

		// add the issue as tag of the button
		childView.findViewById(R.id.issue_view_edit).setTag(object);
		childView.findViewById(R.id.issue_view_delete).setTag(object);

		return childView;
	}

	@Override
	public int getGroupCount() {
		return objects.size() + 1;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition == 0)
			return 0;

		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if (groupPosition == 0)
			return null;

		return objects.get(groupPosition - 1);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (childPosition > 0)
			return null;

		return objects.get(groupPosition - 1);
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

	public void replaceItems(ArrayList<IssueTask> objects) {
		this.objects.clear();
		this.objects.addAll(objects);
		notifyDataSetChanged();
	}
}