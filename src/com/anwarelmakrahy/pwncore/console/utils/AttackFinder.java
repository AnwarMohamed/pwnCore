package com.anwarelmakrahy.pwncore.console.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.structures.ModuleItem;
import com.anwarelmakrahy.pwncore.structures.TargetItem;

public class AttackFinder {
	
	public static final String FINDATTACKS_BY_PORTS = "FINDATTACKS_BY_PORTS";
	public static final String FINDATTACKS_BY_SERVICES = "FINDATTACKS_BY_SERVICES";
	public static final String FINDATTACKS_BY_OS = "FINDATTACKS_BY_OS";
	
	private TargetItem target;
	public AttackFinder(TargetItem target) {
		this.target = target;
	}

	public Map<String, Map<String, String>> findAttacks(String attackFlag) {
		if (!target.isUp()) return null;
		
		if (FINDATTACKS_BY_OS.equals(attackFlag)) {
			return suggestByOS(target.getOSCodeName());
		}
		else if (FINDATTACKS_BY_SERVICES.equals(attackFlag)) {
			return null;
		}
		else if (FINDATTACKS_BY_PORTS.equals(attackFlag)) {
			return null;
		}
		else
			return new HashMap<String, Map<String, String>>();
	}

	private Map<String, Map<String, String>> suggestByOS(String[] os) {
		
		Map<String, Map<String, String>> suggested = new HashMap<String, Map<String, String>>();
		List<String> osList = Arrays.asList(os);
		
		for (ModuleItem temp : MainService.modulesMap.ExploitItems) {
			String[] exploitDesc = temp.getPath().split("/");
			if (exploitDesc.length >= 3 &&
					osList.contains(exploitDesc[0])) {
				
				if (!suggested.containsKey(exploitDesc[1]))
					suggested.put(exploitDesc[1], new HashMap<String, String>());
				
				if (!suggested.get(exploitDesc[1]).containsKey(exploitDesc[2]))
					suggested.get(exploitDesc[1]).put(exploitDesc[2], temp.getPath());
				
			}
		};
		
		return suggested;
	}
}
