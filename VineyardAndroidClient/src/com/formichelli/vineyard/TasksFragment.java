package com.formichelli.vineyard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.formichelli.vineyard.entities.IssueTask;
import com.formichelli.vineyard.entities.Place;
import com.formichelli.vineyard.entities.SimpleTask;
import com.formichelli.vineyard.utilities.IssueExpandableAdapter;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;
import com.tyczj.extendedcalendarview.ExtendedCalendarView.OnDayClickListener;

public class TasksFragment extends Fragment {
	private static final String TAG = "TasksFragment";
	VineyardMainActivity activity;
	Place selectedPlace;
	
	ExpandableListView agendaListView;
	ExtendedCalendarView calendarView;
	SparseArray<IssueTask> tasks = new SparseArray<IssueTask>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_tasks, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (VineyardMainActivity) getActivity();
		populateCalendarProvider();
		
		calendarView = (ExtendedCalendarView) activity.findViewById(R.id.calendar);
		agendaListView = (ExpandableListView) activity.findViewById(R.id.daily_agenda);
		
		calendarView.setOnDayClickListener(onDayClickListener);
	}

	public void populateCalendarProvider() {
		String issuesAndTasksJSON = activity.getCache().getIssuesAndTasks();

		if (issuesAndTasksJSON == null) {
			Log.e(TAG,
					"issuesAndTasks not available neither from server nor from sharedPreference");
			return;
		}
		
		// clean content provider table
		activity.getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);

		try {
			JSONArray issuesAndTasks = new JSONArray(issuesAndTasksJSON);
			for (int i = 0, l = issuesAndTasks.length(); i < l; i++) {

				JSONObject object = issuesAndTasks.getJSONObject(i);
				IssueTask task = new IssueTask(object);
				tasks.append(task.getId(), task);
				
				ContentValues values = generateContentValuesFromTask(task);
				
				activity.getContentResolver().insert(
						CalendarProvider.CONTENT_URI, values);
			}

		} catch (JSONException e) {
			Log.e(TAG, "invalid JSON");
		}
	}
	
	private ContentValues generateContentValuesFromTask(SimpleTask task) {
		ContentValues values = new ContentValues();
		Calendar cal = Calendar.getInstance();
		TimeZone tz = TimeZone.getDefault();
		
		String location = task.getLatitude() + ", "
				+ task.getLongitude();
		
		values.put(CalendarProvider.DESCRIPTION, task.getTitle());
		values.put(CalendarProvider.LOCATION, location);
		values.put(CalendarProvider.EVENT, task.getId());
		
		int color = Event.DEFAULT_EVENT_ICON;
		
		if (task.getPriority() != null) {
		
			switch (task.getPriority()) {
			case HIGH:
				color = Event.COLOR_RED;
				break;
			case LOW:
				color = Event.COLOR_GREEN;
				break;
			case MEDIUM:
				color = Event.COLOR_YELLOW;
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
			
			// TODO IssueExpandableAdapter is used,
			// but should be better to write a new Adapter, good for both
			// Tasks and Issues.
			// So far, I'm instantiating IssueTask either for SimpleTask JSON
			// and some quirks on JSON parsing are occurring.
			
			ArrayList<IssueTask> issues = new ArrayList<IssueTask>();
			
			for (Event e: day.getEvents())
				issues.add(tasks.get(Integer.valueOf(e.getTitle()))); // title contains issue ID
			
			IssueExpandableAdapter issueAdapter = new IssueExpandableAdapter(activity,
					R.layout.issues_list_item, R.layout.issue_view, issues,
					null, null, null);
			
			agendaListView.setAdapter(issueAdapter);			
		}
		
	};

}
