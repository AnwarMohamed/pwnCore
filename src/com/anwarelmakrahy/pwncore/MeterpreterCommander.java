package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.console.ControlSession;
import com.anwarelmakrahy.pwncore.plugins.ProcessesActivity;
import com.anwarelmakrahy.pwncore.plugins.ProcessesActivity.ProcessItem;

public class MeterpreterCommander {

	private final String COMMAND_ERROR = "Couldn't execute command";
	
	private boolean isReady = false;
	private ControlSession session;
	private MsfRpcClient client;
	
	public MeterpreterCommander(MsfRpcClient client, ControlSession session) {
		this.client = client;
		this.session = session;
		
		if (client != null && session != null)
			isReady = true;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	private List<Object> params = new ArrayList<Object>();
	private Map<String, Value> read() {
		if (isReady) {
			params.clear();
			params.add("session.meterpreter_read");
			params.add(session.getId());
			return client.call(params);
		}
		else return null;
	}

	private Object execute(String cmd) {
		if (isReady) {
			params.clear();
			params.add("session.meterpreter_write");
			params.add(session.getId());
			params.add(cmd + "\n");
			if (client.call(params) != null) {
				
				Map<String, Value> readRes = read();
				if (readRes != null && readRes.containsKey("data")) {
					try {
						return readRes.get("data")
								.asRawValue().getString();
					} catch (Exception e) {
						return readRes.get("data")
								.asRawValue().getByteArray();
					}
				}
				return null;
			}
			return null;
		}
		return null;
	}
	
	public String getSystemInfo() {
		String data = execute("sysinfo").toString();
		if (data.length() > 0 && 
				data.startsWith("Computer "))
			return data;
		else return COMMAND_ERROR;
	}
	
	public String getIdletime() {
		String data = execute("idletime").toString();
		if (data.length() > 0 && 
				data.startsWith("User has been idle"))
			return data;
		else return COMMAND_ERROR;		
	}

	public String getUid() {
		String data = execute("getuid").toString();
		if (data.length() > 0 && 
				data.startsWith("Server "))
			return data;
		else return COMMAND_ERROR;		
	}
	
	public List<String> getProcList() {
		List<String> procList = new ArrayList<String>();
		String data = execute("ps").toString();
		if (data.length() > 0 && 
				data.startsWith("\nProcess List\n")) {
			
			String proc;
			String[] lines = data.split("\n");
			for (int i=0; i<lines.length; i++) {
				if (lines[i].trim().length() > 0 && 
						lines[i].trim().startsWith("Process List")) {
					i+=4;
					continue;
				}
				else if (lines[i].trim().length() > 0) {
					proc = lines[i].trim().replaceAll(" +", " ");
					procList.add(proc.split(" ")[0]+ ":" + proc.split(" ")[2]);
				}
				
			}
		}
		
		return procList;
	}
	
	public boolean killProcess(String id) {
		return false;
	}
}
