package com.anwarelmakrahy.pwncore.structures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.console.utils.AttackFinder;
import com.anwarelmakrahy.pwncore.console.utils.PortScanner;
import com.anwarelmakrahy.pwncore.console.utils.ServiceEnum;

import android.content.Context;

public class HostItem {
	
	private String host;
	private String os = "Unknown";
	private boolean isPwned = false,
					isUp = false;
	
	private Map<String, String> tcpPorts = new HashMap<String, String>();
	private Map<String, String> udpPorts = new HashMap<String, String>();
	private Map<String, List<String>> suggestedAttacks = new HashMap<String, List<String>>();
	
	private Context context;
	
	private AttackFinder attackFinder;
	
	public HostItem(Context context) {
		this.context = context;
		attackFinder = new AttackFinder(this);
	}
	
	public HostItem(Context context, String host) {
		this.host = host;
		this.context = context;
		attackFinder = new AttackFinder(this);
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
		
		if (isPwned)
			this.isUp = true;
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
			    	scanner.scan(HostItem.this);
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
			    	services.enumerate(HostItem.this);
			}	
		}).start();
	}

	public void findAttacks(final String attackFlag) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				suggestedAttacks.clear();
				suggestedAttacks.putAll(attackFinder.findAttacks(attackFlag));
			}	
		}).start();
	}
	
	public boolean isUp() {
		if (!isUp)
			return (tcpPorts.size() + udpPorts.size() == 0) ? false : true;
		else return true;
	}

	public String[] getOSCodeName() {
		if (os.toLowerCase().startsWith("windows"))
			return new String[] { "multi", "windows" };
		
		else if (os.toLowerCase().startsWith("solaris"))
			return new String[] { "solaris", "multi", "unix" };
		
		else if (os.toLowerCase().startsWith("linux"))
			return new String[] { "linux", "multi", "unix"};
		
		else if (os.toLowerCase().startsWith("mac"))
			return new String[] { "osx", "multi", "unix" };
		
		else if (os.toLowerCase().startsWith("freebsd"))
			return new String[] { "freebsd", "multi", "unix" };
		
		else 
			return new String[] {"multi", "unix"};
	}	
}
