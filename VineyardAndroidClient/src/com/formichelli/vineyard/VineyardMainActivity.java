package com.formichelli.vineyard;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.formichelli.vineyard.entities.WorkGroup;
import com.formichelli.vineyard.entities.Worker;
import com.formichelli.vineyard.gcm.GcmClient;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.Cache;
import com.formichelli.vineyard.utilities.SendImagesIntent;
import com.formichelli.vineyard.utilities.VineyardServer;

public class VineyardMainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	public static final String TAG = "VineyardMainActivity";
	public static final String PLACE_ID = "com.formichelli.vineyard.placeId";
	public static final String ISSUE_ID = "com.formichelli.vineyard.issueId";
	public static final String TASK_ID = "com.formichelli.vineyard.taskId";

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
	SparseArray<Worker> workers;
	SparseArray<WorkGroup> workGroups;
	AsyncHttpRequest rootPlaceRequest, issuesRequest, tasksRequest,
			workersRequest, workGroupsRequest;
	int timeout, userId;
	GcmClient gcmClient;

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
		serverUrl = sp.getString(getString(R.string.prefs_server_url), null);
		if (userId == -1 || serverUrl == null) {
			startLoginActivity();
			return;
		}

		timeout = Integer.valueOf(sp.getString(getString(R.string.prefs_request_timeout), null));

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		cache = new Cache(sp);

		gcmClient = new GcmClient(this);
		if (!gcmClient.checkGooglePlayServices()) {
			Log.e(TAG, "Play services not found");
			Toast.makeText(this,
					getString(R.string.gcm_error_no_play_services),
					Toast.LENGTH_LONG).show();
		} else
			Log.i(TAG, "GCM client id: " + gcmClient.getRegId());

		serverInit();
	}

	private void serverInit() {
		vineyardServer = new VineyardServer(serverUrl);
		loadData();
	}

	public void loadData() {
		loadingFragment.setLoadingMessage(getString(R.string.loading_places));

		places = null;
		rootPlaceRequest = new RootPlaceAsyncHttpRequest(
				vineyardServer.getUrl());
		rootPlaceRequest.execute();

		issues = null;
		issuesRequest = new IssuesAsyncHttpRequest(vineyardServer.getUrl());
		issuesRequest.execute();

		tasks = null;
		tasksRequest = new TasksAsyncHttpRequest(vineyardServer.getUrl());
		tasksRequest.execute();

		workers = null;
		workersRequest = new WorkersAsyncHttpRequest(vineyardServer.getUrl());
		workersRequest.execute();

		workGroups = null;
		workGroupsRequest = new WorkGroupsAsyncHttpRequest(
				vineyardServer.getUrl());
		workGroupsRequest.execute();

	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		switch (position) {

		case 0: // places
			if (rootPlace != null)
				setCurrentPlace(rootPlace);
			if (currentFragment != placeViewerFragment)
				switchFragment(placeViewerFragment);
			else
				placeViewerFragment.loadData();
			break;

		case 1: // issues
			issuesFragment.setSelectedPlace(null);
			if (currentFragment != issuesFragment)
				switchFragment(issuesFragment);
			else
				issuesFragment.loadData();
			break;

		case 2: // tasks
			tasksFragment.setSelectedPlace(null);
			if (currentFragment != tasksFragment)
				switchFragment(tasksFragment);
			else
				tasksFragment.loadData();
			break;

		case 3: // settings
			startActivity(new Intent(this, SettingsActivity.class));
			break;

		case 4: // logout
			sp.edit().remove(getString(R.string.preference_user_id)).commit();
			startLoginActivity();
			break;

		default:
			return;
		}
	}

	private void startLoginActivity() {
		if (gcmClient != null)
			gcmClient.unregister(this);
		startActivity(new Intent(this, LoginActivity.class));
		finish();
	}

	@Override
	public void onBackPressed() {
		if (currentFragment == placeViewerFragment) {
			if (!placeViewerFragment.onBackPressed())
				askExit();
		} else if (currentFragment == issuesFragment)
			switchFragment(placeViewerFragment);

		else if (currentFragment == tasksFragment) {
			if (!tasksFragment.onBackPressed())
				switchFragment(placeViewerFragment);
		} else if (currentFragment == loadingFragment)
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
					VineyardMainActivity.this.cancelRequests();
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
	 *            place to be set
	 */
	public void setCurrentPlace(Place place) {
		if (place == null)
			throw new IllegalArgumentException("place cannot be null");

		currentPlace = place;
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

		// commitAllowingStateLoss avoid crashes when the activity is no more in
		// foreground
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, currentFragment)
				.commitAllowingStateLoss();
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

	public int getTimeout() {
		return timeout;
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

	public SparseArray<Place> getPlaces() {
		return places;
	}

	public void setPlace(SparseArray<Place> places) {
		if (places != null)
			this.places = places;
		else
			this.places.clear();
	}

	public void addPlace(Place place) {
		if (place != null)
			places.put(place.getId(), place);
	}

	public void removePlace(Place place) {
		places.remove(place.getId());
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

	public SparseArray<Worker> getWorkers() {
		return workers;
	}

	public void setWorkers(SparseArray<Worker> workers) {
		if (workers != null)
			this.workers = workers;
		else
			this.workers.clear();
	}

	public void addWorker(Worker worker) {
		if (worker != null)
			workers.put(worker.getId(), worker);
	}

	public void removeWorker(Worker worker) {
		workers.remove(worker.getId());
	}

	public SparseArray<WorkGroup> getWorkGroups() {
		return workGroups;
	}

	public void setWorkGroups(SparseArray<WorkGroup> workGroups) {
		if (workGroups != null)
			this.workGroups = workGroups;
		else
			this.workGroups.clear();
	}

	public void addWorkGroup(WorkGroup workGroup) {
		if (workGroup != null)
			workGroups.put(workGroup.getId(), workGroup);
	}

	public void removeWorkGroup(Worker workGroup) {
		workGroups.remove(workGroup.getId());
	}

	private void onAsyncHttpRequestFinished(AsyncHttpRequest asyncHttpRequest,
			boolean success) {
		if (!success) {
			// one of the requests is failed: cancel requests
			cancelRequests();
			return;
		}

		if (markAsFinished(asyncHttpRequest)) {
			int id;
			associateEntities();

			// if the application is starting because of a notification click,
			// bring the user to the cause of the notification
			Intent intent = getIntent();
			if ((id = intent.getIntExtra(PLACE_ID, -1)) != -1) {
				Place notifiedPlace = places.get(id);

				if (notifiedPlace != null) {
					if ((id = intent.getIntExtra(ISSUE_ID, -1)) != -1) {
						IssueTask notifiedIssue = issues.get(id);

						if (notifiedIssue != null) {
							setCurrentPlace(notifiedPlace);
							issuesFragment.setSelectedPlace(notifiedPlace);
							issuesFragment.setSelectedIssue(notifiedIssue);
							switchFragment(issuesFragment);
							return;
						}
					} else if ((id = intent.getIntExtra(TASK_ID, -1)) != -1) {
						SimpleTask notifiedTask = tasks.get(id);

						if (notifiedTask != null) {
							setCurrentPlace(notifiedPlace);
							tasksFragment.setSelectedPlace(notifiedPlace);
							tasksFragment.setSelectedTask(notifiedTask);
							switchFragment(tasksFragment);
							return;
						}
					}
				}
			}

			setCurrentPlace(rootPlace);
			switchFragment(placeViewerFragment);
		}
	}

	private void cancelRequests() {
		if (rootPlaceRequest != null) {
			rootPlaceRequest.cancel(true);
			rootPlaceRequest = null;
		}

		if (issuesRequest != null) {
			issuesRequest.cancel(true);
			issuesRequest = null;
		}

		if (tasksRequest != null) {
			tasksRequest.cancel(true);
			tasksRequest = null;
		}

		if (workersRequest != null) {
			workersRequest.cancel(true);
			workersRequest = null;
		}

		if (workGroupsRequest != null) {
			workGroupsRequest.cancel(true);
			workGroupsRequest = null;
		}
	}

	/**
	 * Called to signal that a task is finished, changes the loading message
	 * depending on the finished tasks
	 * 
	 * @param asyncHttpRequest
	 *            finished task
	 * @return true if all tasks are finished, false otherwise
	 */
	private boolean markAsFinished(AsyncHttpRequest asyncHttpRequest) {
		if (asyncHttpRequest == rootPlaceRequest)
			rootPlaceRequest = null;
		else if (asyncHttpRequest == issuesRequest)
			issuesRequest = null;
		else if (asyncHttpRequest == tasksRequest)
			tasksRequest = null;
		else if (asyncHttpRequest == workersRequest)
			workersRequest = null;
		else if (asyncHttpRequest == workGroupsRequest)
			workGroupsRequest = null;

		// set loading message
		if (rootPlaceRequest == null)
			if (issuesRequest == null)
				if (tasksRequest == null)
					if (workersRequest == null)
						if (workGroupsRequest == null)
							return true;
						else
							loadingFragment
									.setLoadingMessage(getString(R.string.loading_workgroups));
					else
						loadingFragment
								.setLoadingMessage(getString(R.string.loading_workers));
				else
					loadingFragment
							.setLoadingMessage(getString(R.string.loading_tasks));
			else
				loadingFragment
						.setLoadingMessage(getString(R.string.loading_issues));

		return false;
	}

	private void associateEntities() {
		// associate issues with places, workers and workgroups
		for (int i = 0, l = issues.size(); i < l; i++) {
			IssueTask issue = issues.valueAt(i);

			issue.setPlace(places.get(issue.getPlace().getId()));
			issue.getPlace().addIssue(issue);

			if (issue.getAssignedWorker() != null)
				issue.setAssignedWorker(workers.get(issue.getAssignedWorker()
						.getId()));

			if (issue.getAssignedGroup() != null)
				issue.setAssignedGroup(workGroups.get(issue.getAssignedGroup()
						.getId()));
		}

		// associate tasks with places, workers and workgroups
		for (int i = 0, l = tasks.size(); i < l; i++) {
			SimpleTask task = tasks.valueAt(i);

			task.setPlace(places.get(task.getPlace().getId()));
			task.getPlace().addTask(task);

			if (task.getAssignedWorker() != null)
				task.setAssignedWorker(workers.get(task.getAssignedWorker()
						.getId()));

			if (task.getAssignedGroup() != null)
				task.setAssignedGroup(workGroups.get(task.getAssignedGroup()
						.getId()));
		}

		// associate workers and workgroups
		for (int i = 0, l = workGroups.size(); i < l; i++) {
			WorkGroup workGroup = workGroups.valueAt(i);

			ArrayList<Worker> newWorkers = new ArrayList<Worker>();

			for (Worker worker : workGroup.getWorkers())
				newWorkers.add(workers.get(worker.getId()));

			workGroup.setWorkers(newWorkers);
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
		protected final static String TAG = "RootPlaceAsyncHttpRequest";

		public RootPlaceAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.PLACES_HIERARCHY_API, Type.GET);

			setTimeout(VineyardMainActivity.this.timeout);

			setLastModified(cache.getPlacesLastModified());
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String rootPlaceJSON = null;

			if (response != null) {
				switch (response.first) {

				case HttpStatus.SC_OK:
					// get places hierarchy from server response
					rootPlaceJSON = response.second;
					cache.putPlaces(rootPlaceJSON, getLastModified());
					break;

				case HttpStatus.SC_NOT_MODIFIED:
					Log.i(TAG, "NOT_MODIFIED");
					// get places from shared preferences
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

					onAsyncHttpRequestFinished(this, false);
					return;
				}

				Toast.makeText(VineyardMainActivity.this,
						getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
						.show();
			}

			try {

				rootPlace = new Place(new JSONObject(rootPlaceJSON));

				places = new SparseArray<Place>();
				addPlaceToHashMap(rootPlace);

			} catch (JSONException e) {
				// show an error fragment if something is gone wrong
				Log.e(TAG, e.getLocalizedMessage());
				loadingFragment.setLoading(false);
				onAsyncHttpRequestFinished(this, false);
			}

			onAsyncHttpRequestFinished(this, true);
		}

		private void addPlaceToHashMap(Place place) {
			places.put(place.getId(), place);

			for (Place child : place.getChildren())
				addPlaceToHashMap(child);
		}
	}

	/*
	 * Sends a GET request to the server to obtain issues and tasks. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution issues and tasks will be associated to places and viceversa. If
	 * something goes wrong an error message will be displayed in
	 * loadingFragment.
	 */
	private class IssuesAsyncHttpRequest extends AsyncHttpRequest {
		protected final static String TAG = "IssuesAsyncHttpRequest";

		public IssuesAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.OPEN_ISSUES_API, Type.GET);

			setTimeout(VineyardMainActivity.this.timeout);

			setLastModified(cache.getIssuesLastModified());
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String issuesJSON = null;

			if (response != null)
				switch (response.first) {

				case HttpStatus.SC_OK:
					// get issues and tasks from server response
					issuesJSON = response.second;
					cache.putIssues(issuesJSON, getLastModified());
					break;
				case HttpStatus.SC_NOT_MODIFIED:
					Log.i(TAG, "NOT_MODIFIED");
					// get issues and tasks from shared preferences
					issuesJSON = cache.getIssues();
					break;
				}

			if (issuesJSON == null) {
				// response == null or invalid response code: get issues and
				// tasks from shared preferences
				issuesJSON = cache.getIssues();

				if (issuesJSON == null) {
					Log.e(TAG,
							"issues not available neither from server nor from sharedPreference");
					loadingFragment.setLoading(false);
					onAsyncHttpRequestFinished(this, false);
					return;
				}

				Toast.makeText(VineyardMainActivity.this,
						getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
						.show();
			}

			// parse issues and tasks from JSON
			try {
				JSONArray issuesArray = new JSONArray(issuesJSON);

				issues = new SparseArray<IssueTask>();

				for (int i = 0, l = issuesArray.length(); i < l; i++) {
					JSONObject object = issuesArray.getJSONObject(i);

					addIssue(new IssueTask(object));
				}

			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG,
						"Error parsing issues JSON: " + e.getLocalizedMessage());
				loadingFragment.setLoading(false);
				onAsyncHttpRequestFinished(this, false);
			}

			onAsyncHttpRequestFinished(this, true);
		}
	}

	/*
	 * Sends a GET request to the server to obtain issues and tasks. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution issues and tasks will be associated to places and viceversa. If
	 * something goes wrong an error message will be displayed in
	 * loadingFragment.
	 */
	private class TasksAsyncHttpRequest extends AsyncHttpRequest {
		protected final static String TAG = "TasksAsyncHttpRequest";

		public TasksAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.OPEN_TASKS_API, Type.GET);

			setTimeout(VineyardMainActivity.this.timeout);

			setLastModified(cache.getTasksLastModified());
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String tasksJSON = null;

			if (response != null)
				switch (response.first) {

				case HttpStatus.SC_OK:
					// get issues and tasks from server response
					tasksJSON = response.second;
					cache.putTasks(tasksJSON, getLastModified());
					break;
				case HttpStatus.SC_NOT_MODIFIED:
					Log.i(TAG, "NOT_MODIFIED");
					// get issues and tasks from shared preferences
					tasksJSON = cache.getTasks();
					break;
				}

			if (tasksJSON == null) {
				// response == null or invalid response code: get issues and
				// tasks from shared preferences
				tasksJSON = cache.getTasks();

				if (tasksJSON == null) {
					Log.e(TAG,
							"tasks not available neither from server nor from sharedPreference");
					loadingFragment.setLoading(false);
					onAsyncHttpRequestFinished(this, false);
					return;
				}

				Toast.makeText(VineyardMainActivity.this,
						getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
						.show();
			}

			// parse issues and tasks from JSON
			try {
				JSONArray tasksArray = new JSONArray(tasksJSON);

				tasks = new SparseArray<SimpleTask>();

				for (int i = 0, l = tasksArray.length(); i < l; i++) {
					JSONObject object = tasksArray.getJSONObject(i);

					addTask(new SimpleTask(object));
				}

			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG,
						"Error parsing tasks JSON: " + e.getLocalizedMessage());
				loadingFragment.setLoading(false);
				onAsyncHttpRequestFinished(this, false);
			}

			onAsyncHttpRequestFinished(this, true);
		}
	}

	/*
	 * Sends a GET request to the server to obtain workers. During the loading
	 * the fragment loadingFragment will be displayed. At the end of the
	 * execution workers will be associated to issues. If something goes wrong
	 * an error message will be displayed in loadingFragment.
	 */
	private class WorkersAsyncHttpRequest extends AsyncHttpRequest {
		protected final static String TAG = "WorkersAsyncHttpRequest";

		public WorkersAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.WORKERS_API, Type.GET);

			setTimeout(VineyardMainActivity.this.timeout);

			setLastModified(cache.getWorkersLastModified());
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String workersJSON = null;

			if (response != null)
				switch (response.first) {

				case HttpStatus.SC_OK:
					// get issues and tasks from server response
					workersJSON = response.second;
					cache.putWorkers(workersJSON, getLastModified());
					break;
				case HttpStatus.SC_NOT_MODIFIED:
					Log.i(TAG, "NOT_MODIFIED");
					// get issues and tasks from shared preferences
					workersJSON = cache.getWorkers();
					break;
				}

			if (workersJSON == null) {
				// response == null or invalid response code: get issues and
				// tasks from shared preferences
				workersJSON = cache.getWorkers();

				if (workersJSON == null) {
					Log.e(TAG,
							"workers not available neither from server nor from sharedPreference");
					loadingFragment.setLoading(false);
					onAsyncHttpRequestFinished(this, false);
					return;
				}

				Toast.makeText(VineyardMainActivity.this,
						getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
						.show();
			}

			// parse workers from JSON
			try {
				JSONArray workersArray = new JSONArray(workersJSON);

				workers = new SparseArray<Worker>();

				for (int i = 0, l = workersArray.length(); i < l; i++)
					addWorker(new Worker(workersArray.getJSONObject(i)));

			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG,
						"Error parsing workers JSON: "
								+ e.getLocalizedMessage());
				loadingFragment.setLoading(false);
				onAsyncHttpRequestFinished(this, false);
			}

			onAsyncHttpRequestFinished(this, true);
		}
	}

	/*
	 * Sends a GET request to the server to obtain workgroups. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution workgroups will be associated to workers. If something goes
	 * wrong an error message will be displayed in loadingFragment.
	 */
	private class WorkGroupsAsyncHttpRequest extends AsyncHttpRequest {
		protected final static String TAG = "WorkGroupsAsyncHttpRequest";

		public WorkGroupsAsyncHttpRequest(String serverUrl) {
			super(serverUrl + VineyardServer.WORKGROUPS_API, Type.GET);

			setTimeout(VineyardMainActivity.this.timeout);

			setLastModified(cache.getWorkGroupsLastModified());
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String workGroupsJSON = null;

			if (response != null)
				switch (response.first) {

				case HttpStatus.SC_OK:
					// get issues and tasks from server response
					workGroupsJSON = response.second;
					cache.putWorkGroups(workGroupsJSON, getLastModified());
					break;
				case HttpStatus.SC_NOT_MODIFIED:
					Log.i(TAG, "NOT_MODIFIED");
					// get issues and tasks from shared preferences
					workGroupsJSON = cache.getWorkGroups();
					break;
				}

			if (workGroupsJSON == null) {
				// response == null or invalid response code: get issues and
				// tasks from shared preferences
				workGroupsJSON = cache.getWorkGroups();

				if (workGroupsJSON == null) {
					Log.e(TAG,
							"workgroups not available neither from server nor from sharedPreference");
					loadingFragment.setLoading(false);
					onAsyncHttpRequestFinished(this, false);
					return;
				}

				Toast.makeText(VineyardMainActivity.this,
						getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
						.show();
			}

			// parse workers from JSON
			try {
				JSONArray workGroupsArray = new JSONArray(workGroupsJSON);

				workGroups = new SparseArray<WorkGroup>();

				for (int i = 0, l = workGroupsArray.length(); i < l; i++)
					addWorkGroup(new WorkGroup(workGroupsArray.getJSONObject(i)));

			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG,
						"Error parsing workgroups JSON: "
								+ e.getLocalizedMessage());
				loadingFragment.setLoading(false);
				onAsyncHttpRequestFinished(this, false);
			}

			onAsyncHttpRequestFinished(this, true);
		}
	}

	public class UserLogoutTask extends AsyncHttpRequest {

		public UserLogoutTask(String serverUrl, int userId) {
			super(serverUrl + String.format(VineyardServer.LOGOUT_API, userId),
					AsyncHttpRequest.Type.POST);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			startLoginActivity();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				onPhotoUploaded, new IntentFilter(SendImagesIntent.IMAGE_SENT));
	}

	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				onPhotoUploaded);
	}

	private BroadcastReceiver onPhotoUploaded = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int issueId = intent.getExtras().getInt(SendImagesIntent.ISSUE_ID);
			String photoName = intent.getExtras().getString(
					SendImagesIntent.PHOTO_NAME);

			Log.e(TAG, "photos uploaded");

			IssueTask issue = issues.get(issueId);
			if (issue != null)
				issue.addPhoto(photoName);
		}
	};

}
