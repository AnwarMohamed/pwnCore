package com.anwarelmakrahy.pwncore.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.anwarelmakrahy.plugindroid.PluginManager;
import com.anwarelmakrahy.plugindroid.PluginManager.PluginDetails;
import com.anwarelmakrahy.pwncore.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class PluginsActivity extends Activity {
	
	private List<pwnCorePlugin> plugins = new ArrayList<pwnCorePlugin>();
	private PluginsListAdapter listAdapter;
	private ListView listView;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_plugins);
        
        
        listView = (ListView)findViewById(R.id.listView1);
        listAdapter = new PluginsListAdapter(
        		getApplicationContext(),
        		preparePluginsList());
        listView.setAdapter(listAdapter);
	}
	
	private List<pwnCorePlugin> preparePluginsList() {
		plugins.clear();
		
		Map<String, PluginDetails> pluginsPkgs = PluginManager.getLoadedPlugins();
		
		String[] packageNames = pluginsPkgs.keySet().toArray(new String[pluginsPkgs.size()]);
		
		for (int i=0; i<pluginsPkgs.size(); i++) {
			pwnCorePlugin plugin = new pwnCorePlugin(pluginsPkgs.get(packageNames[i]));
			
			plugins.add(plugin);
		}
		
		return plugins;
	}
}
