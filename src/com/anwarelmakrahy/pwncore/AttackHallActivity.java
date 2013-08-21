package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class AttackHallActivity extends Activity {
	
	private ArrayList<TargetHostItem> mTargets = MainActivity.mTargetHostList;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_attackhall);
        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
	}
	
	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();	
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_LOST);
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}
			
		isConnected = prefs.getBoolean("isConnected", false);
		super.onResume();
	}
    
	private boolean conStatusReceiverRegistered = false;
	private boolean isConnected = true;
	private SharedPreferences prefs;
	
	@Override
	public void onDestroy() {		
		if (conStatusReceiverRegistered ) {
			unregisterReceiver(conStatusReceiver);
			conStatusReceiverRegistered = false;
		}
		super.onDestroy();
	}
    
    public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticsClass.PWNCORE_CONNECTION_TIMEOUT) {		
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionTimeout: Please check that server is running", 
    					Toast.LENGTH_SHORT).show();
    		}  				
    		else if (action == StaticsClass.PWNCORE_CONNECTION_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionFailed: " + intent.getStringExtra("error"), 
    					Toast.LENGTH_SHORT).show();    	
    		}		
    		else if (action == StaticsClass.PWNCORE_CONNECTION_LOST) {
    			prefs.edit().putBoolean("isConnected", false).commit();
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionLost: Please check your network settings", 
    					Toast.LENGTH_SHORT).show();
    			finish();
    		}
    	}
    };
}
