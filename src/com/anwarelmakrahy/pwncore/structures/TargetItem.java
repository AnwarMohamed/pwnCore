package com.anwarelmakrahy.pwncore.structures;

import java.util.HashMap;
import java.util.Map;

public class TargetItem {
	
	private String host;
	private String os = "Unknown";
	private boolean isPwned = false;
	
	private Map<String, String> tcpPorts = new HashMap<String, String>();
	private Map<String, String> udpPorts = new HashMap<String, String>();

	
	TargetItem() {		
	}
	
	public TargetItem(String host) {
		this.host = host;
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
	
	public boolean isUp() {
		return (udpPorts.size() + tcpPorts.size() == 0) ? false: true;
	}
	
}
