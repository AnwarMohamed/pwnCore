package com.anwarelmakrahy.pwncore.fragments;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.activities.AttackHallActivity;
import com.anwarelmakrahy.pwncore.structures.TargetsListAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TargetsFragment extends Fragment {
	
	public static ListView listview;
	public static TargetsListAdapter listAdapter;
	private SharedPreferences prefs;

	public static final TargetsFragment newInstance() {
		TargetsFragment fragment = new TargetsFragment();
		return fragment;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_targets, container, false);
		 
		listview = (ListView)view.findViewById(R.id.targetsFragmentListView);
		listAdapter = new TargetsListAdapter(
				getActivity().getApplicationContext(), 
				MainService.mTargetHostList);
		
		listview.setAdapter(listAdapter);
		
		if (AttackHallActivity.getCurListPosition() + 1 > MainService.mTargetHostList.size())
			AttackHallActivity.setCurListPosition(0);
		
		listAdapter.setSelectedIndex(AttackHallActivity.getCurListPosition());

		setupListViewListener();
		return view;
	}
	
	private void setupListViewListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 	        	
	        	prefs.edit().putString("target_id", Integer.toString(position)).commit();      	
        		listAdapter.setSelectedIndex(position);
        		AttackHallActivity.pager.setCurrentItem(1);
	        }
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		prefs = getActivity().getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
		prefs.edit().putString("target_id", "0").commit();
		registerForContextMenu(listview);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser &&
				listAdapter != null)
			listAdapter.notifyDataSetChanged();
	}
}
