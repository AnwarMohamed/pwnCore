package com.anwarelmakrahy.pwncore.plugins;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MetFileBrowserActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_filebrowser); 
    	
    	Intent intent = getIntent();

        if (intent == null || !intent.hasExtra("id")) { 
			Toast.makeText(getApplicationContext(), 
					"Error launching file browser", 
					Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        
        String id = intent.getStringExtra("id");
        
		MetFileBrowser session = (MetFileBrowser) MainService.sessionMgr.getSession(id);
		if (session == null) {
			Toast.makeText(getApplicationContext(), 
					"Invalid session id", 
					Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
    	
    }
}
