package com.formichelli.vineyard;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.Cache;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.Toast;
import android.support.v4.widget.DrawerLayout;

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

		userId = sp.getInt(LoginActivity.USERID, 0);
		serverUrl = sp.getString(getString(R.string.preference_server_url),
				null);

		if (userId == 0 || serverUrl == null) {
			startLoginActivity();
			return;
		}

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		// Set up the drawer
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		cache = new Cache(sp);
		places = new SparseArray<Place>();

		serverInit();
	}

	private void serverInit() {
		vineyardServer = new VineyardServer(serverUrl);

		sendRootPlaceRequest();
	}

	public void sendRootPlaceRequest() {
		new RootPlaceAsyncHttpRequest(vineyardServer.getUrl(),
				AsyncHttpRequest.Type.GET).execute();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		switch (position) {
		case 0:
			switchFragment(placeViewerFragment);
			break;
		case 1:
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
			if (currentPlace == rootPlace)
				askExit();
			else {
				Place parent = currentPlace.getParent();
				if (parent != null)
					placeViewerFragment.loadPlace(parent);
			}
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

	public void setRootPlace(Place rootPlace) {
		this.rootPlace = rootPlace;
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

	public void setCurrentPlace(Place place) {
		if (place == null)
			return;

		currentPlace = place;
		setTitle(place.getName());
	}

	public Menu getMenu() {
		return menu;
	}

	public void switchFragment(Fragment nextFragment) {
		if (nextFragment == currentFragment)
			return;

		lastFragment = currentFragment;
		currentFragment = nextFragment;

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, currentFragment).commit();
	}

	public void switchFragment() {
		switchFragment(lastFragment);
	}

	public void setTitle(String title) {
		actionBar.setTitle(title);
	}

	public VineyardServer getServer() {
		return vineyardServer;
	}

	public LoadingFragment getLoadingFragment() {
		return loadingFragment;
	}

	public IssuesFragment getIssuesFragment() {
		return issuesFragment;
	}

	public void setNavigationDrawerLocked(boolean lock) {
		mNavigationDrawerFragment.setLocked(lock);
	}

	public int getUserId() {
		return userId;
	}

	private class RootPlaceAsyncHttpRequest extends AsyncHttpRequest {
		private final static String TAG = "RootPlaceAsyncHttpRequest";
		String server;

		public RootPlaceAsyncHttpRequest(String serverUrl, Type type) {
			super(serverUrl + VineyardServer.PLACES_HIERARCHY_API, type);
			server = serverUrl;
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String rootPlaceJSON;

			if (response != null && response.first == HttpStatus.SC_OK) {

				rootPlaceJSON = response.second;
				cache.putPlaces(rootPlaceJSON);

			} else {

				rootPlaceJSON = cache.getPlaces();
				if (rootPlace == null) {
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
				addPlaceToHashMap(rootPlace);

				new IssuesAndTasksAsyncHttpRequest(server,
						AsyncHttpRequest.Type.GET).execute();

			} catch (JSONException e) {
				Log.e(TAG, e.getLocalizedMessage());
				loadingFragment.setLoading(false);
			}
		}

	}

	private void addPlaceToHashMap(Place place) {
		places.put(place.getId(), place);

		for (Place child : place.getChildren())
			addPlaceToHashMap(child);
	}

	private class IssuesAndTasksAsyncHttpRequest extends AsyncHttpRequest {
		private final static String TAG = "IssuesAndTasksAsyncHttpRequest";

		public IssuesAndTasksAsyncHttpRequest(String serverUrl, Type type) {
			super(serverUrl + VineyardServer.ISSUES_AND_TASKS_API, type);
		}

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			String issuesAndTasksJSON;

			if (response != null && response.first == HttpStatus.SC_OK) {

				issuesAndTasksJSON = response.second;

				cache.putIssuesAndTasks(issuesAndTasksJSON);

			} else {
				issuesAndTasksJSON = cache.getIssuesAndTasks();

				if (issuesAndTasksJSON == null) {
					Log.e(TAG,
							"issuesAndTasks not available neither from server nor from sharedPreference");
					loadingFragment.setLoading(false);
					return;
				}

				if (response == null
						|| response.first != HttpStatus.SC_NOT_MODIFIED)
					Toast.makeText(VineyardMainActivity.this,
							getString(R.string.cache_data_used),
							Toast.LENGTH_SHORT).show();
				else
					Log.e(TAG, "not modified");
			}

			try {
				JSONArray issuesAndTasks = new JSONArray(issuesAndTasksJSON);

				for (int i = 0, l = issuesAndTasks.length(); i < l; i++) {
					JSONObject object = issuesAndTasks.getJSONObject(i);
					Place place = places.get(object.getInt(SimpleTask.PLACE));

					if (place != null) {
						// TODO change in has()
						if (!object.isNull(IssueTask.ISSUER)) {
							IssueTask issue = new IssueTask(object);
							issue.setPlace(place);
							place.addIssue(issue);
						} else {
							SimpleTask task = new SimpleTask(object);
							task.setPlace(place);
							place.addTask(task);
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

			setNavigationDrawerLocked(false);
			switchFragment(lastFragment);
		}
	}
}
