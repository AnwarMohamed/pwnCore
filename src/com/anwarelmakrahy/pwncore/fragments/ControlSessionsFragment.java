package com.anwarelmakrahy.pwncore.fragments;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;

import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.console.ControlSession;
import com.anwarelmakrahy.pwncore.structures.SessionsListAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ControlSessionsFragment extends Fragment {

	private ListView listview;
	public static SessionsListAdapter listAdapter;

	public static final ControlSessionsFragment newInstance() {
		ControlSessionsFragment fragment = new ControlSessionsFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sessions, container,
				false);

		listview = (ListView) view.findViewById(R.id.sessionsListView);
		listview.setEmptyView(view.findViewById(R.id.noSessions));
		listAdapter = new SessionsListAdapter(getActivity(),
				MainService.sessionMgr.controlSessionsList);

		listview.setAdapter(listAdapter);

		setupListViewListener();
		return view;
	}

	private void setupListViewListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ControlSession session = MainService.sessionMgr.controlSessionsList
						.get(position);
				Intent intent = new Intent(getActivity(), ConsoleActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("type", "current." + session.getType());
				intent.putExtra("id", session.getId());
				startActivity(intent);
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(listview);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser && listAdapter != null)
			listAdapter.notifyDataSetChanged();
	}
}
