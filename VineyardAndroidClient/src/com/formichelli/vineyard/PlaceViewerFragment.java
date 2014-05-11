package com.formichelli.vineyard;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.utilities.ImageLoader;
import com.formichelli.vineyard.utilities.PlaceAdapter;
import com.formichelli.vineyard.utilities.Util;
import com.formichelli.vineyard.utilities.VineyardServer;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment which shows the info of the selected place and allows navigation
 * between places
 */
public class PlaceViewerFragment extends Fragment {
	VineyardMainActivity activity;
	TextView ancestors, description, issuesCount, tasksCount,
			childrenIssuesCount, childrenTasksCount, childrenLabel;
	ViewGroup attributesLabels, attributesValues, issues, tasks, header;
	ImageView photo;
	PlaceAdapter placeAdapter;
	ListView childrenList;
	MenuItem upItem;
	Drawable redBorder, wineBorder;
	ProgressBar progress;
	boolean first;
	AsyncTask<String, Void, Bitmap> imageLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_place_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		first = true;

		activity = (VineyardMainActivity) getActivity();

		ancestors = (TextView) activity.findViewById(R.id.place_view_ancestors);

		header = (ViewGroup) activity.findViewById(R.id.place_view_header);

		progress = (ProgressBar) activity
				.findViewById(R.id.place_view_progress);

		issues = (ViewGroup) activity.findViewById(R.id.place_view_issues);
		issuesCount = (TextView) activity
				.findViewById(R.id.place_view_issues_count);
		childrenIssuesCount = (TextView) activity
				.findViewById(R.id.place_view_children_issues_count);

		tasks = (ViewGroup) activity.findViewById(R.id.place_view_tasks);
		tasksCount = (TextView) activity
				.findViewById(R.id.place_view_tasks_count);
		childrenTasksCount = (TextView) activity
				.findViewById(R.id.place_view_children_tasks_count);

		description = (TextView) activity
				.findViewById(R.id.place_view_description);

		attributesLabels = (ViewGroup) activity
				.findViewById(R.id.place_view_attributes_labels);
		attributesValues = (ViewGroup) activity
				.findViewById(R.id.place_view_attributes_values);

		childrenLabel = (TextView) activity
				.findViewById(R.id.place_view_children_label);
		childrenList = (ListView) activity
				.findViewById(R.id.place_view_children_list);
		placeAdapter = new PlaceAdapter(activity, R.layout.place_list_item,
				null);
		childrenList.setAdapter(placeAdapter);
		childrenList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// cancel loading of previous image
				if (imageLoader != null)
					imageLoader.cancel(true);

				loadPlace((Place) view.getTag());
			}
		});

		redBorder = getResources()
				.getDrawable(R.drawable.white_with_red_border);
		wineBorder = getResources().getDrawable(
				R.drawable.white_with_wine_border);

		issues.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.getIssuesFragment().setSelectedPlace(activity.getCurrentPlace());
				activity.switchFragment(activity.getIssuesFragment());
			}
		});

		tasks.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.getTasksFragment().setSelectedPlace(activity.getCurrentPlace());
				activity.switchFragment(activity.getTasksFragment());
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.place_viewer, menu);

		upItem = menu.findItem(R.id.action_place_viewer_up);

		if (first) {
			init();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	// This function must be called only once after both onActivityCreated and
	// onCreateOptionMenu
	private void init() {
		// loadPlace needs that the header is already placed in the layout
		header.post(new Runnable() {
			@Override
			public void run() {
				loadPlace(activity.getCurrentPlace());
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_place_viewer_up:
			loadPlace(activity.getCurrentPlace().getParent());
			break;
		case R.id.action_place_viewer_refresh:
			activity.sendRootPlaceRequest();
			break;
		default:
			return false;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	public void loadPlace(Place place) {
		if (place == null)
			throw new IllegalArgumentException("place cannot be null");

		activity.setCurrentPlace(place);

		if (place.getParent() == null)
			upItem.setVisible(false);
		else
			upItem.setVisible(true);

		// set ancestors string
		String ancestorsString = "";
		for (Place p = place; p != null; p = p.getParent())
			ancestorsString = p.getName() + " > " + ancestorsString;
		ancestors.setText(ancestorsString.substring(0,
				ancestorsString.length() - 3));

		// load photo
		header.setBackgroundColor(getResources().getColor(R.color.wine_light));
		if (place.getPhoto() != null) {
			final String imageUrl = activity.getServer().getUrl()
					+ String.format(Locale.US, VineyardServer.PHOTO_API,
							place.getPhoto(), header.getMeasuredWidth(),
							header.getMeasuredHeight());
			imageLoader = new ImageLoader(activity, header, progress, imageUrl);
			imageLoader.execute();
		}

		int c;

		// set issues count
		c = place.getIssuesCount();
		issuesCount.setText(String.valueOf(c));
		childrenIssuesCount.setText("(" + (place.getChildrenIssuesCount() - c)
				+ ")");
		if (c != 0)
			issues.setBackgroundDrawable(redBorder);
		else
			issues.setBackgroundDrawable(wineBorder);

		// set tasks count
		c = place.getTasksCount();
		tasksCount.setText(String.valueOf(c));
		childrenTasksCount.setText("(" + (place.getChildrenTasksCount() - c)
				+ ")");
		if (c != 0)
			tasks.setBackgroundDrawable(redBorder);
		else
			tasks.setBackgroundDrawable(wineBorder);

		// set description
		description.setText(place.getDescription());

		// set attributes
		attributesLabels.removeAllViews();
		attributesValues.removeAllViews();
		HashMap<String, String> attributes = place.getAttributes();
		if (attributes.size() != 0)
			for (String key : place.getAttributes().keySet()) {
				TextView t = new TextView(activity);
				t.setTypeface(null, Typeface.BOLD_ITALIC);
				t.setText(key + ":");
				attributesLabels.addView(t);

				t = new TextView(activity);
				t.setText(attributes.get(key));
				attributesValues.addView(t);
			}

		// setChildren
		List<Place> children = place.getChildren();
		if (children.size() != 0) {
			placeAdapter.replaceItems(children);
			Util.fixListHeight(childrenList);
			showChildren(true);
		} else
			showChildren(false);

	}

	private void showChildren(boolean show) {
		final int visibility = show ? View.VISIBLE : View.GONE;

		childrenLabel.setVisibility(visibility);
		childrenList.setVisibility(visibility);
	}

}
