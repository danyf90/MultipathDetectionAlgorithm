package com.formichelli.vineyard;

import com.formichelli.vineyard.entities.IssueTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_IMAGE_CAPTURE = 1;

	VineyardMainActivity activity;
	IssueTask i;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_report_issue, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		activity.reportIssueFragment = this;

		activity.findViewById(R.id.report_issue_add_photo).setOnClickListener(
				dispatchTakePictureIntent);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_report_issue, menu);
		// showGlobalContextActionBar();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_report_issue_cancel:
			closeFragment();
			return true;
		case R.id.action_report_issue_send:
			if (parseFields()) {
				Toast.makeText(activity, "TODO: send issue to server",
						Toast.LENGTH_LONG).show();
				closeFragment();
			}
			return true;
		default:
			return false;
		}
	}

	private void closeFragment() {
		activity.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container,
						((VineyardMainActivity) getActivity()).issuesFragment)
				.commit();
	}

	private boolean parseFields() {
		Activity activity = getActivity();
		EditText t;
		String s;

		i = new IssueTask();

		t = (EditText) activity.findViewById(R.id.report_issue_title);
		if (t == null)
			return false;
		else {
			s = t.getText().toString();
			if (s.compareTo("") == 0)
				return false;

			i.setTitle(s);
		}

		t = (EditText) activity.findViewById(R.id.report_issue_description);
		if (t == null)
			return false;
		else {
			s = t.getText().toString();
			if (s.compareTo("") != 0)
				i.setDescription(s);
		}

		return true;
	}

	private OnClickListener dispatchTakePictureIntent = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE
				&& resultCode == Activity.RESULT_OK) {
			Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
			ImageView image = new ImageView(activity);
			image.setImageBitmap(imageBitmap);
			setMargins(image, 5, 0, 0, 5);

			((LinearLayout) activity.findViewById(R.id.report_issue_photos))
					.addView(image);
		}
	}

	private void setMargins(ImageView image, int left, int top, int right,
			int bottom) {
		// TODO
	}
};