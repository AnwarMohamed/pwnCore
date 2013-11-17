package com.anwarelmakrahy.pwncore.fragments;

import java.util.ArrayList;
import java.util.List;

import com.anwarelmakrahy.pwncore.MainActivity;
import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.activities.AttackHallActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ConsolesFragment extends Fragment {

	private ListView listview = null;
	public static ArrayAdapter<String> listAdapter = null;
	public static List<String> consoleArray = new ArrayList<String>();

	public static void UpdateConsoleRecords() {

		if (listAdapter != null && AttackHallActivity.getActivity() != null) {
			
			AttackHallActivity.getActivity().runOnUiThread(new Runnable() {  
                @Override
                public void run() {
    				consoleArray.clear();
    				consoleArray.addAll(MainService.sessionMgr.getConsoleListArray());
    				listAdapter.notifyDataSetChanged();
                }
            });
		}
	}
	
	public static final ConsolesFragment newInstance() {
		ConsolesFragment fragment = new ConsolesFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_consoles, container,
				false);

		listview = (ListView) view.findViewById(R.id.targetsConsolesListView);
		listview.setEmptyView(view.findViewById(R.id.noConsoles));
		consoleArray = MainService.sessionMgr.getConsoleListArray();
		listAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.payload_item, consoleArray);
		listview.setAdapter(listAdapter);

		setupListViewListener();
		return view;
	}

	private void setupListViewListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String consoleId = consoleArray.get(position).split(" ")[0]
						.replace("]", "").replace("[", "");

				ConsoleSession tmpConsole = MainService.sessionMgr
						.getConsole(consoleId);
				if (tmpConsole != null && tmpConsole.isWindowReady) {
					Intent intent = new Intent(getActivity()
							.getApplicationContext(), ConsoleActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("type", "current.console");
					intent.putExtra(
							"id",
							consoleArray.get(position).split(" ")[0].replace(
									"]", "").replace("[", ""));
					startActivity(intent);
				} else {
					Toast.makeText(getActivity(), "Internal Console",
							Toast.LENGTH_SHORT).show();
				}
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
		if (isVisibleToUser && listAdapter != null) {
			consoleArray.clear();
			consoleArray.addAll(MainService.sessionMgr.getConsoleListArray());
			listAdapter.notifyDataSetChanged();
		}
	}
}
