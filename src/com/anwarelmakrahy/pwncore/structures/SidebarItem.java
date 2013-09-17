package com.anwarelmakrahy.pwncore.structures;

public class SidebarItem {

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void setHeader(boolean value) {
		this.isHeader = value;
	}
	
	public boolean isHeader() {
		return isHeader;
	}
	
	public void showCount(boolean count) {
		this.showCount = count;
	}
	
	public boolean showCount() {
		return showCount;
	}
	
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}
	
	public boolean getClickable() {
		return clickable;
	}
	
	public int getImage() {
		return image;
	}
	
	public void setImage(int image) {
		this.image = image;
	}
	
	
	private String title;
	private int count = 0;
	private boolean isHeader = false;
	private int image;
	private boolean showCount = true, clickable = true;
}
