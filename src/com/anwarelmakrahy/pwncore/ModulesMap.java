package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class ModulesMap {

	public List<ModuleItem>	ExploitItems = new ArrayList<ModuleItem>(), 
							PayloadItems = new ArrayList<ModuleItem>(), 
							PostItems = new ArrayList<ModuleItem>(), 
							NopItems = new ArrayList<ModuleItem>(), 
							AuxiliaryItems = new ArrayList<ModuleItem>(), 
							EncoderItems = new ArrayList<ModuleItem>();
	
	private List<ModuleItem> tmp = new ArrayList<ModuleItem>();
	
	public ModuleListAdapter modulesAdapter;
	
	ModulesMap(Context context) {
		modulesAdapter = new ModuleListAdapter(context, tmp);
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
}
