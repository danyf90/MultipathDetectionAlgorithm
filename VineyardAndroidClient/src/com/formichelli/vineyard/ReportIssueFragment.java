package com.formichelli.vineyard;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import android.widget.ToggleButton;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task.Priority;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.SendImagesIntent;
import com.formichelli.vineyard.utilities.VineyardGallery;
import com.formichelli.vineyard.utilities.VineyardServer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * Fragment which allows to fill and sends an issue report
 */
@SuppressLint("ValidFragment")
public class ReportIssueFragment extends Fragment {
	public static final int REQUEST_PLACE = 3;

	VineyardMainActivity activity;
	VineyardServer vineyardServer;
	LocationClient locationClient;
	IssueTask issue;
	String currentPhotoPath;
	VineyardGallery gallery;
	Button placeButton;
	Spinner priorities;
	ToggleButton locationButton;
	EditText title, description;
	Menu menu;
	boolean first, editMode;

	public ReportIssueFragment() {
		editMode = false;
	}

	public ReportIssueFragment(IssueTask issue) {
		super();

		if (issue == null || issue.getPlace() == null)
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
	public void onStart() {
		super.onStart();
		locationClient.connect();
	}

	@Override
	public void onStop() {
		locationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		first = true;

		activity = (VineyardMainActivity) getActivity();
		activity.setTitle(getString(R.string.title_report_issue));
		vineyardServer = activity.getServer();

		/* Create a LocationClients that connects to Google Play Services. */
		/* So far, callbacks for connection events are not handled. */
		LocationClientCallbacks dummyCallbacks = new LocationClientCallbacks();
		locationClient = new LocationClient(activity, dummyCallbacks,
				dummyCallbacks);

		title = (EditText) activity.findViewById(R.id.report_issue_title);

		description = (EditText) activity
				.findViewById(R.id.report_issue_description);

		locationButton = (ToggleButton) activity
				.findViewById(R.id.report_issue_use_location);

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
		setSpinnerAdapter(priorities, R.array.task_priorities);
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
			loadData();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	// This function must be called only once after both onActivityCreated and
	// onCreateOptionMenu and
	private void loadData() {
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

			for (String photo : issue.getPhotos())
				gallery.addImageFromServer(vineyardServer.getUrl()
						+ VineyardServer.PHOTO_API, photo);
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

		issue.setDescription(description.getText().toString());

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

		if (locationButton.isEnabled() && locationButton.isChecked()) {
			Location loc = locationClient.getLastLocation();
			if (loc != null) {
				issue.setLatitude(loc.getLatitude());
				issue.setLongitude(loc.getLongitude());
			} else {
				Log.i("ReportIssue", "No location info available.. sure?");
			}
		}

		return true;
	}

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
		case VineyardGallery.REQUEST_TAKE_PHOTO:
		case VineyardGallery.REQUEST_PICK_PHOTO:
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
			this.issue = issue;

			if (!editMode) {
				// POST request to add an issue
				setServerUrl(serverUrl + VineyardServer.ISSUES_API);
				setType(Type.POST);
			} else {
				// PUT request to edit an issue
				setServerUrl(serverUrl + VineyardServer.ISSUES_API
						+ issue.getId());
				setType(Type.PUT);
			}
			
			setTimeout(activity.getTimeout());

			List<NameValuePair> params = issue.getParams();
			params.add(new BasicNameValuePair(SimpleTask.MODIFIER, String.valueOf(activity.getUserId())));

			setParams(params);
		}

		@Override
		protected void onPreExecute() {
			activity.getLoadingFragment().setLoadingMessage(
					getString(R.string.loading_sending_request));
			activity.switchFragment(activity.getLoadingFragment());
		};

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			if (response != null) {
				if (!editMode && response.first == HttpStatus.SC_CREATED) {

					// set issue modifierId
					issue.setModifierId(activity.getUserId());
					
					// associate issue to place
					issue.getPlace().addIssue(issue);

					// send images to server
					ArrayList<String> images = gallery.getImagesFromCamera();
					if (images != null)
						sendImages(issue.getId(), images);

					activity.switchFragment(activity.getIssuesFragment());
					return;
				} else if (editMode && response.first == HttpStatus.SC_ACCEPTED) {

					// send images to server
					ArrayList<String> images = gallery.getImagesFromCamera();
					if (images != null)
						sendImages(issue.getId(), images);

					activity.switchFragment(activity.getIssuesFragment());
					return;
				}
			}

			Toast.makeText(activity,
					activity.getString(R.string.issue_report_error),
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, String.valueOf(response.first) + ": " + response.second);
			activity.switchFragment();
		}

		private void sendImages(int issueId, ArrayList<String> images) {
			Intent intent = new Intent(activity, SendImagesIntent.class);
			intent.putExtra(SendImagesIntent.SERVER_URL,
					String.format(vineyardServer.getUrl()
							+ VineyardServer.PHOTO_SEND_API, issueId));
			intent.putExtra(SendImagesIntent.ISSUE_ID, issueId);
			intent.putStringArrayListExtra(SendImagesIntent.IMAGES, images);
			activity.startService(intent);
			Toast.makeText(activity,
					activity.getString(R.string.issue_report_sending_images),
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * GooglePlayServicesClient callbacks implementation. Does notihng so far.
	 */
	private class LocationClientCallbacks implements
			GooglePlayServicesClient.ConnectionCallbacks,
			GooglePlayServicesClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			Log.i("ReportIssue", "Connection to Google Play Services failed..");
		}

		@Override
		public void onConnected(Bundle arg0) {
			Log.i("ReportIssue", "Connected to Google Play Services");
			locationButton.setEnabled(true);
			locationButton.setChecked(true);
		}

		@Override
		public void onDisconnected() {
		}

	}

};