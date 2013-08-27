package com.anwarelmakrahy.pwncore;

import com.anwarelmakrahy.pwncore.ConsoleSession.ConsoleSessionParams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        setContentView(R.layout.activity_console);
        setTitle("Console Session");
        
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
             
        if (intent == null || type == null) { 
			Toast.makeText(getApplicationContext(), 
					"Error launching console", 
					Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        
        if (type.startsWith("current"))
        	getCurrentConsole(intent.getStringExtra("id"));
        else if (type.startsWith("new"))
        	getNewConsole();
        else {
        	finish();
        	return;
        }
	}
	
	@Override
	public void onDestroy() {		
		if (console != null && console.isReady())
			MainService.sessionMgr.destroyConsole(console);
		super.onDestroy();
	}
	
	private void getCurrentConsole(String id) {
		
	}
	
	private void getNewConsole() {
		 final EditText commander = (EditText)findViewById(R.id.consoleWrite);
		    final ScrollView scroller = (ScrollView)findViewById(R.id.textAreaScroller);		    
		    ((TextView)findViewById(R.id.consoleRead)).addTextChangedListener(new TextWatcher() {
             @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
             @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
             @Override
             public void afterTextChanged(Editable s) {
             	Log.d("scrollview", "scrollview");
             	scroller.post(new Runnable() {            
             	    @Override
             	    public void run() {
             	           scroller.fullScroll(View.FOCUS_DOWN);              
             	    }
             	});
             }
         });
		    		    
		    ConsoleSessionParams params = new ConsoleSessionParams();
		    params.setAcivity(this);
		    params.setCmdView((TextView)findViewById(R.id.consoleRead));
		    params.setPromptView((TextView)findViewById(R.id.consolePrompt));
		    
		    console = MainService.sessionMgr.getNewConsole(params);
		    MainService.sessionMgr.switchConsoleWindow(console.getId());
		    
		    commander.setOnEditorActionListener(new OnEditorActionListener() {

		    	String cmd = null;
		    	
	    		@Override
		        public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
	    			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){               

	    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    				imm.hideSoftInputFromWindow(commander.getWindowToken(), 0);
		               
	    				cmd = v.getText().toString();
	    				v.setText("");
	    				
						new Thread(new Runnable() {
						    public void run() {
						    	console.write(cmd);
						    }
						  }).start();
					
		               return true;
		            }
	    			
		            return false;
		        }
		    });
	}
}
