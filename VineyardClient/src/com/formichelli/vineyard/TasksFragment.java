package com.formichelli.vineyard;

import com.formichelli.vineyard.entities.Place;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class TasksFragment extends Fragment {
	private Place place;

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_tasks, container, false);
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.menu_tasks, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
