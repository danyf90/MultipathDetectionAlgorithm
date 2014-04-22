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
import android.widget.ExpandableListView;

public class IssuesFragment extends Fragment {
	VineyardMainActivity activity;
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
	public void onAttach(Activity activity){
		super.onAttach(activity);
		
		VineyardMainActivity vmactivity = (VineyardMainActivity) activity;
		((VineyardMainActivity) vmactivity).setTitle(vmactivity.getCurrentPlace().getName());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		issuesList = (ExpandableListView) activity
				.findViewById(R.id.issues_list_view);
		ArrayList<IssueTask> issues = VineyardServer.getIssues(activity
				.getCurrentPlace());
		issueAdapter = new IssueExpandableAdapter(activity,
				R.layout.issues_list_item, R.layout.issue_view, issues);
		issuesList.setAdapter(issueAdapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_issues, menu);

		upItem = menu.findItem(R.id.action_issues_up);
		if (activity.getCurrentPlace().getParent() != null)
			upItem.setVisible(true);
		else
			upItem.setVisible(false);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_issues_up:
			activity.setCurrentPlace(activity.getCurrentPlace().getParent());
			issueAdapter.replaceItems(VineyardServer.getIssues(activity
					.getCurrentPlace()));

			if (activity.getCurrentPlace().getParent() == null)
				upItem.setVisible(false);

			return true;
		default:
			return false;
		}
	}

}
