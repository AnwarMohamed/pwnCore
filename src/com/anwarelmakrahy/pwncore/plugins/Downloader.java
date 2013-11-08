package com.anwarelmakrahy.pwncore.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

public class Downloader {
	
	private int size, marker = 0;
	private String path;
	private FileOutputStream fOut;
	BufferedOutputStream bos;
	File sdCard, dir, file;
	
	public Downloader( String path, int size, String type, boolean view) {
		this.size = size;
		this.path = path;
		
		try {
			sdCard = Environment.getExternalStorageDirectory();
			dir = new File (sdCard.getAbsolutePath() + "/download");
			dir.mkdirs();
			file  = new File(dir, path.split("/")[path.split("/").length - 1]);
			fOut = new FileOutputStream(file);
			bos = new BufferedOutputStream(fOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String getFullPath() {
		return file.getPath();
	}
	
	public String getDownloadCmd() {
		return ("cat " + path);
	}
	
	public void addToBuffer(byte[] data) {
		if (!hasFinished()) {
			marker += data.length;
			try {
				bos.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (hasFinished())
				stop();
		}
	}
	
	public void stop() {
		if (bos != null) {
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasFinished() {
		return (marker != size) ? false : true;
	}
}
