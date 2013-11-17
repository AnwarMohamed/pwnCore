package com.anwarelmakrahy.pwncore.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.console.utils.AttackFinder;
import com.anwarelmakrahy.pwncore.console.utils.PortScanner;
import com.anwarelmakrahy.pwncore.console.utils.ServiceEnum;

import android.content.Context;
import android.util.Log;

public class HostItem {

	private String host;
	private String os = "Unknown";
	private boolean isPwned = false, isUp = false;

	private Map<String, String> tcpPorts = new HashMap<String, String>();
	private Map<String, String> udpPorts = new HashMap<String, String>();
	private Map<String, List<String>> suggestedAttacks = new HashMap<String, List<String>>();
	private Map<String, List<String>> activeSessions = new HashMap<String, List<String>>();

	private Context context;

	private AttackFinder attackFinder;

	public HostItem(Context context) {
		this.context = context;
		attackFinder = new AttackFinder(this);
		setupSessionsList();
	}

	public HostItem(Context context, String host) {
		this.host = host;
		this.context = context;
		attackFinder = new AttackFinder(this);
		setupSessionsList();
	}

	private void setupSessionsList() {
		activeSessions.put("meterpreter", new ArrayList<String>());
		activeSessions.put("shell", new ArrayList<String>());
	}

	public Map<String, List<String>> getActiveSessions() {
		return activeSessions;
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
		for (int i=0; i<StaticClass.osTitles.length; i++) {
			Log.d("OS", os);
			if (StaticClass.osTitles[i].toLowerCase().contains(os.toLowerCase()) ||
				os.toLowerCase().contains(StaticClass.osTitles[i].toLowerCase())) {
				this.os = StaticClass.osTitles[i];
				return;
			}
		}
		
		this.os = StaticClass.osTitles[StaticClass.osTitles.length-1];
	}

	public void setPwned(boolean isPwned) {
		this.isPwned = isPwned;

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
				PortScanner scanner = new PortScanner(context);
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
				ServiceEnum services = new ServiceEnum(context);
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
		else
			return true;
	}

	public String[] getOSCodeName() {
		if (os.toLowerCase().contains("windows"))
			return new String[] { "multi", "windows" };

		else if (os.toLowerCase().contains("solaris"))
			return new String[] { "solaris", "multi", "unix" };

		else if (os.toLowerCase().contains("linux"))
			return new String[] { "linux", "multi", "unix" };

		else if (os.toLowerCase().contains("mac"))
			return new String[] { "osx", "multi", "unix" };

		else if (os.toLowerCase().contains("freebsd"))
			return new String[] { "freebsd", "multi", "unix" };

		else
			return new String[] { "multi", "unix" };
	}
}
