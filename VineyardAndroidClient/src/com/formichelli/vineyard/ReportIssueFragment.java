package com.formichelli.vineyard;

import java.io.ByteArrayOutputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
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
import com.formichelli.vineyard.entities.Task.Priority;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
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
	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_SELECT_PHOTO = 2;
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
	// onCreateOptionMenu and
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
			this.issue = issue;
			
			if (!editMode) {
				// POST request to add an issue
				setServerUrl(serverUrl + VineyardServer.ISSUES_AND_TASKS_API);
				setType(Type.POST);
				setParams(issue.getParams());
			} else {
				// PUT request to edit an issue
				setServerUrl(serverUrl + VineyardServer.ISSUES_AND_TASKS_API
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
			activity.getLoadingFragment().setLoadingMessage(
					getString(R.string.loading_sending_request));
			activity.switchFragment(activity.getLoadingFragment());
		};

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			if (response != null) {
				if (!editMode && response.first == HttpStatus.SC_CREATED) {

					// associate issue to place
					issue.getPlace().addIssue(issue);

					// send images to server
					for (String image : gallery.getImagesFromCamera())
						new AsyncImageSend(vineyardServer.getUrl(), issue
								.getPlace().getId(), image).execute();

					if (gallery.getImagesFromCamera() != null)
						Toast.makeText(activity, "Image uploading continues in background...", Toast.LENGTH_SHORT);
					
					activity.switchFragment(activity.getIssuesFragment());
					return;
				} else if (editMode && response.first == HttpStatus.SC_ACCEPTED) {

					for (String image : gallery.getImagesFromCamera())
						new AsyncImageSend(vineyardServer.getUrl(), issue
								.getPlace().getId(), image).execute();

					if (gallery.getImagesFromCamera() != null)
						Toast.makeText(activity, "Image uploading continues in background...", Toast.LENGTH_SHORT);
					
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
	}

	private class AsyncImageSend extends AsyncHttpRequest {
		private static final String TAG = "AsyncImageSend";
		
		String path;
		
		public AsyncImageSend(String serverUrl, int placeId, String path) {
			super(String.format(serverUrl + VineyardServer.PHOTO_SEND_API, placeId), Type.POST);

			this.path = path;
			
			Bitmap image = BitmapFactory.decodeFile(path);
			if (image == null)
				throw new IllegalArgumentException(path + " cannot be decoded");
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        image.compress(Bitmap.CompressFormat.JPEG, 90, out);
	        String imageString = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);

	        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
	        postParams.add(new BasicNameValuePair("photo", imageString));
	        setParams(postParams);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			
			File f = new File(path);
			if (f != null)
				f.delete();
			
			Log.e(TAG, response.first + ": " + response.second);
		}
	}

	/**
	 * GooglePlayServicesClient callbacks implementation. Does notihng so far.
	 * 
	 * @author Fabio Carrara
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