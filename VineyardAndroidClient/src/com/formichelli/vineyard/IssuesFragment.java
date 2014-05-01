package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.utilities.AsyncHttpRequests;
import com.formichelli.vineyard.utilities.IssueExpandableAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class IssuesFragment extends Fragment {
	VineyardMainActivity activity;
	VineyardServer vineyardServer;
	ExpandableListView issuesList;
	IssueExpandableAdapter issueAdapter;
	MenuItem upItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_issues, container, false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		VineyardMainActivity vmactivity = (VineyardMainActivity) activity;
		((VineyardMainActivity) vmactivity).setTitle(vmactivity
				.getCurrentPlace().getName());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		vineyardServer = activity.getServer();
		issuesList = (ExpandableListView) activity
				.findViewById(R.id.issues_list_view);

		if (upItem != null)
			init();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_issues, menu);

		upItem = menu.findItem(R.id.action_issues_up);

		if (activity != null)
			init();

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void init() {
		if (activity.getCurrentPlace().getParent() != null)
			upItem.setVisible(true);
		else
			upItem.setVisible(false);

		if (activity.getCurrentPlace().getIssues() == null)
			sendPlaceIssuesRequest();
		else {
//			issueAdapter = new IssueExpandableAdapter(activity,
//					R.layout.issues_list_item, R.layout.issue_view, activity.getCurrentPlace().getIssues(),
//					reportIssueOnClickListener, editOnClickListener,
//					doneOnClickListener);
//			issuesList.setAdapter(issueAdapter);

			issueAdapter = new IssueExpandableAdapter(activity,
					R.layout.issues_list_item, R.layout.issue_view, activity.getCurrentPlace().getIssues(),
					reportIssueOnClickListener, editOnClickListener,
					doneOnClickListener);
			issuesList.setAdapter(issueAdapter);
		}
	}

	public void sendPlaceIssuesRequest() {
		activity.getCurrentPlace().setIssues(vineyardServer.getIssues(activity
					.getCurrentPlace()));

		issueAdapter = new IssueExpandableAdapter(activity,
				R.layout.issues_list_item, R.layout.issue_view, activity.getCurrentPlace().getIssues(),
				reportIssueOnClickListener, editOnClickListener,
				doneOnClickListener);
		issuesList.setAdapter(issueAdapter);
//		TODO
//		final String placeIssuesRequest = vineyardServer.getUrl() + ":"
//				+ vineyardServer.getPort()
//				+ VineyardServer.PLACE_ISSUES_API;
//
//		new PlaceIssuesAsyncHttpRequest().execute(placeIssuesRequest);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_issues_up:
			activity.setCurrentPlace(activity.getCurrentPlace().getParent());
			issueAdapter.replaceItems(vineyardServer.getIssues(activity
					.getCurrentPlace()));

			if (activity.getCurrentPlace().getParent() == null)
				upItem.setVisible(false);

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

	private class PlaceIssuesAsyncHttpRequest extends AsyncHttpRequests {

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.loadingFragment);
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			if (result != null && result.size() == 1 && result.get(0) != null) {
				JSONArray issuesArray;
				ArrayList<IssueTask> issues = new ArrayList<IssueTask>();

				try {
					issuesArray = new JSONArray(result.get(0));

				} catch (JSONException e) {
					return;
				}

				for (int i = 0, l = issuesArray.length(); i < l; i++) {
					try {
						issues.add(new IssueTask(issuesArray.getJSONObject(i)));
					} catch (JSONException e) {
					}
				}

				activity.getCurrentPlace().setIssues(issues);

				activity.switchFragment(activity.lastFragment);
			} else {
				// TODO
			}
		}
	}
}
