package com.anwarelmakrahy.pwncore;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class StaticClass {

	public static String PWNCORE_SERVICE_STARTED = "SERVICE_STARTED";
	public static String PWNCORE_WEB_TAG = "pwnCore-WebServer";
	public static String PWNCORE_TAG = "pwnCore";

	public static String PWNCORE_CONNECTION_SUCCESS = "CONNECTION_SUCCESS";
	public static String PWNCORE_CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
	public static String PWNCORE_CONNECTION_FAILED = "CONNECTION_FAILED";
	public static String PWNCORE_CONNECTION_LOST = "CONNECTION_LOST";
	public static String PWNCORE_CONNECTION_CHECK = "CONNECTION_CHECK";

	public static String PWNCORE_CONNECT = "CONNECT";
	public static String PWNCORE_DISCONNECT = "DISCONNECT";

	public static String PWNCORE_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";

	public static String PWNCORE_LOAD_EXPLOITS = "LOAD_EXPLOITS";
	public static String PWNCORE_LOAD_EXPLOITS_SUCCESS = "LOAD_EXPLOITS_SUCCESS";
	public static String PWNCORE_LOAD_EXPLOITS_FAILED = "LOAD_EXPLOITS_FAILED";

	public static String PWNCORE_LOAD_ENCODERS = "LOAD_ENCODERS";
	public static String PWNCORE_LOAD_ENCODERS_SUCCESS = "LOAD_ENCODERS_SUCCESS";
	public static String PWNCORE_LOAD_ENCODERS_FAILED = "LOAD_ENCODERS_FAILED";

	public static String PWNCORE_LOAD_POSTS = "LOAD_POSTS";
	public static String PWNCORE_LOAD_POSTS_SUCCESS = "LOAD_POSTS_SUCCESS";
	public static String PWNCORE_LOAD_POSTS_FAILED = "LOAD_POSTS_FAILED";

	public static String PWNCORE_LOAD_NOPS = "LOAD_NOPS";
	public static String PWNCORE_LOAD_NOPS_SUCCESS = "LOAD_NOPS_SUCCESS";
	public static String PWNCORE_LOAD_NOPS_FAILED = "LOAD_NOPS_FAILED";

	public static String PWNCORE_LOAD_PAYLOADS = "LOAD_PAYLOADS";
	public static String PWNCORE_LOAD_PAYLOADS_SUCCESS = "LOAD_PAYLOADS_SUCCESS";
	public static String PWNCORE_LOAD_PAYLOADS_FAILED = "LOAD_PAYLOADS_FAILED";

	public static String PWNCORE_LOAD_AUXILIARY = "LOAD_AUXILIARY";
	public static String PWNCORE_LOAD_AUXILIARY_SUCCESS = "LOAD_AUXILIARY_SUCCESS";
	public static String PWNCORE_LOAD_AUXILIARY_FAILED = "LOAD_AUXILIARY_FAILED";

	public static String PWNCORE_LOAD_ALL_MODULES = "LOAD_ALL_MODULES";

	public static String PWNCORE_NMAP_SCAN_HOST = "NMAP_SCAN_HOST";
	public static String PWNCORE_NMAP_SCAN_ARGS = "NMAP_SCAN_ARGS";
	public static String PWNCORE_NMAP_SCAN_FILENAME = "NMAP_SCAN_FILE_";

	public static String PWNCORE_CONSOLE_TYPE = "CONSOLE_TYPE";
	public static String PWNCORE_CONSOLE_TYPE_NMAP = "CONSOLE_TYPE_NMAP";
	public static String PWNCORE_CONSOLE_TYPE_NONE = "CONSOLE_TYPE_NONE";

	public static String PWNCORE_CONSOLE_CREATE = "CONSOLE_CREATE";
	public static String PWNCORE_CONSOLE_WRITE = "CONSOLE_WRITE";
	public static String PWNCORE_CONSOLE_READ = "CONSOLE_READ";
	public static String PWNCORE_CONSOLE_DESTROY = "CONSOLE_DESTROY";
	public static String PWNCORE_CONSOLE_RUN_MODULE = "CONSOLE_RUN_MODULE";

	public static String PWNCORE_CONSOLE_METERPRETER_WRITE = "CONSOLE_METERPRETER_WRITE";
	public static String PWNCORE_CONSOLE_METERPRETER_READ = "CONSOLE_METERPRETER_READ";
	public static String PWNCORE_CONSOLE_METERPRETER_DESTROY = "CONSOLE_METERPRETER_DESTORY";
	public static String PWNCORE_CONSOLE_SHELL_WRITE = "CONSOLE_SHELL_WRITE";
	public static String PWNCORE_CONSOLE_SHELL_READ = "CONSOLE_SHELL_READ";
	public static String PWNCORE_CONSOLE_SHELL_DESTROY = "CONSOLE_SHELL_DESTROY";

	public static String PWNCORE_NOTIFY_ADAPTER_UPDATE = "NOTIFY_ADAPTER_UPDATE";

	public static String PWNCORE_NOT_IMPLEMENTED = "Sorry not implemented yet";
	public static String PWNCORE_PACKAGE_NAME = "com.anwarelmakrahy.pwncore";

	public static String PWNCORE_DATABASE_UPDATE_STARTED = "DATABASE_UPDATE_STARTED";
	public static String PWNCORE_DATABASE_UPDATE_STOPPED = "DATABASE_UPDATE_STOPPED";

	public static String PWNCORE_SESSION_GET_AVAILABLE_COMMANDS = "SESSION_GET_AVAILABLE_COMMANDS";
	
	public static String PWNCORE_SESSION_SCREENSHOT = "SESSION_SCREENSHOT";
	public static String PWNCORE_SESSION_GET_SCREENSHOT_SIZE = "SESSION_GET_SCREENSHOT_SIZE";
	public static String PWNCORE_SESSION_GET_SCREENSHOT_FILE = "SESSION_GET_SCREENSHOT_FILE";
	
	public static String PWNCORE_SESSION_WEBCAM_LIST = "SESSION_WEBCAM_LIST";
	public static String PWNCORE_SESSION_WEBCAM_SNAP = "SESSION_WEBCAM_SNAP";
	public static String PWNCORE_SESSION_GET_WEBCAM_SNAP_SIZE = "SESSION_GET_WEBCAM_SNAP_SIZE";
	public static String PWNCORE_SESSION_GET_WEBCAM_SNAP_FILE = "SESSION_GET_WEBCAM_SNAP_FILE";
	
	public static String PWNCORE_SESSION_KILL_PROCESS = "SESSION_KILL_PROCESS";
	
	public static String[] PWNCORE_SESSION_IMPLEMENTED_COMMANDS = {
		"screenshot",
		"webcam_snap",
		"getuid",
		"sysinfo",
		"idletime",
		"ps",
	};
	
	public static List<String> getSessionImplCmds() {
		return Arrays.asList(PWNCORE_SESSION_IMPLEMENTED_COMMANDS);
	}
	
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public final static boolean validateIPAddress(String ipAddress,
			boolean allowSub) {
		String[] parts = ipAddress.split("/")[0].split("\\.");
		if (parts.length != 4) {
			return false;
		}
		for (String s : parts) {
			try {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
				if (ipAddress.split("/").length > 2
						|| (ipAddress.split("/").length == 2 && !isNumeric(ipAddress
								.split("/")[1]))
						|| (ipAddress.split("/").length > 1 && !allowSub))
					return false;
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	public static String getPath(Context context, Uri uri)
			throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}
	
	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public static String[] osTitles = { "Windows 2000", "Windows XP",
		"Windows 7", "Windows 8", "Windows Server 2003",
		"Windows Server 2008 R2", "Windows Server 2008", "Linux",
		"Android", "Cisco IOS", "FreeBSD", "NetBSD", "Mac OS X", "OpenBSD",
		"Printer", "Solaris", "Unknown" };
	
	public static int[] osImages = { R.drawable.windows2000,
			R.drawable.windowsxp, R.drawable.windows7, R.drawable.windows8,
			R.drawable.winserver, R.drawable.winserver, R.drawable.winserver,
			R.drawable.linux, R.drawable.android, R.drawable.cisco,
			R.drawable.bsd, R.drawable.bsd, R.drawable.macosx, R.drawable.bsd,
			R.drawable.printer, R.drawable.solaris, R.drawable.unknown };
}
