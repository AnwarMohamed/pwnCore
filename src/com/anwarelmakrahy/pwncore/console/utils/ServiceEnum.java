package com.anwarelmakrahy.pwncore.console.utils;

import java.util.ArrayList;

import android.content.Context;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.structures.TargetItem;


public class ServiceEnum extends ConsoleSession {
	
	//private ArrayList<String> tmpPortQueries = new ArrayList<String>();
	
	private int scanCounter = 0;
	
	public ServiceEnum(Context context) {
		super(context);
	}

	public ServiceEnum(Context context, ConsoleSessionParams params) {
		super(context, params);
	}
	
	private TargetItem target;
	
	public void enumerate(TargetItem target) {
		waitForReady();	
		this.target = target;
		
		if (this.target == null) 
			return;
		
		String[] tcp_ports = target.getTcpPorts().keySet().toArray(new String[target.getTcpPorts().size()]);
		
		for (int i=0; i<tcp_ports.length; i++)
			enumeratePort(target.getHost(), tcp_ports[i]);
	}
	
	@Override
	protected void processDataLine(String data) {
		if (data.endsWith("- TCP OPEN"))
			addPortToHost(
					data.split(" ")[1].split(":")[0], 
					data.split(" ")[1].split(":")[1], 
					"TCP", 
					"Unknown Service");
		
		else if (data.startsWith("[*] Auxiliary module execution completed")) {
			if (params == null && --scanCounter == 0)
				MainService.sessionMgr.destroyConsole(this);
		}
		
		/*else {
			for (int j=0; j<tmpPortQueries.size(); j++)
				if (data.trim().split(" ").length > 1 && 
						data.trim().split(" ")[1].equals(tmpPortQueries.get(j))) {	
					
					addPortToHost(
							tmpPortQueries.get(j).split(":")[0], 
							tmpPortQueries.get(j).split(":")[1],
							"TCP",
							data.substring(16 + tmpPortQueries.get(j).length()));

					tmpPortQueries.remove(j);
					break;
				}
		}*/
	}
	
	private void addPortToHost(String host, String port, String protocol, String details) {
		//enumeratePort(host, port);
		for (int i=0; i<MainService.mTargetHostList.size(); i++)
			if (MainService.mTargetHostList.get(i).getHost().equals(host)) {
				MainService.mTargetHostList.get(i).addPort(protocol, port, details);
				updateAdapters();
				return;		
			}
		
		TargetItem t = new TargetItem(host);
		t.addPort(protocol, port, details);
		MainService.mTargetHostList.add(t);
		updateAdapters();
	}
	
	private void enumeratePort(final String host, final String port) {
		new Thread(new Runnable() {
			@Override public void run() {
				
				if (port.equals("21")) {			
					scanCounter++;
					write("use scanner/ftp/ftp_version\nset THREADS 10\nset RHOSTS " + host + "\nrun -j");
				}
				else if (port.equals("22")) {
					scanCounter++;
					write("use scanner/ssh/ssh_version\nset THREADS 10\nset RHOSTS " + host + "\nrun -j");
				}
				else if (port.equals("23")) {
					scanCounter++;
					write("use scanner/telnet/telnet_version\nset THREADS 10\nset RHOSTS " + host + "\nrun -j");
				}
				else if (port.equals("80"))	{
					scanCounter++;
					write("use scanner/http/http_version\nset THREADS 10\nset RHOSTS " + host + "\nrun -j");
				}
				else if (port.equals("445")) {
					scanCounter++;
					write("use scanner/smb/smb_version\nset THREADS 10\nset RHOSTS " + host + "\nrun -j");
				}
				
			}
		}).start();	
	}
	
	/*public ArrayList<String> getTmpPortQueries() {
		return tmpPortQueries;
	}
	
	public void deleteFromTmpPortQueries(String info) {
		if (tmpPortQueries.contains(info))
			tmpPortQueries.remove(tmpPortQueries.indexOf(info));
	}*/
}
