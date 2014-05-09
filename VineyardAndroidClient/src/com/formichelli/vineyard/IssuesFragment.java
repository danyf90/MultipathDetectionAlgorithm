package com.formichelli.vineyard;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.IssueExpandableAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
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

public class IssuesFragment extends Fragment {
	VineyardMainActivity activity;
	VineyardServer vineyardServer;
	ExpandableListView issuesList;
	IssueExpandableAdapter issueAdapter;
	MenuItem upItem;
	TextView noIssuesMessage;
	boolean first;
	AsyncHttpRequest asyncTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_issues, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		first = true;

		activity = (VineyardMainActivity) getActivity();
		vineyardServer = activity.getServer();
		issuesList = (ExpandableListView) activity
				.findViewById(R.id.issues_issues_list);

		noIssuesMessage = (TextView) activity
				.findViewById(R.id.issues_no_issues);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.issues, menu);

		upItem = menu.findItem(R.id.action_issues_up);

		if (first) {
			init();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void init() {
		if (activity.getCurrentPlace().getParent() != null)
			upItem.setVisible(true);
		else
			upItem.setVisible(false);

		List<IssueTask> issues = activity.getCurrentPlace().getIssues();
		issueAdapter = new IssueExpandableAdapter(activity,
				R.layout.issues_list_item, R.layout.issue_view, issues,
				reportIssueOnClickListener, editOnClickListener,
				doneOnClickListener);
		issuesList.setAdapter(issueAdapter);

		if (issues.size() != 0)
			noIssuesMessage.setVisibility(View.INVISIBLE);
		else
			noIssuesMessage.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_issues_up:
			activity.setCurrentPlace(activity.getCurrentPlace().getParent());
			init();
			return true;
		default:
			return false;
		}
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
			ReportIssueFragment r = new ReportIssueFragment();
			IssueTask issue = (IssueTask) v.getTag();
			issue.setPlace(activity.getCurrentPlace());
			r.setIssue(issue);
			activity.switchFragment(r);
		}
	};

	OnClickListener doneOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new AsyncMarkIssueAsDone(activity.getServer().getUrl(),
					(IssueTask) v.getTag()).execute();
		}
	};

	private class AsyncMarkIssueAsDone extends AsyncHttpRequest {
		IssueTask issue;

		public AsyncMarkIssueAsDone(String serverUrl, IssueTask issue) {
			super(serverUrl + VineyardServer.EDIT_ISSUE_API + issue.getId(),
					AsyncHttpRequest.Type.PUT);

			this.issue = issue;

			addParam(new BasicNameValuePair(SimpleTask.MODIFIER,
					String.valueOf(activity.getUserId())));
			addParam(new BasicNameValuePair(SimpleTask.STATUS,
					Task.Status.RESOLVED.toString()));
		}

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.getLoadingFragment());
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			if (response != null && response.first == HttpStatus.SC_OK) {
				issue.getPlace().removeIssue(issue);
				activity.switchFragment(activity.getIssuesFragment());
			} else {
				Toast.makeText(activity,
						activity.getString(R.string.issue_mark_done_error),
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "SC: " + response.first);

				activity.switchFragment(activity.getIssuesFragment());
			}
		}
	};
}
