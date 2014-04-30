package com.formichelli.vineyard;

import org.json.JSONException;
import org.json.JSONObject;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
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
	String rootPlaceJSON;
	String serverURL = "http://vineyard-server.no-ip.org/";
	int serverPort;
	ActionBar actionBar;
	VineyardServer vineyardServer;
	SharedPreferences sp;

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

		sendRootPlaceRequest();
	}

	public void sendRootPlaceRequest() {
		new LoadingAsyncHttpRequest().execute(vineyardServer.getUrl(),
				String.valueOf(vineyardServer.getPort()),
				VineyardServer.PLACE_HIERARCHY_API);
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

	public void setTitle(String title) {
		actionBar.setTitle(title);
	}

	public VineyardServer getServer() {
		return vineyardServer;
	}

	public String getRootPlaceJSON() {
		return rootPlaceJSON;
	}

	private class LoadingAsyncHttpRequest extends AsyncHttpRequest {

		@Override
		protected void onPreExecute() {
			switchFragment(loadingFragment);
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				// request OK, parse JSON to get rootPlace and restore previous fragment
				try {
					rootPlaceJSON = result;
					rootPlace = new Place(new JSONObject(rootPlaceJSON));
				} catch (JSONException e) {
					finish();
				}

				setCurrentPlace(rootPlace);
				switchFragment(lastFragment);
			} else {
				loadingFragment.setError();
			}
		}
	};
}
