package com.anwarelmakrahy.pwncore;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class SessionManager {

	private Context context;
	
	private int incConsoleIDs = 1000;
	private Map<String, ConsoleSession> consoleSessions = new HashMap<String, ConsoleSession>();
	
	SessionManager(Context context) {
		this.context = context;
	}
	
	public ConsoleSession getNewConsole() {		
		String id = Integer.toString(incConsoleIDs++);
		ConsoleSession newConsole = new ConsoleSession(context, id);
		consoleSessions.put(id, newConsole);			
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_CREATE);
		tmpIntent.putExtra("id", id);
		context.sendBroadcast(tmpIntent);		
		return newConsole;
	}
	
	public ConsoleSession getNewConsole(Activity activity, TextView prompt, TextView cmd) {			
		String id = Integer.toString(incConsoleIDs++);
		ConsoleSession newConsole = new ConsoleSession(
				context, 
				id,
				activity,
				prompt,
				cmd);
		
		consoleSessions.put(id, newConsole);			
		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_CREATE);
		tmpIntent.putExtra("id", id);
		context.sendBroadcast(tmpIntent);		
		return newConsole;
	}
	
	public void notifyNewConsole(String id, String msfId, String prompt) {
		if (consoleSessions.containsKey(id)) {
			consoleSessions.get(id).setPrompt(prompt);
			consoleSessions.get(id).setMsfId(msfId);	
		}
	}
	
	public void notifyConsoleNewRead(String id, String data, String prompt, boolean busy) {
		if (consoleSessions.containsKey(id)) {
			consoleSessions.get(id).newRead(data, prompt, busy);	
		}
	}
	
	public void notifyDestroyedConsole(String Id, String msfId) {
		if (consoleSessions.containsKey(Id))
			consoleSessions.remove(Id);
	}
	

}
