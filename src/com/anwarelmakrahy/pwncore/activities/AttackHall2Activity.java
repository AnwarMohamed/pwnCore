package com.anwarelmakrahy.pwncore.activities;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.console.ConsoleSession.ConsoleSessionParams;
import com.anwarelmakrahy.pwncore.structures.TargetItem;
import com.anwarelmakrahy.pwncore.structures.TargetsListAdapter;

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
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AttackHall2Activity extends Activity {
	
	private TabHost tabHost;
	private ListView mTargetsListView;
	private TargetsListAdapter mTargetsListAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_attackhall);
        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
        MainService.sessionMgr.getNewConsole(newConsole);
        
        mTargetsListView = (ListView)findViewById(R.id.targetsListView2);
        mTargetsListAdapter =  new TargetsListAdapter(this, MainService.mTargetHostList);
        mTargetsListView.setAdapter(mTargetsListAdapter);
        registerForContextMenu(mTargetsListView);
       
        setupTabHost();
        setupListViewListener();
        
    	prepareTargetDetails(0);	        	
    	mTargetsListAdapter.setSelectedIndex(0);

	}
	
	private ConsoleSession newConsole;
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
        TabSpec spec = tabHost.newTabSpec("Target Details");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Target Details");  
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("Consoles");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Consoles");   
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Sessions");
        spec.setContent(R.id.tab5);
        spec.setIndicator("Sessions");   
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("Jobs");
        spec.setContent(R.id.tab4);
        spec.setIndicator("Jobs"); 
        tabHost.addTab(spec);
        
        tabHost.setOnTabChangedListener(
    		new OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					
				}
			}
		);
	}
	
	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();	
			filter.addAction(StaticClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticClass.PWNCORE_CONNECTION_LOST);
			filter.addAction(StaticClass.PWNCORE_NOTIFY_ADAPTER_UPDATE);
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

		MainService.sessionMgr.destroyConsole(newConsole);
		super.onDestroy();
	}
    
    public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticClass.PWNCORE_CONNECTION_TIMEOUT) {		
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionTimeout: Please check that server is running", 
    					Toast.LENGTH_SHORT).show();
    		}  				
    		else if (action == StaticClass.PWNCORE_CONNECTION_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionFailed: " + intent.getStringExtra("error"), 
    					Toast.LENGTH_SHORT).show();    	
    		}		
    		else if (action == StaticClass.PWNCORE_CONNECTION_LOST) {
    			prefs.edit().putBoolean("isConnected", false).commit();
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionLost: Please check your network settings", 
    					Toast.LENGTH_SHORT).show();
    			finish();
    		}
    		else if (action == StaticClass.PWNCORE_NOTIFY_ADAPTER_UPDATE) {
    			mTargetsListAdapter.notifyDataSetChanged();
    			prepareTargetDetails(curListPosition);
    		}
    	}
    };
     
    private int curListPosition = 0;
    
    private void prepareTargetDetails(int position) {
    	TargetItem t = MainService.mTargetHostList.get(position);
    	
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
    	
    	if (!t.isUp()) {
    		((TextView)findViewById(R.id.targetDetailsUp)).setText("Availability: Down");
    		((TextView)findViewById(R.id.targetDetailsPorts)).setText("Open Ports: None");
    	}
    	else {
    		((TextView)findViewById(R.id.targetDetailsUp)).setText("Availability: Up");
    		
    		String openPorts = "";
    		
    		String[] tcpServiceArray = t.getTcpPorts().values().toArray(new String[t.getTcpPorts().size()]);
    		String[] tcpPortArray = t.getTcpPorts().keySet().toArray(new String[t.getTcpPorts().size()]);
    		
    		for (int i=0; i<t.getTcpPorts().size(); i++)
    			openPorts += "\n\t" +tcpPortArray[i] + "\t\tTCP\t\t" + tcpServiceArray[i];
    		
    		
    		String[] udpServiceArray = t.getUdpPorts().values().toArray(new String[t.getUdpPorts().size()]);
    		String[] udpPortArray = t.getUdpPorts().keySet().toArray(new String[t.getUdpPorts().size()]);
    		
    		for (int i=0; i<t.getUdpPorts().size(); i++)
    			openPorts += "\n\t" + udpPortArray[i] + "\t\tUDP\t\t" + udpServiceArray[i];
    		
    		((TextView)findViewById(R.id.targetDetailsPorts)).setText("Open Ports:" + openPorts);
    	}
    		
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attackhall, menu);
        return true;
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	getMenuInflater().inflate(R.menu.context_attackhall, menu);
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        
        if ( MainService.mTargetHostList.get(position).isUp()) {
        	menu.findItem(R.id.mnuTargetLogin).setVisible(true);
        	menu.findItem(R.id.mnuTargetFindAttacks).setVisible(true);
        }
        
        String[] tcpPorts = MainService.mTargetHostList.get(position).getTcpPorts().
        		keySet().toArray(new String[MainService.mTargetHostList.get(position).
        		                            getTcpPorts().size()]);
        
        for (int i=0; i<tcpPorts.length; i++)
        	if (tcpPorts[i].equals("21"))
        		menu.findItem(R.id.mnuTargetLogin21).setVisible(true);
        	else if (tcpPorts[i].equals("22"))
        		menu.findItem(R.id.mnuTargetLogin22).setVisible(true);
        	else if (tcpPorts[i].equals("23"))
        		menu.findItem(R.id.mnuTargetLogin23).setVisible(true);
        	else if (tcpPorts[i].equals("80"))
        		menu.findItem(R.id.mnuTargetLogin80).setVisible(true);
        	else if (tcpPorts[i].equals("445"))
        		menu.findItem(R.id.mnuTargetLogin445).setVisible(true);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null)
        	curListPosition = info.position;
        
        switch (item.getItemId()) {
        case R.id.mnuTargetRemove:
        	removeHostFromTargetList(info.position);
        	return true;
        //case R.id.mnuTargetScan:
        //	scanTarget(MainService.mTargetHostList.get(info.position));
        //	return true;
        case R.id.mnuTargetOS:
           	AlertDialog builder = new AlertDialog.Builder(this)
            .setSingleChoiceItems(TargetsListAdapter.osTitles, -1, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	        		MainService.mTargetHostList.get(info.position).setOS(TargetsListAdapter.osTitles[item]);
	        		mTargetsListAdapter.notifyDataSetChanged();	
                }
            })
            .create();
            builder.show(); 
        	return true;
        	
        case R.id.mnuTargetLogin21:
        case R.id.mnuTargetLogin22:
        case R.id.mnuTargetLogin23:
        case R.id.mnuTargetLogin80:
        case R.id.mnuTargetLogin445:
        default:
        	return false;
        }
    }
    
    private void scanTarget(final TargetItem t) {
    	
    	String ports =     					
    			"50000, 21, 1720, 80, 143, 3306, 110, 5432, 25, 22, 23, 443, 1521, 50013, 161, 17185, 135, " + 
				"8080, 4848, 1433, 5560, 512, 513, 514, 445, 5900, 5038, 111, 139, 49, 515, 7787, 2947, 7144, " + 
				"9080, 8812, 2525, 2207, 3050, 5405, 1723, 1099, 5555, 921, 10001, 123, 3690, 548, 617, 6112, " +
				"6667, 3632, 783, 10050, 38292, 12174, 2967, 5168, 3628, 7777, 6101, 10000, 6504, 41523, 41524, "+
				"2000, 1900, 10202, 6503, 6070, 6502, 6050, 2103, 41025, 44334, 2100, 5554, 12203, 26000, 4000, "+
				"1000, 8014, 5250, 34443, 8028, 8008, 7510, 9495, 1581, 8000, 18881, 57772, 9090, 9999, 81, 3000, "+
				"8300, 8800, 8090, 389, 10203, 5093, 1533, 13500, 705, 623, 4659, 20031, 16102, 6080, 6660, 11000, "+
				"19810, 3057, 6905, 1100, 10616, 10628, 5051, 1582, 65535, 105, 22222, 30000, 113, 1755, 407, 1434, "+
				"2049, 689, 3128, 20222, 20034, 7580, 7579, 38080, 12401, 910, 912, 11234, 46823, 5061, 5060, 2380, "+
				"69, 5800, 62514, 42, 5631, 902, 3389";
    	
    	final String cmd = 	"use auxiliary/scanner/portscan/tcp\n" + 
    						"set RHOSTS " + t.getHost() + "\n" + 
    						"set THREADS 15\n" + 
    						"set PORTS " + ports + "\n" +
    						"run";

    	new Thread(new Runnable() {
			@Override public void run() {
				newConsole.waitForReady();
				newConsole.write(cmd);		
			}
    	}).start();
    }
    
    private void removeHostFromTargetList(int pos) {	
		MainService.mTargetHostList.remove(pos);
    	if (MainService.mTargetHostList.size() == 0)
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
	    case R.id.mnuRemoveDeadHosts:
	    	for (int i=0; i<MainService.mTargetHostList.size(); i++)
	    		if (!MainService.mTargetHostList.get(i).isUp())
	    			MainService.mTargetHostList.remove(MainService.mTargetHostList.get(i)); 	
	    	if (MainService.mTargetHostList.size() == 0)
	    		finish();	
	    	mTargetsListAdapter.notifyDataSetChanged();
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
	    	
		    LayoutInflater inflater = (LayoutInflater)AttackHall2Activity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
		    final View layout = inflater.inflate(R.layout.activity_console, (ViewGroup)findViewById(R.id.popup_element));
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
		    params.setCmdViewId(R.id.consoleRead);
		    params.setPromptViewId(R.id.consolePrompt);
		    
		    final ConsoleSession newConsole = null;
		    //MainService.sessionMgr.getNewConsole(newConsole, params);
		    
		    MainService.sessionMgr.switchWindow("console", newConsole.getId(), this);
		    
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
