package com.anwarelmakrahy.pwncore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TargetsFragment extends Fragment implements OnFocusChangeListener  {
	
	public static ListView mTargetsListView;
	public static TargetsListAdapter mTargetsListAdapter;
	

	public static final TargetsFragment newInstance() {
		TargetsFragment fragment = new TargetsFragment();
		return fragment;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_targets, container, false);
		 
		mTargetsListView = (ListView)view.findViewById(R.id.targetsFragmentListView);
		mTargetsListAdapter = new TargetsListAdapter(
				getActivity().getApplicationContext(), 
				MainService.mTargetHostList);
		
		mTargetsListView.setAdapter(mTargetsListAdapter);
		
		if (AttackHallActivity.getCurListPosition() + 1 > MainService.mTargetHostList.size())
			AttackHallActivity.setCurListPosition(0);
		
		mTargetsListAdapter.setSelectedIndex(AttackHallActivity.getCurListPosition());

		setupListViewListener();
		return view;
	}
	
	private void setupListViewListener() {
		mTargetsListView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 	        	
	        	//prepareTargetDetails(position);	        	
        		mTargetsListAdapter.setSelectedIndex(position);
        		AttackHallActivity.pager.setCurrentItem(1);
	        }
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(mTargetsListView);
	}

	@Override
	public void onFocusChange(View arg0, boolean hasFocus) {
		if (hasFocus &&
				mTargetsListAdapter != null)
			mTargetsListAdapter.notifyDataSetChanged();
	}
}
