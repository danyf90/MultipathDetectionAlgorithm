package com.formichelli.vineyard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class LoadingFragment extends Fragment {
	VineyardMainActivity activity;
	ViewGroup mainLayout, progressLayout, errorLayout;
	ImageButton retry;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		mainLayout = (ViewGroup) inflater.inflate(R.layout.fragment_loading,
				container, false);

		return mainLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();

		// inflate both the progress and error layout and attach the former to
		// the container
		progressLayout = (ViewGroup) activity.getLayoutInflater().inflate(
				R.layout.fragment_loading_progress, mainLayout, false);
		errorLayout = (ViewGroup) activity.getLayoutInflater().inflate(
				R.layout.fragment_loading_error, mainLayout, false);
		
		retry = (ImageButton) errorLayout
				.findViewById(R.id.loading_error_retry);
		retry.setOnClickListener(retryOnClickListener);
		
		setLoading();
	}

	public void setLoading() {
		activity.setNavigationDrawerLocked(true);
		activity.setTitle(activity.getString(R.string.loading));
		mainLayout.removeAllViews();
		mainLayout.addView(progressLayout);
	}

	public void setError() {
		activity.setNavigationDrawerLocked(false);
		activity.setTitle(activity.getString(R.string.loading_error));
		mainLayout.removeAllViews();
		mainLayout.addView(errorLayout);
	}

	OnClickListener retryOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setLoading();
			activity.sendRootPlaceRequest();
		}
	};
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		activity.setNavigationDrawerLocked(false);
	}
}
