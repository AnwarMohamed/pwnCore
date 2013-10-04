package com.anwarelmakrahy.pwncore.activities;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.console.ControlSession;
import com.anwarelmakrahy.pwncore.fragments.HostsFragment;
import com.anwarelmakrahy.pwncore.structures.HostItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HostSessionsActivity extends Activity {
	
	private HostItem host;
	private String currentSessionId;
	
	private static final int BAD_ID = 9999;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        
        Intent intent = getIntent();
        int hostId;
        
        if (!intent.hasExtra("hostId") || 
        		(hostId = intent.getIntExtra("hostId", BAD_ID)) == BAD_ID ||
        		MainService.hostsList.size() <= hostId ||
        		(host = MainService.hostsList.get(hostId)) == null) {
        	finish();
        	return;
        }
        
        setContentView(R.layout.activity_hostsessions);
        openContextMenu(findViewById(R.id.sessionCommandsList));
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		String sessionId;
		ControlSession session;
		for (int i=0; i<host.getActiveSessions().get("meterpreter").size(); i++) {
			sessionId = host.getActiveSessions().get("meterpreter").get(i);
			if ((session = MainService.sessionMgr.getSession(sessionId)) != null)
				menu.add("["+ sessionId +"] Meterpreter @ " + session.getViaPayload());
		}
		
		for (int i=0; i<host.getActiveSessions().get("shell").size(); i++) {
			sessionId = host.getActiveSessions().get("shell").get(i);
			if ((session = MainService.sessionMgr.getSession(sessionId)) != null)
				menu.add("["+ sessionId +"] Shell @ " + session.getViaPayload());
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sessions, menu);
        return true;
    }
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {    
	    case R.id.mnuSessionTerminate:
	    	if (currentSessionId != null)
	    		MainService.sessionMgr.destroySession(currentSessionId);
	    	finish();
	    	return true;
	    	
	    case R.id.mnuSessionSwitch:
	    	openContextMenu(findViewById(R.id.sessionCommandsList));
	    	return true;
	    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
