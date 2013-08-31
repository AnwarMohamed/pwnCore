package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ConsolesFragment extends Fragment {
	
	public static ListView mConsolesListView = null;
	public static ArrayAdapter<String> mConsolesListAdapter = null;
	public static List<String> consoleArray = new ArrayList<String>();
	
	public static final ConsolesFragment newInstance() {
		ConsolesFragment fragment = new ConsolesFragment();
		return fragment;
	}
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_consoles, container, false);
	
		mConsolesListView = (ListView)view.findViewById(R.id.targetsConsolesListView);
		consoleArray = MainService.sessionMgr.getConsoleListArray();
		mConsolesListAdapter = new ArrayAdapter<String>(getActivity(), 
				R.layout.payload_item, consoleArray);	
		mConsolesListView.setAdapter(mConsolesListAdapter);

		setupListViewListener();
		return view;
	}
	 
	private void setupListViewListener() {
		mConsolesListView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 	        	
	        	Intent intent = new Intent(getActivity().getApplicationContext(), ConsoleActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	intent.putExtra("type", "current");
	        	intent.putExtra("id", consoleArray.get(position).
	        			split(" ")[0].replace("]", "").replace("[", ""));
	        	startActivity(intent); 
	        }
		});
	}
	
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(mConsolesListView);
	}
}
