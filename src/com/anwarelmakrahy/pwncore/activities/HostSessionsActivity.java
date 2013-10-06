package com.anwarelmakrahy.pwncore.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.console.ControlSession;
import com.anwarelmakrahy.pwncore.structures.HostItem;
import com.anwarelmakrahy.pwncore.structures.SessionCommand;
import com.anwarelmakrahy.pwncore.structures.SessionCommandsAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnActionExpandListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class HostSessionsActivity extends Activity implements OnQueryTextListener {

	private HostItem host;
	private String currentSessionId;
	private ControlSession session;
	private ListView sessionsListview, sessionCmdsListview;
	private ArrayAdapter<String> sessionsAdapter;
	private SessionCommandsAdapter commandsAdapter;

	private ArrayList<String> sessions = new ArrayList<String>();
	private Map<String, SessionCommand> commands = new HashMap<String, SessionCommand>();

	private static final int BAD_ID = 9999;
	private int currentPosition = 0;
	private Menu optionsMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		int hostId;

		if (!intent.hasExtra("hostId")
				|| (hostId = intent.getIntExtra("hostId", BAD_ID)) == BAD_ID
				|| MainService.hostsList.size() <= hostId
				|| (host = MainService.hostsList.get(hostId)) == null
				|| host.getActiveSessions().get("shell").size()
						+ host.getActiveSessions().get("meterpreter").size() == 0) {
			finish();
			return;
		}

		setContentView(R.layout.activity_hostsessions);

		((TextView) findViewById(R.id.sessionHost)).setText("Pwned Host: "
				+ host.getHost());

		sessionsListview = (ListView) findViewById(R.id.sessionList);
		sessionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.session_item, sessions);
		sessionsListview.setAdapter(sessionsAdapter);
		sessionsListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long itemId) {
				loadSession(position);
				invalidateOptionsMenu();
			}
		});
		setupSessionsList();

		sessionCmdsListview = (ListView) findViewById(R.id.sessionCommandsList);
		commandsAdapter = new SessionCommandsAdapter(getApplicationContext(), commands);
		sessionCmdsListview.setAdapter(commandsAdapter);
		sessionCmdsListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long itemId) {
				session.processVisualCommand(position);
			}
		});
	}

	private void setupOptionsMenu(boolean check) {
		optionsMenu.findItem(R.id.mnuSessionTerminate).setVisible(check);
		optionsMenu.findItem(R.id.mnuSessionReloadCmds).setVisible(check);
	}
	
	private void setupSessionsList() {
		sessions.clear();
		String sessionId;
		ControlSession session;
		for (int i = 0; i < host.getActiveSessions().get("meterpreter").size(); i++) {
			sessionId = host.getActiveSessions().get("meterpreter").get(i);
			if ((session = MainService.sessionMgr.getSession(sessionId)) != null)
				sessions.add("[" + sessionId + "] Meterpreter @ "
						+ session.getViaPayload());
		}

		for (int i = 0; i < host.getActiveSessions().get("shell").size(); i++) {
			sessionId = host.getActiveSessions().get("shell").get(i);
			if ((session = MainService.sessionMgr.getSession(sessionId)) != null)
				sessions.add("[" + sessionId + "] Shell @ "
						+ session.getViaPayload());
		}
		sessionsAdapter.notifyDataSetChanged();
	}

	private MenuItem mnuSearch;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sessions, menu);
		this.optionsMenu = menu;
		
		mnuSearch = menu.findItem(R.id.mnuSearch);
		SearchView searchView = (SearchView) mnuSearch.getActionView();
		searchView.setOnQueryTextListener(this);
		mnuSearch.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (commandsAdapter != null)
					commandsAdapter.getFilter().filter("");
				return true;
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}
		});
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (session == null)
			setupOptionsMenu(false);
		else setupOptionsMenu(true);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuSessionTerminate:
			if (session != null) {
				MainService.sessionMgr.destroySession(session.getId());
				setupSessionsList();
				currentSessionId = null;
				commands.clear();
				commandsAdapter.notifyDataSetChanged();
				
				((TextView) findViewById(R.id.sessionDetails)).setText("Please select session from list");
			}
			
			if (sessions.size() == 0)
				finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadSession(int id) {
		if (sessions.size() == 0)
			finish();

		String sessionId;
		if (id < sessions.size()
				&& (sessionId = sessions.get(id).split(" ")[0].replace("]", "")
						.replace("[", "")) != null
				&& (session = MainService.sessionMgr.getSession(sessionId)) != null) {

			((TextView) findViewById(R.id.sessionDetails)).setText(WordUtils
					.capitalizeFully(session.getType())
					+ " @ "
					+ session.getPeer());

			setSessionCheckbox(currentPosition, false);
			setSessionCheckbox(id, true);
			currentPosition = id;
			currentSessionId = sessionId;

			commands.clear();
			session.updateImplementedCommands();
			commands.putAll(session.getAllCommands());
			commandsAdapter.notifyDataSetChanged();
		}
	}

	private void setSessionCheckbox(int pos, boolean check) {
		int wantedChild = pos
				- (sessionsListview.getFirstVisiblePosition() - sessionsListview
						.getHeaderViewsCount());
		if (wantedChild >= 0 && wantedChild < sessionsListview.getChildCount()) {
			CheckedTextView selectedSession = (CheckedTextView) sessionsListview
					.getChildAt(wantedChild);
			selectedSession.setChecked(check);
		}
	}

	@Override
	public boolean onQueryTextChange(String text) {
		if (commandsAdapter != null)
			commandsAdapter.getFilter().filter(text);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String text) {
		if (commandsAdapter != null)
			commandsAdapter.getFilter().filter(text);
		return true;
	}
}
