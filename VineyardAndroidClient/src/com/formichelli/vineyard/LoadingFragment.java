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
	ViewGroup mainLayout, progressView, errorView;
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

		progressView = (ViewGroup) activity.findViewById(R.id.loading_progress);
		errorView = (ViewGroup) activity.findViewById(R.id.loading_error);
		
		retry = (ImageButton) activity.findViewById(R.id.loading_error_retry);
		retry.setOnClickListener(retryOnClickListener);
		
		setLoading(true);
	}

	public void setLoading(boolean loading) {
		activity.setNavigationDrawerLocked(loading);
		activity.setTitle(activity.getString(loading ? R.string.loading : R.string.loading_error));
		progressView.setVisibility(loading ? View.VISIBLE : View.GONE);
		errorView.setVisibility(loading ? View.GONE : View.VISIBLE);
	}
	
	OnClickListener retryOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setLoading(true);
			activity.sendRootPlaceRequest();
		}
	};
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		activity.setNavigationDrawerLocked(false);
	}
}
