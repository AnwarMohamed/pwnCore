package com.anwarelmakrahy.pwncore;

public class TargetHostItem {
	
	private String host;
	private String os = "Unknown";
	
	TargetHostItem() {		
	}
	
	TargetHostItem(String host) {
		this.host = host;
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
}
