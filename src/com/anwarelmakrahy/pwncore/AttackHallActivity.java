package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class AttackHallActivity extends Activity {
	
	private TabHost tabHost;
	private ListView mTargetsListView;
	private TargetsListAdapter mTargetsListAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_attackhall);
        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
        
        mTargetsListView = (ListView)findViewById(R.id.targetsListView2);
        mTargetsListAdapter =  new TargetsListAdapter(this, MainActivity.mTargetHostList);
        mTargetsListView.setAdapter(mTargetsListAdapter);
        registerForContextMenu(mTargetsListView);
        
        setupTabHost();
        setupListViewListener();
	}
	
	private void setupListViewListener() {
		mTargetsListView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
	        	
	    		Object target = mTargetsListView.getItemAtPosition(position);
	        	TargetItem targetDetails = (TargetItem)target;
	        	
	        	mTargetsListAdapter.setSelectedIndex(position);
	        	tabHost.setCurrentTab(1);
	    	}
		});
    }


	private void setupTabHost() {
        tabHost=(TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
      
        TabSpec spec1=tabHost.newTabSpec("Attacks");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Attacks"); 
      
        TabSpec spec2=tabHost.newTabSpec("Target Details");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Target Details");
      
        TabSpec spec3=tabHost.newTabSpec("Console Sessions");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Console Sessions");
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
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
