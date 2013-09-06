package com.anwarelmakrahy.pwncore.fragments;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class JobsFragment extends Fragment {
	
	private ListView listview = null;
	public static ArrayAdapter<String> listadapter = null;
	
	public static final JobsFragment newInstance() {
		JobsFragment fragment = new JobsFragment();
		return fragment;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_jobs, container, false);
	
		listview = (ListView)view.findViewById(R.id.jobsListView);
		listview.setEmptyView(view.findViewById(R.id.noJobs));
		listadapter = new ArrayAdapter<String>(
				getActivity(), 
				R.layout.payload_item, 
				MainService.sessionMgr.jobsList);	
		listview.setAdapter(listadapter);

		return view;
	}
	 

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(listview);
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser &&
				listadapter != null) {
			MainService.sessionMgr.updateJobsList();
			listadapter.notifyDataSetChanged();
		}
	}
	
}
