package com.formichelli.vineyard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Fragment which shows either a progress bar or an error message
 */
public class LoadingFragment extends Fragment {
	VineyardMainActivity activity;
	ViewGroup mainLayout, progressView, errorView;
	ImageButton retry;
	String errorMessage, loadingMessage;
	TextView errorMessageTextView, loadingMessageTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_loading, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();

		progressView = (ViewGroup) activity.findViewById(R.id.loading_progress);
		errorView = (ViewGroup) activity.findViewById(R.id.loading_error);

		loadingMessageTextView = (TextView) progressView
				.findViewById(R.id.loading_message);
		if (loadingMessage == null)
			loadingMessage = getString(R.string.loading_message);

		errorMessageTextView = (TextView) progressView
				.findViewById(R.id.loading_error_message);
		if (errorMessage == null)
			errorMessage = getString(R.string.loading_error_message);
		retry = (ImageButton) errorView.findViewById(R.id.loading_error_retry);
		retry.setOnClickListener(retryOnClickListener);

		setLoading(true);
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;

		if (errorMessageTextView != null)
			errorMessageTextView.setText(errorMessage);
	}

	public void setLoadingMessage(String loadingMessage) {
		this.loadingMessage = loadingMessage;

		if (loadingMessageTextView != null)
			loadingMessageTextView.setText(loadingMessage);
	}

	/**
	 * Disables the navigation drawer and shows the progress bar or enables the
	 * navigation drawer and shows an error message otherwise
	 * 
	 * @param loading
	 *            whether to show the progress bar or the error
	 */
	public void setLoading(boolean loading) {
		activity.setNavigationDrawerLocked(loading);
		activity.setTitle(activity.getString(loading ? R.string.loading
				: R.string.loading_error));
		progressView.setVisibility(loading ? View.VISIBLE : View.GONE);
		errorView.setVisibility(loading ? View.GONE : View.VISIBLE);
		if (loading)
			loadingMessageTextView.setText(loadingMessage);
		else
			errorMessageTextView.setText(errorMessage);
	}

	private OnClickListener retryOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setLoading(true);
			activity.loadData();
		}
	};

	@Override
	public void onDetach() {
		super.onDetach();

		// enable navigation drawer
		activity.setNavigationDrawerLocked(false);
	}
}
