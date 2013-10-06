package com.anwarelmakrahy.pwncore.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.structures.HostItem;
import com.anwarelmakrahy.pwncore.structures.HostsAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AttackWizardActivity extends Activity {
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attackwizard);

		prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore",
				Context.MODE_PRIVATE);

		listView = (ListView) findViewById(R.id.targetsListView1);
		hostsAdapter = new HostsAdapter(this, MainService.hostsList);
		listView.setAdapter(hostsAdapter);
		listView.setEmptyView(findViewById(R.id.consolePrompt));
		registerForContextMenu(listView);
		((TextView) findViewById(R.id.targetsCount)).setText("Current Hosts: "
				+ MainService.hostsList.size());

		progress = (ProgressBar) findViewById(R.id.progress2);
		setProgressBar(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.attackwizard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.mnuHostsManualFeed:
			showManualHostDlg();
			return true;
		case R.id.mnuImportHostsFile:
			showFileChooser();
			return true;
		case R.id.mnuClearHosts:
			MainService.hostsList.clear();
			hostsAdapter.notifyDataSetChanged();
			((TextView) findViewById(R.id.targetsCount))
					.setText("Current Hosts: 0");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private ListView listView;
	private HostsAdapter hostsAdapter;
	private boolean conStatusReceiverRegistered = false;
	private boolean isConnected = true;
	private SharedPreferences prefs;

	private void addHostToList(HostItem item) {
		for (int i = 0; i < MainService.hostsList.size(); i++)
			if (MainService.hostsList.get(i).getHost().equals(item.getHost())) {
				return;
			}

		MainService.hostsList.add(0, item);
		hostsAdapter.notifyDataSetChanged();
		item.scanPorts();
		((TextView) findViewById(R.id.targetsCount)).setText("Current Hosts: "
				+ MainService.hostsList.size());
	}

	private void removeHostFromList(int pos) {
		MainService.hostsList.remove(pos);
		hostsAdapter.notifyDataSetChanged();
		((TextView) findViewById(R.id.targetsCount)).setText("Current Hosts: "
				+ MainService.hostsList.size());
	}

	private void showManualHostDlg() {
		final EditText input = new EditText(this);
		input.setSingleLine(false);
		AlertDialog.Builder alert = new AlertDialog.Builder(this)
				.setMessage("Enter one host/line").setView(input)
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String[] hosts = input.getText().toString().split("\n");

						for (int i = 0; i < hosts.length; i++) {
							if (StaticClass.validateIPAddress(hosts[i], false)) {
								addHostToList(new HostItem(
										getApplicationContext(), hosts[i]));
							}
						}
					}
				});
		alert.show();
	}

	private static final int FILE_SELECT_CODE = 100;

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select a File"),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				try {
					String path = StaticClass.getPath(this, uri);
					File file = new File(path);
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					while ((line = br.readLine()) != null) {
						if (StaticClass.validateIPAddress(line, false)) {
							addHostToList(new HostItem(getApplicationContext(),
									line));
						}
					}
					br.close();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private static String[] host_contextmenu_titles = { "Change OS",
			"Remove Host" };

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v == listView) {
			menu.add(0, v.getId(), 0, host_contextmenu_titles[0]);
			menu.add(0, v.getId(), 0, host_contextmenu_titles[1]);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		if (item.getTitle().equals(host_contextmenu_titles[1])) {
			removeHostFromList(info.position);
		} else if (item.getTitle().equals(host_contextmenu_titles[0])) {
			AlertDialog builder = new AlertDialog.Builder(this)
					.setSingleChoiceItems(HostsAdapter.osTitles, -1,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									dialog.dismiss();
									MainService.hostsList.get(info.position)
											.setOS(HostsAdapter.osTitles[item]);
									hostsAdapter.notifyDataSetChanged();
								}
							}).create();
			builder.show();
		}

		return super.onContextItemSelected(item);
	}

	private void setProgressBar(boolean state) {
		if (state) {
			progress.setIndeterminate(true);
			progress.setVisibility(View.VISIBLE);
		} else {
			progress.setIndeterminate(false);
			progress.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(StaticClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticClass.PWNCORE_CONNECTION_LOST);
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}

		isConnected = prefs.getBoolean("isConnected", false);
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (conStatusReceiverRegistered) {
			unregisterReceiver(conStatusReceiver);
			conStatusReceiverRegistered = false;
		}
		super.onDestroy();
	}

	public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action == StaticClass.PWNCORE_CONNECTION_TIMEOUT) {
				Toast.makeText(
						getApplicationContext(),
						"ConnectionTimeout: Please check that server is running",
						Toast.LENGTH_SHORT).show();
			} else if (action == StaticClass.PWNCORE_CONNECTION_FAILED) {
				Toast.makeText(getApplicationContext(),
						"ConnectionFailed: " + intent.getStringExtra("error"),
						Toast.LENGTH_SHORT).show();
			} else if (action == StaticClass.PWNCORE_CONNECTION_LOST) {
				prefs.edit().putBoolean("isConnected", false).commit();
				Toast.makeText(getApplicationContext(),
						"ConnectionLost: Please check your network settings",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	};

	public void launchAttack(View v) {
		isConnected = prefs.getBoolean("isConnected", false);

		if (MainService.hostsList.size() == 0)
			Toast.makeText(getApplicationContext(), "You have no hosts",
					Toast.LENGTH_SHORT).show();

		else if (isConnected) {
			startActivity(new Intent(getApplicationContext(),
					AttackHallActivity.class));
			finish();
		} else
			Toast.makeText(getApplicationContext(),
					"NoConnection: Please check your connection",
					Toast.LENGTH_SHORT).show();
	}

}
