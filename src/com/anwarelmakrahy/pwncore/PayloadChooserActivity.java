package com.anwarelmakrahy.pwncore;

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.tList;
import static org.msgpack.template.Templates.tMap;
import static org.msgpack.template.Templates.TValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PayloadChooserActivity extends Activity {

	private Intent intent;
	private String payloadName;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_payloadchooser);
        
        intent = getIntent();
        if (intent == null || 
        		!intent.hasExtra("exploit") || 
        		!intent.hasExtra("target")) {
 			Toast.makeText(getApplicationContext(), 
 					"Error launching payload options", 
 					Toast.LENGTH_SHORT).show();
         	finish();
         	return;
        }
        
        new AsyncTask<Void, Void, Void>() {  	 	
    		@Override protected void onPreExecute() {
    			pd = ProgressDialog.show(
    					PayloadChooserActivity.this,
    	                null, "Loading Payloads",
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
    			while (!payloadsLoaded) {
	    			try {
	    				Thread.sleep(50);
	    			} catch (InterruptedException e) {
	    				if (!payloadsLoaded) finish();
	    			}	
    			}
    			return null;
    		}
    		
    		@Override protected void onPostExecute(Void result) {
    			if (pd!=null) { pd.dismiss(); }
    		}  			
    	}
    	.execute((Void[])null);
        
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
		        layout = (GridLayout) findViewById(R.id.payloadOptLayout);       
		        loadPayloads(
		        		intent.getStringExtra("exploit"),
		        		intent.getStringExtra("target"));
			}}, 0);
	}
	
	private ProgressDialog pd = null;
	
	private void loadPayloads(String name, String target) {
		List<Object> params = new ArrayList<Object>();
		params.add("module.target_compatible_payloads");
		params.add(name);
		params.add(Integer.parseInt(target));
		
		Map<String, Value> payloads = MainService.client.call(params);
		
		if (payloads == null || 
				!payloads.containsKey("payloads")) {
			finish();
			return;
		}
		
		try {
			Converter mapCon = new Converter(payloads.get("payloads").asArrayValue());
			List<String> payloadList = mapCon.read(tList(TString));
			mapCon.close();
			
			
			final ListView list = (ListView)findViewById(R.id.payloadList);
			
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, 
					R.layout.payload_item, payloadList.toArray(new String[payloadList.size()]));
			list.setAdapter(spinnerArrayAdapter);
			list.setOnItemClickListener(new OnItemClickListener() {
		        @Override
		        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 	
		        	payloadName = (String)list.getItemAtPosition(position);
	
		        	new Handler().postDelayed(new Runnable() {
		    			@Override public void run() {
		    		        List<Object> params = new ArrayList<Object>();
		    		        params.add("module.options");
		    		        params.add("payload");
		    		        params.add(payloadName); 
		    		        parseOptions(MainService.client.call(params));		
		    			}}, 0);
		        }
			});
			
		}
		catch (Exception e) {
			finish();
			return;
		}
		payloadsLoaded  = true;
	}
	
	private GridLayout layout;
	private Map<String, View> optionsView = new HashMap<String, View>();
	private boolean payloadsLoaded = false; 
	
	public static Map<String, Map<String, Value>> moduleOptions = new HashMap<String, Map<String, Value>>();
	private void parseOptions(final Map<String, Value> opts) {
		if (opts == null) {		
			finish();
			return;
		}
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				
				Converter mapCon;
				
				TextView title;
				EditText edit;
				Map<String, Value> mOpts;
				
				layout.removeAllViews();
				optionsView.clear();
				
				findViewById(R.id.payloadOptStart).setVisibility(View.VISIBLE);
				findViewById(R.id.payloadOptScroll).setVisibility(View.VISIBLE);
				findViewById(R.id.payloadOptTitle).setVisibility(View.VISIBLE);

				for (int i=0; i<opts.size(); i++) {
					String key = opts.keySet().toArray()[i].toString();
					
					try {
						mapCon = new Converter(opts.get(key).asMapValue());
						mOpts = mapCon.read(tMap(TString,TValue));
						mapCon.close();
						
						moduleOptions.put(key, mOpts);
						
						title = getTextView(key);										
						edit = getEditText();
						
						if (mOpts.containsKey("default") && mOpts.containsKey("type")) {
							
							if (!mOpts.get("type").asRawValue().getString().contains("bool")) {
								edit.setText(mOpts.get("default").asRawValue().getString());
							}
							else
								edit.setText(mOpts.get("default").asBooleanValue().getBoolean()? "true":"false");
						}
						
						optionsView.put(key, edit);
						layout.addView(title);
						layout.addView(edit);
					}
					catch (Exception e) { }
				}	
				
				
			}}, 0);
	}
	
	private EditText getEditText() {
		EditText edit = new EditText(PayloadChooserActivity.this);	
		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.setGravity(Gravity.FILL_HORIZONTAL);
		edit.setLayoutParams(params);
		edit.setWidth(0);
		edit.setSingleLine(true);
		return edit;
	}
	
	private TextView getTextView(String text) {
		TextView title = new TextView(PayloadChooserActivity.this);
		title.setText(text);
		title.setTextSize(16);
		return title;
	}
	
	private Map<String, Object> moduleParams = new HashMap<String, Object>();
	public void launch(View v) {
		
		String cmd = "use " + intent.getStringExtra("exploit") + "\n" +
					 "set PAYLOAD " + payloadName + "\n";
		
		String[] moduleKeys = ModuleOptionsActivity.moduleParams.keySet().
				toArray(new String[ModuleOptionsActivity.moduleParams.size()]);
		
		for (int i=0; i<moduleKeys.length; i++) {
			cmd += "set " + moduleKeys[i] + " \"" + 
						ModuleOptionsActivity.moduleParams.get(moduleKeys[i]) + "\"\n";
		}
		
		moduleParams.clear();
		
		Object[] keys = optionsView.keySet().toArray();
		Object[] values = optionsView.values().toArray();
		
		String text;
		
		for (int i=0; i<optionsView.size(); i++) {
			
			text = ((TextView)(values[i])).getText().toString().trim();
			
			if (text.length() == 0 && 
					moduleOptions.containsKey(keys[i]) && 
					moduleOptions.get(keys[i]).get("required").asBooleanValue().getBoolean()) {
				Toast.makeText(
						getApplicationContext(), 
						keys[i].toString() + " has to be set", 
						Toast.LENGTH_SHORT
						).show();
				return;
			}
			else if (text.length() > 0) {
				if (moduleOptions.get(keys[i]).containsKey("default")) { 
					
					if (moduleOptions.get(keys[i]).containsKey("type")) {
					
						if (moduleOptions.get(keys[i]).get("type").asRawValue().getString().equals("bool")) {
							
							if (!text.equals("true") && !text.equals("false")) {
								
							}
							else {
								String bool = moduleOptions.get(keys[i]).get("default").
										asBooleanValue().getBoolean() ? "true" : "false";
								
								if (!bool.equals(text))
									moduleParams.put(keys[i].toString(), text);							
							}

						}
						else {
							if (!moduleOptions.get(keys[i]).get("default").
									asRawValue().getString().equals(text))
								moduleParams.put(keys[i].toString(), text);
						}
					}
				}
				else
					moduleParams.put(keys[i].toString(), text);
			}
		}
		
		moduleKeys = moduleParams.keySet().
				toArray(new String[moduleParams.size()]);
		
		for (int i=0; i<moduleKeys.length; i++) {
			cmd += "set " + moduleKeys[i] + " \"" + 
						moduleParams.get(moduleKeys[i]) + "\"\n";
		}
		
		cmd += "exploit -j";
		
    	Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.putExtra("type", "new");
    	intent.putExtra("cmd", cmd);
    	startActivity(intent); 
    	
    	ModuleOptionsActivity.activity.finish();
    	finish();
	}
}
