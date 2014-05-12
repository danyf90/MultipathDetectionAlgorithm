package com.formichelli.vineyard;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.Toast;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.Cache;
import com.formichelli.vineyard.utilities.VineyardServer;

public class VineyardMainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	public static final String TAG = "VineyardMainActivity";

	PlaceViewerFragment placeViewerFragment;
	IssuesFragment issuesFragment;
	TasksFragment tasksFragment;
	LoadingFragment loadingFragment;
	SettingsActivity settingsActivity;

	Fragment lastFragment, currentFragment;
	Menu menu;
	Place currentPlace, rootPlace;
	String placesStatsJSON;
	String serverUrl;
	ActionBar actionBar;
	VineyardServer vineyardServer;
	SharedPreferences sp;
	Cache cache;
	SparseArray<Place> places;
	SparseArray<IssueTask> issues;
	SparseArray<SimpleTask> tasks;
	AsyncHttpRequest rootPlaceRequest, issuesAndTasksRequest;
	Pair<Integer, String> rootPlaceResponse, issuesAndTasksResponse;
	int userId;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		actionBar = getSupportActionBar();

		placeViewerFragment = new PlaceViewerFragment();
		issuesFragment = new IssuesFragment();
		tasksFragment = new TasksFragment();
		loadingFragment = new LoadingFragment();
		settingsActivity = new SettingsActivity();

		setContentView(R.layout.activity_vineyardmain);

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		// Retrieve values written by LoginActivity
		userId = sp.getInt(LoginActivity.USERID, -1);
		serverUrl = sp.getString(getString(R.string.preference_server_url),
				null);
		if (userId == -1 || serverUrl == null) {
			startLoginActivity();
			return;
		}

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		cache = new Cache(sp);

		serverInit();
	}

	private void serverInit() {
		vineyardServer = new VineyardServer(serverUrl);

		sendRootPlaceRequest();
	}

	public void sendRootPlaceRequest() {

		rootPlaceResponse = null;
		rootPlaceRequest = new RootPlaceAsyncHttpRequest(
				vineyardServer.getUrl());
		rootPlaceRequest.execute();

		issuesAndTasksResponse = null;
		issuesAndTasksRequest = new IssuesAndTasksAsyncHttpRequest(
				vineyardServer.getUrl());
		issuesAndTasksRequest.execute();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		switch (position) {
		case 0:
			switchFragment(placeViewerFragment);
			break;
		case 1:
			lastFragment = null; // force fragment switch
			issuesFragment.setSelectedPlace(null);
			switchFragment(issuesFragment);
			break;
		case 2:
			switchFragment(tasksFragment);
			break;
		case 3:
			startActivity(new Intent(this, SettingsActivity.class));
			return;
		case 4:
			sp.edit().remove(getString(R.string.preference_user_id)).commit();
			startLoginActivity();
			return;
		default:
			return;
		}
	}

	private void startLoginActivity() {
		startActivity(new Intent(this, LoginActivity.class));
		finish();
	}

	@Override
	public void onBackPressed() {
		if (currentFragment == placeViewerFragment)
			// navigate the hierarchy up or ask for close if the current place
			// is the root
			if (currentPlace == rootPlace)
				askExit();
			else {
				Place parent = currentPlace.getParent();
				if (parent != null)
					placeViewerFragment.loadPlace(parent);
			}
		// switch to previous fragment
		else if (currentFragment == issuesFragment)
			switchFragment(placeViewerFragment);
		else if (currentFragment == tasksFragment)
			switchFragment(placeViewerFragment);
		else if (currentFragment == loadingFragment)
			askExit();
		return;
	}

	private void askExit() {
		new DialogFragment() {
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				VineyardMainActivity activity = VineyardMainActivity.this;

				return new AlertDialog.Builder(activity)
						.setIcon(
								activity.getResources().getDrawable(
										R.drawable.action_info_dark))
						.setTitle(activity.getString(R.string.dialog_title))
						.setMessage(
								activity.getString(R.string.dialog_exit_message))
						.setPositiveButton(
								activity.getString(R.string.dialog_confirm),
								positiveClick)
						.setNegativeButton(
								activity.getString(R.string.dialog_cancel),
								null).create();
			}

			OnClickListener positiveClick = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					VineyardMainActivity.this.finish();
				}
			};
		}.show(getSupportFragmentManager(), "asd");
	}

	public Place getRootPlace() {
		return rootPlace;
	}

	public Place getCurrentPlace() {
		return currentPlace;
	}

	public Cache getCache() {
		return cache;
	}

	/**
	 * Sets current place and sets the actionbar title to place name
	 * 
	 * @param place
	 *            place to be setted
	 */
	public void setCurrentPlace(Place place) {
		if (place == null)
			throw new IllegalArgumentException("place cannot be null");

		currentPlace = place;
		setTitle(place.getName());
	}

	/**
	 * Changes the showed fragment
	 * 
	 * @param nextFragment
	 *            fragment to be shown
	 */
	public void switchFragment(Fragment nextFragment) {
		if (nextFragment == currentFragment)
			return;

		lastFragment = currentFragment;
		currentFragment = nextFragment;

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, currentFragment).commit();
	}

	/**
	 * Changes the showed fragment to last showed one
	 */
	public void switchFragment() {
		switchFragment(lastFragment);
	}

	/**
	 * Sets the actionbar title
	 * 
	 * @param title
	 *            new actionbar title
	 */
	public void setTitle(String title) {
		actionBar.setTitle(title);
	}

	public VineyardServer getServer() {
		return vineyardServer;
	}

	public PlaceViewerFragment getPlaceViewerFragment() {
		return placeViewerFragment;
	}

	public IssuesFragment getIssuesFragment() {
		return issuesFragment;
	}

	public TasksFragment getTasksFragment() {
		return tasksFragment;
	}

	public LoadingFragment getLoadingFragment() {
		return loadingFragment;
	}

	public void setNavigationDrawerLocked(boolean lock) {
		mNavigationDrawerFragment.setLocked(lock);
	}

	public int getUserId() {
		return userId;
	}

	public SparseArray<IssueTask> getIssues() {
		return issues;
	}

	public void setIssues(SparseArray<IssueTask> issues) {
		if (issues != null)
			this.issues = issues;
		else
			this.issues.clear();
	}

	public void addIssue(IssueTask issue) {
		if (issues != null)
			issues.put(issue.getId(), issue);
	}

	public void removeIssue(IssueTask issue) {
		issues.remove(issue.getId());
	}

	public SparseArray<SimpleTask> getTasks() {
		return tasks;
	}

	public void setTasks(SparseArray<SimpleTask> tasks) {
		if (tasks != null)
			this.tasks = tasks;
		else
			this.tasks.clear();
	}

	public void addTask(SimpleTask task) {
		if (task != null)
			tasks.put(task.getId(), task);
	}

	public void removeTask(SimpleTask task) {
		tasks.remove(task.getId());
	}

	private void onAsyncHttpRequestFinished(AsyncHttpRequest asyncHttpRequest,
			Pair<Integer, String> response) {

		if (asyncHttpRequest == rootPlaceRequest) {
			rootPlaceResponse = response;

			rootPlaceResponseHandler(response);
			if (issuesAndTasksResponse != null)
				issuesAndTasksResponseHandler(issuesAndTasksResponse);
		}

		if (asyncHttpRequest == issuesAndTasksRequest) {
			issuesAndTasksResponse = response;

			if (rootPlaceResponse != null)
				issuesAndTasksResponseHandler(response);
		}
	}

	/*
	 * Sends a GET request to the server to obtain places. During the loading
	 * the fragment loadingFragment will be displayed. At the end of the
	 * execution rootPlace will contain the entire hierarchy of places and
	 * IssuesAndTasksAsyncHttpRequest will be called. If something goes wrong an
	 * error message will be displayed in loadingFragment.
	 */
	private class RootPlaceAsyncHttpRequest extends AsyncHttpRequest {
		public RootPlaceAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.PLACES_HIERARCHY_API, Type.GET);
		}

		@Override
		protected void onPreExecute() {
			loadingFragment.setLoadingMessage(getString(R.string.loading_places));
			switchFragment(loadingFragment);
		}

		/*
		 * Writes the received places into rootPlace and sends a GET request for
		 * issues and tasks
		 */
		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			onAsyncHttpRequestFinished(this, response);
		}

	}

	private void rootPlaceResponseHandler(Pair<Integer, String> response) {
		String rootPlaceJSON = null;

		if (response != null) {
			switch (response.first) {

			case HttpStatus.SC_OK:
				// get places hierarchy from server response
				rootPlaceJSON = response.second;
				cache.putPlaces(rootPlaceJSON);
				break;

			case HttpStatus.SC_NOT_MODIFIED:
				rootPlaceJSON = cache.getPlaces();
				break;

			}
		}

		if (rootPlaceJSON == null) {
			// response == null or invalid response code: get places
			// hierarchy from shared preferences
			rootPlaceJSON = cache.getPlaces();

			if (rootPlaceJSON == null) {
				Log.e(TAG,
						"places not available neither from server nor from sharedPreference");
				loadingFragment.setLoading(false);
				return;
			}

			Toast.makeText(VineyardMainActivity.this,
					getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
					.show();
		}

		try {

			rootPlace = new Place(new JSONObject(rootPlaceJSON));
			setCurrentPlace(rootPlace);

			// needed by IssuesAndTasksAsyncHttpRequest
			places = new SparseArray<Place>();
			addPlaceToHashMap(rootPlace);

		} catch (JSONException e) {
			// show an error fragment if something is gone wrong
			Log.e(TAG, e.getLocalizedMessage());
			loadingFragment.setLoading(false);
		}

	}

	private void addPlaceToHashMap(Place place) {
		places.put(place.getId(), place);

		for (Place child : place.getChildren())
			addPlaceToHashMap(child);
	}

	/*
	 * Sends a GET request to the server to obtain issues and tasks. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution issues and tasks will be associated to places and viceversa. If
	 * something goes wrong an error message will be displayed in
	 * loadingFragment.
	 */
	private class IssuesAndTasksAsyncHttpRequest extends AsyncHttpRequest {
		public IssuesAndTasksAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.OPEN_ISSUES_AND_TASKS_API,
					Type.GET);
		}

		@Override
		protected void onPreExecute() {
			loadingFragment.setLoadingMessage(getString(R.string.loading_issues_and_tasks));
			switchFragment(loadingFragment);
		}

		/*
		 * Add received issues and places to the respective place.
		 */
		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			onAsyncHttpRequestFinished(this, response);
		}
	}

	private void issuesAndTasksResponseHandler(Pair<Integer, String> response) {
		String issuesAndTasksJSON = null;

		if (rootPlace == null)
			return;

		if (response != null)
			switch (response.first) {

			case HttpStatus.SC_OK:
				// get issues and tasks from server response
				issuesAndTasksJSON = response.second;
				cache.putIssuesAndTasks(issuesAndTasksJSON);
				break;
			case HttpStatus.SC_NOT_MODIFIED:
				// get issues and tasks from shared preferences
				issuesAndTasksJSON = cache.getIssuesAndTasks();
				Log.e(TAG, "not modified");
				break;
			}

		if (issuesAndTasksJSON == null) {
			// response == null or invalid response code: get issues and
			// tasks from shared preferences
			issuesAndTasksJSON = cache.getIssuesAndTasks();

			if (issuesAndTasksJSON == null) {
				Log.e(TAG,
						"issuesAndTasks not available neither from server nor from sharedPreference");
				loadingFragment.setLoading(false);
				return;
			}

			Toast.makeText(VineyardMainActivity.this,
					getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
					.show();
		}

		// associate issues and tasks to places and viceversa
		try {
			JSONArray issuesAndTasks = new JSONArray(issuesAndTasksJSON);

			issues = new SparseArray<IssueTask>();
			tasks = new SparseArray<SimpleTask>();

			for (int i = 0, l = issuesAndTasks.length(); i < l; i++) {
				JSONObject object = issuesAndTasks.getJSONObject(i);

				Place place = places.get(object.getInt(SimpleTask.PLACE));

				if (place != null) {
					if (!object.isNull(IssueTask.ISSUER)) {
						IssueTask issue = new IssueTask(object);
						issue.setPlace(place);
						place.addIssue(issue);
						addIssue(issue);
					} else {
						SimpleTask task = new SimpleTask(object);
						task.setPlace(place);
						place.addTask(task);
						addTask(task);
					}
				} else
					Log.e(TAG,
							"Place not found: "
									+ String.valueOf(object
											.getInt(SimpleTask.PLACE)));
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG,
					"Error parsing issues and tasks JSON: "
							+ e.getLocalizedMessage());
		}

		switchFragment(placeViewerFragment);
	}
}
