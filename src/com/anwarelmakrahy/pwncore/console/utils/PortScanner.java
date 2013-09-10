package com.anwarelmakrahy.pwncore.console.utils;

import android.content.Context;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.structures.TargetItem;


public class PortScanner extends ConsoleSession {

	public PortScanner(Context context, String title) {
		super(context, title);
	}

	public PortScanner(Context context, ConsoleSessionParams params, String title) {
		super(context, params, title);
	}
	
	private TargetItem target;
	private final String ports =     					
			"50000, 21, 1720, 80, 143, 3306, 110, 5432, 25, 22, 23, 443, 1521, 50013, 161, 17185, 135, " + 
			"8080, 4848, 1433, 5560, 512, 513, 514, 445, 5900, 5038, 111, 139, 49, 515, 7787, 2947, 7144, " + 
			"9080, 8812, 2525, 2207, 3050, 5405, 1723, 1099, 5555, 921, 10001, 123, 3690, 548, 617, 6112, " +
			"6667, 3632, 783, 10050, 38292, 12174, 2967, 5168, 3628, 7777, 6101, 10000, 6504, 41523, 41524, "+
			"2000, 1900, 10202, 6503, 6070, 6502, 6050, 2103, 41025, 44334, 2100, 5554, 12203, 26000, 4000, "+
			"1000, 8014, 5250, 34443, 8028, 8008, 7510, 9495, 1581, 8000, 18881, 57772, 9090, 9999, 81, 3000, "+
			"8300, 8800, 8090, 389, 10203, 5093, 1533, 13500, 705, 623, 4659, 20031, 16102, 6080, 6660, 11000, "+
			"19810, 3057, 6905, 1100, 10616, 10628, 5051, 1582, 65535, 105, 22222, 30000, 113, 1755, 407, 1434, "+
			"2049, 689, 3128, 20222, 20034, 7580, 7579, 38080, 12401, 910, 912, 11234, 46823, 5061, 5060, 2380, "+
			"69, 5800, 62514, 42, 5631, 902, 3389";
	
	private boolean isScanning = false;
	public void scan(TargetItem target) {
		waitForReady();
		this.target = target;
		
		if (this.target == null) 
			return;
		
    	String cmd =	"use auxiliary/scanner/portscan/tcp\n" + 
						"set RHOSTS " + target.getHost() + "\n" + 
						"set THREADS 15\n" + 
						"set PORTS " + ports + "\n" +
						"run";
    	write(cmd);
    	startObserver();
	}
	
	private void startObserver() {
		isScanning  = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				int seconds = 60;
				while(isScanning) {
					if (--seconds ==  0) {
						target.scanPorts();
						if (params == null)			
							MainService.sessionMgr.destroyConsole(PortScanner.this);
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}	
				}
			}		
		}).start();
	}

	@Override
	protected void processDataLine(String data) {
		if (data.endsWith("- TCP OPEN"))
			addPortToHost(
					data.split(" ")[1].split(":")[1], 
					"TCP", 
					"Unknown Service");
		
		else if (data.startsWith("[*] Auxiliary module execution completed")) {
			isScanning = false;
			if (params == null) {			
				MainService.sessionMgr.destroyConsole(this);
			}
		}
	}
	
	private void addPortToHost(String port, String protocol, String details) {
		target.addPort(protocol, port, details);
	}
}
