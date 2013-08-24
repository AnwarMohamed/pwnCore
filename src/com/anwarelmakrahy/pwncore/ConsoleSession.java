package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class ConsoleSession {

	private String msfId= null;
	private String id, prompt;
	private Context context;
	private boolean isWindowReady = false,
					isWindowActive = false,
					logoLoaded = false;
		
	private ConsoleSessionParams params;
	
	private ArrayList<String> conversation = new ArrayList<String>();
	
	ConsoleSession(Context context, String id) {
		this.id = id;
		this.context = context;
	}
	
	ConsoleSession(Context context, String id, ConsoleSessionParams params) {
		this.id = id;
		this.context = context;
		this.params = params;
		this.isWindowReady = params.hasWindowViews();
	}
	

	public void setWindowActive(boolean flag) {
		isWindowActive = flag;
	}
	
	public String getId() {
		return id;	
	}
	
	public String getMsfId() {
		return msfId;
	}
	
	public boolean isReady() {
		return logoLoaded;
	}
	
	public void setMsfId(String id) {
		if (msfId == null) {
			msfId = id;
			read();
		}
	}
	
	public void setPrompt(final String p) {
		this.prompt = p;
		if (isWindowActive && isWindowReady)
			params.getAcivity().runOnUiThread(new Runnable() {  
                @Override
                public void run() {
        			params.getPromptView().setText(p);
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
		
		if (data.trim().length() > 0) {
			conversation.add(data);
			processIncomingData(data);
			if (!logoLoaded)
				logoLoaded = true;	
		
			if (isWindowActive && isWindowReady) {
				params.getAcivity().runOnUiThread(new Runnable() {  
	                @Override
	                public void run() {
	                	params.getPromptView().setText(prompt);
	                	params.getCmdView().append(data);
	                	//params.getCmdView().setText(StringUtils.join(conversation.toArray()));
	                }
	            });
			}
		}
		
		if (busy) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			read();
		}
	}
	
	public void processIncomingData(final String data) {
		new Thread(new Runnable() {
			@Override public void run() {
				String[] lines = data.split("\n");				
				for (int i=0; i<lines.length; i++) {
					if (lines[i].trim().endsWith("- TCP OPEN"))
						addPortToHost(lines[i].split(" ")[1].split(":")[0], lines[i].split(" ")[1].split(":")[1], "TCP");
				}
			}
		}).start();
	}
	
	private void addPortToHost(String host, String port, String protocol) {
		Log.d("newPort", host + ":" + port);
		for (int i=0; i<MainActivity.mTargetHostList.size(); i++)
			if (MainActivity.mTargetHostList.get(i).getHost().equals(host)) {
				MainActivity.mTargetHostList.get(i).addPort(protocol, port);
				updateAdapters();
				return;
			}
		
		TargetItem t = new TargetItem(host);
		t.addPort(protocol, port);
		MainActivity.mTargetHostList.add(t);
		updateAdapters();
	}
	
	private void updateAdapters() {
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_NOTIFY_ADAPTER_UPDATE);
		context.sendBroadcast(tmpIntent);	
	}
	
	public void pingReadListener() {
		new Thread(new Runnable() {  
            @Override
            public void run() {
            	read();
            }
        }).start();
	}
	
	public void write(final String data) {
		if (logoLoaded) {
			Intent tmpIntent = new Intent();
			tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_WRITE);
			tmpIntent.putExtra("id", id);
			tmpIntent.putExtra("msfId", msfId);
			tmpIntent.putExtra("data", data);
			context.sendBroadcast(tmpIntent);
			
			conversation.add(prompt + data + "\n");
			
			if (isWindowActive && isWindowReady) {		
				params.getAcivity().runOnUiThread(new Runnable() {  
	                @Override
	                public void run() {
	                	params.getCmdView().append(prompt + data + "\n");
	                	//params.getCmdView().setText(StringUtils.join(conversation.toArray()));
	                }
	            });	
			}
		}			
	}
	
	public void waitForReady() {
		while (!isReady()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class ConsoleSessionParams {
		private TextView prompt = null;
		private TextView cmd = null;
		private Activity activity = null;
		
		public void setPromptView(TextView v) {
			prompt = v;
		}
		
		public void setCmdView(TextView v) {
			cmd = v;
		}
		
		public TextView getPromptView() {
			return prompt;
		}
		
		public TextView getCmdView() {
			return cmd;
		}
		
		public void setAcivity(Activity a) {
			activity = a;
		}	
		
		public Activity getAcivity() {
			return activity;
		}
		
		public boolean hasWindowViews() {
			if (prompt == null || cmd == null || activity == null)
				return false;
			return true;
		}
	}
}
