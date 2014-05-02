package com.formichelli.vineyard;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.AsyncHttpRequests;
import com.formichelli.vineyard.utilities.Cache;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.Toast;
import android.support.v4.widget.DrawerLayout;

public class VineyardMainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	public PlaceViewerFragment placeViewerFragment;
	IssuesFragment issuesFragment;
	TasksFragment tasksFragment;
	LoadingFragment loadingFragment;
	SettingsActivity settingsActivity;

	Fragment lastFragment, currentFragment;
	Menu menu;
	Place currentPlace, rootPlace;
	String placesStatsJSON;
	String serverURL = "http://vineyard-server.no-ip.org/";
	int serverPort;
	ActionBar actionBar;
	VineyardServer vineyardServer;
	SharedPreferences sp;
	Cache cache;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		placeViewerFragment = new PlaceViewerFragment();
		issuesFragment = new IssuesFragment();
		tasksFragment = new TasksFragment();
		loadingFragment = new LoadingFragment();
		settingsActivity = new SettingsActivity();

		setContentView(R.layout.activity_vineyardmain);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		actionBar = getSupportActionBar();

		sp = PreferenceManager.getDefaultSharedPreferences(this);
		cache = new Cache(sp);

		serverInit();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// if the server settings have been changed reload places
		if (serverSettingsAreChanged())
			serverInit();
	}

	private boolean serverSettingsAreChanged() {
		if (serverURL != sp.getString(
				getString(R.string.preference_server_url),
				getString(R.string.preference_server_url_default)))
			return true;

		if (serverPort != Integer.parseInt(sp.getString(
				getString(R.string.preference_server_port),
				getString(R.string.preference_server_port_default))))
			return true;

		return false;
	}

	private void serverInit() {
		serverURL = sp.getString(getString(R.string.preference_server_url),
				getString(R.string.preference_server_url_default));
		serverPort = Integer.parseInt(sp.getString(
				getString(R.string.preference_server_port),
				getString(R.string.preference_server_port_default)));

		vineyardServer = new VineyardServer(serverURL, serverPort);

		if (sp.getBoolean(getString(R.string.preference_preload_all),
				Boolean.valueOf(getString(R.string.preference_preload_all))))
			;// TODO
		// else
		sendRootPlaceRequest();
	}

	public void sendRootPlaceRequest() {
		final String placesHierarchyRequest = vineyardServer.getUrl() + ":"
				+ vineyardServer.getPort()
				+ VineyardServer.PLACES_HIERARCHY_API;
		final String placesStatsRequest = vineyardServer.getUrl() + ":"
				+ vineyardServer.getPort() + VineyardServer.PLACES_STATS_API;

		new RootPlaceAsyncHttpRequest().execute(placesHierarchyRequest,
				placesStatsRequest);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Fragment nextFragment;

		// don't allow switch while loading
		if (currentFragment == loadingFragment) {
			Toast.makeText(this, "Wait server response...", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		switch (position) {
		case 0:
			nextFragment = placeViewerFragment;
			break;
		case 1:
			nextFragment = issuesFragment;
			break;
		case 2:
			nextFragment = tasksFragment;
			break;
		case 3:
			startActivity(new Intent(this, SettingsActivity.class));
			return;
		default:
			// after loading is completed lastFragment will be shown
			currentFragment = placeViewerFragment;
			nextFragment = loadingFragment;
			break;
		}

		switchFragment(nextFragment);
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
		new AlertDialog.Builder(this)
				.setMessage(R.string.dialog_exit_message)
				.setPositiveButton(R.string.dialog_confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						})
				.setNegativeButton(R.string.dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								return;
							}
						}).create().show();
		;
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

	private class RootPlaceAsyncHttpRequest extends AsyncHttpRequests {

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			String rootPlaceJSON, statsJSON;

			if (result != null && result.size() == 2 && result.get(0) != null
					&& result.get(1) != null) {
				// request OK, parse JSON to get rootPlace, cache data and show
				// previous
				// fragment

				rootPlaceJSON = result.get(0);
				statsJSON = result.get(1);

				cache.setRootPlaceJSON(rootPlaceJSON);
				cache.setPlacesStatsJSON(statsJSON);

			} else {
				rootPlaceJSON = cache.getRootPlaceJSON();
				statsJSON = cache.getPlacesStatsJSON();

				Toast.makeText(VineyardMainActivity.this,
						getString(R.string.cache_data_used), Toast.LENGTH_SHORT)
						.show();
			}

			try {
				rootPlace = new Place(new JSONObject(rootPlaceJSON));
				setCurrentPlace(rootPlace);
				setStats(rootPlace, getStats(statsJSON));
				switchFragment(lastFragment);
			} catch (JSONException e) {
				loadingFragment.setError();
			}
		}

		private void setStats(Place place,
				SparseArray<Pair<Integer, Integer>> stats) {
			final int placeId = place.getId();

			if (stats.get(placeId) != null) {
				place.setIssuesCount(stats.get(placeId).first);
				place.setTasksCount(stats.get(placeId).second);
			} else {
				place.setIssuesCount(0);
				place.setTasksCount(0);
			}

			for (Place child : place.getChildren())
				setStats(child, stats);
		}

		private SparseArray<Pair<Integer, Integer>> getStats(String placesStats) {
			int place, issues, tasks;
			final String PLACE = "place";
			final String ISSUES = "issues";
			final String TASKS = "tasks";

			SparseArray<Pair<Integer, Integer>> stats = new SparseArray<Pair<Integer, Integer>>();

			JSONArray placesStatsArray;
			try {
				placesStatsArray = new JSONArray(placesStats);
			} catch (JSONException e1) {
				return null;
			}

			for (int i = 0, l = placesStatsArray.length(); i < l; i++) {
				JSONObject placeStats;
				try {
					placeStats = placesStatsArray.getJSONObject(i);
					place = placeStats.getInt(PLACE);
				} catch (JSONException e) {
					break;
				}

				try {
					issues = placeStats.getInt(ISSUES);
				} catch (JSONException e) {
					issues = 0;
				}
				try {
					tasks = placeStats.getInt(TASKS);
				} catch (JSONException e) {
					tasks = 0;
				}

				stats.put(place, new Pair<Integer, Integer>(issues, tasks));
			}

			cache.setPlacesStatsJSON(placesStats);

			return stats;

		}
	}

	public LoadingFragment getLoadingFragment() {
		return loadingFragment;
	};

}
