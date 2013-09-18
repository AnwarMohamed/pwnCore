package com.anwarelmakrahy.pwncore.webserver;

import com.anwarelmakrahy.pwncore.StaticClass;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WebServerService extends Service {

	private WebServer server = null;

	@Override
	public void onCreate() {
		Log.i(StaticClass.PWNCORE_WEB_TAG, "Creating and starting pwnCore WebService");
		super.onCreate();
		server = new WebServer(this);
		server.startServer();
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