package com.anwarelmakrahy.pwncore.console.utils;

import java.util.Map;

import com.anwarelmakrahy.pwncore.structures.TargetItem;
import com.anwarelmakrahy.pwncore.structures.TargetsListAdapter;

public class OSEnum {

	public static void enumerate(TargetItem target) {
		Map<String, String> tcpPorts = target.getTcpPorts();
		
		if (tcpPorts.containsKey("445") && 
				tcpPorts.get("445").split(" ").length > 4 &&
				tcpPorts.get("445").split(" ")[1].equals("running")) {
			for (int i=0; i<TargetsListAdapter.osTitles.length; i++) {
				if (tcpPorts.get("445").contains(TargetsListAdapter.osTitles[i])) {
					target.setOS(TargetsListAdapter.osTitles[i]);
					break;
				}
			}
		}
	}
}
