package com.anwarelmakrahy.pwncore;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TargetDetailsFragment extends Fragment {
	
	public static final TargetDetailsFragment newInstance() {
		TargetDetailsFragment fragment = new TargetDetailsFragment();
		return fragment;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_targetdetails, container, false);
		 
		return view;
	}
	 
}
