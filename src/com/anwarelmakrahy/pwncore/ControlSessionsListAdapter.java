package com.anwarelmakrahy.pwncore;

import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ControlSessionsListAdapter extends BaseAdapter {
	
	private static List<ControlSession> itemDetailsrrayList;
	private LayoutInflater l_Inflater;

	public ControlSessionsListAdapter(Context context, List<ControlSession> results) {
		itemDetailsrrayList = results;	
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		try {
			return itemDetailsrrayList.size();
		} catch (Exception e) {
			return 0;
		}
	}

	public Object getItem(int position) {
		return itemDetailsrrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.fragment_sessionlistitem, null);
			
			holder = new ViewHolder();
			holder.sessionType = (TextView) convertView.findViewById(R.id.sessionType);
			holder.sessionHandler = (TextView) convertView.findViewById(R.id.sessionHandler);
			holder.sessionPayload = (TextView) convertView.findViewById(R.id.sessionPayload);
			convertView.setTag(holder);
			
		} else {		
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.sessionPayload.setText(itemDetailsrrayList.get(position).getViaPayload());
		holder.sessionHandler.setText("via " + itemDetailsrrayList.get(position).getViaExploit());
		holder.sessionType.setText(
				WordUtils.capitalize(itemDetailsrrayList.get(position).getType()) + 
				" @ " + 
				itemDetailsrrayList.get(position).getPeer());
		
		return convertView;
	}

	static class ViewHolder {
		TextView sessionType;
		TextView sessionPayload;
		TextView sessionHandler;

	}

}
