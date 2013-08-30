package com.anwarelmakrahy.pwncore;

import com.anwarelmakrahy.pwncore.ConsoleSession.ConsoleSessionParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ConsoleActivity extends Activity {
	
	private ConsoleSession console = null;
	private Intent intent = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        setContentView(R.layout.activity_console);
        setTitle("Console Session");
        
        intent = getIntent();

        if (intent == null || !intent.hasExtra("type")) { 
			Toast.makeText(getApplicationContext(), 
					"Error launching console", 
					Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        
        String type = intent.getStringExtra("type");
        
        if (type.startsWith("current"))
        	getCurrentConsole(intent.getStringExtra("id"));
        else if (type.startsWith("new"))
        	getNewConsole();
        else {
        	finish();
        	return;
        }
        
	}
	
	private void getCurrentConsole(String id) {
		console = MainService.sessionMgr.getConsole(id);
		if (console == null) {
			Toast.makeText(getApplicationContext(), 
					"Invalid console id", 
					Toast.LENGTH_SHORT).show();
        	finish();
        	return;
		}
		
	    MainService.sessionMgr.switchConsoleWindow(console.getId(), this);	    
	    setupWindowTrigger();
	    
	    if (intent.hasExtra("cmd"))
	    	new Thread(new Runnable() { public void run() {	
	    		console.waitForReady();
	    		console.write(intent.getStringExtra("cmd"));
	    		} }).start();	
	}
	
	private void getNewConsole() { 		    
	    ConsoleSessionParams params = new ConsoleSessionParams();
	    params.setAcivity(this);
	    params.setCmdViewId(R.id.consoleRead);
	    params.setPromptViewId(R.id.consolePrompt);
		    
	    console = MainService.sessionMgr.getNewConsole(params);
	    MainService.sessionMgr.switchConsoleWindow(console.getId(), this);
	    
	    setupWindowTrigger();
	    
	    if (intent.getStringExtra("cmd") != null)
	    	new Thread(new Runnable() { public void run() {	
	    		console.waitForReady();
	    		console.write(intent.getStringExtra("cmd"));
	    		} }).start();	
	}
	
	private void setupWindowTrigger() {
		final EditText commander = (EditText)findViewById(R.id.consoleWrite);
	    final ScrollView scroller = (ScrollView)findViewById(R.id.textAreaScroller);
	    
	    ((TextView)findViewById(R.id.consoleRead)).addTextChangedListener(new TextWatcher() {
             @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
             @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
             @Override
             public void afterTextChanged(Editable s) {
             	scroller.post(new Runnable() {            
             	    @Override
             	    public void run() {
             	           scroller.fullScroll(View.FOCUS_DOWN);              
             	    }
             	});
             }
         });
	    
	    commander.setOnEditorActionListener(new OnEditorActionListener() {
	    	String cmd = null;	    	
    		@Override
	        public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
    			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){               
    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    				imm.hideSoftInputFromWindow(commander.getWindowToken(), 0);
    				cmd = v.getText().toString();
    				v.setText("");
					new Thread(new Runnable() { public void run() {	console.write(cmd); } }).start();			
					return true;
	            }    			
	            return false;
	        }
	    });	
	}
	
	private float consoleFontSize = 12;
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            if (action == KeyEvent.ACTION_UP) {
            	((TextView)findViewById(R.id.consoleRead)).
            		setTextSize(++consoleFontSize);
            }
            return true;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (action == KeyEvent.ACTION_UP) {
            	((TextView)findViewById(R.id.consoleRead)).
        		setTextSize(--consoleFontSize);
            }
            return true;
        default:
            return super.dispatchKeyEvent(event);
        }
    }
	
	@Override
	public void onBackPressed() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    	.setTitle("Terminate Console")	
    	.setMessage("Do you want to terminate console ?")
    	.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    			if (console != null && console.isReady())
    				MainService.sessionMgr.destroyConsole(console);
    	    	finish();
    	    }
    	})
    	.setNegativeButton("Run in Background", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	MainService.sessionMgr.closeConsoleWindow(console.getId());
    	    	finish();
    	    }
    	})
    	.setCancelable(false)
    	.show();
	}
}
