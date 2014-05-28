package com.formichelli.vineyard.utilities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.VineyardMainActivity;
import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.Task.Priority;
import com.formichelli.vineyard.entities.Task.Status;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
	int offset;
	boolean showPlace;
	int groupResource, childResource;
	OnClickListener reportIssueOnClickListener, editOnClickListener,
			doneOnClickListener;

	ViewGroup attributesLabels, attributesValues;
	String placeLabel, priorityLabel, statusLabel, assignedWorkerLabel,
			assignedGroupLabel, dueTimeLabel;
	String[] priorities, status;

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
	 * @param showAdd
	 *            indicates wether an "Add" button should be shown or not
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
			int childResource, List<T> objects, boolean showAdd,
			boolean showPlace, OnClickListener reportIssueOnClickListener,
			OnClickListener editOnClickListener,
			OnClickListener doneOnClickListener) {

		this.context = context;
		this.groupResource = groupResource;
		this.childResource = childResource;
		if (objects != null)
			this.objects = objects;
		else
			this.objects = new ArrayList<T>();
		this.offset = showAdd ? 1 : 0;
		this.showPlace = showPlace;

		this.reportIssueOnClickListener = reportIssueOnClickListener;
		this.editOnClickListener = editOnClickListener;
		this.doneOnClickListener = doneOnClickListener;

		placeLabel = context.getString(R.string.issue_place_label);
		priorityLabel = context.getString(R.string.issue_priority_label);
		statusLabel = context.getString(R.string.issue_status_label);
		priorities = context.getResources().getStringArray(
				R.array.task_priorities);
		status = context.getResources().getStringArray(R.array.task_status);
		assignedWorkerLabel = context
				.getString(R.string.issue_assigned_worker_label);
		assignedGroupLabel = context
				.getString(R.string.issue_assigned_group_label);
		dueTimeLabel = context.getString(R.string.issue_due_time_label);
	}

	public boolean isShowPlace() {
		return showPlace;
	}

	public void setShowPlace(boolean showPlace) {
		this.showPlace = showPlace;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView t;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(groupResource, parent, false);
		t = (TextView) item.findViewById(R.id.issue_list_item_label);

		if (groupPosition < offset) {
			// First item is report issue
			t.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_add_dark, 0, 0, 0);
			t.setText(context.getString(R.string.action_report_issue));
			item.setOnClickListener(reportIssueOnClickListener);

		} else {
			// Other items are real issues
			Task object = objects.get(groupPosition - offset);

			t.setText(object.getTitle());
			t.setTag(object);

			if (isExpanded)
				t.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.action_up_dark, 0, 0, 0);
			else {
				t.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.action_expand_dark, 0, 0, 0);

				if (showPlace)
					((TextView) item.findViewById(R.id.issue_list_item_place))
							.setText(object.getPlace().getName());
			}
		}

		return item;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Task object = objects.get(groupPosition - offset);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup childView = (ViewGroup) inflater.inflate(childResource,
				parent, false);
		attributesLabels = (ViewGroup) childView
				.findViewById(R.id.issue_view_attributes_labels);
		attributesValues = (ViewGroup) childView
				.findViewById(R.id.issue_view_attributes_values);

		// set description
		((TextView) childView.findViewById(R.id.issue_view_description))
				.setText(object.getDescription());

		// set place
		if (object.getPlace() != null)
			addAttribute(placeLabel, object.getPlace().getName());

		// set priority
		if (object.getPriority() != null)
			addAttribute(priorityLabel,
					priorities[Priority.getIndex(object.getPriority())]);

		// set status
		if (object.getStatus() != null)
			addAttribute(statusLabel,
					status[Status.getIndex(object.getStatus())]);

		// set assignedWorker and assignedGroup
		if (object.getAssignedWorker() != null)
			addAttribute(assignedWorkerLabel,
					String.valueOf(object.getAssignedWorker().getName()));

		if (object.getAssignedGroup() != null)
			addAttribute(assignedGroupLabel,
					String.valueOf(object.getAssignedGroup().getName()));

		// set dueTime
		if (object.getDueTime() != null)
			addAttribute(
					dueTimeLabel,
					DateFormat.getDateInstance(DateFormat.SHORT,
							Locale.getDefault()).format(object.getDueTime()));

		// show map
		TextView locationLabel = (TextView) childView
				.findViewById(R.id.issue_view_attributes_location_label);
		TextView locationShow = (TextView) childView
				.findViewById(R.id.issue_view_attributes_location_show);
		if (object.getLatitude() != null && object.getLongitude() != null) {
			locationShow.setTag(object);
			setLatitudeLink(locationShow);
		} else {
			locationLabel.setVisibility(View.GONE);
			locationShow.setVisibility(View.GONE);
		}

		// set photos if the object is an issue
		VineyardGallery gallery = (VineyardGallery) childView
				.findViewById(R.id.issue_view_gallery);

		if (object instanceof IssueTask) {
			List<String> photos = ((IssueTask) object).getPhotos();
			if (photos.size() == 0)
				childView.removeView(gallery);
			else {
				VineyardServer vineyardServer = ((VineyardMainActivity) context)
						.getServer();
				String photoApi = vineyardServer.getUrl()
						+ VineyardServer.PHOTO_API;
				for (String photo : photos)
					gallery.addImageFromServer(photoApi, photo);
			}

			childView.findViewById(R.id.issue_view_edit).setTag(object);
			childView.findViewById(R.id.issue_view_edit).setOnClickListener(
					editOnClickListener);
		} else {
			childView.removeView(gallery);
			childView.findViewById(R.id.issue_view_edit).setVisibility(
					View.INVISIBLE);
		}

		childView.findViewById(R.id.issue_view_done).setTag(object);
		childView.findViewById(R.id.issue_view_done).setOnClickListener(
				doneOnClickListener);

		return childView;
	}

	private void setLatitudeLink(TextView locationShow) {
		locationShow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final double latitude = ((Task) v.getTag()).getLatitude();
				final double longitude = ((Task) v.getTag()).getLongitude();
				final String label = ((Task) v.getTag()).getTitle();
				final String uri = "geo:" + latitude + "," + longitude + "?q="
						+ latitude + "," + longitude + "(" + label + ")";

				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(uri)));
			}
		});
	}

	@Override
	public int getGroupCount() {
		return objects.size() + offset;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition < offset)
			return 0;

		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if (groupPosition < offset)
			return null;

		return objects.get(groupPosition - offset);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (groupPosition < offset || childPosition > 0)
			return null;

		return objects.get(groupPosition - offset);
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

	private void addAttribute(String key, String value) {
		TextView t;

		// add the key
		t = new TextView(context);
		t.setTypeface(null, Typeface.BOLD_ITALIC);
		t.setText(key + ":");
		attributesLabels.addView(t);

		// add the value
		t = new TextView(context);
		t.setText(value);
		attributesValues.addView(t);

	}
}