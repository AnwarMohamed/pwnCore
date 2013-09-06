package com.anwarelmakrahy.pwncore.console;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

import com.anwarelmakrahy.pwncore.MainActivity;
import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticsClass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;


public class ConsoleSession {

	protected static final long WAIT_TIMEOUT = 10000;
	private String title = "";
	private boolean queryPoolActive = false;
	private String msfId= null;
	private String id, prompt;
	private Context context;
	private boolean isWindowReady = false,
					isWindowActive = false,
					logoLoaded = false;
		
	private ConsoleSessionParams params = null;
	
	private ArrayList<String> conversation = new ArrayList<String>();

	private ArrayList<String> queryPool = new ArrayList<String>();
	
	public ConsoleSession(Context context, String id) {
		this.id = id;
		this.context = context;
	}
	
	public ConsoleSession(Context context, String id, ConsoleSessionParams params) {
		this.id = id;
		this.context = context;
		this.params = params;
		this.isWindowReady = params.hasWindowViews();
		setupSessionInteractDlg();
	}
	

	
	public void setWindowActive(boolean flag, Activity activity) {
		isWindowActive = flag;
		if (params != null && activity != null)
			params.setAcivity(activity);
		
		if (flag)
			params.getCmdView().setText(StringUtils.join(conversation.toArray()));
	}
	
	AlertDialog.Builder newMeterpreterSessionDlg;
	
	private void setupSessionInteractDlg() {
		newMeterpreterSessionDlg = new AlertDialog.Builder(params.getAcivity());
		newMeterpreterSessionDlg
    	.setTitle("Meterpreter Session")
    	.setMessage("Meterpreter Session attached, Interact ?")
    	.setIcon(android.R.drawable.ic_dialog_alert)
    	.setCancelable(false)
    	.setNegativeButton("No",  new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	dialog.dismiss();
    	    }
    	})
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	
    	    	Intent intent = new Intent(context, ConsoleActivity.class);
    	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    	intent.putExtra("type", "current.meterpreter");
    	    	intent.putExtra("id", session.getId());
    	    	context.startActivity(intent);   
    	    	
    	    }
    	});
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
						tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_WRITE);
						tmpIntent.putExtra("id", id);
						tmpIntent.putExtra("msfId", msfId);
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
		
			appendToLog(data, prompt);
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
						
					processDataLine(lines[i].trim());
				
					if (lines[i].trim().startsWith("[*] Meterpreter session") &&
							lines[i].trim().split(" ")[4].equals("opened")) {
						sessionInteract(lines[i].trim(), "meterpreter");
					}
					
					else if (lines[i].trim().startsWith("[*] Shell session") &&
							lines[i].trim().split(" ")[4].equals("opened")) {
						sessionInteract(lines[i].trim(), "shell");
					}
					
					else if (lines[i].trim().startsWith("[*] Started")) {
						MainService.sessionMgr.updateJobsList();
					}					
				}
			}
		}).start();
	}
	
	protected void processDataLine(String data) {
		//TODO
	}

	protected ControlSession session;
	
	private void sessionInteract(final String data, final String type) {
		new Thread(new Runnable() {
			@Override public void run() {

				MainService.sessionMgr.updateSessionsRemoteInfo();
				String sessionId = data.split(" ")[3];
				
				if (MainService.sessionMgr.getSessionsRemoteInfo().containsKey(sessionId)) {
					
					ConsoleSessionParams params = new ConsoleSessionParams();
	    		    params.setCmdViewId(R.id.consoleRead);
	    		    params.setPromptViewId(R.id.consolePrompt);
	    		    params.setAcivity(MainActivity.getActivity());
	    		    
	    	    	session  = MainService.sessionMgr.getNewSession("meterpreter", sessionId, params);
					
					if (isWindowActive && isWindowReady)				
						params.getAcivity().runOnUiThread(new Runnable() {  
			                @Override public void run() { 
			                	newMeterpreterSessionDlg.show(); 
		                	}
			            });	
				}
			}}).start();
	}
	
	protected void updateAdapters() {
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
			notifyQueryPool(data);	
			if (title.equals(""))
				title = data.split("\n")[0];
			conversation.add(prompt + data + "\n");
			appendToLog(prompt + data, null);
		}		
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
		Intent tmpIntent = new Intent();
		tmpIntent.putExtra("msfiId", msfId);
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_DESTROY);
		context.sendBroadcast(tmpIntent);	
	}
	
	public static class ConsoleSessionParams {
		private int prompt = 0;
		private int cmd = 0;
		private Activity activity = null;
		
		public void setPromptViewId(int v) {
			prompt = v;
		}
		
		public void setCmdViewId(int v) {
			cmd = v;
		}
		
		public TextView getPromptView() {
			return (TextView) activity.findViewById(prompt);
		}
		
		public TextView getCmdView() {
			return (TextView) activity.findViewById(cmd);
		}
		
		public void setAcivity(Activity a) {
			activity = a;
		}	
		
		public Activity getAcivity() {
			return activity;
		}
		
		public boolean hasWindowViews() {
			if (prompt == 0 || cmd == 0 || activity == null)
				return false;
			return true;
		}
	}
	
	public String getTitle() {
		return title;
	}
}
