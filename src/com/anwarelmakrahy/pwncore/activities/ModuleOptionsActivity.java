package com.anwarelmakrahy.pwncore.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.msgpack.template.Templates.*;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.console.ConsoleSession.ConsoleSessionParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class ModuleOptionsActivity extends Activity {
	
	private String moduleName = null;
	private String moduleType = null;
	boolean infoLoaded = false;
	boolean optsLoaded = false;
	
	private Spinner spinner;
	private GridLayout layout;
	private List<View> advancedView = new ArrayList<View>();
	private Map<String, View> optionsView = new HashMap<String, View>(); 
	public static Activity activity;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_moduleoptions);

        activity = this;
        Intent intent = getIntent();
        moduleName = intent.getStringExtra("name");
        moduleType = intent.getStringExtra("type");
        
        if (moduleType == null || moduleName == null) { 
 			Toast.makeText(getApplicationContext(), 
 					"Error launching module options", 
 					Toast.LENGTH_SHORT).show();
         	finish();
         	return;
         }
        
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
  
        new AsyncTask<Void, Void, Void>() {  	 	
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
	    				Thread.sleep(50);
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
    	
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
	
				loadOptions();
			        
		        spinner = (Spinner) findViewById(R.id.moduleOptTargets);
		        layout = (GridLayout) findViewById(R.id.moduleOptLayout);
		        
		        if (moduleType.equals("exploit"))
		        	((Button)findViewById(R.id.moduleOptStart)).setText("Select Payload");
		        
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
				
			}}, 0);
	}
	
	private ProgressDialog pd = null;
	
	@Override
	public void onDestroy() {
		if (pd != null)
			pd.dismiss();
		super.onDestroy();
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
		if (info == null) { 
			finish();
			return;
		}
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
				infoLoaded = true;
			}}, 0);
	}
	
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
	
	public static Map<String, String> moduleParams = new HashMap<String, String>();
	public void launch(View v) {
		
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
		
		if (moduleType.equals("exploit")) {
	    	Intent intent = new Intent(getApplicationContext(), PayloadChooserActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.putExtra("exploit", moduleName);
	    	intent.putExtra("target", Integer.toString(spinner.getSelectedItemPosition()));
	    	startActivity(intent);  
		}
		else if (moduleType.equals("payload")) {
			
			String cmd = "use exploit/multi/handler\n" +
					 "set PAYLOAD " + moduleName + "\n";
		
			String[] moduleKeys = moduleParams.keySet().toArray(new String[moduleParams.size()]);
		
			for (int i=0; i<moduleKeys.length; i++)
				cmd += "set " + moduleKeys[i] + " \"" + 
							moduleParams.get(moduleKeys[i]) + "\"\n";
						
			cmd += "exploit -j";
			
	    	Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.putExtra("type", "new.console");
	    	intent.putExtra("cmd", cmd);
	    	startActivity(intent);   
	    	finish();
		}
		else if (moduleType.equals("auxiliary")) {
			
			String cmd = "use " + moduleName + "\n";
		
			String[] moduleKeys = moduleParams.keySet().toArray(new String[moduleParams.size()]);
		
			for (int i=0; i<moduleKeys.length; i++)
				cmd += "set " + moduleKeys[i] + " \"" + 
							moduleParams.get(moduleKeys[i]) + "\"\n";
						
			cmd += "run";
			
	    	Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.putExtra("type", "new.console");
	    	intent.putExtra("cmd", cmd);
	    	startActivity(intent);   
	    	finish();
		}
		else {
		    ConsoleSessionParams consoleParams = new ConsoleSessionParams();
		    consoleParams.setAcivity(this);
		    consoleParams.setCmdViewId(R.id.consoleRead);
		    consoleParams.setPromptViewId(R.id.consolePrompt);
			    
		    ConsoleSession console = new ConsoleSession(getApplicationContext(), consoleParams, moduleName);
		    MainService.sessionMgr.getNewConsole(console);
			
	    	Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.putExtra("type", "current.console");
	    	intent.putExtra("id", console.getId());
	    	startActivity(intent);   
	    	finish();
		}
	}
	
	private EditText getEditText() {
		EditText edit = new EditText(ModuleOptionsActivity.this);	
		GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.setGravity(Gravity.FILL_HORIZONTAL);
		edit.setLayoutParams(params);
		edit.setWidth(0);
		edit.setSingleLine(true);
		return edit;
	}
	
	private TextView getTextView(String text) {
		TextView title = new TextView(ModuleOptionsActivity.this);
		title.setText(text);
		title.setTextSize(16);
		return title;
	}
}
