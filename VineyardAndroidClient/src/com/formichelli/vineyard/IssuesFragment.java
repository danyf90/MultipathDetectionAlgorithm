package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONException;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.utilities.AsyncHttpRequests;
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

		activity = (VineyardMainActivity) getActivity();
		vineyardServer = activity.getServer();
		issuesList = (ExpandableListView) activity
				.findViewById(R.id.issues_issues_list);

		noIssuesMessage = (TextView) activity
				.findViewById(R.id.issues_no_issues);
		
		if (upItem != null)
			init();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.issues, menu);

		upItem = menu.findItem(R.id.action_issues_up);

		if (activity != null)
			init();

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void init() {
		if (issueAdapter != null)
			return;
		
		if (activity.getCurrentPlace().getParent() != null)
			upItem.setVisible(true);
		else
			upItem.setVisible(false);

		if (activity.getCurrentPlace().getIssues() == null)
			sendPlaceIssuesAndTasksRequest();
		else {
			ArrayList<IssueTask> i = activity
					.getCurrentPlace().getIssues();
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
				vineyardServer.getUrl() + VineyardServer.PLACE_ISSUES_API, activity
						.getCurrentPlace().getId());

		new PlaceIssuesAsyncHttpRequest().execute(placeIssuesAndTasksRequest);
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
			r.setIssue((IssueTask) v.getTag());
			activity.switchFragment(r);
		}
	};

	OnClickListener doneOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Toast.makeText(activity, "TODO: mark the issue as resolved",
					Toast.LENGTH_SHORT).show();
		}
	};

	class PlaceIssuesAsyncHttpRequest extends AsyncHttpRequests {
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

					android.util.Log.e("sad", issuesJSON);

					activity.getCache().setPlaceIssuesJSON(
							activity.getCurrentPlace().getId(), issuesJSON);

				} else {
					issuesJSON = activity.getCache().getPlaceIssuesJSON(
							activity.getCurrentPlace().getId());

					if (issuesJSON == null) {
						Log.w(TAG, "issues not available in sharedPreference");
						activity.getLoadingFragment().setError();
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
				activity.getLoadingFragment().setError();
			}
		}
	}
}
