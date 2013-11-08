package com.anwarelmakrahy.pwncore.console;

import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.tMap;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import com.anwarelmakrahy.pwncore.MainActivity;
import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.activities.HostSessionsActivity;
import com.anwarelmakrahy.pwncore.plugins.MeterpreterActivity;
import com.anwarelmakrahy.pwncore.structures.HostItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class ConsoleSession {

	protected static final long WAIT_TIMEOUT = 10000;
	private String title = "";
	private boolean queryPoolActive = false;
	private String msfId = null;
	private String id, prompt;
	private Context context;
	private boolean isWindowActive = false, logoLoaded = false;
	public boolean isWindowReady = false;
	protected ConsoleSessionParams params = null;

	private ArrayList<String> conversation = new ArrayList<String>();

	private ArrayList<String> queryPool = new ArrayList<String>();

	public ConsoleSession(Context context, String title) {
		this.context = context;
		this.title = title;
	}

	public ConsoleSession(Context context, ConsoleSessionParams params,
			String title) {
		this.context = context;
		this.params = params;
		this.title = title;
		this.isWindowReady = params.hasWindowViews();
	}

	public void setWindowActive(boolean flag, Activity activity) {
		isWindowActive = flag;
		if (params != null && activity != null)
			params.setAcivity(activity);

		if (flag)
			if (params != null)
				params.getCmdView().setText(StringUtils.join(conversation.toArray()));
	}

	AlertDialog.Builder newMeterpreterSessionDlg;

	private void setupSessionInteractDlg() {
		newMeterpreterSessionDlg = new AlertDialog.Builder(params.getActivity());
		newMeterpreterSessionDlg
				.setTitle("Meterpreter Session")
				.setMessage("Meterpreter Session attached, Interact ?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setCancelable(false)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton("Console",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {

								Intent intent = new Intent(context, ConsoleActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.putExtra("type", "current.meterpreter");
								String id = session.getId();
								intent.putExtra("id", id);
								context.startActivity(intent);
							}
						})
				.setNeutralButton("VisualCommander",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {

								context.startActivity(new Intent(context, HostSessionsActivity.class)
								.putExtra("hostId", session.getLinkedHostId())
								.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
								
								if (ConsoleActivity.getActivity() != null)
									ConsoleActivity.getActivity().finish();
							}
						}).setCancelable(true);
	}

	public String getId() {
		return id;
	}

	public String getMsfId() {
		return msfId;
	}

	public boolean isReady() {
		return (logoLoaded && id != null);
	}

	public void setMsfId(String id) {
		if (msfId == null) {
			msfId = id;
			read();
		}
	}

	public void setId(String id) {
		if (this.id == null)
			this.id = id;
	}

	public void setPrompt(final String p) {
		this.prompt = p.replaceAll("[^\\p{Punct}\\w]", " ")
				.replaceAll(" +", " ").trim()
				.replaceAll("\\s+(?=[^()]*\\))", "") + " > ";
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
					for (int i = 0; i < queryPool.size(); i++) {
						Intent tmpIntent = new Intent();
						tmpIntent.setAction(StaticClass.PWNCORE_CONSOLE_WRITE);
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
		tmpIntent.setAction(StaticClass.PWNCORE_CONSOLE_READ);
		tmpIntent.putExtra("id", id);
		tmpIntent.putExtra("msfId", msfId);
		context.sendBroadcast(tmpIntent);
	}


	public void newRead(final String data, final String prompt, boolean busy) {
		this.prompt = prompt.replaceAll("[^\\p{Punct}\\w]", " ")
				.replaceAll(" +", " ").trim()
				.replaceAll("\\s+(?=[^()]*\\))", "") + " > ";

		if (data.trim().length() > 0) {
			conversation.add(data);
			processIncomingData(data);
			if (!logoLoaded)
				logoLoaded = true;

			appendToLog(data, prompt.replaceAll("[^\\p{Punct}\\w]", " ")
					.replaceAll(" +", " ").trim()
					.replaceAll("\\s+(?=[^()]*\\))", "") + " > ");
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
			@Override
			public void run() {
				String[] lines = data.split("\n");

				for (int i = 0; i < lines.length; i++) {

					processDataLine(lines[i].trim());

					if (lines[i].trim().startsWith("[*] Meterpreter session")
							&& lines[i].trim().split(" ")[4].equals("opened")) {
						sessionInteract(lines[i].trim(), "meterpreter");
					}

					else if (lines[i].trim().startsWith("[*] Shell session")
							&& lines[i].trim().split(" ")[4].equals("opened")) {
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
		// TODO
	}

	private ControlSession session;

	private void sessionInteract(final String data, final String type) {
		new Thread(new Runnable() {
			@Override
			public void run() {

				MainService.sessionMgr.updateSessionsRemoteInfo();

				String sessionId = data.split(" ")[3];
				HostItem hostItem = null;

				if (MainService.sessionMgr.getSessionsRemoteInfo().containsKey(
						sessionId)) {

					try {

						Converter mapCon = new Converter(MainService.sessionMgr
								.getSessionsRemoteInfo().get(sessionId)
								.asMapValue());
						Map<String, Value> info = mapCon.read(tMap(TString,TValue));
						mapCon.close();

						if (info.containsKey("tunnel_peer")) {
							boolean wasFound = false;
							String host = info.get("tunnel_peer").asRawValue().getString().split(":")[0];
							
							for (HostItem item : MainService.hostsList) {
								if (item.getHost().equals(host)) {
									item.setPwned(true);
									if (info.containsKey("type")
											&& item.getActiveSessions()
													.containsKey(
															info.get("type")
																	.asRawValue()
																	.getString()))
										item.getActiveSessions()
												.get(info.get("type")
														.asRawValue()
														.getString())
												.add(sessionId);
									hostItem = item;
									wasFound = true;
									break;
								}
							}

							if (!wasFound) {
								hostItem = new HostItem(context, host);
								hostItem.setPwned(true);

								if (info.containsKey("type")
										&& hostItem.getActiveSessions()
												.containsKey(
														info.get("type")
																.asRawValue()
																.getString()))
									hostItem.getActiveSessions()
											.get(info.get("type").asRawValue()
													.getString())
											.add(sessionId);

								hostItem.scanPorts();
								MainService.hostsList.add(hostItem);
							}
						}

					} catch (Exception e) {
					}

					ConsoleSessionParams params = new ConsoleSessionParams();
					params.setCmdViewId(R.id.consoleRead);
					params.setPromptViewId(R.id.attackHall);
					params.setAcivity(ConsoleActivity.getActivity());

					MainService.sessionMgr.getNewSession(session,
							"meterpreter", sessionId, params);
					session = MainService.sessionMgr.getSession(sessionId);
					session.setLinkedHostId(MainService.hostsList.indexOf(hostItem));

					if (isWindowActive && isWindowReady
							&& !(params.getActivity().isFinishing()))
						params.getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setupSessionInteractDlg();
								newMeterpreterSessionDlg.show();
							}
						});
				}
			}
		}).start();
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

			conversation.add(prompt + data + "\n");
			appendToLog(prompt + data, null);
		}
	}

	private void appendToLog(final String data, final String prompt) {
		if (isWindowActive && isWindowReady) {
			params.getActivity().runOnUiThread(new Runnable() {
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
		tmpIntent.putExtra("msfId", msfId);
		tmpIntent.putExtra("id", id);
		tmpIntent.setAction(StaticClass.PWNCORE_CONSOLE_DESTROY);
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

		public Activity getActivity() {
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
