package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.utilities.AsyncHttpGetRequests;
import com.formichelli.vineyard.utilities.AsyncHttpPutRequest;
import com.formichelli.vineyard.utilities.IssueExpandableAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
	AsyncHttpGetRequests asyncTask;

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

		if (activity.getCurrentPlace().getIssues() == null)
			sendPlaceIssuesAndTasksRequest();
		else {
			List<IssueTask> i = activity.getCurrentPlace().getIssues();
			issueAdapter = new IssueExpandableAdapter(activity,
					R.layout.issues_list_item, R.layout.issue_view, i,
					reportIssueOnClickListener, editOnClickListener,
					doneOnClickListener);
			issuesList.setAdapter(issueAdapter);

			if (i.size() != 0)
				noIssuesMessage.setVisibility(View.INVISIBLE);
			else
				noIssuesMessage.setVisibility(View.VISIBLE);

		}

	}

	public void sendPlaceIssuesAndTasksRequest() {
		final String placeIssuesAndTasksRequest = String.format(
				vineyardServer.getUrl() + VineyardServer.PLACE_ISSUES_API,
				activity.getCurrentPlace().getId());

		asyncTask = new PlaceIssuesAsyncHttpRequest();
		asyncTask.execute(placeIssuesAndTasksRequest);
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
			new AsyncMarkIssueAsDone(activity.getServer().getUrl()
					+ VineyardServer.ADD_ISSUE_API, (IssueTask) v.getTag())
					.execute();
		}
	};

	private class PlaceIssuesAsyncHttpRequest extends AsyncHttpGetRequests {
		private static final String TAG = "PlaceIssuesAsyncHttpRequest";

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.getLoadingFragment());
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			String issuesJSON;

			try {
				if (result != null && result.size() == 1
						&& result.get(0) != null) {
					// request OK, parse JSON to get issues, cache data and show
					// retrieved issues
					issuesJSON = result.get(0);

					activity.getCache().setPlaceIssuesJSON(
							activity.getCurrentPlace().getId(), issuesJSON);

				} else {
					issuesJSON = activity.getCache().getPlaceIssuesJSON(
							activity.getCurrentPlace().getId());

					if (issuesJSON == null) {
						Log.w(TAG, "issues not available in sharedPreference");
						activity.getLoadingFragment().setLoading(false);
						return;
					}

					Toast.makeText(activity,
							activity.getString(R.string.cache_data_used),
							Toast.LENGTH_SHORT).show();
				}

				activity.getCurrentPlace().setIssues(issuesJSON);
				activity.setTitle(activity.getCurrentPlace().getName());
				activity.switchFragment();

			} catch (JSONException e) {
				android.util.Log.e(TAG, e.getLocalizedMessage());
				activity.getLoadingFragment().setLoading(false);
			}
		}
	}

	private class AsyncMarkIssueAsDone extends AsyncHttpPutRequest {
		IssueTask issue;

		public AsyncMarkIssueAsDone(String serverUrl, IssueTask issue) {
			super();

			this.issue = issue;

			this.setServerUrl(serverUrl);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(SimpleTask.ID, String
					.valueOf(issue.getId())));
			params.add(new BasicNameValuePair(SimpleTask.MODIFIER, String
					.valueOf(activity.getUserId())));
			params.add(new BasicNameValuePair(SimpleTask.STATUS,
					Task.Status.DONE.toString()));

			this.setParams(params);
		}

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.getLoadingFragment());
		}

		@Override
		protected void onPostExecute(Integer response) {
			if (response == HttpStatus.SC_ACCEPTED) {
				// TODO remove issue;
				issue.getPlace().removeIssue(issue);
				activity.switchFragment(activity.getIssuesFragment());

			} else {
				Toast.makeText(activity,
						activity.getString(R.string.issue_mark_done_error),
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "SC: " + response);

				activity.switchFragment(activity.getIssuesFragment());
			}
		}
	};
}
