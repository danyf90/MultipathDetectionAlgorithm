package com.formichelli.vineyard;

import java.util.ArrayList;

import com.formichelli.vineyard.entities.IssueTask;
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
		ArrayList<IssueTask> issues = vineyardServer.getIssues(activity
				.getCurrentPlace());
		issueAdapter = new IssueExpandableAdapter(activity,
				R.layout.issues_list_item, R.layout.issue_view, issues,
				reportIssueOnClickListener, editOnClickListener,
				doneOnClickListener);
		issuesList.setAdapter(issueAdapter);

		if (upItem != null) {
			if (activity.getCurrentPlace().getParent() != null)
				upItem.setVisible(true);
			else
				upItem.setVisible(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_issues, menu);

		upItem = menu.findItem(R.id.action_issues_up);

		if (activity != null) {
			if (activity.getCurrentPlace().getParent() != null)
				upItem.setVisible(true);
			else
				upItem.setVisible(false);
		}

		super.onCreateOptionsMenu(menu, inflater);
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
}