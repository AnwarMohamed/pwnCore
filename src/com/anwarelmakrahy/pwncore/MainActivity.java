package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.activities.AttackHallActivity;
import com.anwarelmakrahy.pwncore.activities.AttackWizardActivity;
import com.anwarelmakrahy.pwncore.activities.HostSessionsActivity;
import com.anwarelmakrahy.pwncore.activities.ModuleOptionsActivity;
import com.anwarelmakrahy.pwncore.activities.SettingsActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.structures.SidebarItem;
import com.anwarelmakrahy.pwncore.structures.SidebarAdapter;
import com.anwarelmakrahy.pwncore.structures.ModuleItem;
import com.anwarelmakrahy.pwncore.webserver.WebServer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnQueryTextListener {

	public static AlertDialog.Builder checkConDlgBuilder;
	private boolean conStatusReceiverRegistered = false;
	public Menu main_menu;
	private Intent serviceIntent;	
	private NotificationManager mNotificationManager;
	private Notification noti;
	public static SharedPreferences prefs;
	private Activity activity;
	private String 	con_txtUsername, 
					con_txtPassword,
					con_txtHost, 
					con_txtPort;
	
	public static boolean debug_mode = false;
    private ListView sidebarList;
    private SidebarAdapter sidebarAdapter;
    private ArrayList<SidebarItem> sidebarItems = new ArrayList<SidebarItem>();
    private DrawerLayout sidebarLayout;
    private ActionBarDrawerToggle sidebarToggle;	
    private ProgressDialog pd; 
    private ListView modulesList; 
    private WebServer webService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {  	
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_main);      
        activity = this;
        
        serviceIntent = new Intent(this, MainService.class);     
		startService(serviceIntent);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
		sidebarLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		sidebarToggle = new ActionBarDrawerToggle(
				this, 
				sidebarLayout, 
				R.drawable.ic_drawer, 
				R.string.drawer_open, 
				R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
		
        sidebarLayout.setDrawerListener(sidebarToggle);
		sidebarAdapter = new SidebarAdapter(this, sidebarItems);
        sidebarList = (ListView) findViewById(R.id.listview);
        sidebarList.setAdapter(sidebarAdapter); 
        sidebarList.setOnItemClickListener(new ModulesListItemClickListener()); 
        
        
		modulesList = (ListView)findViewById(R.id.modulesListView);
		modulesList.setEmptyView(findViewById(R.id.imageView11));
		modulesList.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 	
	        	if (isConnected && MainService.checkConnection(MainActivity.this)) {
		        	Object o = modulesList.getItemAtPosition(position);
		        	ModuleItem m = (ModuleItem)o;
		        	
		        	if (m.getType().contains("encoder") || 
		        			m.getType().contains("nop") || 
		        			m.getType().contains("post"))
		    			Toast.makeText(
		    					getApplicationContext(), 
		    					StaticClass.PWNCORE_NOT_IMPLEMENTED, 
		    					Toast.LENGTH_SHORT).show();
		        	else {
			        	Intent intent = new Intent(getApplicationContext(), ModuleOptionsActivity.class);
			        	intent.putExtra("type", m.getType());
			        	intent.putExtra("name", m.getPath());
			        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			        	getApplicationContext().startActivity(intent);        	
		        	}
		    	}
	        	else {
	    			Toast.makeText(
	    					getApplicationContext(), 
	    					R.string.pwncore_notconnected, 
	    					Toast.LENGTH_SHORT).show();
	        	}
	        }
		});
        
        prefs = getSharedPreferences(StaticClass.PWNCORE_PACKAGE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("isConnected", false).commit();
        loadSharedPreferences();
        
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}

		checkConDlgBuilder = new AlertDialog.Builder(this);
		checkConDlgBuilder
    	.setTitle("No Connectivity")
    	.setMessage("No Network connection! Do you want to try again ?")
    	.setIcon(android.R.drawable.ic_dialog_alert)
    	.setCancelable(false)
    	.setNeutralButton("Turn on Wifi or 3G", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {			      	
    	    	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    	    }
    	})
    	.setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	})
    	.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    			if (!MainService.checkConnection(getApplicationContext()))
    				checkConDlgBuilder.show();
    	    }
    	});
		
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		setNotification();
		
		prepareSidebar(); 	
    } 
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        sidebarToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sidebarToggle.onConfigurationChanged(newConfig);
    }
    
    private boolean titlesHas(String s) {
    	for (int i=0; i<titles.length; i++)
    		if (titles[i].equals(s))
    			return true;
    	return false;
    }
    
    private final String[] titles = { "Exploits", "Payloads", "Post", "Encoders", "Auxiliary", "Nops", "Plugins"};
    private void prepareSidebar() {	
    	
    	int[] icons = { R.drawable.shield, R.drawable.payload, R.drawable.post, R.drawable.encode, 
    			R.drawable.auxi, R.drawable.nop, R.drawable.plugin , R.drawable.icon, R.drawable.wizard,
    			R.drawable.gun, R.drawable.win };
    	
	   	SidebarItem item; 
	   	
    	for (int i=0; i<6; i++) {
    		
    	   	item = new SidebarItem();      	
    	   	item.setTitle(titles[i]);
    	   	item.setImage(icons[i]);
    	   	item.setCount(0);
        	sidebarItems.add(item);
    	}
    	
		if (sidebarAdapter != null)
			sidebarAdapter.notifyDataSetChanged();
    }
    

    private MenuItem mnuSearch;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.main_menu = menu;
        
        mnuSearch = menu.findItem(R.id.mnuSearch);
        SearchView searchView = (SearchView) mnuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        mnuSearch.setOnActionExpandListener(new OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
            	MainService.modulesMap.modulesAdapter.getFilter().filter("");
                return true;
            }

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}

        });
        
        return true;
    }
    
    public static boolean isConnected = false;
        
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem tmpItem = main_menu.findItem(R.id.mnuConnectionAction);
    	if (!isConnected) {
    		tmpItem.setIcon(R.drawable.plug);
    		tmpItem.setTitle(R.string.connect);	
    		menu.findItem(R.id.mnuConnection).setTitle(R.string.connect);
    	}
    	else {
    		tmpItem.setIcon(R.drawable.unplug);
    		tmpItem.setTitle(R.string.disconnect);
    		menu.findItem(R.id.mnuConnection).setTitle(R.string.disconnect);
    		menu.findItem(R.id.mnuNewConsole).setVisible(true);
    	} 	
    	
    	if (WebServer.RUNNING) 
    		menu.findItem(R.id.mnuWeb).setTitle("Stop WebService");
    	else
    		menu.findItem(R.id.mnuWeb).setTitle("Start WebService");
    	
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

    	if (sidebarToggle.onOptionsItemSelected(item)) {
            return true;
        }
    	
	    switch (item.getItemId()) {    

	    case R.id.mnuWeb:
	    	if (WebServer.RUNNING) {
	    		new Thread(new Runnable() {
					@Override
					public void run() {
			    		webService.stopServer();
			    		webService = null;
					}}).start();

		    	Toast.makeText(
		    			getApplicationContext(),
		    			"WebServer Stopped", 
		    			Toast.LENGTH_SHORT
		    			).show();
	    	}
	    	else {
	    		new Thread(new Runnable() {
					@Override
					public void run() {
			    		webService = new WebServer(
			    				getApplicationContext(), 
			    				prefs.getInt("webserver_port", 8080));
			    		webService.startServer();
					}}).start();

		    	Toast.makeText(
		    			getApplicationContext(),
		    			"WebServer Started", 
		    			Toast.LENGTH_SHORT
		    			).show();
	    	}
	    	return true;
	    case R.id.mnuRpcSettings:
	    	
	    	if (!isConnected) {
		    	Intent intent = new Intent(this, SettingsActivity.class);
		    	startActivity(intent);
	    	} else     		
	    		Toast.makeText(this, "You have to disconnect first", Toast.LENGTH_SHORT).show();
	    	
	    	return true;
	    	
	    case R.id.mnuAbout:
	    	String msgbox_string = 	"pwnCore v1.0b\n" + 
					"Android Cyber Attack Management tool for Metasploit\n\n" +
					"Anwar Mohamed\n" + 
					"anwarelmakrahy@gmail.com";

			AlertDialog dlg = new AlertDialog.Builder(this).create();
			dlg.setMessage(msgbox_string);
			dlg.setTitle(R.string.about_pwncore);
			dlg.setCancelable(true);
			dlg.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", (DialogInterface.OnClickListener) null);
			dlg.show();
	    	return true;
	    
	    case R.id.mnuConnection: case R.id.mnuConnectionAction:
	    	
	    	if (!isConnected) {
	    		if (checkConSettings()) {
	    			
	    			if (!MainService.checkConnection(getApplicationContext()))
	    				checkConDlgBuilder.show();
	    			else {
	    				
	    				showConnectingProgress();
	    				
		    			Intent tmpIntent = new Intent();
		    			tmpIntent.setAction(StaticClass.PWNCORE_CONNECT);
		    			sendBroadcast(tmpIntent);
	    			}
	    		}
	    	}
	    	else if (isConnected) {
    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticClass.PWNCORE_DISCONNECT);
    			sendBroadcast(tmpIntent);	
    			Disconnect();
    			
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionDisconnected: Disconnected from server", 
    					Toast.LENGTH_SHORT).show();
    			
    			setNotification();
	    	}
	    	return true;
	    //case R.id.mnuPlugins:
	    //	Intent intent = new Intent(this, PluginsActivity.class);
	    //	startActivity(intent);
	    //	return true;
	    	
	    case R.id.mnuExit:

	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder
	    	.setTitle(R.string.exit_pwncore)	
	    	.setMessage("Are you sure?")
	    	.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	finish();
	    	    }
	    	})
	    	.setNegativeButton("No", null)
	    	.setCancelable(false)
	    	.show();
	    	
	        return true;
	        
	    case R.id.mnuNewConsole:
	    	Intent intent1 = new Intent(getApplicationContext(), ConsoleActivity.class);
	    	intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent1.putExtra("type", "new.console");
	    	intent1.putExtra("cmd", "use multi/handler\n" +
	    					"set PAYLOAD windows/meterpreter/reverse_tcp\n" +
	    					"set LHOST " + con_txtHost + "\n" + 
	    					"exploit -z");
	    	startActivity(intent1);   
	    	
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
        
	private void showConnectingProgress() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {	

			@Override
			protected void onPreExecute() {				
				pd = new ProgressDialog(activity);
				pd.setMessage("Connecting to Server, Please wait.");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}
				
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					while (pd != null)
						Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (pd!=null) { pd.dismiss(); pd = null;}
			}	
		};
		
		task.execute((Void[])null);
	}

	private void showDbUpdateProgress() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {	
			
			
			@Override
			protected void onPreExecute() {				
				pd = new ProgressDialog(activity);
				pd.setMessage("Updating Database, Please wait.");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.show();
			}
				
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					while (MainService.modulesMap.ExploitItems.size() == 0)
						Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (pd!=null) { pd.dismiss(); pd = null; }
			}	
		};
		
		task.execute((Void[])null);
	}
	
	@Override
	protected void onPause() {
		if (conStatusReceiverRegistered) {
			unregisterReceiver(conStatusReceiver);
			conStatusReceiverRegistered = false;
		}
		
		loadSharedPreferences();
		prefs.edit().putBoolean("isConnected", isConnected).commit();
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();	
			filter.addAction(StaticClass.PWNCORE_CONNECTION_SUCCESS);	
			filter.addAction(StaticClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticClass.PWNCORE_LOAD_EXPLOITS_FAILED);
			filter.addAction(StaticClass.PWNCORE_LOAD_EXPLOITS_SUCCESS);			
			filter.addAction(StaticClass.PWNCORE_LOAD_PAYLOADS_FAILED);
			filter.addAction(StaticClass.PWNCORE_LOAD_PAYLOADS_SUCCESS);			
			filter.addAction(StaticClass.PWNCORE_LOAD_POSTS_FAILED);
			filter.addAction(StaticClass.PWNCORE_LOAD_POSTS_SUCCESS);
			filter.addAction(StaticClass.PWNCORE_LOAD_ENCODERS_FAILED);
			filter.addAction(StaticClass.PWNCORE_LOAD_ENCODERS_SUCCESS);
			filter.addAction(StaticClass.PWNCORE_LOAD_NOPS_FAILED);
			filter.addAction(StaticClass.PWNCORE_LOAD_NOPS_SUCCESS);
			filter.addAction(StaticClass.PWNCORE_LOAD_AUXILIARY_FAILED);
			filter.addAction(StaticClass.PWNCORE_LOAD_AUXILIARY_SUCCESS);
			filter.addAction(StaticClass.PWNCORE_AUTHENTICATION_FAILED);
			filter.addAction(StaticClass.PWNCORE_DATABASE_UPDATE_STARTED);
			filter.addAction(StaticClass.PWNCORE_DATABASE_UPDATE_STOPPED);
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}
			
		isConnected = prefs.getBoolean("isConnected", false);
		
		if (!isConnected)
			Disconnect();
		
		loadSharedPreferences();
		setNotification();
		super.onResume();
	}
	
	@Override
	public void onDestroy() {		
		saveAppState();	
		
		stopService(serviceIntent);
		mNotificationManager.cancelAll();
		super.onDestroy();
	}
	
	private void saveAppState() {
		prefs.edit().putBoolean("isConnected", false).commit();
	}

	public void launchAttackHall(View v) {
		if (!isConnected || !MainService.checkConnection(this))
			Toast.makeText(
					getApplicationContext(), 
					"You have to be connected", 
					Toast.LENGTH_SHORT).show();
		else if (MainService.hostsList.size() == 0)
			Toast.makeText(
					getApplicationContext(), 
					"You have no hosts", 
					Toast.LENGTH_SHORT).show();
		else
			startActivity(new Intent(getApplicationContext(), AttackHallActivity.class));
	}
	
	public void launchAttackWizard(View v) {
		if (!isConnected || !MainService.checkConnection(this))
			Toast.makeText(getApplicationContext(), "You have to be connected", Toast.LENGTH_SHORT).show();
		else
			startActivity(new Intent(getApplicationContext(), AttackWizardActivity.class));
		//startActivity(new Intent(getApplicationContext(), HostSessionsActivity.class).putExtra("hostId", 2));
	}
	
	private void Disconnect() {
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
		
		isConnected = false;	
		invalidateOptionsMenu();
		main_menu.findItem(R.id.mnuConnectionAction).setIcon(R.drawable.plug);
		main_menu.findItem(R.id.mnuConnectionAction).setTitle(R.string.connect);
	}
	
	public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticClass.PWNCORE_CONNECTION_TIMEOUT) {
    			Disconnect();
    			Toast.makeText(getApplicationContext(), 
    					R.string.pwncore_connectiontimeout, 
    					Toast.LENGTH_SHORT).show();
    		}   
    		else if (action == StaticClass.PWNCORE_AUTHENTICATION_FAILED) {
    			Disconnect();
    			Toast.makeText(getApplicationContext(), 
    					R.string.pwncore_authenticationfailed, 
    					Toast.LENGTH_SHORT).show();	
    		}
    		else if (action == StaticClass.PWNCORE_CONNECTION_SUCCESS) {
    			isConnected = true;
    			if (pd != null) {
	    			pd.dismiss();
	    			pd = null;
    			}
    			
    	    	invalidateOptionsMenu();
    			main_menu.findItem(R.id.mnuConnectionAction).setIcon(R.drawable.unplug);
    			main_menu.findItem(R.id.mnuConnectionAction).setTitle(R.string.disconnect);
		
    			setNotification();

    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticClass.PWNCORE_LOAD_ALL_MODULES);
    			sendBroadcast(tmpIntent);	
    		}    		
    		else if (action == StaticClass.PWNCORE_CONNECTION_FAILED) {
    			Disconnect();	
    			Log.e("ConnectionFailed", "" + intent.getStringExtra("error"));
    			Toast.makeText(getApplicationContext(), 
    					R.string.pwncore_connectionfailed, 
    					Toast.LENGTH_SHORT).show();    	
  
    			setNotification();
    		}		
  		
    		else if (action == StaticClass.PWNCORE_LOAD_EXPLOITS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch exploits list", 
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_EXPLOITS_SUCCESS) {
    			if (pd != null) pd.dismiss();
    			MainService.modulesMap.setList("exploit", MainService.databaseHandler.getAllModules("exploits")); 	
    			
    			//new Thread(new Runnable() {
				//	@Override public void run() {				
				//		MainService.modulesMap.loadModulesOptions("exploits");
				//	}}).start();
    			
    			sidebarItems.get(0).setCount(MainService.databaseHandler.getModulesCount("exploits"));
    			sidebarAdapter.notifyDataSetChanged();
    			
    			modulesList.setAdapter(MainService.modulesMap.modulesAdapter);
    			
				getActionBar().setTitle(titles[0]);
				MainService.modulesMap.switchAdapter("exploit");
				
    		}
    		
    		else if (action == StaticClass.PWNCORE_LOAD_PAYLOADS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch payloads list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_PAYLOADS_SUCCESS) {
    			MainService.modulesMap.setList("payload", MainService.databaseHandler.getAllModules("payloads"));
    			sidebarItems.get(1).setCount(MainService.databaseHandler.getModulesCount("payloads"));
    			sidebarAdapter.notifyDataSetChanged();			
    		}
    		
    		else if (action == StaticClass.PWNCORE_LOAD_POSTS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch posts list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_POSTS_SUCCESS) {
    			MainService.modulesMap.setList("post", MainService.databaseHandler.getAllModules("post"));
    			sidebarItems.get(2).setCount(MainService.databaseHandler.getModulesCount("post"));
    			sidebarAdapter.notifyDataSetChanged();    		
    		} 		
    		
    		else if (action == StaticClass.PWNCORE_LOAD_ENCODERS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch encoderss list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_ENCODERS_SUCCESS) {
    			MainService.modulesMap.setList("encoder", MainService.databaseHandler.getAllModules("encoders"));
    			sidebarItems.get(3).setCount(MainService.databaseHandler.getModulesCount("encoders"));
    			sidebarAdapter.notifyDataSetChanged();
    		}
    		
    		else if (action == StaticClass.PWNCORE_LOAD_AUXILIARY_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch auxiliary list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_AUXILIARY_SUCCESS) {  			
    			MainService.modulesMap.setList("auxiliary", MainService.databaseHandler.getAllModules("auxiliary"));
    			sidebarItems.get(4).setCount(MainService.databaseHandler.getModulesCount("auxiliary"));
    			sidebarAdapter.notifyDataSetChanged();
    		}
    		
    		else if (action == StaticClass.PWNCORE_LOAD_NOPS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch nops list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_NOPS_SUCCESS) {
    			MainService.modulesMap.setList("nop", MainService.databaseHandler.getAllModules("nops"));
    			sidebarItems.get(5).setCount(MainService.databaseHandler.getModulesCount("nops"));
    			sidebarAdapter.notifyDataSetChanged();
    		}
    		
    		else if (action == StaticClass.PWNCORE_DATABASE_UPDATE_STARTED) {
    				showDbUpdateProgress();
    		}
    		else if (action == StaticClass.PWNCORE_DATABASE_UPDATE_STOPPED) {
    			//if (pd != null) {
    			//	pd.dismiss();
    			//	pd = null;
    			//}
    		}
    	}
    };
    
	private void setNotification() {
		
		String notiText;	
		if (isConnected) 
			notiText = "Connected " + con_txtHost + ":" + con_txtPort;
		else notiText = "Not Connected";
		
		Intent NotificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pNotificationIntent = PendingIntent.getActivity(this, 0, NotificationIntent, 0);
		
		noti = new Notification.Builder(getApplicationContext())
		.setWhen(0)
        .setContentTitle("pwnCore")
        .setContentText(notiText)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentIntent(pNotificationIntent)
        .getNotification();
    	
		noti.flags |=  Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(0, noti); 
	}
	
	private boolean checkConSettings() {
		loadSharedPreferences();

		boolean hasError = false;
		String error = "";
		
		if (con_txtUsername.equals("")) {
			hasError = true;
			error = "Username not set";
		} else if (con_txtPassword.equals("")) {
			hasError = true;
			error = "Password not set";			
		} else if (!StaticClass.isNumeric(con_txtPort)) {
			hasError = true;
			error = "Port not valid";
		} else if (!StaticClass.validateIPAddress(con_txtHost, false)) {
			hasError = true;
			error = "IP address not valid";			
		}
		
		if (hasError) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder
	    	.setCancelable(false)
	    	.setTitle("Connection Settings Error")
	    	.setMessage("Error: " + error + "\nDo you want to edit settings ?")
	    	.setIcon(android.R.drawable.ic_menu_preferences)    	
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) {
	            	Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
	                startActivity(intent);
	    	    }
	    	})
	    	.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int which) { finish(); }
	    	})
	    	.setNeutralButton("No", null)
	    	.show();
	    	return false;
		}
		else return true;		
	}
	
	private void loadSharedPreferences() {	
		debug_mode 		= 	prefs.getBoolean("debug_mode", false);
        con_txtUsername	=	prefs.getString("connection_Username", "");
        con_txtPassword	=	prefs.getString("connection_Password", "");
        con_txtHost		=	prefs.getString("connection_Host", "");
        con_txtPort		=	prefs.getString("connection_Port", "55553"); 
        
		if (!con_txtUsername.equals("") && 
				StaticClass.isNumeric(con_txtPort) && 
				StaticClass.validateIPAddress(con_txtHost, false)) {			
			getActionBar().setSubtitle(con_txtUsername + "@" + con_txtHost + ":" + con_txtPort);
		}
		
	}
	    
    private class ModulesListItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
    		Object obj = sidebarList.getItemAtPosition(position);
        	SidebarItem objDetails = (SidebarItem)obj;

        	if (!objDetails.isHeader() && ( titlesHas(objDetails.getTitle()) || 
        			objDetails.getTitle().equals("pwnCore"))) {
        		
        		boolean loaded = false;
        		
        		if (objDetails.getTitle().equals(titles[0])) {
        			if (MainService.modulesMap.ExploitItems.size() > 0) {
        				getActionBar().setTitle(objDetails.getTitle());
        				MainService.modulesMap.switchAdapter("exploit");
	        			loaded = true;
        			}
        		}      		 
        		else if (objDetails.getTitle().equals(titles[1])) {
        			if (MainService.modulesMap.PayloadItems.size() > 0) {
        				getActionBar().setTitle(objDetails.getTitle());
        				MainService.modulesMap.switchAdapter("payload");
	        			loaded = true;
        			}
        		}    		
           		else if (objDetails.getTitle().equals(titles[2])) {
        			if (MainService.modulesMap.PostItems.size() > 0) {
        				getActionBar().setTitle(objDetails.getTitle());
        				MainService.modulesMap.switchAdapter("post");
	        			loaded = true;
        			}
        		}       		
           		else if (objDetails.getTitle().equals(titles[3])) {
        			if (MainService.modulesMap.EncoderItems.size() > 0) {
        				getActionBar().setTitle(objDetails.getTitle());
        				MainService.modulesMap.switchAdapter("encoder");
	        			loaded = true;
        			}
        		}        		
           		else if (objDetails.getTitle().equals(titles[4])) {
        			if (MainService.modulesMap.AuxiliaryItems.size() > 0) {
        				getActionBar().setTitle(objDetails.getTitle());
        				MainService.modulesMap.switchAdapter("auxiliary");
	        			loaded = true;
        			}
        		}       		
           		else if (objDetails.getTitle().equals(titles[5])) {
        			if (MainService.modulesMap.NopItems.size() > 0) {
        				getActionBar().setTitle(objDetails.getTitle());
        				MainService.modulesMap.switchAdapter("nop");
	        			loaded = true;
        			}
        		}
        		
        		if (!loaded) 
    				Toast.makeText(
    						getApplicationContext(), 
    						R.string.pwncore_modulesnotloaded, 
    						Toast.LENGTH_SHORT
    						).show();
        	}
        }
    }

	@Override
	public boolean onQueryTextChange(String text) {
		if (MainService.modulesMap.modulesAdapter != null)
		MainService.modulesMap.modulesAdapter.getFilter().filter(text); 
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String text) {
		MainService.modulesMap.modulesAdapter.getFilter().filter(text);
		return true;
	}
   
}
