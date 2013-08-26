package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.msgpack.type.Value;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class ModuleOptionsActivity extends Activity {
	
	String moduleName = null;
	String moduleType = null;
	boolean isLoaded = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        setContentView(R.layout.activity_moduleoptions);
        setTitle("Module Options");

        Intent intent = getIntent();
        moduleName = intent.getStringExtra("name");
        moduleType = intent.getStringExtra("type");
        
        if (moduleType.contains("exploits"))
        	moduleType = "exploit";
        else if (moduleType.contains("payloads"))
        	moduleType = "payload";
        else if (moduleType.contains("encoders"))
        	moduleType = "encoder";
        else if (moduleType.contains("nops"))
        	moduleType = "nop";
        else if (moduleType.contains("posts"))
        	moduleType = "post";
        
        Toast.makeText(getApplicationContext(), moduleType, Toast.LENGTH_SHORT).show();
        
        loadOptions();
        
        new AsyncTask<Void, Void, Void>() {
        	private ProgressDialog pd = null;
        	
    		@Override protected void onPreExecute() {
    			pd = ProgressDialog.show(
    	                ModuleOptionsActivity.this,
    	                null,
    	                "Loading Module Options",
    	                true,
    	                true,
    	                new DialogInterface.OnCancelListener(){
    	                    @Override
    	                    public void onCancel(DialogInterface dialog) {
    	                        cancel(true);
    	                    }
    	                }
    	        );
    		}
    			
    		@Override protected Void doInBackground(Void... arg0) {
    			while (!isLoaded) {
	    			try {
	    				Thread.sleep(500);
	    			} catch (InterruptedException e) {
	    				if (!isLoaded) finish();
	    			}	
    			}
    			return null;
    		}
    		
    		@Override protected void onPostExecute(Void result) {
    			if (pd!=null) { pd.dismiss(); }
    		}  			
    	}
    	.execute((Void[])null);
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	private void loadOptions() {
		new Handler().postAtTime(new Runnable() {
			@Override public void run() {
		        List<Object> params = new ArrayList<Object>();
		        params.add("module.info");
		        params.add(moduleType);
		        params.add(moduleName);
		        parseInfo(MainService.client.call(params));
		        
		        params = new ArrayList<Object>();
		        params.add("module.options");
		        params.add(moduleType);
		        params.add(moduleName); 
		        parseOptions(MainService.client.call(params));		
			}}, 0);
	}
	
	private void parseInfo(Map<String, Value> info) {
		if (info == null) finish();
	}
	
	private void parseOptions(Map<String, Value> opts) {
		if (opts == null) finish();
	}
}
