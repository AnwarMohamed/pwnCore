package com.anwarelmakrahy.pwncore.structures;

import org.apache.commons.lang3.text.WordUtils;

public class SessionCommand {
	private boolean implemented = true;
	private String title, codename, description;

	public SessionCommand(String codename, String title) {
		this.title = title;
		this.codename = codename;
	}

	public SessionCommand(String codename) {
		this.codename = codename;
	}

	public String getTitle() {
		return title == null ? codename : WordUtils.capitalizeFully(title);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isImplemented() {
		return implemented;
	}

	public void setImplemented(boolean bool) {
		this.implemented = bool;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public String getDescription() {
		return description;
	}

	public String getCodename() {
		return codename;
	}
}
