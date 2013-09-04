package com.anwarelmakrahy.pwncore.structures;

public class ModuleItem {
	
	public ModuleItem() {
		
	}
	
	public ModuleItem(String path) {
		this.path = path;
	}
	
	public ModuleItem(String path, String type) {
		this.path = path;
		this.type = type;
	}
	
	private long id;
	private String path, type;

	public long getID() {
	    return id;
	}

	public void setID(long id) {
	    this.id = id;
	}

	public String getPath() {
	    return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
	    return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return path;
	}
}
