package com.anwarelmakrahy.pwncore.structures;

import java.util.HashMap;
import java.util.Map;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.console.utils.PortScanner;
import com.anwarelmakrahy.pwncore.console.utils.ServiceEnum;

import android.content.Context;

public class TargetItem {
	
	private String host;
	private String os = "Unknown";
	private boolean isPwned = false;
	
	private Map<String, String> tcpPorts = new HashMap<String, String>();
	private Map<String, String> udpPorts = new HashMap<String, String>();

	private Context context;
	
	public TargetItem(Context context) {
		this.context = context;
	}
	
	public TargetItem(Context context, String host) {
		this.host = host;
		this.context = context;
	}
	 
	public void addPort(String type, String port, String details) {
		if (type.toLowerCase().equals("tcp"))
			tcpPorts.put(port, details);
		else if (type.toLowerCase().equals("udp"))
			udpPorts.put(port, details);
	}
	
	public Map<String, String> getTcpPorts() {
		return tcpPorts;
	}
	
	public Map<String, String> getUdpPorts() {
		return udpPorts;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getOS() {
		return os;
	}
	
	public void setOS(String os) {
		this.os = os;
	}
	
	public void setPwned(boolean isPwned) {
		this.isPwned  = isPwned;
	}
	
	public boolean isPwned() {
		return isPwned;
	}
	
	public void scanPorts() {
		new Thread(new Runnable() {
			@Override
			public void run() {
			    PortScanner scanner = new PortScanner(context, "PortScanner " + getHost());
			    MainService.sessionMgr.getNewConsole(scanner);
			    if (scanner != null)
			    	scanner.scan(TargetItem.this);
			}	
		}).start();
	}
	
	public void scanServices() {
		new Thread(new Runnable() {
			@Override
			public void run() {
			    ServiceEnum services = new ServiceEnum(context, "ServiceEnum " + getHost());
			    MainService.sessionMgr.getNewConsole(services);
			    if (services != null)
			    	services.enumerate(TargetItem.this);
			}	
		}).start();
	}

	public boolean isUp() {
		return (tcpPorts.size() + udpPorts.size() == 0) ? false : true;
	}	
}
