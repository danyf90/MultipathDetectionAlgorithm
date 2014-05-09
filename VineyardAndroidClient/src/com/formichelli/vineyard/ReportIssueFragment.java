package com.formichelli.vineyard;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Pair;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.Task.Priority;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.VineyardGallery;
import com.formichelli.vineyard.utilities.VineyardServer;

/**
 * Fragment which allows to fill and sends an issue report
 */
@SuppressLint("ValidFragment")
public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_SELECT_PHOTO = 2;
	public static final int REQUEST_PLACE = 3;

	VineyardMainActivity activity;
	VineyardServer vineyardServer;
	IssueTask issue;
	String currentPhotoPath;
	VineyardGallery gallery;
	Button placeButton;
	Spinner priorities;
	EditText title, description;
	Menu menu;
	boolean first, editMode;

	public ReportIssueFragment() {
		editMode = false;
	}

	public ReportIssueFragment(IssueTask issue) {
		super();

		if (issue == null)
			throw new IllegalArgumentException("issue cannot be null");

		this.issue = issue;
		editMode = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_report_issue, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		first = true;

		activity = (VineyardMainActivity) getActivity();
		activity.setTitle(getString(R.string.title_report_issue));
		vineyardServer = activity.getServer();

		title = (EditText) activity.findViewById(R.id.report_issue_title);

		description = (EditText) activity
				.findViewById(R.id.report_issue_description);

		placeButton = (Button) activity.findViewById(R.id.report_issue_place);
		placeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent placePicker = new Intent(activity,
						PlacePickerActivity.class);

				// put the JSON of the place hierarchy in the intent extras
				placePicker.putExtra(PlacePickerActivity.HIERARCHY, activity
						.getCache().getPlaces());

				// put the id of the selected place and of all its ancestors in
				// the intent extras
				ArrayList<Integer> ids = new ArrayList<Integer>();
				for (Place currentPlace = activity.getCurrentPlace(); currentPlace != null; currentPlace = currentPlace
						.getParent())
					ids.add(currentPlace.getId());
				placePicker.putExtra(PlacePickerActivity.ANCESTORS, ids);

				startActivityForResult(placePicker, REQUEST_PLACE);
			}
		});
		placeButton.setText(activity.getCurrentPlace().getName());

		priorities = (Spinner) activity
				.findViewById(R.id.report_issue_priority);
		setSpinnerAdapter(priorities, R.array.issue_priorities);
	}

	private void setSpinnerAdapter(Spinner s, int array) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				activity, array, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		s.setAdapter(adapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.report_issue, menu);

		this.menu = menu;

		if (first) {
			init();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	// This function must be called only once after both onActivityCreated and
	// onCreateOptionMenu
	private void init() {
		gallery = (VineyardGallery) activity
				.findViewById(R.id.report_issue_gallery);
		gallery.setFragment(this);

		if (issue == null) {
			// create a new issue
			issue = new IssueTask();
			issue.setIssuer(activity.getUserId());
			issue.setPlace(activity.getCurrentPlace());
		} else {
			// edit an existing issue
			title.setText(issue.getTitle());

			description.setText(issue.getDescription());

			placeButton.setText(String.valueOf(issue.getPlace().getName()));

			priorities.setSelection(Priority.getIndex(issue.getPriority()));

			// TODO
			// for (String photo: i.getPhotos()) {
			// ImageView i = new ImageView(activity);
			// gallery.a
			// imageLoader = new ImageLoader(activity, header, progress);
			// imageLoader.execute(p.getPhoto());
			// }
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_report_issue_cancel:
			activity.switchFragment();
			break;
		case R.id.action_report_issue_send:
			if (parseFields()) {
				new AsyncIssueSend(vineyardServer.getUrl(), issue).execute();
				break;
			}
		default:
			return false;
		}

		return true;
	}

	// checks if the fields are valid and populates issueTask
	private boolean parseFields() {
		String s;

		// title can't be empty
		s = title.getText().toString();
		if (s.compareTo("") == 0) {
			title.setError(getString(R.string.issue_title_error));
			return false;
		} else
			issue.setTitle(s);

		issue.setDescription(s);

		switch (priorities.getSelectedItemPosition()) {
		case 1:
			issue.setPriority(Priority.LOW);
			break;
		case 2:
			issue.setPriority(Priority.MEDIUM);
			break;
		case 3:
			issue.setPriority(Priority.HIGH);
			break;
		default:
			break;
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

		return image;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_TAKE_PHOTO:
			gallery.onActivityResult(requestCode, resultCode, data);
			break;
		case REQUEST_PLACE:
			switch (resultCode) {
			case Activity.RESULT_OK:
				Place selectedPlace = activity.getRootPlace();

				// Navigate from root to selected place (root is already
				// selected)
				ArrayList<Integer> ids = data.getExtras().getIntegerArrayList(
						PlacePickerActivity.ANCESTORS);
				for (int i = ids.size() - 2; i >= 0; i--) {
					for (Place p : selectedPlace.getChildren())
						if (p.getId() == ids.get(i)) {
							selectedPlace = p;
							break;
						}
				}

				issue.setPlace(selectedPlace);
				placeButton.setText(selectedPlace.getName());
			case Activity.RESULT_CANCELED:
				// canceled by the user
			case Activity.RESULT_FIRST_USER:
				// an error occurred while parsing places JSON
				break;
			}
			break;
		}
	}

	/*
	 * Sends the issue (either new or edited) to the server using either a POST
	 * (creation) or a PUT (edit) HTTP message. During the loading the fragment
	 * loadingFragment will be displayed. At the end of the execution the
	 * created issue will be added to its place. If something goes wrong a toast
	 * will be shown and the report editor is shown again.
	 */
	private class AsyncIssueSend extends AsyncHttpRequest {
		IssueTask issue;

		public AsyncIssueSend(String serverUrl, IssueTask issue) {
			if (!editMode) {
				// POST request to add an issue
				setServerUrl(serverUrl + VineyardServer.ADD_ISSUE_API);
				setType(Type.POST);
				setParams(issue.getParams());
			} else {
				// PUT request to edit an issue
				setServerUrl(serverUrl + VineyardServer.EDIT_ISSUE_API
						+ issue.getId());
				setType(Type.PUT);
				List<NameValuePair> params = issue.getParams();
				for (NameValuePair param : params)
					if (param.getName() == IssueTask.ID) {
						params.remove(param);
						break;
					}
				setParams(params);
			}

		}

		@Override
		protected void onPreExecute() {
			activity.switchFragment(activity.getLoadingFragment());
		};

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			if (response != null && response.first == HttpStatus.SC_CREATED) {
				// TODO delete photos from sdcard
				issue.getPlace().addIssue(issue);
			} else {
				Toast.makeText(activity,
						activity.getString(R.string.issue_report_error),
						Toast.LENGTH_SHORT).show();
				return;
			}

			activity.switchFragment();
		}

	};
};