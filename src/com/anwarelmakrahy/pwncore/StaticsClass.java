package com.anwarelmakrahy.pwncore;

import java.net.URISyntaxException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class StaticsClass {
	
	static String PWNCORE_CONNECTION_SUCCESS = "CONNECTION_SUCCESS";
	static String PWNCORE_CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
	static String PWNCORE_CONNECTION_FAILED = "CONNECTION_FAILED";
	static String PWNCORE_CONNECTION_LOST = "CONNECTION_LOST";
	static String PWNCORE_CONNECTION_CHECK = "CONNECTION_CHECK";
	
	static String PWNCORE_CONNECT = "CONNECT";
	static String PWNCORE_DISCONNECT = "DISCONNECT";
	
	
	static String PWNCORE_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
	
	static String PWNCORE_LOAD_EXPLOITS = "LOAD_EXPLOITS";
	static String PWNCORE_LOAD_EXPLOITS_SUCCESS = "LOAD_EXPLOITS_SUCCESS";
	static String PWNCORE_LOAD_EXPLOITS_FAILED = "LOAD_EXPLOITS_FAILED";
	
	static String PWNCORE_LOAD_ENCODERS = "LOAD_ENCODERS";
	static String PWNCORE_LOAD_ENCODERS_SUCCESS = "LOAD_ENCODERS_SUCCESS";
	static String PWNCORE_LOAD_ENCODERS_FAILED = "LOAD_ENCODERS_FAILED";
	
	static String PWNCORE_LOAD_POSTS = "LOAD_POSTS";
	static String PWNCORE_LOAD_POSTS_SUCCESS = "LOAD_POSTS_SUCCESS";
	static String PWNCORE_LOAD_POSTS_FAILED = "LOAD_POSTS_FAILED";
	
	static String PWNCORE_LOAD_NOPS = "LOAD_NOPS";
	static String PWNCORE_LOAD_NOPS_SUCCESS = "LOAD_NOPS_SUCCESS";
	static String PWNCORE_LOAD_NOPS_FAILED = "LOAD_NOPS_FAILED";
	
	static String PWNCORE_LOAD_PAYLOADS = "LOAD_PAYLOADS";
	static String PWNCORE_LOAD_PAYLOADS_SUCCESS = "LOAD_PAYLOADS_SUCCESS";
	static String PWNCORE_LOAD_PAYLOADS_FAILED = "LOAD_PAYLOADS_FAILED";
	
	static String PWNCORE_LOAD_AUXILIARY = "LOAD_AUXILIARY";
	static String PWNCORE_LOAD_AUXILIARY_SUCCESS = "LOAD_AUXILIARY_SUCCESS";
	static String PWNCORE_LOAD_AUXILIARY_FAILED = "LOAD_AUXILIARY_FAILED";
	
	static String PWNCORE_LOAD_ALL_MODULES = "LOAD_ALL_MODULES";
	
	static String PWNCORE_NMAP_SCAN_HOST = "NMAP_SCAN_HOST";
	static String PWNCORE_NMAP_SCAN_ARGS = "NMAP_SCAN_ARGS";
	static String PWNCORE_NMAP_SCAN_FILENAME = "NMAP_SCAN_FILE_";
	
	static String PWNCORE_CONSOLE_TYPE = "CONSOLE_TYPE";
	static String PWNCORE_CONSOLE_TYPE_NMAP = "CONSOLE_TYPE_NMAP";
	static String PWNCORE_CONSOLE_TYPE_NONE = "CONSOLE_TYPE_NONE";
	
	static String PWNCORE_CONSOLE_CREATE = "CONSOLE_CREATE";
	static String PWNCORE_CONSOLE_WRITE = "CONSOLE_WRITE";
	static String PWNCORE_CONSOLE_READ = "CONSOLE_READ";
	static String PWNCORE_CONSOLE_DESTROY = "CONSOLE_DESTROY";
	static String PWNCORE_CONSOLE_RUN_MODULE = "CONSOLE_RUN_MODULE";

	static String PWNCORE_CONSOLE_METERPRETER_WRITE = "CONSOLE_METERPRETER_WRITE";
	static String PWNCORE_CONSOLE_METERPRETER_READ = "CONSOLE_METERPRETER_READ";
	static String PWNCORE_CONSOLE_METERPRETER_DESTROY = "CONSOLE_METERPRETER_DESTORY";
	static String PWNCORE_CONSOLE_SHELL_WRITE = "CONSOLE_SHELL_WRITE";
	static String PWNCORE_CONSOLE_SHELL_READ = "CONSOLE_SHELL_READ";
	static String PWNCORE_CONSOLE_SHELL_DESTROY = "CONSOLE_SHELL_DESTROY";
	
	static String PWNCORE_NOTIFY_ADAPTER_UPDATE = "NOTIFY_ADAPTER_UPDATE";
	
	public static boolean isNumeric(String str) {  
		try {  Double.parseDouble(str); } 
		catch(NumberFormatException nfe) { return false; }  
		return true;  
	}
	
	public final static boolean validateIPAddress(String ipAddress, boolean allowSub) {
	    String[] parts = ipAddress.split("/")[0].split( "\\." );
	    if (parts.length != 4) { return false; }
	    for (String s : parts) {
	    	try { 		
		        int i = Integer.parseInt(s);
		        if ((i < 0) || (i > 255)) { return false; }	        
		        if (ipAddress.split("/").length > 2 || 
		        		(ipAddress.split("/").length == 2 && 
		        		!isNumeric(ipAddress.split("/")[1])) || (ipAddress.split("/").length > 1 && !allowSub))
		        	return false;	        
	    	} catch (Exception e) {
	    		return false;
	    	}
	    }
	    return true;
	}
	
	public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    } 
}
