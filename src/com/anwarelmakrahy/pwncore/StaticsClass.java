package com.anwarelmakrahy.pwncore;

import java.net.URISyntaxException;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class StaticsClass {
	
	static String PWNCORE_CONNECTION_SUCCESS = "com.anwarelmakrahy.pwncore.CONNECTION_SUCCESS";
	static String PWNCORE_CONNECTION_TIMEOUT = "com.anwarelmakrahy.pwncore.CONNECTION_TIMEOUT";
	static String PWNCORE_CONNECTION_FAILED = "com.anwarelmakrahy.pwncore.CONNECTION_FAILED";
	static String PWNCORE_CONNECTION_LOST = "com.anwarelmakrahy.pwncore.CONNECTION_LOST";
	static String PWNCORE_CONNECTION_CHECK = "com.anwarelmakrahy.pwncore.CONNECTION_CHECK";
	
	static String PWNCORE_CONNECT = "com.anwarelmakrahy.pwncore.CONNECT";
	static String PWNCORE_DISCONNECT = "com.anwarelmakrahy.pwncore.DISCONNECT";
	
	static String PWNCORE_SEND_TESTDATA = "com.anwarelmakrahy.pwncore.SEND_TESTDATA";
	
	static String PWNCORE_AUTHENTICATION_FAILED = "com.anwarelmakrahy.pwncore.AUTHENTICATION_FAILED";
	static String PWNCORE_DEAUTHENTICATION_SUCCESS = "com.anwarelmakrahy.pwncore.DEAUTHENTICATION_SUCCESS";
	
	static String PWNCORE_LOAD_EXPLOITS = "com.anwarelmakrahy.pwncore.LOAD_EXPLOITS";
	static String PWNCORE_LOAD_EXPLOITS_SUCCESS = "com.anwarelmakrahy.pwncore.LOAD_EXPLOITS_SUCCESS";
	static String PWNCORE_LOAD_EXPLOITS_FAILED = "com.anwarelmakrahy.pwncore.LOAD_EXPLOITS_FAILED";
	
	static String PWNCORE_LOAD_ENCODERS = "com.anwarelmakrahy.pwncore.LOAD_ENCODERS";
	static String PWNCORE_LOAD_ENCODERS_SUCCESS = "com.anwarelmakrahy.pwncore.LOAD_ENCODERS_SUCCESS";
	static String PWNCORE_LOAD_ENCODERS_FAILED = "com.anwarelmakrahy.pwncore.LOAD_ENCODERS_FAILED";
	
	static String PWNCORE_LOAD_POSTS = "com.anwarelmakrahy.pwncore.LOAD_POSTS";
	static String PWNCORE_LOAD_POSTS_SUCCESS = "com.anwarelmakrahy.pwncore.LOAD_POSTS_SUCCESS";
	static String PWNCORE_LOAD_POSTS_FAILED = "com.anwarelmakrahy.pwncore.LOAD_POSTS_FAILED";
	
	static String PWNCORE_LOAD_NOPS = "com.anwarelmakrahy.pwncore.LOAD_NOPS";
	static String PWNCORE_LOAD_NOPS_SUCCESS = "com.anwarelmakrahy.pwncore.LOAD_NOPS_SUCCESS";
	static String PWNCORE_LOAD_NOPS_FAILED = "com.anwarelmakrahy.pwncore.LOAD_NOPS_FAILED";
	
	static String PWNCORE_LOAD_PAYLOADS = "com.anwarelmakrahy.pwncore.LOAD_PAYLOADS";
	static String PWNCORE_LOAD_PAYLOADS_SUCCESS = "com.anwarelmakrahy.pwncore.LOAD_PAYLOADS_SUCCESS";
	static String PWNCORE_LOAD_PAYLOADS_FAILED = "com.anwarelmakrahy.pwncore.LOAD_PAYLOADS_FAILED";
	
	static String PWNCORE_LOAD_AUXILIARY = "com.anwarelmakrahy.pwncore.LOAD_AUXILIARY";
	static String PWNCORE_LOAD_AUXILIARY_SUCCESS = "com.anwarelmakrahy.pwncore.LOAD_AUXILIARY_SUCCESS";
	static String PWNCORE_LOAD_AUXILIARY_FAILED = "com.anwarelmakrahy.pwncore.LOAD_AUXILIARY_FAILED";
	
	static String PWNCORE_LOAD_ALL_MODULES = "com.anwarelmakrahy.pwncore.LOAD_ALL_MODULES";
	
	static String PWNCORE_NMAP_SCAN_HOST = "com.anwarelmakrahy.pwncore.NMAP_SCAN_HOST";
	static String PWNCORE_NMAP_SCAN_ARGS = "com.anwarelmakrahy.pwncore.NMAP_SCAN_ARGS";
	static String PWNCORE_NMAP_SCAN_FILENAME = "NMAP_SCAN_FILE_";
	
	static String PWNCORE_CONSOLE_TYPE = "com.anwarelmakrahy.pwncore.CONSOLE_TYPE";
	static String PWNCORE_CONSOLE_TYPE_NMAP = "com.anwarelmakrahy.pwncore.CONSOLE_TYPE_NMAP";
	static String PWNCORE_CONSOLE_TYPE_NONE = "com.anwarelmakrahy.pwncore.CONSOLE_TYPE_NONE";
	
	static String PWNCORE_CONSOLE_CREATE = "com.anwarelmakrahy.pwncore.CONSOLE_CREATE";
	
	static String PWNCORE_CONSOLE_CREATED = "com.anwarelmakrahy.pwncore.CONSOLE_CREATED";
	static String PWNCORE_CONSOLE_WRITE = "com.anwarelmakrahy.pwncore.CONSOLE_WRITE";
	static String PWNCORE_CONSOLE_WRITE_COMPLETE = "com.anwarelmakrahy.pwncore.CONSOLE_WRITE_COMPLETE";
	static String PWNCORE_CONSOLE_READ = "com.anwarelmakrahy.pwncore.CONSOLE_READ";
	static String PWNCORE_CONSOLE_READ_COMPLETE = "com.anwarelmakrahy.pwncore.CONSOLE_READ_COMPLETE";
	static String PWNCORE_CONSOLE_DESTROY = "com.anwarelmakrahy.pwncore.CONSOLE_DESTROY";
	static String PWNCORE_CONSOLE_DESTROYED = "com.anwarelmakrahy.pwncore.CONSOLE_DESTROYED";
	
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
