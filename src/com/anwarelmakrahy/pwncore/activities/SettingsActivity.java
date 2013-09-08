package com.anwarelmakrahy.pwncore.activities;

import com.anwarelmakrahy.pwncore.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class SettingsActivity extends Activity {
	
	private SharedPreferences prefs;
	private CheckBox con_chkSSL, chkDebug;
	private EditText con_txtUsername, con_txtPassword, con_txtHost, con_txtPort;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpc);

        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
        
        chkDebug = (CheckBox)findViewById(R.id.chkDebug);
        con_chkSSL = (CheckBox)findViewById(R.id.chkSSL);
        con_txtUsername = (EditText)findViewById(R.id.txtUsername);
        con_txtPassword = (EditText)findViewById(R.id.txtPassword);
        con_txtHost = (EditText)findViewById(R.id.txtHost);
        con_txtPort = (EditText)findViewById(R.id.txtPort);
        
        chkDebug.setChecked(prefs.getBoolean("debug_mode", false));
        con_chkSSL.setChecked(prefs.getBoolean("connection_useSSL", false));
        con_txtUsername.setText(prefs.getString("connection_Username", ""));
        con_txtPassword.setText(prefs.getString("connection_Password", ""));
        con_txtHost.setText(prefs.getString("connection_Host", ""));
        con_txtPort.setText(prefs.getString("connection_Port", "55553"));    
        
        Button button= (Button) findViewById(R.id.cmdCancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        button= (Button) findViewById(R.id.cmdSaveSettings);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       	
            	saveSettings();
                finish();
            }
        });
       
    }
	
	@Override
	public void onBackPressed() {
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    	.setCancelable(false)
    	.setTitle("Save Settings")
    	.setMessage("Do you want to save current settings ?")
    	.setIcon(android.R.drawable.ic_menu_preferences)
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	saveSettings();
    	    	finish();
    	    }
    	})
    	.setNegativeButton("No", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	finish();
    	    }
    	})
    	.setNeutralButton("Cancel", null)
    	.show();
	}
	
	private void saveSettings() {
    	SharedPreferences.Editor editor = prefs.edit();
    	
    	editor.putBoolean("debug_mode", chkDebug.isChecked());
    	editor.putBoolean("connection_useSSL", con_chkSSL.isChecked());
    	editor.putString("connection_Port", con_txtPort.getText().toString());
    	editor.putString("connection_Host", con_txtHost.getText().toString());
    	editor.putString("connection_Username", con_txtUsername.getText().toString());
    	editor.putString("connection_Password", con_txtPassword.getText().toString());
    	
    	editor.commit();
	}
}
