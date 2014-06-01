package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;

import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.entities.Task;
import com.formichelli.vineyard.entities.WorkGroup;
import com.formichelli.vineyard.entities.Worker;
import com.formichelli.vineyard.utilities.AsyncHttpRequest;
import com.formichelli.vineyard.utilities.TaskExpandableAdapter;
import com.formichelli.vineyard.utilities.VineyardServer;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.ExtendedCalendarView.OnDayClickListener;

public class TasksFragment extends Fragment {
	VineyardMainActivity activity;

	ExpandableListView tasksListView;
	TaskExpandableAdapter<SimpleTask> taskAdapter;
	ExtendedCalendarView calendarView;
	SparseArray<SimpleTask> tasks = new SparseArray<SimpleTask>();
	boolean calendarMode, first, showMine;
	MenuItem showMode, viewMode;
	String showAllLabel, showMineLabel;
	Day currentDay;
	List<SimpleTask> tasksList;
	Drawable viewModeCalendarIcon, viewModeListIcon;
	Place selectedPlace;
	SimpleTask selectedTask;

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
		calendarMode = true;

		activity = (VineyardMainActivity) getActivity();
		populateCalendarProvider();

		calendarView = (ExtendedCalendarView) activity
				.findViewById(R.id.tasks_calendar);
		calendarView.setOnDayClickListener(onDayClickListener);

		tasks = activity.getTasks();
		tasksList = new ArrayList<SimpleTask>();
		for (int i = 0, l = tasks.size(); i < l; i++)
			tasksList.add(tasks.valueAt(i));
		taskAdapter = new TaskExpandableAdapter<SimpleTask>(activity,
				R.layout.issues_list_item, R.layout.issue_view, tasksList,
				false, true, null, null, doneOnClickListener);
		tasksListView = (ExpandableListView) activity
				.findViewById(R.id.tasks_list);
		tasksListView.setAdapter(taskAdapter);

		showAllLabel = getString(R.string.issue_view_all);
		showMineLabel = getString(R.string.issue_view_mine);
		viewModeCalendarIcon = activity.getResources().getDrawable(
				R.drawable.action_task_calendar);
		viewModeListIcon = activity.getResources().getDrawable(
				R.drawable.action_task_list);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tasks, menu);

		showMode = menu.findItem(R.id.action_task_view_all_mine);
		viewMode = menu.findItem(R.id.action_task_view_mode);

		if (first) {
			// loadData() must be called just once after that both
			// onActivityCreated
			// and onCreateOptionMenu are called
			loadData();
			first = false;
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_task_view_all_mine:
			showMine = !showMine;
			showMode.setTitle(showMine ? showAllLabel : showMineLabel);
			break;

		case R.id.action_task_view_mode:
			calendarMode = !calendarMode;
			showMode.setVisible(false);
			showMine = false;
			currentDay = null;
			viewMode.setIcon(calendarMode ? viewModeListIcon
					: viewModeCalendarIcon);
			break;
		default:
			return false;
		}
		loadData();
		return true;
	}

	public void loadData() {
		List<SimpleTask> tasks;
		final boolean showAllTasks = selectedPlace == null;

		if (showAllTasks) {
			viewMode.setVisible(true);

			// show tasks of all places
			activity.setTitle(activity
					.getString(R.string.title_tasks_fragment_all));
			taskAdapter.setShowPlace(true);

			if (calendarMode) {
				// show calendar
				showMode.setVisible(false);
				calendarView.setVisibility(View.VISIBLE);
				tasksListView.setVisibility(View.GONE);
				return;
			}

			if (currentDay != null)
				tasks = getTasksOfTheDay(currentDay);
			else
				tasks = tasksList;
		} else {
			// show tasks of the selected place only
			activity.setTitle(String.format(
					activity.getString(R.string.title_tasks_fragment),
					selectedPlace.getName()));
			viewMode.setVisible(false);
			taskAdapter.setShowPlace(false);

			tasks = selectedPlace.getTasks();
		}

		// show tasks for the selected day
		showMode.setVisible(true);
		calendarView.setVisibility(View.GONE);
		tasksListView.setVisibility(View.VISIBLE);

		if (showMine) {
			int userId = activity.getUserId();

			List<SimpleTask> myTasksList = new ArrayList<SimpleTask>();
			for (SimpleTask task : tasks) {
				Worker assignedWorker = task.getAssignedWorker();
				if (assignedWorker != null && assignedWorker.getId() == userId)
					myTasksList.add(task);
				else {
					WorkGroup group = task.getAssignedGroup();
					if (group != null)
						for (Worker worker : group.getWorkers())
							if (worker.getId() == userId) {
								myTasksList.add(task);
								break;
							}
				}
			}

			tasks = myTasksList;
		}

		taskAdapter.replaceItems(tasks);

		if (taskAdapter.getGroupCount() > 0)
			tasksListView.setVisibility(View.VISIBLE);
		else
			tasksListView.setVisibility(View.GONE);

		if (selectedTask != null) {
			// used in case of notification
			tasksListView.expandGroup(tasks.indexOf(selectedTask));
			selectedTask = null;
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
		int color;
		ContentValues values = new ContentValues();
		Calendar cal = Calendar.getInstance();
		TimeZone tz = TimeZone.getDefault();

		String location = task.getLatitude() + ", " + task.getLongitude();

		values.put(CalendarProvider.DESCRIPTION, task.getTitle());
		values.put(CalendarProvider.LOCATION, location);
		values.put(CalendarProvider.EVENT, task.getId());

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
			default:
				color = Event.COLOR_PURPLE;
				break;
			}
		}
		else
			color = Event.COLOR_PURPLE;

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

	public SimpleTask getSelectedTask() {
		return selectedTask;
	}

	public void setSelectedTask(SimpleTask selectedTask) {
		this.selectedTask = selectedTask;
	}

	private OnDayClickListener onDayClickListener = new OnDayClickListener() {
		@Override
		public void onDayClicked(AdapterView<?> adapter, View view,
				int position, long id, Day day) {
			// change view only if there is at least one task
			if (getTasksOfTheDay(day).size() != 0) {
				calendarMode = false;
				currentDay = day;
				viewMode.setIcon(viewModeCalendarIcon);
				showMode.setVisible(true);
				loadData();
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
			calendarMode = true;
			viewMode.setIcon(calendarMode ? viewModeListIcon
					: viewModeCalendarIcon);
			currentDay = null;
			showMine = false;
			showMode.setTitle(showMine ? showAllLabel : showMineLabel);
			showMode.setVisible(false);
			return true;
		}

		return false;
	}

	OnClickListener doneOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			new AsyncMarkTaskAsDone(activity.getServer().getUrl(),
					(SimpleTask) v.getTag()).execute();
		}
	};

	/*
	 * Sends a PUT request to the server to mark a task as solved. During the
	 * loading the fragment loadingFragment will be displayed. At the end of the
	 * execution the issue will be removed from the list. If something goes
	 * wrong the issue will not be removed ad a toast will be displayed
	 */
	private class AsyncMarkTaskAsDone extends AsyncHttpRequest {
		private final static String TAG = "AsyncMarkIssueAsDone";
		SimpleTask task;

		public AsyncMarkTaskAsDone(String serverUrl, SimpleTask task) {
			super(serverUrl + VineyardServer.TASKS_API + task.getId(),
					AsyncHttpRequest.Type.PUT);

			this.task = task;

			addParam(new BasicNameValuePair(SimpleTask.MODIFIER,
					String.valueOf(activity.getUserId())));
			addParam(new BasicNameValuePair(SimpleTask.STATUS,
					Task.Status.RESOLVED.toString()));
		}

		@Override
		protected void onPreExecute() {
			activity.getLoadingFragment().setLoadingMessage(
					getString(R.string.loading_sending_request));
			activity.switchFragment(activity.getLoadingFragment());
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			if (response != null && response.first == HttpStatus.SC_ACCEPTED)
				task.getPlace().removeTask(task);
			else {
				Log.e(TAG, response.first + ": " + response.second);
				Toast.makeText(activity,
						activity.getString(R.string.issue_mark_done_error),
						Toast.LENGTH_SHORT).show();
			}

			activity.switchFragment();
		}
	}
}
