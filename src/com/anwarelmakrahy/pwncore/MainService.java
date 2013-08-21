package com.anwarelmakrahy.pwncore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.msgpack.MessagePack;
import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.TInteger;
import static org.msgpack.template.Templates.tMap;
import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tList;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.type.Value;



public class MainService extends Service {

	private SharedPreferences prefs;
	private boolean con_useSSL;
	private String con_txtUsername, con_txtPassword, con_txtHost, con_txtPort;
	
	private boolean isAuthenticated = false;	

	private String CLIENT_TOKEN = "";
	private boolean clientTokenSet = false;
	
	private ExecutorService executor;
	
	private DatabaseHandler db;
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Toast.makeText(getApplicationContext(), "pwnCore service started", Toast.LENGTH_SHORT).show();
		prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);

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
		registerReceiver(mainReceiver, filter);
   
		executor = Executors.newFixedThreadPool(10);
		
		//deleteDatabase(DatabaseHandler.DATABASE_NAME);
		db = new DatabaseHandler(this);
		
		if (!checkConReceiverRegistered) {
			filter = new IntentFilter();
			filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_CHECK);
			registerReceiver(checkConReceiver, filter);
			checkConReceiverRegistered = true;
		}
		
		return Service.START_NOT_STICKY;
	}
	
	private String THREAD_METHOD;
	private void startConnection() {
			
		con_useSSL 		= 	prefs.getBoolean("connection_useSSL", false);
        con_txtUsername	=	prefs.getString("connection_Username", "admin");
        con_txtPassword	=	prefs.getString("connection_Password", "admin");
        con_txtHost		=	prefs.getString("connection_Host", "127.0.0.1");
        con_txtPort		=	prefs.getString("connection_Port", "55553"); 
        
        THREAD_METHOD = "authenticate";
        executor.execute(thread);   
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private boolean checkConReceiverRegistered = false;
	
	@Override
	public void onDestroy () {
		unregisterReceiver(mainReceiver);
		
		if (isAuthenticated) {
	        THREAD_METHOD = "deauthenticate";
	        executor.execute(thread);
		}
		
		
		if (checkConReceiverRegistered) {
			unregisterReceiver(checkConReceiver);
			checkConReceiverRegistered = false;
		}
		
		//Toast.makeText(getApplicationContext(), 
		//		"pwnCore service stopped", 
		//		Toast.LENGTH_SHORT).show();
		
		super.onDestroy();
	}
	
    public BroadcastReceiver mainReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticsClass.PWNCORE_CONNECT) { 
    			if (!isAuthenticated) {
    				startConnection();
    			}
    		}
    		else if (action == StaticsClass.PWNCORE_DISCONNECT) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "deauthenticate";
    		        executor.execute(thread);
    			}
    		}

    		else if (action == StaticsClass.PWNCORE_LOAD_ALL_MODULES) {
    			if (isAuthenticated) {
      		        THREAD_METHOD = "get_all";
    		        executor.execute(thread);
    			}
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_EXPLOITS) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "get_exploits";
    		        executor.execute(thread);
    			}    			
    		}    		
    		else if (action == StaticsClass.PWNCORE_LOAD_PAYLOADS) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "get_payloads";
    		        executor.execute(thread);
    			}    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_POSTS) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "get_post";
    		        executor.execute(thread);
    			}    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_AUXILIARY) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "get_auxiliary";
    		        executor.execute(thread);
    			}     			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_NOPS) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "get_nops";
    		        executor.execute(thread);
    			}    			
    		}
    		else if (action == StaticsClass.PWNCORE_LOAD_ENCODERS) {
    			if (isAuthenticated) {   			
    		        THREAD_METHOD = "get_encoders";
    		        executor.execute(thread);
    			}    			
    		}
    		
    		else if (action == StaticsClass.PWNCORE_CONSOLE_CREATE) {
    			THREAD_METHOD = StaticsClass.PWNCORE_CONSOLE_CREATE;
    			CONSOLE_TYPE = intent.getStringExtra(StaticsClass.PWNCORE_CONSOLE_TYPE);
    			
    			if (CONSOLE_TYPE.equals(StaticsClass.PWNCORE_CONSOLE_TYPE_NMAP)) {
    		        NMAP_SCAN_ARGS = intent.getStringArrayExtra(StaticsClass.PWNCORE_NMAP_SCAN_ARGS);
    		        NMAP_SCAN_HOST = intent.getStringExtra(StaticsClass.PWNCORE_NMAP_SCAN_HOST);
    			}
    			else return;
    			
    			executor.execute(thread);
    		}
    		else if (action == StaticsClass.PWNCORE_CONSOLE_READ) {
    			THREAD_METHOD = StaticsClass.PWNCORE_CONSOLE_READ;
    			CONSOLE_READ_ID = intent.getStringExtra("id");
    			executor.execute(thread);
    		}
    		else if (action == StaticsClass.PWNCORE_CONSOLE_WRITE) {
    			THREAD_METHOD = StaticsClass.PWNCORE_CONSOLE_WRITE;
    			CONSOLE_WRITE_ID = intent.getStringExtra("id");
    			CONSOLE_WRITE_DATA = intent.getStringExtra("data");
    			executor.execute(thread);  			
    		}
    		else if (action == StaticsClass.PWNCORE_CONSOLE_DESTROY) {
    			THREAD_METHOD = StaticsClass.PWNCORE_CONSOLE_DESTROY;
    			CONSOLE_DESTROY_ID = intent.getStringExtra("id");
    			executor.execute(thread);  			
    		}
    	}
    };

    private String[] 	NMAP_SCAN_ARGS;
    private String 		NMAP_SCAN_HOST = "127.0.0.1";
    private int 		NMAP_SCAN_COUNT = 0;
    private String		CONSOLE_READ_ID, CONSOLE_WRITE_ID, CONSOLE_WRITE_DATA, CONSOLE_DESTROY_ID, CONSOLE_TYPE;
    
    Runnable thread = new Runnable()
    {
    	String urlStart;
        String urlEnd = "/api";
        
        Map<String, Integer> StatsMap;
        
    	@Override
        public void run() 
        {
    		urlStart = con_useSSL ? "https://" : "http://";
    		
            try {
            	
            	if (THREAD_METHOD == "authenticate" && !isAuthenticated) {
	            	if (authenticate()) {
	            		isAuthenticated = true;
	            		
	            		StatsMap = getStats();
	            		
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_SUCCESS);
	        			sendBroadcast(tmpIntent);
	        			return;
	            	}
	            	
	            	else {
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(StaticsClass.PWNCORE_AUTHENTICATION_FAILED);
	        			sendBroadcast(tmpIntent);
	        			return;
	            	}
            	}
            	
            	/*else if (THREAD_METHOD == "nmap_scan") {
            		int curID = ++NMAP_SCAN_COUNT;
            		if (nmapScan(NMAP_SCAN_ROOT, NMAP_SCAN_ARGS, NMAP_SCAN_HOST, curID)) {
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(MainActivity.PWNCORE_NMAP_SCAN_SUCCESS);
	        			tmpIntent.putExtra("id", curID);
	        			sendBroadcast(tmpIntent);
            		}
            		else {
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(MainActivity.PWNCORE_NMAP_SCAN_FAILED);
	        			tmpIntent.putExtra("id", curID);
	        			sendBroadcast(tmpIntent);
            		}
            	}*/
            	
            	else if (isAuthenticated) {
            		
	            	String[] gets = {"exploits", "payloads", "post", "nops", "auxiliary", "encoders"};
	            	String[][] gets_msg = { { StaticsClass.PWNCORE_LOAD_EXPLOITS_SUCCESS, StaticsClass.PWNCORE_LOAD_EXPLOITS_FAILED },
	            							{ StaticsClass.PWNCORE_LOAD_PAYLOADS_SUCCESS, StaticsClass.PWNCORE_LOAD_PAYLOADS_FAILED },
	            							{ StaticsClass.PWNCORE_LOAD_POSTS_SUCCESS, StaticsClass.PWNCORE_LOAD_POSTS_FAILED },
	            							{ StaticsClass.PWNCORE_LOAD_NOPS_SUCCESS, StaticsClass.PWNCORE_LOAD_NOPS_FAILED },
	            							{ StaticsClass.PWNCORE_LOAD_AUXILIARY_SUCCESS, StaticsClass.PWNCORE_LOAD_AUXILIARY_FAILED },
	            							{ StaticsClass.PWNCORE_LOAD_ENCODERS_SUCCESS, StaticsClass.PWNCORE_LOAD_ENCODERS_FAILED } };
	            	
	            	if (THREAD_METHOD == "deauthenticate") {
	            		isAuthenticated = false;
	    				clientTokenSet = false;
	    				CLIENT_TOKEN = "";
	
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(StaticsClass.PWNCORE_DEAUTHENTICATION_SUCCESS);
	        			sendBroadcast(tmpIntent);
	        			return;
	            	}
	            	
	            	else if (THREAD_METHOD.equals(StaticsClass.PWNCORE_CONSOLE_CREATE)) {
	            		Map<String, Value> newConDes = newConsole();
	            		
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_CREATED);
	        			tmpIntent.putExtra("id", newConDes.get("id").asRawValue().getString());
	        			tmpIntent.putExtra("prompt", newConDes.get("prompt").asRawValue().getString());
	        			tmpIntent.putExtra("busy", newConDes.get("busy").asBooleanValue().getBoolean());
	        			tmpIntent.putExtra(StaticsClass.PWNCORE_CONSOLE_TYPE, StaticsClass.PWNCORE_CONSOLE_TYPE_NONE);
	        				   
	        			
	        			String id = newConDes.get("id").asRawValue().getString();
	        			readConsole(id);
	        			
	        			if (CONSOLE_TYPE.equals(StaticsClass.PWNCORE_CONSOLE_TYPE_NMAP)) {
	        				String cmd = "nmap " + StringUtils.join(NMAP_SCAN_ARGS, " ") + " " +  NMAP_SCAN_HOST + "\n";;
	        				nmapScan(id, cmd);
	        				
	        				tmpIntent.putExtra(StaticsClass.PWNCORE_CONSOLE_TYPE, StaticsClass.PWNCORE_CONSOLE_TYPE_NMAP);
	        			}

	        			sendBroadcast(tmpIntent);
	            	}          	
	            	else if (THREAD_METHOD.equals(StaticsClass.PWNCORE_CONSOLE_READ)) {
	            		Map<String, Value> newConDes = readConsole(CONSOLE_READ_ID);
	            		
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_READ_COMPLETE);
	        			tmpIntent.putExtra("id", CONSOLE_READ_ID);
	        			tmpIntent.putExtra("data", newConDes.get("data").asRawValue().getString());
	        			tmpIntent.putExtra("prompt", newConDes.get("prompt").asRawValue().getString());
	        			tmpIntent.putExtra("busy", newConDes.get("busy").asBooleanValue().getBoolean());
	        			sendBroadcast(tmpIntent);      			
	            	}
	            	else if (THREAD_METHOD.equals(StaticsClass.PWNCORE_CONSOLE_WRITE)) {
	            		Map<String, Value> newConDes = writeConsole(CONSOLE_WRITE_DATA, CONSOLE_WRITE_ID);
	            		
	        			Intent tmpIntent = new Intent();
	        			tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_WRITE_COMPLETE);
	        			tmpIntent.putExtra("id", CONSOLE_WRITE_ID);
	        			tmpIntent.putExtra("wrote", newConDes.get("wrote").asIntegerValue().getInt());
	        			sendBroadcast(tmpIntent);      			
	            	}
	            	else if (THREAD_METHOD.equals(StaticsClass.PWNCORE_CONSOLE_DESTROY)) {
	            		if (destroyConsole(CONSOLE_DESTROY_ID)) {	            		
		        			Intent tmpIntent = new Intent();
		        			tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_DESTROYED);
		        			tmpIntent.putExtra("id", CONSOLE_DESTROY_ID);
		        			sendBroadcast(tmpIntent); 
	            		}
	            	}	            	
	            	
	            	else if (THREAD_METHOD == "get_all") {
	            		for (int i=0; i<gets.length; i++)
	            			if (getModules(gets[i])) {
			        			Intent tmpIntent = new Intent();
			        			tmpIntent.setAction(gets_msg[i][0]);
			        			//tmpIntent.putExtra("count", 0);
			        			sendBroadcast(tmpIntent); 
	            				continue;
	            			}
	            			else {
			        			Intent tmpIntent = new Intent();
			        			tmpIntent.setAction(gets_msg[i][1]);
			        			sendBroadcast(tmpIntent);  
	            				return;
	            			}
	            		
	            		return;
	            	}
	            	
	            	for (int i=0; i<gets.length && THREAD_METHOD.startsWith("gets_"); i++) { 
	            		if (THREAD_METHOD.equals("get_" + gets[i])) {
		            		if (getModules(gets[i])) {	
			        			Intent tmpIntent = new Intent();
			        			tmpIntent.setAction(gets_msg[i][0]);
			        			//tmpIntent.putExtra("count", 0);
			        			sendBroadcast(tmpIntent); 
			        			break;
		            		} else {
			        			Intent tmpIntent = new Intent();
			        			tmpIntent.setAction(gets_msg[i][1]);
			        			sendBroadcast(tmpIntent);  
			        			break;
		            		}
	            		}
	            	}	            	
            	}
				
			} catch (SocketTimeoutException e) {
				isAuthenticated = false;
    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_TIMEOUT);
    			sendBroadcast(tmpIntent);
    			
			} catch (ConnectException e) {
				isAuthenticated = false;
    			Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
    			sendBroadcast(tmpIntent);
    			
			} catch (Exception e) {		
				e.printStackTrace();
				isAuthenticated = false;
    			Intent tmpIntent = new Intent();
    			tmpIntent.putExtra("error", e.getMessage());
    			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
    			sendBroadcast(tmpIntent);
			}
    	}
    	    	
    	private void nmapScan(String id, String cmd) 
    			throws KeyManagementException, NoSuchAlgorithmException, IOException, InterruptedException {
    		
    		writeConsole(cmd, id);
    		readConsole(id);
    		Map<String, Value> tmp = readConsole(id);
    		
    		String res = tmp.get("data").asRawValue().getString();
    		
    		while(!tmp.get("data").asRawValue().getString().trim().endsWith("</nmaprun>")) {
    			Thread.sleep(500);
    			tmp = readConsole(id);
    			res += tmp.get("data").asRawValue().getString();
    		}
    		
    		db.addNmapScan(id, cmd, res);
    	}
    	
    	private byte[] connectToGetBytes(byte[] toBePostedBytes) throws IOException, KeyManagementException, NoSuchAlgorithmException {
			
        	URL url = new URL(urlStart + con_txtHost + ":" + con_txtPort + urlEnd);	 
        	HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        	
        	if (con_useSSL) {
        		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        	        @Override
        	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) { }
        	        
        	        @Override
        	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) { }
        	        
        	        @Override
        	        public X509Certificate[] getAcceptedIssuers() {
        	            return null;
        	        }
        	    } };
        	    
        	    class NullHostNameVerifier implements HostnameVerifier {
        	    	@Override
        	        public boolean verify(String hostname, SSLSession session) {
        	            return true;
        	        }
        	    };
        		
        	    final SSLContext sslContext = SSLContext.getInstance( "SSL" );
        	    sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
        	    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
    
        	    ((HttpsURLConnection)urlConn).setSSLSocketFactory( sslSocketFactory );
        	    ((HttpsURLConnection)urlConn).setHostnameVerifier(new NullHostNameVerifier());
        	}
 
	        urlConn.setDoInput (true);
	        urlConn.setDoOutput (true);
	        urlConn.setUseCaches (false);
            urlConn.setRequestMethod("POST");
            urlConn.setConnectTimeout(5000);
            urlConn.setReadTimeout(5000);
            urlConn.setRequestProperty("Content-Type","binary/message-pack");
            urlConn.connect();
   
            OutputStream toBePosted = urlConn.getOutputStream();
            
            toBePosted.write(toBePostedBytes);
            toBePosted.flush();
            toBePosted.close();

	        byte[] result = IOUtils.toByteArray(urlConn.getInputStream());
	        urlConn.disconnect();
	        
	        return result;
    	}
    	
    	public byte[] packedBytesOf(String[] s) throws IOException {   	
    		
    		MessagePack msgpack = new MessagePack();			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        Packer packer = msgpack.createPacker(out);		      	        
	        packer.write(s);		        
	
	        return out.toByteArray();
    	}
    	    	
    	private Template<Map<String, List<String>>> mapArrayTmpl = tMap(TString, tList(TString));
    	private Template<Map<String, String>> mapTmpl = tMap(TString, TString);
    	private Template<Map<String, Integer>> mapInt = tMap(TString, TInteger);
    	
    	private boolean authenticate() throws IOException, JSONException, KeyManagementException, NoSuchAlgorithmException {

            String[] request_details = { "auth.login", con_txtUsername, con_txtPassword };
            
            byte[] packedRequest = packedBytesOf(request_details);
			
            byte[] packedResponse = connectToGetBytes(packedRequest);
			
            MessagePack msgpack = new MessagePack();
            ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
            Unpacker unpacker = msgpack.createUnpacker(in);

            Map<String, String> res = unpacker.read(mapTmpl);
            
            if (res.containsKey("result") && res.get("result").equals("success")) {
				clientTokenSet = true;
				CLIENT_TOKEN = res.get("token");
				return true;
			}
			
    		return false;
    	}
    	
    	private Map<String, Integer> getStats() throws IOException, KeyManagementException, NoSuchAlgorithmException {
    		String[] request_details = { "core.module_stats", CLIENT_TOKEN };
            
            byte[] packedRequest = packedBytesOf(request_details);
			
            byte[] packedResponse = connectToGetBytes(packedRequest);
			
            MessagePack msgpack = new MessagePack();
            ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
            Unpacker unpacker = msgpack.createUnpacker(in);
            
            Map<String, Integer> res = unpacker.read(mapInt);
			return res;
    	}
    	
    	private int getCurrentCount(String  module) {
    		if (module == "exploits")
    			return db.getModulesCount(module);
    		else if (module == "payloads")
    			return db.getModulesCount(module);
    		else if (module == "post")
    			return db.getModulesCount(module);
    		else if (module == "nops")
    			return db.getModulesCount(module);
    		else if (module == "encoders")
    			return db.getModulesCount(module);
    		else if (module == "auxiliary")
    			return db.getModulesCount(module);
    		else 
    			return 0;
    	}
    	 
    	private Template<Map<String, Value>> mapConsole = tMap(TString, TValue);
    	
    	private Map<String, Value> newConsole() throws IOException, KeyManagementException, NoSuchAlgorithmException {
            String[] request_details = { "console.create", CLIENT_TOKEN };            
            byte[] packedRequest = packedBytesOf(request_details);			
            byte[] packedResponse = connectToGetBytes(packedRequest);			
            MessagePack msgpack = new MessagePack();
            ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
            Unpacker unpacker = msgpack.createUnpacker(in);           
            Map<String, Value> res = unpacker.read(mapConsole);  
    		return res;
    	}
    	
    	private Map<String, Value> readConsole(String id) throws IOException, KeyManagementException, NoSuchAlgorithmException {
            String[] request_details = { "console.read", CLIENT_TOKEN, id };            
            byte[] packedRequest = packedBytesOf(request_details);			
            byte[] packedResponse = connectToGetBytes(packedRequest);			
            MessagePack msgpack = new MessagePack();
            ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
            Unpacker unpacker = msgpack.createUnpacker(in);
            Map<String, Value> res = unpacker.read(mapConsole);
    		return res;
    	}
    	
       	private Map<String, Value> writeConsole(String data, String id) throws IOException, KeyManagementException, NoSuchAlgorithmException {
            String[] request_details = { "console.write", CLIENT_TOKEN, id, data };            
            byte[] packedRequest = packedBytesOf(request_details);			
            byte[] packedResponse = connectToGetBytes(packedRequest);			
            MessagePack msgpack = new MessagePack();
            ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
            Unpacker unpacker = msgpack.createUnpacker(in);
            Map<String, Value> res = unpacker.read(mapConsole);
    		return res;
    	}

       	private boolean destroyConsole(String id) throws IOException, KeyManagementException, NoSuchAlgorithmException {
            String[] request_details = { "console.destroy", CLIENT_TOKEN, id };            
            byte[] packedRequest = packedBytesOf(request_details);			
            byte[] packedResponse = connectToGetBytes(packedRequest);			
            MessagePack msgpack = new MessagePack();
            ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
            Unpacker unpacker = msgpack.createUnpacker(in);
            Map<String, Value> res = unpacker.read(mapConsole);
            
            if (res.containsKey("result") && 
            		res.get("result").asRawValue().getString().equals("success"))
            	return true;
            
    		return false;
    	}
       	
    	private boolean getModules(String type) throws IOException, KeyManagementException, NoSuchAlgorithmException {
    		
    		if (StatsMap.containsKey(type) && StatsMap.get(type) > getCurrentCount(type)) {
    			String[] request_details = { "module." + type, CLIENT_TOKEN };           
                byte[] packedRequest = packedBytesOf(request_details);   			
                byte[] packedResponse = connectToGetBytes(packedRequest);    			
                MessagePack msgpack = new MessagePack();
                ByteArrayInputStream in = new ByteArrayInputStream(packedResponse);
                Unpacker unpacker = msgpack.createUnpacker(in);          
                Map<String, List<String>> res = unpacker.read(mapArrayTmpl);
                
                if (res.containsKey("modules")) {
                	List<String> moduleList = res.get("modules");
                	
                	db.deleteTable(db.getTableIdByName(type));
                	db.createTable(db.getTableIdByName(type));
                	 
            		if (type == "exploits")
            			for (int i=0; i<moduleList.size(); i++)
            				db.addModule(new ModuleItem(moduleList.get(i)), type);
            		else if (type == "payloads")
            			for (int i=0; i<moduleList.size(); i++)
            				db.addModule(new ModuleItem(moduleList.get(i)), type);
            		else if (type == "auxiliary")
            			for (int i=0; i<moduleList.size(); i++)
            				db.addModule(new ModuleItem(moduleList.get(i)), type);
            		else if (type == "post")
            			for (int i=0; i<moduleList.size(); i++)
            				db.addModule(new ModuleItem(moduleList.get(i)), type);
            		else if (type == "encoders")
            			for (int i=0; i<moduleList.size(); i++)
            				db.addModule(new ModuleItem(moduleList.get(i)), type);
            		else if (type == "nops")
        				for (int i=0; i<moduleList.size(); i++)
        					db.addModule(new ModuleItem(moduleList.get(i)), type);

                	return true;
                }
                
        		return false;
    		}
    		
			return true;
		}  
    };

    public BroadcastReceiver checkConReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		
    		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	    NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    	    if (!mWifi.isConnected() && !mMobile.isConnected()) {
    	    	
    			if (isAuthenticated) {   			
    				isAuthenticated = false;
    			}
    			
    			MainActivity.prefs.edit().putBoolean("isConnected", false).commit();
    			
    	    	Intent tmpIntent = new Intent();
    			tmpIntent.setAction(StaticsClass.PWNCORE_CONNECTION_LOST);
    			sendBroadcast(tmpIntent);
    			
    			
    	    }
    	}
    };
    
}
