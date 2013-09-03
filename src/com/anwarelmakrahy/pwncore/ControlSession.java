package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

import com.anwarelmakrahy.pwncore.ConsoleSession.ConsoleSessionParams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


public class ControlSession {

	protected static final long WAIT_TIMEOUT = 10000;
	private boolean queryPoolActive = false;

	private String id, prompt, type;
	
	private Context context;
	
	private boolean isWindowReady = false,
					isWindowActive = false;
		
	private ConsoleSessionParams params = null;
	
	private ArrayList<String> conversation = new ArrayList<String>();
	private ArrayList<String> queryPool = new ArrayList<String>();
	
	ControlSession(Context context, String type, String id) {
		this.id = id;
		this.context = context;
		this.type = type;
	}
	
	ControlSession(Context context, String type, String id, ConsoleSessionParams params) {
		this.id = id;
		this.context = context;
		this.params = params;
		this.type = type;
		this.isWindowReady = params.hasWindowViews();
	}
	
	public void setWindowActive(boolean flag, Activity activity) {
		isWindowActive = flag;
		if (params != null && activity != null)
			params.setAcivity(activity);
		
		if (flag)
			params.getCmdView().setText(StringUtils.join(conversation.toArray()));
	}
	
	public String getId() {
		return id;	
	}
	
	public boolean isReady() {
		return true;
	}
		
	public void setPrompt(final String p) {
		this.prompt = p;
		appendToLog(null, p);
	}
	
	public String getPrompt() {
		return prompt;
	}
	
	private void notifyQueryPool(final String data) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				queryPool.add(data);
				if (!queryPoolActive) {		
					queryPoolActive = true;		
					for (int i=0; i<queryPool.size(); i++) {
						
						Intent tmpIntent = new Intent();
						tmpIntent.setAction( 
								type.equals("shell") ? StaticsClass.PWNCORE_CONSOLE_SHELL_WRITE : 
								type.equals("meterpreter") ? StaticsClass.PWNCORE_CONSOLE_METERPRETER_WRITE : 
									null);
						
						tmpIntent.putExtra("id", id);
						tmpIntent.putExtra("data", queryPool.get(i));
						context.sendBroadcast(tmpIntent);
						
						queryPool.remove(i);
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					queryPoolActive = false;
				}
			}
		}).start();
	}
	
	private void read() {	
		Intent tmpIntent = new Intent();
		tmpIntent.setAction( 
				type.equals("shell") ? StaticsClass.PWNCORE_CONSOLE_SHELL_READ : 
				type.equals("meterpreter") ? StaticsClass.PWNCORE_CONSOLE_METERPRETER_READ : 
					null);
		
		tmpIntent.putExtra("id", id);
		context.sendBroadcast(tmpIntent);
	}
	
	public void newRead(final String data) {

		if (data.trim().length() > 0) {
			conversation.add(data);
			processIncomingData(data);
		
			appendToLog(data, prompt);
		}
	}
	
	public void processIncomingData(final String data) {
		new Thread(new Runnable() {
			@Override public void run() {
				
			}
		}).start();
	}
		
	public void pingReadListener() {
		new Thread(new Runnable() {  
            @Override
            public void run() {
            	for (int i=0; i<5; i++) {  	       		
            		try {
            			read();
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
            	}
            }
        }).start();
	}
	
	public void write(final String data) {
		notifyQueryPool(data);	
		conversation.add(prompt + data + "\n");
		appendToLog(prompt + data, null);		
	}
	
	private void appendToLog(final String data, final String prompt) {
		if (isWindowActive && isWindowReady) {		
			params.getAcivity().runOnUiThread(new Runnable() {  
                @Override
                public void run() {
                	if (data != null)
                		params.getCmdView().append(data + "\n");
                	if (prompt != null)
                		params.getPromptView().setText(prompt);
                }
            });	
		}
	}
	
	
	public void waitForReady() {
		while (!isReady()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void destroy() {
		//Intent tmpIntent = new Intent();
		//tmpIntent.putExtra("msfiId", msfId);
		//tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_DESTROY);
		//context.sendBroadcast(tmpIntent);	
	}
}
