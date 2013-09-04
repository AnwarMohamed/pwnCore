package com.anwarelmakrahy.pwncore.fragments;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
