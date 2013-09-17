package com.anwarelmakrahy.pwncore.structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.type.Value;

public class ModuleItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

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
	private Map<String, Value> options, info;

	public Map<String, Value> getInfo() {
		if (info != null)
			return info;
		else
			return new HashMap<String, Value>();
	}
	
	public void setInfo(Map<String, Value> info) {
		this.info = info;
	}

	public Map<String, Value> getOptions() {
		if (options != null)
			return options;
		else
			return new HashMap<String, Value>();
	}
	
	public void setOptions(Map<String, Value> options) {
		this.options = options;
	}
	
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
