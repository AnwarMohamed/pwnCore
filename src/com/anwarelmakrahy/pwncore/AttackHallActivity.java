package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;

import com.viewpagerindicator.TabPageIndicator;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

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
    	getMenuInflater().inflate(R.menu.target_attackhall, menu);
    	
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attackhall, menu);
        return true;
    }
    
    private int curListPosition = 0;
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null)
        	curListPosition = info.position;
        
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
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
}
