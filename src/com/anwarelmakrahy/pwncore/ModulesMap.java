package com.anwarelmakrahy.pwncore;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.structures.ModuleItem;
import com.anwarelmakrahy.pwncore.structures.ModulesAdapter;

import android.content.Context;

public class ModulesMap {

	public List<ModuleItem>	ExploitItems = new ArrayList<ModuleItem>(), 
							PayloadItems = new ArrayList<ModuleItem>(), 
							PostItems = new ArrayList<ModuleItem>(), 
							NopItems = new ArrayList<ModuleItem>(), 
							AuxiliaryItems = new ArrayList<ModuleItem>(), 
							EncoderItems = new ArrayList<ModuleItem>();
	
	private List<ModuleItem> tmp = new ArrayList<ModuleItem>();
	
	public ModulesAdapter modulesAdapter;
	public Map<String, List<String>> portsMap = new HashMap<String, List<String>>();
	
	ModulesMap(Context context) {
		modulesAdapter = new ModulesAdapter(context, tmp);
	}
	
	public void setList(String type, List<ModuleItem> list) {
		if (type.equals("exploit")) {
			ExploitItems.clear();
			ExploitItems.addAll(list);
		}
		else if (type.equals("payload")) {
			PayloadItems.clear();
			PayloadItems.addAll(list);
		}
		else if (type.equals("nop")) {
			NopItems.clear();
			NopItems.addAll(list);
		}
		else if (type.equals("post")) {
			PostItems.clear();
			PostItems.addAll(list);
		}
		else if (type.equals("encoder")) {
			EncoderItems.clear();
			EncoderItems.addAll(list);
		}
		else if (type.equals("auxiliary")) {
			AuxiliaryItems.clear();
			AuxiliaryItems.addAll(list);
		}
		
		modulesAdapter.notifyDataSetChanged();
		System.gc();
	}

	public void switchAdapter(String type) {
		if (type.equals("exploit")) {
			tmp.clear();
			tmp.addAll(ExploitItems);
		}
		else if (type.equals("payload")) {
			tmp.clear();
			tmp.addAll(PayloadItems);
		}
		else if (type.equals("nop")) {
			tmp.clear();
			tmp.addAll(NopItems);
		}
		else if (type.equals("post")) {
			tmp.clear();
			tmp.addAll(PostItems);
		}
		else if (type.equals("encoder")) {
			tmp.clear();
			tmp.addAll(EncoderItems);
		}
		else if (type.equals("auxiliary")) {
			tmp.clear();
			tmp.addAll(AuxiliaryItems);
		}
		
		modulesAdapter.notifyDataSetChanged();
	}
	
	public void loadModulesOptions(String type) {
		if (type.equals("exploits")) {
			for (ModuleItem temp : MainService.modulesMap.ExploitItems) {
				
				List<Object> params = new ArrayList<Object>();
				params.add("module.options");
				params.add("exploit");
				params.add(temp.getPath());
		
				Map<String, Value> res = MainService.client.call(params);	
				
				if (res != null && MainService.databaseHandler != null) {
					try {
						
						MessagePack msgpack = new MessagePack();
				        ByteArrayOutputStream out = new ByteArrayOutputStream();
				        Packer packer = msgpack.createPacker(out);
				        packer.write(res);

						MainService.databaseHandler.updateModuleOptions(temp, type, out.toByteArray());
						out.close();
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				/*if (res != null && res.containsKey("RPORT") &&
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
				}*/
			};
		}
	}
}
