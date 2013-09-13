package com.anwarelmakrahy.pwncore.console.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.structures.TargetItem;


public class ServiceEnum extends ConsoleSession {

	private TargetItem target;
	protected boolean isScanning = false;

	public ServiceEnum(Context context, String title) {
		super(context, title);
	}

	public ServiceEnum(Context context, ConsoleSessionParams params, String title) {
		super(context, params, title);
	}
	
	public void enumerate(TargetItem target) {
		waitForReady();	
		this.target = target;
		
		if (this.target == null) 
			return;
		
		startObserver();
		String[] tcp_ports = target.getTcpPorts().keySet().toArray(new String[target.getTcpPorts().size()]);
		
		for (int i=0; i<tcp_ports.length; i++)
			enumeratePort(target.getHost(), tcp_ports[i]);
	}
	
	@Override
	protected void processDataLine(String data) {
		if (data.split(" ").length > 1 && 
				data.split(" ")[1].split(":").length > 1 &&
				data.split(" ")[1].split(":")[0].equals(target.getHost())) {
			addServiceToHost(
					data.split(" ")[1].split(":")[1].replace(",",""), 
					"TCP", 
					data.substring(5 + data.split(" ")[1].length()));
		}
		else if (data.startsWith("[*] Auxiliary module execution completed")) {
			if (scanQueue.size() > 0)
				scanQueue.remove(0);
			
			if (scanQueue.size() > 0) {
				write(scanQueue.get(0));
			}
			else if (params == null && scanQueue.size() == 0) {
				OSEnum.enumerate(target);
				isScanning = false;
				MainService.sessionMgr.destroyConsole(this);
			}
		}
	}
	
	private void startObserver() {
		isScanning  = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				int seconds = target.getTcpPorts().size() * 10;
				while(isScanning ) {
					if (--seconds ==  0) {
						target.scanServices();
						if (params == null)			
							MainService.sessionMgr.destroyConsole(ServiceEnum.this);
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}	
				}
			}		
		}).start();
	}
	
	private List<String> scanQueue = new ArrayList<String>();
	
	private void addServiceToHost(String port, String protocol, String details) {
		target.addPort(protocol, port, details);
	}
	
	private void enumeratePort(final String host, final String port) {
		new Thread(new Runnable() {
			@Override public void run() {
				
				if (port.equals("21")) {			
					scanQueue.add("use scanner/ftp/ftp_version\nset THREADS 10\nset RHOSTS " + host + "\nrun");
				}
				else if (port.equals("22")) {
					scanQueue.add("use scanner/ssh/ssh_version\nset THREADS 10\nset RHOSTS " + host + "\nrun");
				}
				else if (port.equals("23")) {
					scanQueue.add("use scanner/telnet/telnet_version\nset THREADS 10\nset RHOSTS " + host + "\nrun");
				}
				else if (port.equals("80"))	{
					scanQueue.add("use scanner/http/http_version\nset THREADS 10\nset RHOSTS " + host + "\nrun");
				}
				else if (port.equals("445")) {
					scanQueue.add("use scanner/smb/smb_version\nset THREADS 10\nset RHOSTS " + host + "\nrun");
				}
				
				if (scanQueue.size() == 1)
					write(scanQueue.get(0));
			}
		}).start();	
	}
}
