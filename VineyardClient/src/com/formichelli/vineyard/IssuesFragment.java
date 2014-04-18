package com.formichelli.vineyard;

import java.util.ArrayList;

import com.formichelli.vineyard.entities.IssueTask;
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
import android.widget.ExpandableListView;

public class IssuesFragment extends Fragment {
	VineyardMainActivity activity;
	ExpandableListView issuesList;
	IssueExpandableAdapter issueAdapter;
	
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
		issuesList = (ExpandableListView) activity.findViewById(R.id.issues_list_view);
		ArrayList<IssueTask> issues = VineyardServer.getIssues(activity.getCurrentPlace());
		Log.e("ASD", "size: " + issues.size());
		issueAdapter = new IssueExpandableAdapter(activity, R.layout.issues_list_item, R.layout.issue_view, issues);
		issuesList.setAdapter(issueAdapter);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_issues, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_report_issue) {
			getActivity().getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, new ReportIssueFragment())
					.commit();
			return true;
		}
		return false;
	}

}
