package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.type.Value;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

public class SearchModulesActivity extends Activity {

	private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setTitle("Search Results");
        setTheme(android.R.style.Theme_Holo_Light);
        setContentView(R.layout.activity_searchmodules);
        
        if (!getIntent().hasExtra("q")) {
        	finish();
        	return;
        }
        
        String q = getIntent().getStringExtra("q");
        db = new DatabaseHandler(this);
        
        ListView list = (ListView)findViewById(R.id.searchListView);      
        list.setEmptyView(findViewById(R.id.noResultsSearch));
        
        getSearchResults(q);
    }

	private void getSearchResults(final String q) {
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
		        List<Object> params = new ArrayList<Object>();
		        
		        Map<String, Object> searchOpts = new HashMap<String, Object>();
		        searchOpts.put("include", new String[] {"exploits", "payloads"});
		        searchOpts.put("keywords", new String[] {q});
		        searchOpts.put("maximum", 200);	        
		        
		        params.add("modules.search");
		        params.add(searchOpts);

		        parseSearch(MainService.client.call(params));
			}}, 0);		
	}

	private void parseSearch(Map<String, Value> res) {
		Log.d("size", Integer.toString(res.size()));
	}
}
