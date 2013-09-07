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

import com.anwarelmakrahy.pwncore.structures.TargetItem;


public class MainService extends Service {
	
	private SharedPreferences prefs;
	private boolean con_useSSL;
	private String con_txtUsername, con_txtPassword, con_txtHost, con_txtPort;
	private boolean isAuthenticated = false;	

	private ExecutorService executor;
	private DatabaseHandler databaseHandler;
	
	public static SessionManager sessionMgr;
	public static MsfRpcClient client;
	public static ModulesMap modulesMap;
	
	public static ArrayList<TargetItem> mTargetHostList = new ArrayList<TargetItem>();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
		sessionMgr = new SessionManager(getApplicationContext());
		modulesMap = new ModulesMap(getApplicationContext());
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(StaticsClass.PWNCORE_CONNECT);
		filter.addAction(StaticsClass.PWNCORE_DISCONNECT);
		filter.addAction(StaticsClass.PWNCORE_LOAD_EXPLOITS);
		filter.addAction(StaticsClass.PWNCORE_LOAD_PAYLOADS);
		filter.addAction(StaticsClass.PWNCORE_LOAD_ENCODERS);
		filter.addAction(StaticsClass.PWNCORE_LOAD_NOPS);
		filter.addAction(StaticsClass.PWNCORE_LOAD_AUXILIARY);
		filter.addAction(StaticsClass.PWNCORE_LOAD_POSTS);
		filter.addAction(StaticsClass.PWNCORE_LOAD_ALL_MODULES);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_CREATE);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_WRITE);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_READ);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_DESTROY);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_SHELL_READ);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_SHELL_WRITE);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_METERPRETER_READ);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_METERPRETER_WRITE);	
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_SHELL_DESTROY);
		filter.addAction(StaticsClass.PWNCORE_CONSOLE_METERPRETER_DESTROY);
		registerReceiver(mainReceiver, filter);
   
		executor = Executors.newFixedThreadPool(20);
		
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
		        		tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_SUCCESS);
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
	public IBinder onBind(Intent arg0) {
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
    		
    		if (action == StaticsClass.PWNCORE_CONNECT) { 
    			if (!isAuthenticated)
    				startConnection();
    		}
    		else if (action == StaticsClass.PWNCORE_DISCONNECT) {		
				isAuthenticated = false;
    		}

    		else if (action == StaticsClass.PWNCORE_LOAD_ALL_MODULES && isAuthenticated) {
				String[] modules = {
	        			StaticsClass.PWNCORE_LOAD_EXPLOITS,
	        			StaticsClass.PWNCORE_LOAD_PAYLOADS,
	        			StaticsClass.PWNCORE_LOAD_POSTS,
	        			StaticsClass.PWNCORE_LOAD_AUXILIARY,
						StaticsClass.PWNCORE_LOAD_NOPS,
						StaticsClass.PWNCORE_LOAD_ENCODERS,
						};
				
    			Intent tmpIntent = new Intent();        			
    			for (int i=0; i<modules.length; i++) {
    				tmpIntent.setAction(modules[i]);
    				sendBroadcast(tmpIntent); 
    			}
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_EXPLOITS && isAuthenticated) {			   				
				executor.execute(new NewThread(null) {
					@Override public void run() {							
						getModules(
								"exploits", 
								action, 
								StaticsClass.PWNCORE_LOAD_EXPLOITS_SUCCESS, 
								StaticsClass.PWNCORE_LOAD_EXPLOITS_FAILED
								);
						Thread.currentThread().interrupt();
					}}); 			
    		}    		
    		else if (action == StaticsClass.PWNCORE_LOAD_PAYLOADS) {
    			if (isAuthenticated) {   			
    				executor.execute(new NewThread(null) {
						@Override public void run() {	
							getModules(
									"payloads", 
									action, 
									StaticsClass.PWNCORE_LOAD_PAYLOADS_SUCCESS, 
									StaticsClass.PWNCORE_LOAD_PAYLOADS_FAILED
									);
						}});
    			}    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_POSTS) {
    			if (isAuthenticated) {   			
    				executor.execute(new NewThread(null) {
						@Override public void run() {								
							getModules(
									"post", 
									action, 
									StaticsClass.PWNCORE_LOAD_POSTS_SUCCESS, 
									StaticsClass.PWNCORE_LOAD_POSTS_FAILED
									);
						}});
    			}    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_AUXILIARY) {
    			if (isAuthenticated) {   			
    				executor.execute(new NewThread(null) {
						@Override public void run() {								
							getModules(
									"auxiliary", 
									action, 
									StaticsClass.PWNCORE_LOAD_AUXILIARY_SUCCESS, 
									StaticsClass.PWNCORE_LOAD_AUXILIARY_FAILED
									);
						}});
    			}     			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_NOPS && isAuthenticated) {
    			if (isAuthenticated) {   			
    				executor.execute(new NewThread(null) {
						@Override public void run() {								
							getModules(
									"nops", 
									action, 
									StaticsClass.PWNCORE_LOAD_NOPS_SUCCESS, 
									StaticsClass.PWNCORE_LOAD_NOPS_FAILED
									);
						}});
    			}    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_ENCODERS && isAuthenticated) {		
				executor.execute(new NewThread(null) {
					@Override public void run() {								
						getModules(
								"encoders", 
								action, 
								StaticsClass.PWNCORE_LOAD_ENCODERS_SUCCESS, 
								StaticsClass.PWNCORE_LOAD_ENCODERS_FAILED
								);
					}});	
    		}
    		
    		else if (action == StaticsClass.PWNCORE_CONSOLE_CREATE) {	
				executor.execute(new NewThread(null) {
					@Override public void run() {														
	            		Map<String, Value> newConDes = 
	            				client.call(MsfRpcClient.singleOptCallList("console.create"));	        
	            		if (newConDes != null)
		            		sessionMgr.notifyNewConsole(
		            				intent.getStringExtra("id"), 
		            				newConDes.get("id").asRawValue().getString(), 
		            				newConDes.get("prompt").asRawValue().getString());
					}});	
    		}
    		else if (action == StaticsClass.PWNCORE_CONSOLE_READ) {
				executor.execute(new NewThread(null) {
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
    		else if (action == StaticsClass.PWNCORE_CONSOLE_WRITE) {
				executor.execute(new NewThread(null) {
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
    		else if (action == StaticsClass.PWNCORE_CONSOLE_DESTROY) {
				executor.execute(new NewThread(null) {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("console.destroy");
						params.add(intent.getStringExtra("msfId"));
						Map<String, Value> newConDes = client.call(params);
	        			sessionMgr.notifyDestroyedConsole(
	        					intent.getStringExtra("id"), 
	        					intent.getStringExtra("msfId"));
					}});	
    		}
    		else if (action == StaticsClass.PWNCORE_CONSOLE_METERPRETER_WRITE) {
				executor.execute(new NewThread(null) {
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
    		else if (action == StaticsClass.PWNCORE_CONSOLE_SHELL_WRITE) {
				executor.execute(new NewThread(null) {
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
    		else if (action == StaticsClass.PWNCORE_CONSOLE_METERPRETER_READ) {
				executor.execute(new NewThread(null) {
					@Override public void run() {	
						List<Object> params = new ArrayList<Object>();
						params.add("session.meterpreter_read");
						params.add(intent.getStringExtra("id"));
	            		Map<String, Value> newConDes = client.call(params);
	            		if (newConDes != null && newConDes.containsKey("data"))
		            		sessionMgr.notifySessionNewRead(
		            				intent.getStringExtra("id"), 
		            				newConDes.get("data").asRawValue().getString());  
					}});	
    		}
    		else if (action == StaticsClass.PWNCORE_CONSOLE_SHELL_READ) {
				executor.execute(new NewThread(null) {
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
    		else if (action == StaticsClass.PWNCORE_CONSOLE_SHELL_DESTROY ||
    				action == StaticsClass.PWNCORE_CONSOLE_METERPRETER_DESTROY) {
				executor.execute(new NewThread(null) {
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
			if (modules != null)
				databaseHandler.addModules(modules, type);
			else tmpIntent.setAction(failed);
		}		
		sendBroadcast(tmpIntent); 
	}
	
    abstract class NewThread implements Runnable {
    	//private Map<String, Object> params = null;
    	NewThread(Map<String, Object> params) {
    		//this.params = params;
    	}
    	//public Map<String, Object> getParams() {
    		//return params;
    	//}
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
