package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.activities.AttackHallActivity;
import com.anwarelmakrahy.pwncore.activities.AttackWizardActivity;
import com.anwarelmakrahy.pwncore.activities.ModuleOptionsActivity;
import com.anwarelmakrahy.pwncore.activities.SearchModulesActivity;
import com.anwarelmakrahy.pwncore.activities.SettingsActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.structures.ItemDetails;
import com.anwarelmakrahy.pwncore.structures.ItemListBaseAdapter;
import com.anwarelmakrahy.pwncore.structures.ModuleItem;
import com.anwarelmakrahy.pwncore.structures.TargetItem;

import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;

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

	private String 	con_txtUsername, 
					con_txtPassword, 
					con_txtHost, 
					con_txtPort;
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private ItemListBaseAdapter SidebarAdapter;
    private ArrayList<ItemDetails> SidebarItems = new ArrayList<ItemDetails>();
	
    private DatabaseHandler databaseHandler;
    
   
    private ListView modulesList; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {  	
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_main);
        activity = this;
        
        serviceIntent = new Intent(this, MainService.class);     
		startService(serviceIntent);
		
		databaseHandler = DatabaseHandler.getInstance(this);
        
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
		    					"Sorry not implemented yet", 
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
	    					"You have to be connected", 
	    					Toast.LENGTH_SHORT).show();
	        	}
	        }
		});
        
        /*
         *  Prepare Sidebar
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        SidebarAdapter = new ItemListBaseAdapter(this, SidebarItems);
        mDrawerList.setAdapter(SidebarAdapter); 
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());           
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);  
        mDrawerToggle = new ActionBarDrawerToggle(this,  mDrawerLayout, R.drawable.ic_drawer,
        		R.string.drawer_open,R.string.drawer_close) {   	
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };    
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
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
		
	   	TargetItem t = new TargetItem("10.0.0.1");
    	t.setPwned(true);
    	t.setOS("Linux");
    	MainService.mTargetHostList.add(t);
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
    	
	   	ItemDetails item; 
	   	
    	for (int i=0; i<6; i++) {
    		
    	   	item = new ItemDetails();      	
    	   	item.setTitle(titles[i]);
    	   	item.setImage(icons[i]);
    	   	item.setCount(0);
        	SidebarItems.add(item);
    	}
    	
		if (SidebarAdapter != null)
			SidebarAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
    private MenuItem mnuSearch;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        this.main_menu = menu;
        
        mnuSearch = menu.findItem(R.id.mnuSearch);
        SearchView searchView = (SearchView) mnuSearch.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }
    
    public static boolean isConnected = false;
        
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem tmpItem = main_menu.findItem(R.id.mnuConnectionAction);
    	if (!isConnected) {
    		tmpItem.setIcon(R.drawable.plug);
    		tmpItem.setTitle("Connect");	
    		menu.findItem(R.id.mnuConnection).setTitle("Connect");
    		menu.findItem(R.id.mnuNewConsole).setVisible(false);
    	}
    	else {
    		tmpItem.setIcon(R.drawable.unplug);
    		tmpItem.setTitle("Disconnect");
    		menu.findItem(R.id.mnuConnection).setTitle("Disconnect");
    		menu.findItem(R.id.mnuNewConsole).setVisible(true);
    	} 	    	
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
     
	    switch (item.getItemId()) {    
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
					"anwarelmakrahy@gmail.com\n\n";

			AlertDialog dlg = new AlertDialog.Builder(this).create();
			dlg.setMessage(msgbox_string);
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
		    			Intent tmpIntent = new Intent();
		    			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECT);
		    			sendBroadcast(tmpIntent);
		    			
		    			Toast.makeText(getApplicationContext(), 
		    					"Connecting to server. Please wait", 
		    					Toast.LENGTH_SHORT).show();
	    			}
	    		}
	    	}
	    	else if (isConnected) {
    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticsClass.PWNCORE_DISCONNECT);
    			sendBroadcast(tmpIntent);	
    			Disconnect();
    			
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionDisconnected: Disconnected from server", 
    					Toast.LENGTH_SHORT).show();
    			
    			setNotification();
	    	}
	    	return true;
	    	
	    case R.id.mnuExit:

	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder
	    	.setTitle("Exit pwnCore")	
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
	    	Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.putExtra("type", "new.console");
	    	intent.putExtra("cmd", "use multi/handler\n" +
	    					"set PAYLOAD windows/meterpreter/reverse_tcp\n" +
	    					"set LHOST " + con_txtHost + "\n" + 
	    					"exploit -z");
	    	startActivity(intent);   
	    	
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
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
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_SUCCESS);	
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticsClass.PWNCORE_LOAD_EXPLOITS_FAILED);
			filter.addAction(StaticsClass.PWNCORE_LOAD_EXPLOITS_SUCCESS);			
			filter.addAction(StaticsClass.PWNCORE_LOAD_PAYLOADS_FAILED);
			filter.addAction(StaticsClass.PWNCORE_LOAD_PAYLOADS_SUCCESS);			
			filter.addAction(StaticsClass.PWNCORE_LOAD_POSTS_FAILED);
			filter.addAction(StaticsClass.PWNCORE_LOAD_POSTS_SUCCESS);
			filter.addAction(StaticsClass.PWNCORE_LOAD_ENCODERS_FAILED);
			filter.addAction(StaticsClass.PWNCORE_LOAD_ENCODERS_SUCCESS);
			filter.addAction(StaticsClass.PWNCORE_LOAD_NOPS_FAILED);
			filter.addAction(StaticsClass.PWNCORE_LOAD_NOPS_SUCCESS);
			filter.addAction(StaticsClass.PWNCORE_LOAD_AUXILIARY_FAILED);
			filter.addAction(StaticsClass.PWNCORE_LOAD_AUXILIARY_SUCCESS);
			filter.addAction(StaticsClass.PWNCORE_AUTHENTICATION_FAILED);
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}
			
		isConnected = prefs.getBoolean("isConnected", false);
		
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
		else if (MainService.mTargetHostList.size() == 0)
			Toast.makeText(
					getApplicationContext(), 
					"You have no targets", 
					Toast.LENGTH_SHORT).show();
		else
			startActivity(new Intent(getApplicationContext(), AttackHallActivity.class));
	}
	
	public void launchAttackWizard(View v) {
		if (!isConnected || !MainService.checkConnection(this))
			Toast.makeText(getApplicationContext(), "You have to be connected", Toast.LENGTH_SHORT).show();
		else
			startActivity(new Intent(getApplicationContext(), AttackWizardActivity.class));
	}
	
	/*
	 * Broadcast Receivers
	 */
	
	private void Disconnect() {
		isConnected = false;	
		invalidateOptionsMenu();
		main_menu.findItem(R.id.mnuConnectionAction).setIcon(R.drawable.plug);
		main_menu.findItem(R.id.mnuConnectionAction).setTitle("Connect");
	}
	
	public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticsClass.PWNCORE_CONNECTION_TIMEOUT) {
    			Disconnect();
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionTimeout: Please check that server is running", 
    					Toast.LENGTH_SHORT).show();
    		}   
    		else if (action == StaticsClass.PWNCORE_AUTHENTICATION_FAILED) {
    			Disconnect();
    			Toast.makeText(getApplicationContext(), 
    					"AuthenticationFailed: Please check that your credentials are valid", 
    					Toast.LENGTH_SHORT).show();	
    		}
    		else if (action == StaticsClass.PWNCORE_CONNECTION_SUCCESS) {
    			isConnected = true;
    			
    	    	invalidateOptionsMenu();
    			main_menu.findItem(R.id.mnuConnectionAction).setIcon(R.drawable.unplug);
    			main_menu.findItem(R.id.mnuConnectionAction).setTitle("Disconnect");
		
    			setNotification();

    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticsClass.PWNCORE_LOAD_ALL_MODULES);
    			sendBroadcast(tmpIntent);	
    		}    		
    		else if (action == StaticsClass.PWNCORE_CONNECTION_FAILED) {
    			Disconnect();		
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionFailed: " + intent.getStringExtra("error"), 
    					Toast.LENGTH_SHORT).show();    	
  
    			setNotification();
    		}		
  		
    		else if (action == StaticsClass.PWNCORE_LOAD_EXPLOITS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch exploits list", 
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_EXPLOITS_SUCCESS) {
    			MainService.modulesMap.setList("exploit", databaseHandler.getAllModules("exploits")); 			
    			SidebarItems.get(0).setCount(databaseHandler.getModulesCount("exploits"));
    			SidebarAdapter.notifyDataSetChanged();
    			
    			modulesList.setAdapter(MainService.modulesMap.modulesAdapter);
    			
				getActionBar().setTitle(titles[0]);
				MainService.modulesMap.switchAdapter("exploit");
    		}
    		
    		else if (action == StaticsClass.PWNCORE_LOAD_PAYLOADS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch payloads list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_PAYLOADS_SUCCESS) {
    			MainService.modulesMap.setList("payload", databaseHandler.getAllModules("payloads"));
    			SidebarItems.get(1).setCount(databaseHandler.getModulesCount("payloads"));
    			SidebarAdapter.notifyDataSetChanged();			
    		}
    		
    		else if (action == StaticsClass.PWNCORE_LOAD_POSTS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch posts list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_POSTS_SUCCESS) {
    			MainService.modulesMap.setList("post", databaseHandler.getAllModules("post"));
    			SidebarItems.get(2).setCount(databaseHandler.getModulesCount("post"));
    			SidebarAdapter.notifyDataSetChanged();    		
    		} 		
    		
    		else if (action == StaticsClass.PWNCORE_LOAD_ENCODERS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch encoderss list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_ENCODERS_SUCCESS) {
    			MainService.modulesMap.setList("encoder", databaseHandler.getAllModules("encoders"));
    			SidebarItems.get(3).setCount(databaseHandler.getModulesCount("encoders"));
    			SidebarAdapter.notifyDataSetChanged();
    		}
    		
    		else if (action == StaticsClass.PWNCORE_LOAD_AUXILIARY_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch auxiliary list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_AUXILIARY_SUCCESS) {  			
    			MainService.modulesMap.setList("auxiliary", databaseHandler.getAllModules("auxiliary"));
    			SidebarItems.get(4).setCount(databaseHandler.getModulesCount("auxiliary"));
    			SidebarAdapter.notifyDataSetChanged();
    		}
    		
    		else if (action == StaticsClass.PWNCORE_LOAD_NOPS_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"Failed to fetch nops list",  
    					Toast.LENGTH_SHORT).show();    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_NOPS_SUCCESS) {
    			MainService.modulesMap.setList("nop", databaseHandler.getAllModules("nops"));
    			SidebarItems.get(5).setCount(databaseHandler.getModulesCount("nops"));
    			SidebarAdapter.notifyDataSetChanged();
    		}
    	}
    };
    

    /*
     * Misc Functions
     */
    
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
		} else if (!StaticsClass.isNumeric(con_txtPort)) {
			hasError = true;
			error = "Port not valid";
		} else if (!StaticsClass.validateIPAddress(con_txtHost, false)) {
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
        con_txtUsername	=	prefs.getString("connection_Username", "");
        con_txtPassword	=	prefs.getString("connection_Password", "");
        con_txtHost		=	prefs.getString("connection_Host", "");
        con_txtPort		=	prefs.getString("connection_Port", "55553"); 
        
		if (!con_txtUsername.equals("") && 
				StaticsClass.isNumeric(con_txtPort) && 
				StaticsClass.validateIPAddress(con_txtHost, false)) {			
			getActionBar().setSubtitle(con_txtUsername + "@" + con_txtHost + ":" + con_txtPort);
		}
	}
	    
	
    private class DrawerItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
    		Object obj = mDrawerList.getItemAtPosition(position);
        	ItemDetails objDetails = (ItemDetails)obj;

        	mDrawerLayout.closeDrawer(mDrawerList);
        	
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
    						"Modules not loaded yet", 
    						Toast.LENGTH_SHORT
    						).show();
        	}
        }
    }

	@Override
	public boolean onQueryTextChange(String arg0) {
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		startActivity(new Intent(
				getApplicationContext(), 
				SearchModulesActivity.class
				).putExtra("q", s));
		
		mnuSearch.collapseActionView();
		return true;
	}
    
	private static Activity activity;
	public static Activity getActivity() {
		return activity;
	}
}
