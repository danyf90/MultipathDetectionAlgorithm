package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.WorkGroup;
import com.formichelli.vineyard.entities.Worker;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.TaskExpandableAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment which contains the issues of a given place, it allows to add, edit
 * or mark as solved an issue
 */
public class IssuesFragment extends Fragment {
	VineyardMainActivity activity;
	VineyardServer vineyardServer;
	ExpandableListView issuesList;
	TaskExpandableAdapter<IssueTask> issueAdapter;
	TextView noIssuesMessage;
	boolean first, showMine;
	AsyncHttpRequest asyncTask;
	MenuItem showMode;
	String showAllLabel, showMineLabel;
	IssueTask selectedIssue;
	Place selectedPlace;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_issues, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		first = true;
		showMine = false;

		activity = (VineyardMainActivity) getActivity();
		vineyardServer = activity.getServer();
		issuesList = (ExpandableListView) activity
				.findViewById(R.id.issues_issues_list);

		noIssuesMessage = (TextView) activity
				.findViewById(R.id.issues_no_issues);

		showAllLabel = getString(R.string.issue_view_all);
		showMineLabel = getString(R.string.issue_view_mine);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.issues, menu);

		showMode = menu.findItem(R.id.action_issue_view_mode);

		if (first) {
			// loadData() must be called just once after that both
			// onActivityCreated
			// and onCreateOptionMenu are called
			loadData();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_issue_view_mode) {

			for (int i = 0, l = issueAdapter.getGroupCount(); i < l; i++)
				issuesList.collapseGroup(i);
			showMine = !showMine;
			showMode.setTitle(showMine ? showAllLabel : showMineLabel);
			loadData();
			return true;
		}

		return false;
	}

	public void loadData() {
		List<IssueTask> issues;

		final boolean showAllIssues = selectedPlace == null;

		if (showAllIssues) {
			SparseArray<IssueTask> allIssues = activity.getIssues();

			issues = new ArrayList<IssueTask>();
			for (int i = 0, l = allIssues.size(); i < l; i++)
				issues.add(allIssues.valueAt(i));
		} else {
			activity.setTitle(String.format(
					getString(R.string.title_issue_fragment),
					selectedPlace.getName()));
			issues = selectedPlace.getIssues();
		}

		if (showMine) {
			// show only issues of current user
			int userId = activity.getUserId();
			ArrayList<IssueTask> myIssues = new ArrayList<IssueTask>();

			// show the issue only if it is assigned to the current user or
			// to one of his groups
			for (IssueTask issue : issues) {
				Worker assignedWorker = issue.getAssignedWorker();
				if (assignedWorker != null && assignedWorker.getId() == userId)
					myIssues.add(issue);
				else {
					WorkGroup group = issue.getAssignedGroup();
					if (group != null) {
						for (Worker worker : group.getWorkers())
							if (worker.getId() == userId) {
								myIssues.add(issue);
								break;
							}
					}
				}
			}
			issues = myIssues;
		}

		issueAdapter = new TaskExpandableAdapter<IssueTask>(activity,
				R.layout.issues_list_item, R.layout.issue_view, issues, true,
				showAllIssues, reportIssueOnClickListener, editOnClickListener,
				doneOnClickListener);
		issuesList.setAdapter(issueAdapter);

		if (selectedIssue != null) {
			// used in case of notification
			issuesList.expandGroup(issues.indexOf(selectedIssue) + 1);
			selectedIssue = null;
		}

		// show a message if there are no children
		if (issues.size() != 0)
			noIssuesMessage.setVisibility(View.GONE);
		else
			noIssuesMessage.setVisibility(View.VISIBLE);
	}

	OnClickListener reportIssueOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			activity.switchFragment(new ReportIssueFragment());
		}
	};

	OnClickListener editOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			activity.switchFragment(new ReportIssueFragment((IssueTask) v
					.getTag()));
		}
	};

	OnClickListener doneOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new AsyncMarkIssueAsDone(activity.getServer().getUrl(),
					(IssueTask) v.getTag()).execute();
		}
	};

	public Place getSelectedPlace() {
		return selectedPlace;
	}

	public void setSelectedPlace(Place selectedPlace) {
		this.selectedPlace = selectedPlace;
	}

	public IssueTask getSelectedIssue() {
		return selectedIssue;
	}

	public void setSelectedIssue(IssueTask selectedIssue) {
		this.selectedIssue = selectedIssue;
	}

	/*
	 * Sends a PUT request to the server to mark an issue as solved. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution the issue will be removed from the list. If something goes
	 * wrong the issue will not be removed ad a toast will be displayed
	 */
	private class AsyncMarkIssueAsDone extends AsyncHttpRequest {
		private final static String TAG = "AsyncMarkIssueAsDone";
		IssueTask issue;

		public AsyncMarkIssueAsDone(String serverUrl, IssueTask issue) {
			super(serverUrl + VineyardServer.ISSUES_API + issue.getId(),
					AsyncHttpRequest.Type.PUT);

			this.issue = issue;

			addParam(new BasicNameValuePair(SimpleTask.MODIFIER,
					String.valueOf(activity.getUserId())));
			addParam(new BasicNameValuePair(SimpleTask.STATUS,
					Task.Status.RESOLVED.toString()));
		}

		@Override
		protected void onPreExecute() {
			activity.getLoadingFragment().setLoadingMessage(
					getString(R.string.loading_sending_request));
			activity.switchFragment(activity.getLoadingFragment());
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			if (response != null && response.first == HttpStatus.SC_ACCEPTED) {
				activity.getIssues().remove(issue.getId());
				issue.getPlace().removeIssue(issue);
			} else {
				Log.e(TAG, response.first + ": " + response.second);
				Toast.makeText(activity,
						activity.getString(R.string.issue_mark_done_error),
						Toast.LENGTH_SHORT).show();
			}

			activity.switchFragment();
		}
	}
}
