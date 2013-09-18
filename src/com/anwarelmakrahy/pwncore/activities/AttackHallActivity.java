package com.anwarelmakrahy.pwncore.activities;

import java.util.ArrayList;
import java.util.List;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticClass;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.console.utils.AttackFinder;
import com.anwarelmakrahy.pwncore.fragments.ConsolesFragment;
import com.anwarelmakrahy.pwncore.fragments.ControlSessionsFragment;
import com.anwarelmakrahy.pwncore.fragments.JobsFragment;
import com.anwarelmakrahy.pwncore.fragments.HostDetailsFragment;
import com.anwarelmakrahy.pwncore.fragments.HostsFragment;
import com.anwarelmakrahy.pwncore.structures.HostsAdapter;
import com.viewpagerindicator.TabPageIndicator;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class AttackHallActivity extends FragmentActivity {
 
	private static final String[] CONTENT = 
			new String[] { "HOSTS", "HOST DETAILS", "CONSOLES", "SESSIONS", "JOBS" };
        
	public static ViewPager pager;
	
	private int currentLongPosition = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_hall); 	

    	List<Fragment> fragments = getFragments();
        FragmentPagerAdapter adapter = new AttackHallAdapter(getSupportFragmentManager(), fragments);
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);
        
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }
    
    private List<Fragment> getFragments(){
    	  List<Fragment> fList = new ArrayList<Fragment>();
    	 
    	  fList.add(HostsFragment.newInstance());
    	  fList.add(HostDetailsFragment.newInstance());
    	  fList.add(ConsolesFragment.newInstance());
    	  fList.add(ControlSessionsFragment.newInstance());
    	  fList.add(JobsFragment.newInstance());

    	  return fList;
    }
    
    class AttackHallAdapter extends FragmentPagerAdapter {
    	private List<Fragment> fragments; 	
        public AttackHallAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        
        @Override public Fragment getItem(int position) { return fragments.get(position); }
        @Override public CharSequence getPageTitle(int position) { return CONTENT[position]; }
        @Override public int getCount() { return fragments.size(); }
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	getMenuInflater().inflate(R.menu.context_attackhall, menu);
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        
        switch (v.getId()) {
        case R.id.targetsFragmentListView:
        	
        	menu.findItem(R.id.mnuHostScanPorts).setVisible(true);
        	menu.findItem(R.id.mnuHostRemove).setVisible(true);
        	menu.findItem(R.id.mnuHostOS).setVisible(true);
        	
        	menu.findItem(R.id.mnuHostFindAttacks).setVisible(true);
        	
	        if ( MainService.hostsList.get(position).isUp()) {
	        	menu.findItem(R.id.mnuHostScanServices).setVisible(true);
	        	//menu.findItem(R.id.mnuHostLogin).setVisible(true);
	        	menu.findItem(R.id.mnuHostFindAttacks).setVisible(true);
	        
	        	if (MainService.hostsList.get(position).getActiveSessions().get("shell").size() > 0) {
	        		menu.findItem(R.id.mnuHostShell).setVisible(true);
	        	}
	        	
	        	if (MainService.hostsList.get(position).getActiveSessions().get("meterpreter").size() > 0) {
	        		menu.findItem(R.id.mnuHostMeterpreter).setVisible(true);
	        	}
	        	
	        /*String[] tcpPorts = MainService.mTargetHostList.get(position).getTcpPorts().
	        		keySet().toArray(new String[MainService.mTargetHostList.get(position).
	        		                            getTcpPorts().size()]);
	        
	        for (int i=0; i<tcpPorts.length; i++)
	        	if (tcpPorts[i].equals("21"))
	        		menu.findItem(R.id.mnuHostLogin21).setVisible(true);
	        	else if (tcpPorts[i].equals("22"))
	        		menu.findItem(R.id.mnuHostLogin22).setVisible(true);
	        	else if (tcpPorts[i].equals("23"))
	        		menu.findItem(R.id.mnuHostLogin23).setVisible(true);
	        	else if (tcpPorts[i].equals("80"))
	        		menu.findItem(R.id.mnuHostLogin80).setVisible(true);
	        	else if (tcpPorts[i].equals("445"))
	        		menu.findItem(R.id.mnuHostLogin445).setVisible(true);*/
	        
	        }
	        
	        break;
	        
        case R.id.sessionsListView:
        	menu.findItem(R.id.mnuSessionKill).setVisible(true);
        	break;
        	
        case R.id.targetsConsolesListView:
        	menu.findItem(R.id.mnuConsoleKill).setVisible(true);
        	break;
        	
        case R.id.jobsListView:
        	menu.findItem(R.id.mnuJobKill).setVisible(true);
        	menu.findItem(R.id.mnuJobDetails).setVisible(true);
        	break;        	
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attackhall, menu);
        return true;
    }
    
    private static int curListPosition = 0;
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null)
        	setCurListPosition(info.position);
        
        switch (item.getItemId()) {
        case R.id.mnuHostRemove:
        	removeHostFromTargetList(info.position);
        	return true;
        case R.id.mnuHostScanPorts:
        	MainService.hostsList.get(info.position).scanPorts();
        	return true;
        case R.id.mnuHostScanServices:
        	MainService.hostsList.get(info.position).scanServices();
        	return true;
        case R.id.mnuHostOS:
           	AlertDialog builder = new AlertDialog.Builder(this)
            .setSingleChoiceItems(HostsAdapter.osTitles, -1, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	        		MainService.hostsList.get(info.position).setOS(HostsAdapter.osTitles[item]);
	        		HostsFragment.listAdapter.notifyDataSetChanged();	
                }
            })
            .create();
            builder.show(); 
        	return true;
        	
        case R.id.mnuHostFindAttacks:
        	currentLongPosition = info.position;
        	return true;
        case R.id.mnuFindAttacksOS:
        	MainService.hostsList.get(currentLongPosition).findAttacks(AttackFinder.FINDATTACKS_BY_OS);
        	return true;
        case R.id.mnuFindAttacksPorts:
        	MainService.hostsList.get(currentLongPosition).findAttacks(AttackFinder.FINDATTACKS_BY_PORTS);
        	return true;
        case R.id.mnuFindAttacksServices:
        	MainService.hostsList.get(currentLongPosition).findAttacks(AttackFinder.FINDATTACKS_BY_SERVICES);
        	return true;
        	
        case R.id.mnuHostShell:
        	currentLongPosition = info.position;
        	return true;
        case R.id.mnuHostMeterpreter:
        	currentLongPosition = info.position;
        	return true;
        	
        case R.id.mnuHostLogin21:
        	return true;
        case R.id.mnuHostLogin22:
        	return true;
        case R.id.mnuHostLogin23:
        	return true;
        case R.id.mnuHostLogin80:
        	return true;
        case R.id.mnuHostLogin445:
        	return true;
        	
        	
        case R.id.mnuSessionKill:
        	MainService.sessionMgr.destroySession(
        			MainService.sessionMgr.controlSessionsList.get(info.position).getId());
        	return true;

        case R.id.mnuConsoleKill:
        	ConsoleSession console = MainService.sessionMgr.getConsole(ConsolesFragment.consoleArray.get(info.position).
	        			split(" ")[0].replace("]", "").replace("[", ""));
        	MainService.sessionMgr.destroyConsole(console);
        	return true;
        
        case R.id.mnuJobKill:
        	MainService.sessionMgr.stopJob(
        			MainService.sessionMgr.jobsList.get(info.position).
	        			split(" ")[0].replace("]", "").replace("[", ""));
        	return true;
        	
        default:
        	return false;
        }
    }
    
    private void removeHostFromTargetList(int pos) {	
 		MainService.hostsList.remove(pos);
     	if (MainService.hostsList.size() == 0)
     		finish();	
     	HostsFragment.listAdapter.notifyDataSetChanged();
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {

 	    switch (item.getItemId()) {    
 	    case R.id.mnuAddHosts:
 	    	startActivity(new Intent(getApplicationContext(), AttackWizardActivity.class));
 	    	finish();    	
 	        return true;
 	        
 	    case R.id.mnuRemoveDeadHosts:
 	    	for (int i=0; i<MainService.hostsList.size(); i++)
 	    		if (!MainService.hostsList.get(i).isUp())
 	    			MainService.hostsList.remove(MainService.hostsList.get(i)); 	
 	    	if (MainService.hostsList.size() == 0)
 	    		finish();	
 	    	HostsFragment.listAdapter.notifyDataSetChanged();
 	    	return true;
 	    	
 	    case R.id.mnuNewConsole:
	    	Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.putExtra("type", "new.console");
	    	startActivity(intent);  
	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}

	public static int getCurListPosition() {
		return curListPosition;
	}

	public static void setCurListPosition(int curListPosition) {
		AttackHallActivity.curListPosition = curListPosition;
	}

	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();	
			filter.addAction(StaticClass.PWNCORE_NOTIFY_ADAPTER_UPDATE);	
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}		
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		if (conStatusReceiverRegistered) {
			unregisterReceiver(conStatusReceiver);
			conStatusReceiverRegistered = false;
		}

		super.onPause();
	}
	
	private boolean conStatusReceiverRegistered = false;
	public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticClass.PWNCORE_NOTIFY_ADAPTER_UPDATE) {
				if (ConsolesFragment.listAdapter != null) {
					ConsolesFragment.consoleArray.clear();
					ConsolesFragment.consoleArray.addAll(MainService.sessionMgr.getConsoleListArray()); 
					ConsolesFragment.listAdapter.notifyDataSetChanged();
				}
				
				if (HostsFragment.listAdapter != null) {
					HostsFragment.listAdapter.notifyDataSetChanged();
				}
    		}   
    	}
    };

}
