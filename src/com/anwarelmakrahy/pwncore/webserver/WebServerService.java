package com.anwarelmakrahy.pwncore.webserver;

import com.anwarelmakrahy.pwncore.StaticClass;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class WebServerService extends Service {

	private WebServer server = null;
	private SharedPreferences prefs; 

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		prefs = this.getSharedPreferences(StaticClass.PWNCORE_PACKAGE_NAME, Context.MODE_PRIVATE);
		server = new WebServer(this,prefs.getInt("webserver_port", 8080));
		server.startServer();
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i(StaticClass.PWNCORE_WEB_TAG, "Destroying pwnCore WebService");
		server.stopServer();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}