package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class ConsoleSession {

	private String msfId= null;
	private String id, prompt;
	private Context context;
	private TextView promptView, cmdView;
	private Activity activity;
	private boolean isWindowActive = false;
	
	private ArrayList<String> conversation = new ArrayList<String>();
	
	ConsoleSession(Context context, String id) {
		this.id = id;
		this.context = context;
	}
	
	ConsoleSession(Context context, String id, Activity activity,TextView prompt, TextView cmd) {
		this.id = id;
		this.activity = activity;
		this.context = context;
		this.isWindowActive = true;
		this.promptView = prompt;
		this.cmdView = cmd;
	}
	
	public String getId() {
		return id;	
	}
	
	public String getMsfId() {
		return msfId;
	}
	
	public boolean isReady() {
		if (msfId == null)
			return false;
		return true;
	}
	
	public void setMsfId(String id) {
		if (msfId == null) {
			msfId = id;
			read();
		}
	}
	
	public void setPrompt(final String p) {
		this.prompt = p;
		if (isWindowActive)
			activity.runOnUiThread(new Runnable() {  
                @Override
                public void run() {
        			promptView.setText(p);
                }
            });
	}
	
	public String getPrompt() {
		return prompt;
	}
	
	private void read() {	
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_READ);
		tmpIntent.putExtra("id", id);
		tmpIntent.putExtra("msfId", msfId);
		context.sendBroadcast(tmpIntent);
	}
	
	public void newRead(final String data, final String prompt, boolean busy) {
		this.prompt = prompt;
		
		if (data.trim().length() > 0)
			conversation.add(data);
		
		if (isWindowActive) {
			activity.runOnUiThread(new Runnable() {  
                @Override
                public void run() {
        			promptView.setText(prompt);
                	cmdView.append(data);
                }
            });

		}
	}
	
	public void write(final String data) {
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_WRITE);
		tmpIntent.putExtra("id", id);
		tmpIntent.putExtra("msfId", msfId);
		tmpIntent.putExtra("data", data);
		context.sendBroadcast(tmpIntent);
		
		conversation.add(prompt + data + "\n");
		
		if (isWindowActive) {		
			activity.runOnUiThread(new Runnable() {  
                @Override
                public void run() {
                	cmdView.append(prompt + data + "\n");
                }
            });

		}
	}
	
	
	public void setPromptView(TextView v) {
		promptView = v;
	}
	
	public void setCmdView(TextView v) {
		cmdView = v;
	}
}
