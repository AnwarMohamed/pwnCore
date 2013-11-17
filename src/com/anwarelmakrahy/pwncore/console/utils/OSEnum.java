package com.anwarelmakrahy.pwncore.console.utils;

import java.util.Map;

import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.structures.HostItem;
import com.anwarelmakrahy.pwncore.structures.HostsAdapter;

public class OSEnum {

	public static void enumerate(HostItem target) {
		Map<String, String> tcpPorts = target.getTcpPorts();

		if (tcpPorts.containsKey("445")
				&& tcpPorts.get("445").split(" ").length > 4
				&& tcpPorts.get("445").split(" ")[1].equals("running")) {
			for (int i = 0; i < StaticClass.osTitles.length; i++) {
				if (tcpPorts.get("445").contains(StaticClass.osTitles[i])) {
					target.setOS(StaticClass.osTitles[i]);
					break;
				}
			}
		}
	}
}
