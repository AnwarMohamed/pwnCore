package com.anwarelmakrahy.pwncore.console.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.structures.ModuleItem;
import com.anwarelmakrahy.pwncore.structures.HostItem;

public class AttackFinder {
	
	public static final String FINDATTACKS_BY_PORTS = "FINDATTACKS_BY_PORTS";
	public static final String FINDATTACKS_BY_SERVICES = "FINDATTACKS_BY_SERVICES";
	public static final String FINDATTACKS_BY_OS = "FINDATTACKS_BY_OS";
	
	private HostItem host;
	public AttackFinder(HostItem host) {
		this.host = host;
	}

	public Map<String, List<String>> findAttacks(String attackFlag) {
		if (!host.isUp()) return null;
		
		if (FINDATTACKS_BY_OS.equals(attackFlag)) {
			return suggestByOS(host.getOSCodeName());
		}
		else if (FINDATTACKS_BY_SERVICES.equals(attackFlag)) {
			return null;
		}
		else if (FINDATTACKS_BY_PORTS.equals(attackFlag)) {
			return suggestByPorts();
		}
		else
			return new HashMap<String, List<String>>();
	}

	private Map<String, List<String>> suggestByOS(String[] os) {
		
		Map<String, List<String>> suggested = new HashMap<String, List<String>>();
		List<String> osList = Arrays.asList(os);
		
		for (ModuleItem temp : MainService.modulesMap.ExploitItems) {
			String[] exploitDesc = temp.getPath().split("/");
			if (exploitDesc.length >= 3 &&
					osList.contains(exploitDesc[0])) {
				
				if (!suggested.containsKey(exploitDesc[1]))
					suggested.put(exploitDesc[1], new ArrayList<String>());
				
				if (!suggested.get(exploitDesc[1]).contains(temp.getPath()))
					suggested.get(exploitDesc[1]).add(temp.getPath());
				
			}
		};
		
		return suggested;
	}
	
	private Map<String, List<String>> suggestByPorts() {
		
		Map<String, List<String>> suggested = new HashMap<String, List<String>>();		
		Map<String, List<String>> portsMap = new HashMap<String, List<String>>();
		
		//get modules ports map
		for (ModuleItem temp : MainService.modulesMap.ExploitItems) {
			
			List<Object> params = new ArrayList<Object>();
			params.add("module.option");
			params.add("exploit");
			params.add(temp.getPath());
	
			Map<String, Value> res = MainService.client.call(params);	
			
			if (res != null && res.containsKey("RPORT") &&
					res.get("RPORT").asMapValue().containsKey("default")) {
				String port = res.get("RPORT").asMapValue().get("default").asRawValue().getString();
				
				if (!portsMap.containsKey(port))
					portsMap.put(port, new ArrayList<String>());
				
				portsMap.get(port).add(temp.getPath());
				
				if (port.equals("80")) {
					if (!portsMap.containsKey("443"))
						portsMap.put("443", new ArrayList<String>());
					
					portsMap.get("443").add(temp.getPath());
				}
				else if (port.equals("443")) {
					if (!portsMap.containsKey("80"))
						portsMap.put("80", new ArrayList<String>());
					
					portsMap.get("80").add(temp.getPath());
				}
				else if (port.equals("445")) {
					if (!portsMap.containsKey("139"))
						portsMap.put("139", new ArrayList<String>());
					
					portsMap.get("139").add(temp.getPath());
				}
				else if (port.equals("139")) {
					if (!portsMap.containsKey("445"))
						portsMap.put("445", new ArrayList<String>());
					
					portsMap.get("445").add(temp.getPath());
				}
			}
		};
		
		//select ports for specific target
		for (Entry<String, String> entry : host.getTcpPorts().entrySet()) {
			if (portsMap.containsKey(entry.getKey()))
				suggested.put(entry.getKey(), portsMap.get(entry.getKey()));
		}
		
		return suggested;
	}
}

