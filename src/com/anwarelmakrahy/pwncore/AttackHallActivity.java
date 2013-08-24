package com.anwarelmakrahy.pwncore;

import com.anwarelmakrahy.pwncore.ConsoleSession.ConsoleSessionParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AttackHallActivity extends Activity {
	
	private TabHost tabHost;
	private ListView mTargetsListView;
	private TargetsListAdapter mTargetsListAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setTheme(android.R.style.Theme_Holo_Light);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_attackhall);
        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
        
        mTargetsListView = (ListView)findViewById(R.id.targetsListView2);
        mTargetsListAdapter =  new TargetsListAdapter(this, MainActivity.mTargetHostList);
        mTargetsListView.setAdapter(mTargetsListAdapter);
        registerForContextMenu(mTargetsListView);
       
        setupTabHost();
        setupListViewListener();
        
    	prepareTargetDetails(0);	        	
    	mTargetsListAdapter.setSelectedIndex(0);
	}
	
	private void setupListViewListener() {
		mTargetsListView.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 	        	
	        	prepareTargetDetails(position);	        	
	        	mTargetsListAdapter.setSelectedIndex(position);
	        	tabHost.setCurrentTab(0);
	    	}
		});
    }


	private void setupTabHost() {
        tabHost=(TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
      
    
        TabSpec spec2=tabHost.newTabSpec("Target Details");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Target Details");
      
        TabSpec spec3=tabHost.newTabSpec("Console Sessions");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Console Sessions");
        
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
	}
	
	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();	
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_LOST);
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}
			
		isConnected = prefs.getBoolean("isConnected", false);
		
		if (!isConnected) {
			Toast.makeText(getApplicationContext(), 
					"ConnectionLost: Please check your network settings", 
					Toast.LENGTH_SHORT).show();
			finish();
		}
		
		super.onResume();
	}
    
	private boolean conStatusReceiverRegistered = false;
	private boolean isConnected = true;
	private SharedPreferences prefs;
	
	@Override
	public void onDestroy() {		
		if (conStatusReceiverRegistered ) {
			unregisterReceiver(conStatusReceiver);
			conStatusReceiverRegistered = false;
		}
		super.onDestroy();
	}
    
    public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticsClass.PWNCORE_CONNECTION_TIMEOUT) {		
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionTimeout: Please check that server is running", 
    					Toast.LENGTH_SHORT).show();
    		}  				
    		else if (action == StaticsClass.PWNCORE_CONNECTION_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionFailed: " + intent.getStringExtra("error"), 
    					Toast.LENGTH_SHORT).show();    	
    		}		
    		else if (action == StaticsClass.PWNCORE_CONNECTION_LOST) {
    			prefs.edit().putBoolean("isConnected", false).commit();
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionLost: Please check your network settings", 
    					Toast.LENGTH_SHORT).show();
    			finish();
    		}
    	}
    };
    
    public void attack(View v) {
    	TargetItem t = new TargetItem("10.0.0.20");
    	t.setPwned(true);
    	t.setOS("Linux");
    	MainActivity.mTargetHostList.add(t);
    	mTargetsListAdapter.notifyDataSetChanged();
    	
    }
    
    private void prepareTargetDetails(int position) {
    	TargetItem t = MainActivity.mTargetHostList.get(position);
    	
    	((TextView)findViewById(R.id.targetDetailsHost)).setText(t.getHost());
    	((TextView)findViewById(R.id.targetDetailsOS)).setText("OS: " + t.getOS());
    	
    	if (t.isPwned()) {
    		((TextView)findViewById(R.id.targetDetailsPwn)).setText("Pwned");
    		((TextView)findViewById(R.id.targetDetailsPwn)).setTextColor(Color.parseColor("#006400"));
    	}
    	else {
    		((TextView)findViewById(R.id.targetDetailsPwn)).setText("Not Pwned");
    		((TextView)findViewById(R.id.targetDetailsPwn)).setTextColor(Color.RED);
    	}
    	
    	if (t.isUp())
    		((TextView)findViewById(R.id.targetDetailsUp)).setText("Availability: Up");
    	else
    		((TextView)findViewById(R.id.targetDetailsUp)).setText("Availability: Down");
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attackhall, menu);
        return true;
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	getMenuInflater().inflate(R.menu.target_attackhall, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        
        switch (item.getItemId()) {
        case R.id.mnuTargetRemove:
        	removeHostFromTargetList(info.position);
        	return true;
        case R.id.mnuTargetScan:
        	scanTarget(MainActivity.mTargetHostList.get(info.position));
        	return true;
        case R.id.mnuTargetOS:
           	AlertDialog builder = new AlertDialog.Builder(this)
            .setSingleChoiceItems(TargetsListAdapter.osTitles, -1, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	        		MainActivity.mTargetHostList.get(info.position).setOS(TargetsListAdapter.osTitles[item]);
	        		mTargetsListAdapter.notifyDataSetChanged();	
                }
            })
            .create();
            builder.show(); 
        	return true;
        default:
        	return false;
        }
    }
    
    private void scanTarget(TargetItem t) {
    	
    	final String cmd = "use auxiliary/scanner/portscan/tcp; set PORTS 80; set RHOSTS 10.0.0.1; set THREADS 10; run";
    	
    	new Thread(new Runnable() {
			@Override public void run() {

				ConsoleSession newConsole = MainService.sessionMgr.getNewConsole();
				newConsole.waitForReady();
				newConsole.write(cmd);
				
			}
    	}).start();
    }
    
    private void removeHostFromTargetList(int pos) {	
		MainActivity.mTargetHostList.remove(pos);
    	if (MainActivity.mTargetHostList.size() == 0)
    		finish();	
    	mTargetsListAdapter.notifyDataSetChanged();
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {    
	    case R.id.mnuAddHosts:
	    	startActivity(new Intent(getApplicationContext(), AttackWizardActivity.class));
	    	finish();    	
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    public void launchHead(View v) {
    	popupWindowHandler.postDelayed(popupWindowRunnable, 0);
    	//runOnUiThread(popupWindowRunnable);
    }
    
    final Runnable popupWindowRunnable = new Runnable()
    {
        public void run() 
        {
        	initiatePopupWindow();
        }
    };

    private Handler popupWindowHandler = new Handler();
    
    private PopupWindow pwindo;
    private void initiatePopupWindow() {
	    try {
	    	Display display = getWindowManager().getDefaultDisplay();
	    	Point size = new Point();
	    	display.getSize(size);
	    	
		    LayoutInflater inflater = (LayoutInflater)AttackHallActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
		    final View layout = inflater.inflate(R.layout.layout_console, (ViewGroup)findViewById(R.id.popup_element));
		    pwindo = new PopupWindow(layout, size.x - 30, (int)(size.y * 0.75), true);
		    
		    pwindo.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#eeeeee")));
		    pwindo.setOutsideTouchable(true);
		    
		    pwindo.setTouchable(true);
		    pwindo.setFocusable(true);
		    pwindo.showAtLocation(layout, Gravity.BOTTOM, 0, 15);
		    	    
		    final EditText commander = (EditText)layout.findViewById(R.id.consoleWrite);
		    final ScrollView scroller = (ScrollView)layout.findViewById(R.id.textAreaScroller);		    
		    ((TextView)layout.findViewById(R.id.consoleRead)).addTextChangedListener(new TextWatcher() {
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable s) {
                	Log.d("scrollview", "scrollview");
                	scroller.post(new Runnable() {            
                	    @Override
                	    public void run() {
                	           scroller.fullScroll(View.FOCUS_DOWN);              
                	    }
                	});
                }
            });
		    		    
		    ConsoleSessionParams params = new ConsoleSessionParams();
		    params.setAcivity(this);
		    params.setCmdView((TextView)layout.findViewById(R.id.consoleRead));
		    params.setPromptView((TextView)layout.findViewById(R.id.consolePrompt));
		    
		    final ConsoleSession newConsole = MainService.sessionMgr.getNewConsole(params);
		    MainService.sessionMgr.switchConsoleWindow(newConsole.getId());
		    
		    commander.setOnEditorActionListener(new OnEditorActionListener() {

		    	String cmd = null;
		    	
	    		@Override
		        public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
	    			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){               

	    				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    				imm.hideSoftInputFromWindow(commander.getWindowToken(), 0);
		               
	    				cmd = v.getText().toString();
	    				v.setText("");
	    				
						new Thread(new Runnable() {
						    public void run() {
						    	newConsole.write(cmd);
						    }
						  }).start();
					
		               return true;
		            }
	    			
		            return false;
		        }
		    });
		    
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    }
}
