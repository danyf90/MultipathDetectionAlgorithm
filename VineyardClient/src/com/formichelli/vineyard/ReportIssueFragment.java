package com.formichelli.vineyard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.VineyardServer;
import com.formichelli.vineyard.utilities.Gallery;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_SELECT_PHOTO = 2;
	public static final int REQUEST_PLACE = 3;

	VineyardMainActivity activity;
	IssueTask i;
	String currentPhotoPath;
	Gallery gallery;
	Button placeButton;
	Spinner priorities;
	ImageView addPhoto;
	EditText title, description;
	Menu menu;
	int imagePadding;

	public void setEditMode(IssueTask i) {
		this.i = i;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_report_issue, container,
				false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		VineyardMainActivity vmactivity = (VineyardMainActivity) activity;
		((VineyardMainActivity) vmactivity)
				.setTitle(getString(R.string.title_report_issue));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		addPhoto = (ImageView) activity.findViewById(R.id.action_add_photo);
		imagePadding = getResources().getDimensionPixelSize(
				R.dimen.gallery_padding);
		priorities = (Spinner) activity
				.findViewById(R.id.report_issue_priority);
		placeButton = (Button) activity.findViewById(R.id.report_issue_place);
		title = (EditText) activity.findViewById(R.id.report_issue_title);
		description = (EditText) activity
				.findViewById(R.id.report_issue_description);

		addPhoto.setOnClickListener(dispatchTakePictureIntent);
		setSpinnerAdapter(priorities, R.array.priorities);
		placeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(activity,
						PlacePickerActivity.class), REQUEST_PLACE);
			}
		});
		placeButton.setText(activity.getCurrentPlace().getName());

		if (i == null) {
			i = new IssueTask();
			i.setPlace(activity.getCurrentPlace());
		} else {
			title.setText(i.getTitle());
			description.setText(i.getDescription());
			placeButton.setText(i.getPlaceName());
			if (i.getPriority() != null)
				priorities.setSelection(i.getPriority().toInt() + 1);
			// TODO add photos
		}
	}

	private void setSpinnerAdapter(Spinner s, int array) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				activity, array, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		s.setAdapter(adapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_report_issue, menu);

		this.menu = menu;

		gallery = new Gallery(
				activity,
				(LinearLayout) activity.findViewById(R.id.report_issue_gallery),
				true,
				menu.findItem(R.id.action_report_issue_delete_selected_photos));

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_report_issue_delete_selected_photos:
			// remove selected images from gallery
			gallery.removeSelectedImages();
			break;
		case R.id.action_report_issue_cancel:
			activity.switchFragment(activity.issuesFragment);
			break;
		case R.id.action_report_issue_send:
			if (parseFields()) {
				VineyardServer.sendIssue(i);
				gallery.removeAllImages();
				activity.switchFragment(activity.issuesFragment);
				break;
			}
		default:
			return false;
		}

		return true;
	}

	// Checks if the fields are valid and populates issueTask
	private boolean parseFields() {
		Activity activity = getActivity();
		EditText t;
		String s;

		t = (EditText) activity.findViewById(R.id.report_issue_title);
		if (t == null)
			return false;
		else {
			s = t.getText().toString();
			if (s.compareTo("") == 0) {
				t.setError(getString(R.string.issue_title_error));
				return false;
			}

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

	OnClickListener dispatchTakePictureIntent = new OnClickListener() {
		@Override
		public void onClick(View v) {
			File photoFile;

			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);

			if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
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

		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				activity.getExternalFilesDir(null) /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		currentPhotoPath = image.getAbsolutePath();
		Log.e("PATH", currentPhotoPath);

		return image;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_TAKE_PHOTO:
			if (resultCode == Activity.RESULT_OK) {
				try {
					i.addPhoto(new URL(currentPhotoPath));
				} catch (MalformedURLException e) {
				}

				// create an image view and put it in the gallery
				gallery.addImage(currentPhotoPath);
			}
			break;
		case REQUEST_PLACE:
			if (resultCode == Activity.RESULT_OK) {
				// TODO serialize Place?
				Place p = new Place();
				p.setId(data.getExtras().getInt("placeid"));
				p.setName(data.getExtras().getString("placename"));
				i.setPlace(p);
			}
			break;
		}
	}

};