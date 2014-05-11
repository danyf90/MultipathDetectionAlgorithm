package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.IssueExpandableAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
	IssueExpandableAdapter issueAdapter;
	TextView noIssuesMessage;
	boolean first;
	AsyncHttpRequest asyncTask;
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

		activity = (VineyardMainActivity) getActivity();
		vineyardServer = activity.getServer();
		issuesList = (ExpandableListView) activity
				.findViewById(R.id.issues_issues_list);

		noIssuesMessage = (TextView) activity
				.findViewById(R.id.issues_no_issues);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.issues, menu);

		if (first) {
			// init() must be called just once after that both onActivityCreated
			// and onCreateOptionMenu are called
			init();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void init() {
		List<IssueTask> issues;
		
		if (selectedPlace == null) {
			Place p = activity.getRootPlace();
			issues = getAllIssues(p);
		} else
			issues = selectedPlace.getIssues();
		
		issueAdapter = new IssueExpandableAdapter(activity,
				R.layout.issues_list_item, R.layout.issue_view, issues,
				reportIssueOnClickListener, editOnClickListener,
				doneOnClickListener);
		issuesList.setAdapter(issueAdapter);

		// show a message if there are no children
		if (issues.size() != 0)
			noIssuesMessage.setVisibility(View.GONE);
		else
			noIssuesMessage.setVisibility(View.VISIBLE);
	}

	private List<IssueTask> getAllIssues(Place place) {

		List<IssueTask> issues = new ArrayList<IssueTask>();
		
		issues.addAll(place.getIssues());
		
		for (Place p: place.getChildren())
			issues.addAll(getAllIssues(p));

		return issues;
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

	/*
	 * Sends a PUT request to the server to mark an issue as solved. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution the issue will be removed from the list. If something goes
	 * wrong the issue will not be removed ad a toast will be displayed
	 */
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
			if (response != null && response.first == HttpStatus.SC_OK)
				issue.getPlace().removeIssue(issue);
			else
				Toast.makeText(activity,
						activity.getString(R.string.issue_mark_done_error),
						Toast.LENGTH_SHORT).show();

			activity.switchFragment();
		}
	};
}
