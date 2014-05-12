package com.formichelli.vineyard.utilities;

import java.util.ArrayList;
import java.util.List;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.Task.Priority;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * ExpandableAdapter that shows issues titles and when expanded shows issue
 * details
 */
public class TaskExpandableAdapter<T extends Task> extends
		BaseExpandableListAdapter {
	private final Context context;
	List<T> objects;
	boolean showPlace;
	int groupResource, childResource;
	OnClickListener reportIssueOnClickListener, editOnClickListener,
			doneOnClickListener;

	/**
	 * 
	 * @param context
	 *            Activity context
	 * @param groupResource
	 *            resource representing the issue title. It must contain a
	 *            TextView with id issue_list_item_label that will contain the
	 *            issue title and a TextView with id issue_list_item_place that
	 *            will contain the name of the place related to the issue
	 * @param childResource
	 *            resource representing the issue details. It must contain a
	 *            TextView with id issue_view_description that will contain the
	 *            issue description, a TextView with id
	 *            issue_view_priority_value that will contain the priority, a
	 *            TextView with id issue_view_assigned_worker_name that will
	 *            contain the name of the assigned worker, a VineyardGallery
	 *            with id issue_view_gallery that will contain the images, a
	 *            View with id issue_view_edit that will be associated to the
	 *            editOnClickListener and a View with id issue_view_delete that
	 *            will be associated to the deleteOnClickListener
	 * @param objects
	 *            issues to be added to the adapter
	 * @param showPlace
	 *            indicates wether the place name should be shown or not
	 * @param reportIssueOnClickListener
	 *            onClickListener of the report issue button
	 * @param editOnClickListener
	 *            onClickListener of the edit issue button, the related issue
	 *            will be added as a tag to the associated view
	 * @param deleteOnClickListener
	 *            onClickListener of the done issue button, the related issue
	 *            will be added as a tag to the associated view
	 */
	public TaskExpandableAdapter(Activity context, int groupResource,
			int childResource, List<T> objects, boolean showPlace,
			OnClickListener reportIssueOnClickListener,
			OnClickListener editOnClickListener,
			OnClickListener doneOnClickListener) {

		this.context = context;
		this.groupResource = groupResource;
		this.childResource = childResource;
		if (objects != null)
			this.objects = objects;
		else
			this.objects = new ArrayList<T>();
		this.showPlace = showPlace;

		this.reportIssueOnClickListener = reportIssueOnClickListener;
		this.editOnClickListener = editOnClickListener;
		this.doneOnClickListener = doneOnClickListener;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView t;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(groupResource, parent, false);
		t = (TextView) item.findViewById(R.id.issue_list_item_label);

		if (groupPosition == 0) {
			// First item is report issue
			t.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_add_dark, 0, 0, 0);
			t.setText(context.getString(R.string.action_report_issue));
			t.setOnClickListener(reportIssueOnClickListener);

		} else {
			// Other items are real issues
			Task object = objects.get(groupPosition - 1);

			t.setText(object.getTitle());
			t.setTag(object);

			if (isExpanded)
				t.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.action_up_dark, 0, 0, 0);
			else
				t.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.action_expand_dark, 0, 0, 0);

			if (showPlace && !isExpanded)
				((TextView) item.findViewById(R.id.issue_list_item_place))
						.setText(object.getPlace().getName());
		}

		return item;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Task object = objects.get(groupPosition - 1);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup childView = (ViewGroup) inflater.inflate(childResource,
				parent, false);

		((TextView) childView.findViewById(R.id.issue_view_description))
				.setText(object.getDescription());

		((TextView) childView.findViewById(R.id.issue_view_priority_value))
				.setText(context.getResources().getStringArray(
						R.array.issue_priorities)[Priority.getIndex(object
						.getPriority())]);

		if (object.getAssignedWorker() != null)
			((TextView) childView
					.findViewById(R.id.issue_view_assigned_worker_name))
					.setText(String.valueOf(object.getAssignedWorker().getId()));
		else
			((TextView) childView
					.findViewById(R.id.issue_view_assigned_worker_name))
					.setText("--");

		if (object instanceof IssueTask) {
			// VineyardGallery gallery = (VineyardGallery)
			// childView.findViewById(R.id.issue_view_gallery);
			// List<URL> photos = object.getPhotos();
			// if (photos.size() == 0)
			// childView.removeView(gallery);
			// else
			// for (URL p : photos)
			// gallery.addImage(p.getPath());
			childView.findViewById(R.id.issue_view_edit).setTag(object);
			childView.findViewById(R.id.issue_view_edit).setOnClickListener(
					editOnClickListener);
		}
		else
			childView.findViewById(R.id.issue_view_edit).setVisibility(View.INVISIBLE);


		childView.findViewById(R.id.issue_view_done).setTag(object);
		childView.findViewById(R.id.issue_view_done).setOnClickListener(
				doneOnClickListener);

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

	/**
	 * Replace all items of the ExpandableListView
	 * 
	 * @param objects
	 *            new objects of the ExpandableListView
	 */
	public void replaceItems(List<T> objects) {
		if (objects != null)
			this.objects = objects;
		else
			this.objects.clear();

		notifyDataSetChanged();
	}
}