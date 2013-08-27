package com.anwarelmakrahy.pwncore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.msgpack.template.Templates.*;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Converter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;


public class ModuleOptionsActivity extends Activity {
	
	String moduleName = null;
	String moduleType = null;
	boolean infoLoaded = false;
	boolean optsLoaded = false;
	
	Spinner spinner;
	GridLayout layout;
	List<View> advancedView = new ArrayList<View>();
	Map<String, View> options = new HashMap<String, View>(); 
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        setContentView(R.layout.activity_moduleoptions);
        setTitle("Module Options");

        Intent intent = getIntent();
        moduleName = intent.getStringExtra("name");
        moduleType = intent.getStringExtra("type");
        
        if (moduleType.contains("exploits"))
        	moduleType = "exploit";
        else if (moduleType.contains("payloads"))
        	moduleType = "payload";
        else if (moduleType.contains("encoders"))
        	moduleType = "encoder";
        else if (moduleType.contains("nops"))
        	moduleType = "nop";
        else if (moduleType.contains("posts"))
        	moduleType = "post";

        spinner = (Spinner) findViewById(R.id.moduleOptTargets);
        layout = (GridLayout) findViewById(R.id.moduleOptLayout);
        
    	((CheckBox)findViewById(R.id.moduleOptAdvanced)).
		setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
			if (isChecked)
				for (int i=0; i<advancedView.size(); i++)
					advancedView.get(i).setVisibility(View.VISIBLE);
			else
				for (int i=0; i<advancedView.size(); i++)
					advancedView.get(i).setVisibility(View.GONE);
		}
	});
        
        loadOptions();
        
        new AsyncTask<Void, Void, Void>() {
        	private ProgressDialog pd = null; 	
    		@Override protected void onPreExecute() {
    			pd = ProgressDialog.show(
    	                ModuleOptionsActivity.this,
    	                null, "Loading Module Options",
    	                true, true,
    	                new DialogInterface.OnCancelListener(){
    	                    @Override
    	                    public void onCancel(DialogInterface dialog) {
    	                        cancel(true);
    	                    }
    	                }
    	        );
    		}
    			
    		@Override protected Void doInBackground(Void... arg0) {
    			while (!infoLoaded || !optsLoaded) {
	    			try {
	    				Thread.sleep(200);
	    			} catch (InterruptedException e) {
	    				if (!infoLoaded || !optsLoaded) finish();
	    			}	
    			}
    			return null;
    		}
    		
    		@Override protected void onPostExecute(Void result) {
    			if (pd!=null) { pd.dismiss(); }
    		}  			
    	}
    	.execute((Void[])null);
    	

	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	private void loadOptions() {
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
		        List<Object> params = new ArrayList<Object>();
		        params.add("module.info");
		        params.add(moduleType);
		        params.add(moduleName);
		        parseInfo(MainService.client.call(params));
		        
		        params = new ArrayList<Object>();
		        params.add("module.options");
		        params.add(moduleType);
		        params.add(moduleName); 
		        parseOptions(MainService.client.call(params));		
			}}, 0);
	}
	
	private void parseInfo(final Map<String, Value> info) {
		if (info == null) finish();				
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				
				TextView t = new TextView(ModuleOptionsActivity.this);
				t.setTextSize(15);
				t.setTypeface(Typeface.MONOSPACE);
				
				((TextView)findViewById(R.id.moduleOptTitle)).setText(info.containsKey("name")?info.get("name").
						asRawValue().getString(): "Unknown Name");
				((TextView)findViewById(R.id.moduleOptDesc)).setMovementMethod(ScrollingMovementMethod.getInstance());
				((TextView)findViewById(R.id.moduleOptDesc)).setText(info.containsKey("description")?info.get("description").
						asRawValue().getString().replace("\t", "").
						replace("\n", " ").trim(): "Description not set");		
				((TextView)findViewById(R.id.moduleOptPath)).setText(moduleName);

				
				if (info.containsKey("targets")) {
					try {
						
						Converter mapCon = new Converter(info.get("targets").asMapValue());
						Map<Integer, String> targets = mapCon.read(tMap(TInteger,TString));
						mapCon.close();
						
						String[] targetsArray = targets.values().toArray(new String[targets.size()]);
						ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ModuleOptionsActivity.this, 
								android.R.layout.simple_spinner_item, targetsArray);
						spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spinner.setAdapter(spinnerArrayAdapter);
						
						spinner.setVisibility(View.VISIBLE);
						findViewById(R.id.moduleOptSpinnerTitle).setVisibility(View.VISIBLE);
						
						if (info.containsKey("default_target")) {
							spinner.setSelection(info.get("default_target").asIntegerValue().getInt());
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (moduleType.contains("exploit"))
					findViewById(R.id.moduleOptReverse).setVisibility(View.VISIBLE);

				infoLoaded = true;
			}
		}, 0);
	}
	
	private void parseOptions(final Map<String, Value> opts) {
		if (opts == null) finish();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				
				for (int i=0; i<opts.size(); i++) {
					String key = opts.keySet().toArray()[i].toString();
					
					try {
						Converter mapCon = new Converter(opts.get(key).asMapValue());
						Map<String, Value> mOpts = mapCon.read(tMap(TString,TValue));
						mapCon.close();
						
						TextView title = new TextView(ModuleOptionsActivity.this);
						title.setText(key);
						title.setTextSize(16);
											
						EditText edit = new EditText(ModuleOptionsActivity.this);	
						GridLayout.LayoutParams params = new GridLayout.LayoutParams();
						params.setGravity(Gravity.FILL_HORIZONTAL);
						edit.setLayoutParams(params);
						edit.setWidth(0);
						edit.setSingleLine(true);
						
						if (mOpts.containsKey("default") && mOpts.containsKey("type")) {
							
							if (!mOpts.get("type").asRawValue().getString().contains("bool")) {
								edit.setText(mOpts.get("default").asRawValue().getString());
							}
							else
								edit.setText(mOpts.get("default").asBooleanValue().getBoolean()? "true":"false");
						}
						
						options.put(key, edit);
						
						if (!mOpts.get("advanced").asBooleanValue().getBoolean()) {
							layout.addView(title);
							layout.addView(edit);
						}
						else {
							advancedView.add(title);
							advancedView.add(edit);
							
							title.setVisibility(View.GONE);
							edit.setVisibility(View.GONE);
						}
	
					}
					catch (Exception e) { }
				}	
				
				for (int i=0; i<advancedView.size(); i++)
					layout.addView(advancedView.get(i));
				
				optsLoaded = true;
			}
		}, 0);
	}
}
