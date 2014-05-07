package com.formichelli.vineyard.utilities;

import java.net.URL;
import java.util.ArrayList;

import com.formichelli.vineyard.R;
import com.formichelli.vineyard.entities.IssueTask;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * ExpandableAdapter that shows issues titles and when expandend shows issue
 * details
 */
public class IssueExpandableAdapter extends BaseExpandableListAdapter {
	private final FragmentActivity context;
	ArrayList<IssueTask> objects;
	int groupResource, childResource;
	OnClickListener reportIssueOnClickListener, editOnClickListener,
			doneOnClickListener;

	/**
	 * 
	 * @param context
	 *            Activity context
	 * @param groupResource
	 *            resource representing the issue title, it must contain a
	 *            TextView with id drawer_list_item_label that will contain the
	 *            issue title
	 * @param childResource
	 *            resource representing the issue details, it must contain a
	 *            TextView with id issue_view_description that will contain the
	 *            issue description, a TextView with id
	 *            issue_view_priority_value that will contain the priority, a
	 *            TextView with id issue_view_worker_name that will contain the
	 *            name of the assigned worker, LinearLayout with id
	 *            issue_view_gallery that will contain the images, a View with
	 *            id issue_view_edit that will be associated to the
	 *            editOnClickListener and a View with id issue_view_delete that
	 *            will be associated to the deleteOnClickListener
	 * @param objects
	 *            issues to be added to the adapter
	 * @param reportIssueOnClickListener
	 *            onClickListener of the report issue button
	 * @param editOnClickListener
	 *            onClickListener of the edit issue button, the related issue
	 *            will be added as a tag to the associated view
	 * @param deleteOnClickListener
	 *            onClickListener of the done issue button, the related issue
	 *            will be added as a tag to the associated view
	 */
	public IssueExpandableAdapter(Activity context, int groupResource,
			int childResource, ArrayList<IssueTask> objects,
			OnClickListener reportIssueOnClickListener,
			OnClickListener editOnClickListener,
			OnClickListener doneOnClickListener) {

		this.context = (FragmentActivity) context;
		this.groupResource = groupResource;
		this.childResource = childResource;
		if (objects != null)
			this.objects = objects;
		else
			this.objects = new ArrayList<IssueTask>();
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
				.setText(context.getResources().getStringArray(
						R.array.issue_priorities)[object.getPriority().toInt()]);

		((TextView) childView
				.findViewById(R.id.issue_view_assigned_worker_name))
				.setText(getWorkerString(object.getAssignedWorkerId()));

		VineyardGallery gallery = (VineyardGallery) childView.findViewById(R.id.issue_view_gallery);
		ArrayList<URL> photos = object.getPhotos();
		if (photos.size() == 0)
			childView.removeView(gallery);
		else 
			for (URL p : photos)
				gallery.addImage(p.getPath());

		childView.findViewById(R.id.issue_view_edit).setOnClickListener(
				editOnClickListener);
		childView.findViewById(R.id.issue_view_done).setOnClickListener(
				doneOnClickListener);

		// add the issue as tag of the button
		childView.findViewById(R.id.issue_view_edit).setTag(object);
		childView.findViewById(R.id.issue_view_done).setTag(object);

		return childView;
	}

	private String getWorkerString(int workerId) {
		// TODO print id or get name from server?
		return workerId != 0 ? String.valueOf(workerId) : "--";
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
	public void replaceItems(ArrayList<IssueTask> objects) {
		if (objects != null)
			this.objects = objects;
		else
			this.objects = new ArrayList<IssueTask>();

		notifyDataSetChanged();
	}
}