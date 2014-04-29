package com.formichelli.vineyard;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;

public class VineyardMainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	private static final String TAG = "VineyardMainActivity";
	
	static private final String SERVER_URL = "http://vineyard-server.no-ip.org/";

	PlaceViewerFragment placeViewerFragment;
	IssuesFragment issuesFragment;
	TasksFragment tasksFragment;
	SettingsFragment settingsFragment;
	
	Fragment currentFragment;
	Menu menu;
	Place currentPlace, rootPlace;
	String rootPlaceJSON;
	ActionBar actionBar;
	VineyardServer vineyardServer;

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
		settingsFragment = new SettingsFragment();
		
		vineyardServer = new VineyardServer(SERVER_URL);
		
		setContentView(R.layout.activity_vineyardmain);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		actionBar = getSupportActionBar();

		rootPlaceJSON = vineyardServer.getRootPlaceJSON();
		rootPlace = vineyardServer.getRootPlace();
		if (rootPlace == null)
			finish();

		setCurrentPlace(rootPlace);

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		switch (position) {
		case 0:
			currentFragment = placeViewerFragment;
			break;
		case 1:
			currentFragment = issuesFragment;
			break;
		case 2:
			currentFragment = tasksFragment;
			break;
		case 3:
			currentFragment = settingsFragment;
			break;
		default:
			Log.e(TAG, "onNavigationDrawerItemSelected: Unexpected position");
			return;
		}

		switchFragment(currentFragment);
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
		else if (currentFragment == settingsFragment)
			switchFragment(placeViewerFragment);
		return;
	}

	private void askExit() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.dialog_confirm_exit)
				.setPositiveButton(R.string.dialog_confirm_exit_confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						})
				.setNegativeButton(R.string.dialog_confirm_exit_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								return;
							}
						}).create().show();
		;
	}

	public Place getRootPlace() {
		return rootPlace;
	}

	public Place getCurrentPlace() {
		return currentPlace;
	}

	public void setCurrentPlace(Place place) {
		if (place == null) {
			Log.e("TAG", "rootPlace is null");
			finish();
		}

		currentPlace = place;
		setTitle(place.getName());
	}

	public Menu getMenu() {
		return menu;
	}

	public void switchFragment(Fragment nextFragment) {
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
}
