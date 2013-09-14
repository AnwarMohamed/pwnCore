package com.anwarelmakrahy.pwncore.fragments;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.structures.HostItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

public class HostDetailsFragment extends Fragment {
	
	private SharedPreferences prefs;
	
	public static final HostDetailsFragment newInstance() {
		HostDetailsFragment fragment = new HostDetailsFragment();
		return fragment;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hostdetails, container, false);	 
		return view;
	}
	 
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		prefs = getActivity().getSharedPreferences(
				"com.anwarelmakrahy.pwncore", 
				Context.MODE_PRIVATE);
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			String id = prefs.getString("target_id", "0");
			HostItem item = MainService.hostsList.get(Integer.parseInt(id));
			
			if ((TextView)getActivity().findViewById(R.id.detailHost) != null) {
				
				((TextView)getActivity().findViewById(R.id.detailHost)).setText(item.getHost());
			
				if (!item.isUp())
					((ImageView)getActivity().findViewById(R.id.detailStatus)).
					setImageResource(R.drawable.circle_grey);
				else if (item.isPwned())
					((ImageView)getActivity().findViewById(R.id.detailStatus)).
					setImageResource(R.drawable.circle_green);
				else if (!item.isPwned())
					((ImageView)getActivity().findViewById(R.id.detailStatus)).
					setImageResource(R.drawable.circle_red);
				
				((TextView)getActivity().findViewById(R.id.detailOS)).setText("OS: " + item.getOS());
			}
		}
	}
}
