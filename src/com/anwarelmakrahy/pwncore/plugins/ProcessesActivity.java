package com.anwarelmakrahy.pwncore.plugins;

import java.util.ArrayList;
import java.util.List;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.activities.HostSessionsActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.console.ControlSession;
import com.anwarelmakrahy.pwncore.console.utils.AttackFinder;
import com.anwarelmakrahy.pwncore.fragments.ConsolesFragment;
import com.anwarelmakrahy.pwncore.fragments.HostsFragment;
import com.anwarelmakrahy.pwncore.structures.HostsAdapter;
import com.anwarelmakrahy.pwncore.structures.SessionCommandsAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnActionExpandListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class ProcessesActivity extends Activity implements OnQueryTextListener {

	private String sessionId;
	private ControlSession session;
	public static ProcessesAdapter processesAdapter;
	private ListView processesList;
	
	public static List<ProcessItem> processes = new ArrayList<ProcessItem>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if (!intent.hasExtra("sessionId")
				|| (sessionId = intent.getStringExtra("sessionId")) == null
				|| (session = MainService.sessionMgr.getSession(sessionId)) == null) {
			finish();
			return;
		}
		
		setContentView(R.layout.activity_processes);
		processesList = (ListView)findViewById(R.id.psListview);
		processesAdapter = new ProcessesAdapter(this, processes);
		processesList.setAdapter(processesAdapter);
		processesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long itemId) {
				openContextMenu(view);
			}
		});
		
		registerForContextMenu(processesList);
		session.processVisualCommand("pslist", this);
	}

	private MenuItem mnuSearch;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.processes, menu);
		
		mnuSearch = menu.findItem(R.id.mnuSearch);
		SearchView searchView = (SearchView) mnuSearch.getActionView();
		searchView.setOnQueryTextListener(this);
		mnuSearch.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (processesAdapter != null)
					processesAdapter.getFilter().filter("");
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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuProcessesReload:
			session.processVisualCommand("pslist", this);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onQueryTextChange(String text) {
		if (processesAdapter != null)
			processesAdapter.getFilter().filter(text);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String text) {
		if (processesAdapter != null)
			processesAdapter.getFilter().filter(text);
		return true;
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.context_processes, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.mnuProcessKill:
			session.processVisualCommand("kill pid " + processes.get(info.position).getId(), this);
			return true;
			
		case R.id.mnuProcessMigrate:
			session.processVisualCommand("migrate " + processes.get(info.position).getId(), this);
			return true;

		default:
			return false;
		}
	}
	
	public static class ProcessItem {
		private String id, name, path;
		public void setId(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setPath(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
	}
}
