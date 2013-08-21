package com.anwarelmakrahy.pwncore;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class SearchModulesActivity extends Activity {

	private String q;
	private DatabaseHandler db;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setTitle("Search Results");
        setTheme(android.R.style.Theme_Holo_Light);
        setContentView(R.layout.activity_searchmodules);
        
        q = getIntent().getStringExtra("q");
        db = new DatabaseHandler(this);
        
        ListView list = (ListView)findViewById(R.id.searchListView);      
        list.setEmptyView(findViewById(R.id.noResultsSearch));
    }
}
