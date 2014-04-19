package com.formichelli.vineyard;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;

public class VineyardMainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	private static final String TAG = "VineyardMainActivity";

	PlaceViewerFragment placeViewerFragment;
	IssuesFragment issuesFragment;
	TasksFragment tasksFragment;
	SettingsFragment settingsFragment;
	Menu menu;
	Place currentPlace, rootPlace;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		placeViewerFragment = new PlaceViewerFragment();
		issuesFragment = new IssuesFragment();
		tasksFragment = new TasksFragment();
		settingsFragment = new SettingsFragment();

		setContentView(R.layout.activity_vineyardmain);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		currentPlace = rootPlace = VineyardServer.getRootPlace();
		
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Fragment next = null;
		switch (position) {
		case 0:
			next = placeViewerFragment;
			break;
		case 1:
			next = issuesFragment;
			break;
		case 2:
			next = tasksFragment;
			break;
		case 3:
			next = settingsFragment;
			break;
		default:
			Log.e(TAG, "onNavigationDrawerItemSelected: Unexpected position");
			return;
		}

		// update the main content by replacing fragments
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, next).commit();
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section_main);
			break;
		case 2:
			mTitle = getString(R.string.title_section_issues);
			break;
		case 3:
			mTitle = getString(R.string.title_section_tasks);
			break;
		case 4:
			mTitle = getString(R.string.title_section_settings);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;

		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			restoreActionBar();
			return true;
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	public Place getCurrentPlace() {
		return currentPlace;
	}

	public void setCurrentPlace(Place place) {
		currentPlace = place;
	}

	public Menu getMenu() {
		return menu;
	}
}
