package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.structures.HostItem;


public class MainService extends Service {
	
	private SharedPreferences prefs;
	private boolean con_useSSL;
	private String con_txtUsername, con_txtPassword, con_txtHost, con_txtPort;
	private boolean isAuthenticated = false;	

	private ExecutorService executor;
	public static DatabaseHandler databaseHandler;
	
	public static SessionManager sessionMgr;
	public static MsfRpcClient client;
	public static ModulesMap modulesMap;
	
	public static ArrayList<HostItem> hostsList = new ArrayList<HostItem>();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefs = this.getSharedPreferences(StaticClass.PWNCORE_PACKAGE_NAME, Context.MODE_PRIVATE);
		sessionMgr = new SessionManager(getApplicationContext());
		modulesMap = new ModulesMap(getApplicationContext());
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(StaticClass.PWNCORE_CONNECT);
		filter.addAction(StaticClass.PWNCORE_DISCONNECT);
		filter.addAction(StaticClass.PWNCORE_LOAD_EXPLOITS);
		filter.addAction(StaticClass.PWNCORE_LOAD_PAYLOADS);
		filter.addAction(StaticClass.PWNCORE_LOAD_ENCODERS);
		filter.addAction(StaticClass.PWNCORE_LOAD_NOPS);
		filter.addAction(StaticClass.PWNCORE_LOAD_AUXILIARY);
		filter.addAction(StaticClass.PWNCORE_LOAD_POSTS);
		filter.addAction(StaticClass.PWNCORE_LOAD_ALL_MODULES);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_CREATE);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_WRITE);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_READ);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_DESTROY);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_SHELL_READ);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_SHELL_WRITE);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_METERPRETER_READ);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_METERPRETER_WRITE);	
		filter.addAction(StaticClass.PWNCORE_CONSOLE_SHELL_DESTROY);
		filter.addAction(StaticClass.PWNCORE_CONSOLE_METERPRETER_DESTROY);
		registerReceiver(mainReceiver, filter);
   
		executor = Executors.newFixedThreadPool(30);
		
		//deleteDatabase(DatabaseHandler.DATABASE_NAME);
		databaseHandler = DatabaseHandler.getInstance(this);
		return Service.START_NOT_STICKY;
	}
	
	
	private Map<String, Value> StatsMap;
	private void startConnection() {			
		con_useSSL 		= 	prefs.getBoolean("connection_useSSL", false);
        con_txtUsername	=	prefs.getString("connection_Username", "admin");
        con_txtPassword	=	prefs.getString("connection_Password", "admin");
        con_txtHost		=	prefs.getString("connection_Host", "127.0.0.1");
        con_txtPort		=	prefs.getString("connection_Port", "55553"); 
        
        if (!isAuthenticated) {
        	
	        Map<String, Object> opts = new HashMap<String, Object>();
	        opts.put("host", con_txtHost);
	        opts.put("port", con_txtPort);
	        opts.put("ssl" , con_useSSL);
	        
	        client = new MsfRpcClient(getApplicationContext(), opts);
   
	        new Thread(new Runnable() {
				@Override
				public void run() {
					Intent tmpIntent = new Intent();				
		        	if (client.login(con_txtUsername, con_txtPassword)) {
		        		isAuthenticated = true; 		        		
		        		tmpIntent.setAction(StaticClass.PWNCORE_CONNECTION_SUCCESS);
		        		sendBroadcast(tmpIntent);	        		
		        		StatsMap = client.call(MsfRpcClient.singleOptCallList("core.module_stats"));
		        		sessionMgr.updateJobsList();
		        		sessionMgr.updateSessionsRemoteInfo();
		        	}
				}
			}).start();   	
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mainReceiver);
		executor.shutdownNow();
		super.onDestroy();
	}
	
    public BroadcastReceiver mainReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, final Intent intent) {
    		final String action = intent.getAction();
    		
    		if (action == StaticClass.PWNCORE_CONNECT) { 
    			if (!isAuthenticated)
    				startConnection();
    		}
    		else if (action == StaticClass.PWNCORE_DISCONNECT) {		
				isAuthenticated = false;
    		}

    		else if (action == StaticClass.PWNCORE_LOAD_ALL_MODULES && isAuthenticated) {
    			
    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticClass.PWNCORE_DATABASE_UPDATE_STARTED);
    			sendBroadcast(tmpIntent);
    			
				String[] modules = {
	        			StaticClass.PWNCORE_LOAD_EXPLOITS,
	        			StaticClass.PWNCORE_LOAD_PAYLOADS,
	        			StaticClass.PWNCORE_LOAD_POSTS,
	        			StaticClass.PWNCORE_LOAD_AUXILIARY,
						StaticClass.PWNCORE_LOAD_NOPS,
						StaticClass.PWNCORE_LOAD_ENCODERS,
						};
				
    			tmpIntent = new Intent();  
    			for (int i=0; i<modules.length; i++) {
    				tmpIntent.setAction(modules[i]);
    				sendBroadcast(tmpIntent); 
    			}
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_EXPLOITS && isAuthenticated) {			   				
				executor.execute(new Thread() {
					@Override public void run() {							
						getModules(
								"exploits", 
								action, 
								StaticClass.PWNCORE_LOAD_EXPLOITS_SUCCESS, 
								StaticClass.PWNCORE_LOAD_EXPLOITS_FAILED
								);
						Thread.currentThread().interrupt();
					}}); 			
    		}    		
    		else if (action == StaticClass.PWNCORE_LOAD_PAYLOADS) {
    			if (isAuthenticated) {   			
    				executor.execute(new Thread() {
						@Override public void run() {	
							getModules(
									"payloads", 
									action, 
									StaticClass.PWNCORE_LOAD_PAYLOADS_SUCCESS, 
									StaticClass.PWNCORE_LOAD_PAYLOADS_FAILED
									);
						}});
    			}    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_POSTS) {
    			if (isAuthenticated) {   			
    				executor.execute(new Thread() {
						@Override public void run() {								
							getModules(
									"post", 
									action, 
									StaticClass.PWNCORE_LOAD_POSTS_SUCCESS, 
									StaticClass.PWNCORE_LOAD_POSTS_FAILED
									);
						}});
    			}    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_AUXILIARY) {
    			if (isAuthenticated) {   			
    				executor.execute(new Thread() {
						@Override public void run() {								
							getModules(
									"auxiliary", 
									action, 
									StaticClass.PWNCORE_LOAD_AUXILIARY_SUCCESS, 
									StaticClass.PWNCORE_LOAD_AUXILIARY_FAILED
									);
						}});
    			}     			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_NOPS && isAuthenticated) {
    			if (isAuthenticated) {   			
    				executor.execute(new Thread() {
						@Override public void run() {								
							getModules(
									"nops", 
									action, 
									StaticClass.PWNCORE_LOAD_NOPS_SUCCESS, 
									StaticClass.PWNCORE_LOAD_NOPS_FAILED
									);
						}});
    			}    			
    		}
    		else if (action == StaticClass.PWNCORE_LOAD_ENCODERS && isAuthenticated) {		
				executor.execute(new Thread() {
					@Override public void run() {								
						getModules(
								"encoders", 
								action, 
								StaticClass.PWNCORE_LOAD_ENCODERS_SUCCESS, 
								StaticClass.PWNCORE_LOAD_ENCODERS_FAILED
								);
					}});	
    		}
    		
    		else if (action == StaticClass.PWNCORE_CONSOLE_CREATE) {	
				executor.execute(new Thread() {
					@Override public void run() {														
	            		Map<String, Value> newConDes = 
	            				client.call(MsfRpcClient.singleOptCallList("console.create"));	        
	            		if (newConDes != null &&
	            				newConDes.containsKey("id") &&
	            				newConDes.containsKey("id"))
		            		sessionMgr.notifyNewConsole(
		            				intent.getStringExtra("id"), 
		            				newConDes.get("id").asRawValue().getString(), 
		            				newConDes.get("prompt").asRawValue().getString());
					}});	
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_READ) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("console.read");
						params.add(intent.getStringExtra("msfId"));
	            		Map<String, Value> newConDes = client.call(params);
	            		if (newConDes != null &&
	            				newConDes.containsKey("data") &&
	            				newConDes.containsKey("prompt") &&
	            				newConDes.containsKey("busy"))
		            		sessionMgr.notifyConsoleNewRead(
		            				intent.getStringExtra("id"), 
		            				newConDes.get("data").asRawValue().getString(), 
		            				newConDes.get("prompt").asRawValue().getString(),
		            				newConDes.get("busy").asBooleanValue().getBoolean());  
					}});	
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_WRITE) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("console.write");
						params.add(intent.getStringExtra("msfId"));
						params.add(intent.getStringExtra("data") + "\n");
						Map<String, Value> newConDes = client.call(params);
						if (newConDes != null)
							sessionMgr.notifyConsoleWrite(
									intent.getStringExtra("id"));  
					}});			
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_DESTROY) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("console.destroy");
						params.add(intent.getStringExtra("msfId"));
						Map<String, Value> newConDes = client.call(params);
						if (newConDes != null &&
								newConDes.containsKey("result") &&
								newConDes.get("result").asRawValue().
								getString().equals("success")) {
		        			sessionMgr.notifyDestroyedConsole(
		        					intent.getStringExtra("id"), 
		        					intent.getStringExtra("msfId"));
						}
					}});	
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_METERPRETER_WRITE) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("session.meterpreter_write");
						params.add(intent.getStringExtra("id"));
						params.add(intent.getStringExtra("data") + "\n");
						Map<String, Value> newConDes = client.call(params);
						if (newConDes != null)
							sessionMgr.notifySessionWrite(
									intent.getStringExtra("id"));  
					}});			
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_SHELL_WRITE) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("session.shell_write");
						params.add(intent.getStringExtra("id"));
						params.add(intent.getStringExtra("data") + "\n");
						Map<String, Value> newConDes = client.call(params);
						if (newConDes != null)
							sessionMgr.notifySessionWrite(
									intent.getStringExtra("id"));  
					}});			
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_METERPRETER_READ) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("session.meterpreter_read");
						params.add(intent.getStringExtra("id"));
	            		Map<String, Value> newConDes = client.call(params);
	            		if (newConDes != null && newConDes.containsKey("data")) {
	            			try {
			            		sessionMgr.notifySessionNewRead(
			            				intent.getStringExtra("id"), 
			            				newConDes.get("data").asRawValue().getString());             				
	            			}
	            			catch (Exception e) {
			            		sessionMgr.notifySessionNewRead(
			            				intent.getStringExtra("id"), 
			            				newConDes.get("data").asRawValue().getByteArray()); 
	            			}
	            		}
					}});	
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_SHELL_READ) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("session.shell_read");
						params.add(intent.getStringExtra("id"));
	            		Map<String, Value> newConDes = client.call(params);
	            		if (newConDes != null && newConDes.containsKey("data"))
		            		sessionMgr.notifySessionNewRead(
		            				intent.getStringExtra("id"), 
		            				newConDes.get("data").asRawValue().getString());  
					}});	
    		}
    		else if (action == StaticClass.PWNCORE_CONSOLE_SHELL_DESTROY ||
    				action == StaticClass.PWNCORE_CONSOLE_METERPRETER_DESTROY) {
				executor.execute(new Thread() {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("session.stop");
						params.add(intent.getStringExtra("id"));
	            		client.call(params);
	        			sessionMgr.notifyDestroyedSession(
	        					intent.getStringExtra("id"));
					}});	
    		}
    	}
    };

	private int getCurModulesCount(String  module) {
		if (module == "exploits")
			return databaseHandler.getModulesCount(module);
		else if (module == "payloads")
			return databaseHandler.getModulesCount(module);
		else if (module == "post")
			return databaseHandler.getModulesCount(module);
		else if (module == "nops")
			return databaseHandler.getModulesCount(module);
		else if (module == "encoders")
			return databaseHandler.getModulesCount(module);
		else if (module == "auxiliary")
			return databaseHandler.getModulesCount(module);
		else 
			return 0;
	}
    
	private void getModules(String type, String loadType, String success, String failed) {
		
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(success);		
		if (StatsMap != null && 
				StatsMap.containsKey(type) && 
				StatsMap.get(type).asIntegerValue().getInt() > 
				getCurModulesCount(type)) {		
			String[] modules = client.getModules(loadType);	
			if (modules != null) {
				//databaseHandler.addModules(modules, type);
				//List<String> alreadySaved = databaseHandler.getAllModulesString(type);
				databaseHandler.recreateTable(databaseHandler.getTableIdByName(type));
				databaseHandler.addModules(modules, type);
				//for (int i=0; i<modules.length; i++) {
					//if (!alreadySaved.contains(modules[i]))
				//		databaseHandler.addModule(
				//				modules[i],
				//				/*new HashMap<String, Value>()*/null,
				//				/*new HashMap<String, Value>()*/null,
				//				type);
				//}
			}
			else tmpIntent.setAction(failed);
		}		
		sendBroadcast(tmpIntent); 

		tmpIntent = new Intent();
		tmpIntent.setAction(StaticClass.PWNCORE_DATABASE_UPDATE_STOPPED);
		sendBroadcast(tmpIntent);
	}
	
    public static boolean checkConnection(Context c) {
		ConnectivityManager connManager = (ConnectivityManager)c.getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (!mWifi.isConnected() && !mMobile.isConnected())
	    	return false;
	    return true;
	}
}
