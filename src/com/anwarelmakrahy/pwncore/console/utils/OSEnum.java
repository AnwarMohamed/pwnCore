package com.anwarelmakrahy.pwncore.console.utils;

import java.util.Map;

import com.anwarelmakrahy.pwncore.structures.HostItem;
import com.anwarelmakrahy.pwncore.structures.HostsAdapter;

public class OSEnum {

	public static void enumerate(HostItem target) {
		Map<String, String> tcpPorts = target.getTcpPorts();
		
		if (tcpPorts.containsKey("445") && 
				tcpPorts.get("445").split(" ").length > 4 &&
				tcpPorts.get("445").split(" ")[1].equals("running")) {
			for (int i=0; i<HostsAdapter.osTitles.length; i++) {
				if (tcpPorts.get("445").contains(HostsAdapter.osTitles[i])) {
					target.setOS(HostsAdapter.osTitles[i]);
					break;
				}
			}
		}
	}
}
