package com.formichelli.vineyard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class TasksFragment extends Fragment {

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
    	//showGlobalContextActionBar();

        super.onCreateOptionsMenu(menu, inflater);
    }
}
