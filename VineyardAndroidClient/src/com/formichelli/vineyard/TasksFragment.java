package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Worker;
import com.formichelli.vineyard.utilities.TaskExpandableAdapter;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.ExtendedCalendarView.OnDayClickListener;

public class TasksFragment extends Fragment {
	VineyardMainActivity activity;
	Place selectedPlace;

	ExpandableListView tasksListView;
	TaskExpandableAdapter<SimpleTask> taskAdapter;
	ExtendedCalendarView calendarView;
	SparseArray<IssueTask> tasks = new SparseArray<IssueTask>();
	boolean first, showMine;
	MenuItem showMode;
	String showAllLabel, showMineLabel;
	Day currentDay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_tasks, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		first = true;
		showMine = false;

		activity = (VineyardMainActivity) getActivity();
		populateCalendarProvider();

		calendarView = (ExtendedCalendarView) activity
				.findViewById(R.id.tasks_calendar);
		calendarView.setOnDayClickListener(onDayClickListener);

		taskAdapter = new TaskExpandableAdapter<SimpleTask>(activity,
				R.layout.issues_list_item, R.layout.issue_view, null, false, true,
				null, null, null);
		tasksListView = (ExpandableListView) activity
				.findViewById(R.id.tasks_list);
		tasksListView.setAdapter(taskAdapter);

		showAllLabel = getString(R.string.issue_view_all);
		showMineLabel = getString(R.string.issue_view_mine);


	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tasks, menu);

		showMode = menu.findItem(R.id.action_task_view_mode);

		if (first) {
			// loadData() must be called just once after that both onActivityCreated
			// and onCreateOptionMenu are called
			loadData();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_task_view_mode) {
			showMine = !showMine;
			showMode.setTitle(showMine ? showAllLabel : showMineLabel);
			taskAdapter.replaceItems(getTasksOfTheDay(currentDay));
			return true;
		}

		return false;
	}

	public void loadData() {

		showMode.setVisible(false);
		showMine = false;

		final boolean showAllTasks = selectedPlace == null;

		if (showAllTasks) {
			activity.setTitle(activity
					.getString(R.string.title_tasks_fragment_all));
			calendarView.setVisibility(View.VISIBLE);
			
			taskAdapter.setShowPlace(true);
			
			tasksListView.setVisibility(View.GONE);
		} else {
			activity.setTitle(String.format(
					activity.getString(R.string.title_tasks_fragment),
					selectedPlace.getName()));
			calendarView.setVisibility(View.GONE);
			
			taskAdapter.replaceItems(selectedPlace.getTasks());
			taskAdapter.setShowPlace(false);
			
			if (taskAdapter.getGroupCount() > 0)
				tasksListView.setVisibility(View.VISIBLE);
			else
				tasksListView.setVisibility(View.GONE);
		}

	}

	public void populateCalendarProvider() {
		// clean content provider table
		activity.getContentResolver().delete(CalendarProvider.CONTENT_URI,
				null, null);

		SparseArray<SimpleTask> tasks = activity.getTasks();
		for (int i = 0, l = tasks.size(); i < l; i++) {
			final ContentValues values = generateContentValuesFromTask(tasks
					.valueAt(i));

			activity.getContentResolver().insert(CalendarProvider.CONTENT_URI,
					values);
		}

	}

	private ContentValues generateContentValuesFromTask(SimpleTask task) {
		ContentValues values = new ContentValues();
		Calendar cal = Calendar.getInstance();
		TimeZone tz = TimeZone.getDefault();

		String location = task.getLatitude() + ", " + task.getLongitude();

		values.put(CalendarProvider.DESCRIPTION, task.getTitle());
		values.put(CalendarProvider.LOCATION, location);
		values.put(CalendarProvider.EVENT, task.getId());

		int color = Event.DEFAULT_EVENT_ICON;

		if (task.getPriority() != null) {

			switch (task.getPriority()) {
			case LOW:
				color = Event.COLOR_GREEN;
				break;
			case MEDIUM:
				color = Event.COLOR_YELLOW;
				break;
			case HIGH:
				color = Event.COLOR_RED;
				break;
			}
		}

		values.put(CalendarProvider.COLOR, color);

		Date eventTime;

		if (task.getDueTime() != null)
			eventTime = task.getDueTime();
		else if (task.getAssignTime() != null)
			eventTime = task.getAssignTime();
		else
			eventTime = task.getCreateTime();

		cal.setTime(eventTime);

		// No multi-day event, START = END
		values.put(CalendarProvider.START, cal.getTimeInMillis());
		values.put(CalendarProvider.END, cal.getTimeInMillis());

		// julianDay = cal.get(Calendar.DAY_OF_YEAR)
		int startDayJulian = Time.getJulianDay(cal.getTimeInMillis(),
				TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal
						.getTimeInMillis())));
		values.put(CalendarProvider.START_DAY, startDayJulian);
		values.put(CalendarProvider.END_DAY, startDayJulian);

		return values;
	}

	public Place getSelectedPlace() {
		return selectedPlace;
	}

	public void setSelectedPlace(Place selectedPlace) {
		this.selectedPlace = selectedPlace;
	}

	private OnDayClickListener onDayClickListener = new OnDayClickListener() {
		@Override
		public void onDayClicked(AdapterView<?> adapter, View view,
				int position, long id, Day day) {
			currentDay = day;

			ArrayList<SimpleTask> tasks = getTasksOfTheDay(day);

			// change view only if there is at least one task
			if (tasks.size() != 0) {
				calendarView.setVisibility(View.GONE);
				tasksListView.setVisibility(View.VISIBLE);
				showMode.setVisible(true);
				taskAdapter.replaceItems(tasks);
			}
		}
	};

	private ArrayList<SimpleTask> getTasksOfTheDay(Day day) {
		SparseArray<SimpleTask> allTasks = activity.getTasks();
		ArrayList<SimpleTask> tasks = new ArrayList<SimpleTask>();
		int userId = activity.getUserId();

		for (Event e : day.getEvents()) {
			// title contains issue ID
			SimpleTask task = allTasks.get(Integer.valueOf(e.getTitle()));

			if (showMine) {
				// show the task only if it is assigned to him or to one of
				// his groups
				if (task.getAssignedWorker() != null
						&& task.getAssignedWorker().getId() == userId)
					tasks.add(task);
				else if (task.getAssignedGroup() != null) {
					for (Worker worker : task.getAssignedGroup().getWorkers())
						if (worker.getId() == userId) {
							tasks.add(task);
							break;
						}
				}
			} else
				tasks.add(task);
		}

		return tasks;
	}

	public boolean onBackPressed() {
		if (selectedPlace == null && calendarView.getVisibility() == View.GONE) {
			calendarView.setVisibility(View.VISIBLE);
			tasksListView.setVisibility(View.GONE);
			showMine = false;
			showMode.setTitle(showMine ? showAllLabel : showMineLabel);
			showMode.setVisible(false);
			return true;
		}

		return false;
	}
}
