package com.formichelli.vineyard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.formichelli.vineyard.entities.IssueTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_TAKE_PHOTO = 1;

	VineyardMainActivity activity;
	IssueTask i;
	String currentPhotoPath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		i = new IssueTask();

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
				// Create the File where the photo should go
				File photoFile;

				try {
					photoFile = createImageFile();
				} catch (IOException ex) {
					return;
				}

				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	};

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "issue_" + timeStamp + "";
		File storageDir = activity.getExternalFilesDir(null);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		currentPhotoPath = "file:/" + image.getAbsolutePath();
		Log.e("PATH", currentPhotoPath);

		return image;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			try {
				if (currentPhotoPath != null)
					i.addPhoto(new URL(currentPhotoPath));
			} catch (MalformedURLException e) {
			}

			setThumbnail();
		}
	}

	private void setThumbnail() {
		// TODO
		// Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
		// activity.getContentResolver(),
		// getIdFromString(currentPhotoPath),
		// MediaStore.Images.Thumbnails.MINI_KIND, null);
		// ImageView image = new ImageView(activity);
		// image.setImageBitmap(thumbnail);
		// // TODO setMargins
		//
		// ((LinearLayout) activity.findViewById(R.id.report_issue_photos))
		// .addView(image);
	}

	// private Long getIdFromString(String absolutePath) {
	// Log.e("PUPPA", absolutePath.substring(
	// absolutePath.lastIndexOf("-") + 1,
	// absolutePath.lastIndexOf(".")));
	// return Long.parseLong(absolutePath.substring(
	// absolutePath.lastIndexOf("-") + 1,
	// absolutePath.lastIndexOf(".")));
	// }
};