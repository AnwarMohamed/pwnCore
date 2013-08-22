package com.anwarelmakrahy.pwncore;

public class TargetItem {
	
	private String host;
	private String os = "Unknown";
	private boolean isPwned = false;
	
	TargetItem() {		
	}
	
	TargetItem(String host) {
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
	
	public void setPwned(boolean isPwned) {
		this.isPwned  = isPwned;
	}
	
	public boolean isPwned() {
		return isPwned;
	}
}
