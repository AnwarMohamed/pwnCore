package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.msgpack.type.Value;

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
	private Map<String, Value> info;
	
	private ArrayList<String> conversation = new ArrayList<String>();
	private ArrayList<String> queryPool = new ArrayList<String>();
	
	ControlSession(Context context, String type, String id, Map<String, Value> info) {
		this.id = id;
		this.context = context;
		this.type = type;
		this.prompt = type + " > ";
		this.info = info;
	}
	
	ControlSession(Context context, String type, String id, Map<String, Value> info, ConsoleSessionParams params) {
		this.id = id;
		this.context = context;
		this.params = params;
		this.type = type;
		this.prompt = type + " > ";
		this.info = info;
		this.isWindowReady = params.hasWindowViews();
	}
	
	public void setWindowActive(boolean flag, Activity activity) {
		isWindowActive = flag;
		if (params != null && activity != null)
			params.setAcivity(activity);
		
		if (flag) {
			params.getCmdView().setText(StringUtils.join(conversation.toArray()));
			params.getPromptView().setText(prompt);
		}
	}
	
	public String getId() {
		return id;	
	}
	
	public boolean isReady() {
		return true;
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
			appendToLog(data);
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
		appendToLog(prompt + data);		
	}
	
	private void appendToLog(final String data) {
		if (isWindowActive && isWindowReady) {		
			params.getAcivity().runOnUiThread(new Runnable() {  
                @Override
                public void run() {
                	if (data != null)
                		params.getCmdView().append(data + "\n");
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
		Intent tmpIntent = new Intent();
		tmpIntent.putExtra("id", id);
		//tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_SHELL_DESTROY);
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_METERPRETER_DESTROY);
		context.sendBroadcast(tmpIntent);	
	}
	
	public String getType() {
		return type;
	}
	
	public String getPeer() {
		if (info.containsKey("tunnel_peer"))
			return info.get("tunnel_peer").asRawValue().getString();
		else
			return "unknown";		
	}
	
	public String getViaExploit() {
		if (info.containsKey("via_exploit"))
			return info.get("via_exploit").asRawValue().getString();
		else
			return "unknown";		
	}
	
	public String getViaPayload() {
		if (info.containsKey("via_payload"))
			return info.get("via_payload").asRawValue().getString();
		else
			return "unknown";		
	}
}
