package com.anwarelmakrahy.pwncore.activities;

import java.util.ArrayList;
import java.util.List;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.R.id;
import com.anwarelmakrahy.pwncore.R.layout;
import com.anwarelmakrahy.pwncore.R.menu;
import com.anwarelmakrahy.pwncore.console.ConsoleActivity;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.fragments.ConsolesFragment;
import com.anwarelmakrahy.pwncore.fragments.ControlSessionsFragment;
import com.anwarelmakrahy.pwncore.fragments.TargetDetailsFragment;
import com.anwarelmakrahy.pwncore.fragments.TargetsFragment;
import com.anwarelmakrahy.pwncore.structures.TargetsListAdapter;
import com.viewpagerindicator.TabPageIndicator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
			new String[] { "TARGETS", "TARGET DETAILS", "CONSOLES", "SESSIONS", "JOBS" };
        
	public static ViewPager pager;
	
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
    	 
    	  fList.add(TargetsFragment.newInstance());
    	  fList.add(TargetDetailsFragment.newInstance());
    	  fList.add(ConsolesFragment.newInstance());
    	  fList.add(ControlSessionsFragment.newInstance());

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
        	
        	menu.findItem(R.id.mnuTargetScan).setVisible(true);
        	menu.findItem(R.id.mnuTargetRemove).setVisible(true);
        	menu.findItem(R.id.mnuTargetOS).setVisible(true);
        	
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
	        break;
	        
        case R.id.sessionsListView:
        	menu.findItem(R.id.mnuSessionKill).setVisible(true);
        	break;
        	
        case R.id.targetsConsolesListView:
        	menu.findItem(R.id.mnuConsoleKill).setVisible(true);
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
        case R.id.mnuTargetRemove:
        	removeHostFromTargetList(info.position);
        	return true;
        case R.id.mnuTargetScan:
        	//scanTarget(MainService.mTargetHostList.get(info.position));
        	return true;
        case R.id.mnuTargetOS:
           	AlertDialog builder = new AlertDialog.Builder(this)
            .setSingleChoiceItems(TargetsListAdapter.osTitles, -1, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	        		MainService.mTargetHostList.get(info.position).setOS(TargetsListAdapter.osTitles[item]);
	        		TargetsFragment.mTargetsListAdapter.notifyDataSetChanged();	
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
        case R.id.mnuSessionKill:
        	MainService.sessionMgr.destroySession(
        			MainService.sessionMgr.controlSessionsList.get(info.position).getId());
        	return true;

        case R.id.mnuConsoleKill:
        	ConsoleSession console = MainService.sessionMgr.getConsole(ConsolesFragment.consoleArray.get(info.position).
	        			split(" ")[0].replace("]", "").replace("[", ""));
        	MainService.sessionMgr.destroyConsole(console);
        	return true;
        	
        default:
        	return false;
        }
    }
    
    private void removeHostFromTargetList(int pos) {	
 		MainService.mTargetHostList.remove(pos);
     	if (MainService.mTargetHostList.size() == 0)
     		finish();	
     	TargetsFragment.mTargetsListAdapter.notifyDataSetChanged();
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
 	    	TargetsFragment.mTargetsListAdapter.notifyDataSetChanged();
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

}
