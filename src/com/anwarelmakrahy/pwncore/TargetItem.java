package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetItem {
	
	private String host;
	private String os = "Unknown";
	private boolean isPwned = false,
					isUp = false;
	
	private List<String> tcpPorts = new ArrayList<String>();
	private List<String> udpPorts = new ArrayList<String>();
	
	TargetItem() {		
	}
	
	TargetItem(String host) {
		this.host = host;
	}
	
	public void addPort(String type, String port) {
		if (type.toLowerCase().equals("tcp"))
			tcpPorts.add(port);
		else if (type.toLowerCase().equals("udp"))
			udpPorts.add(port);
	}
	
	public String[] getTcpPorts() {
		return tcpPorts.toArray(new String[tcpPorts.size()]);
	}
	
	public String[] getUdpPorts() {
		return udpPorts.toArray(new String[udpPorts.size()]);
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
	
	public void setUp(boolean isUp) {
		this.isUp  = isUp;
	}
	
	public boolean isUp() {
		return (udpPorts.size() + tcpPorts.size() != 0);
	}
	
}
