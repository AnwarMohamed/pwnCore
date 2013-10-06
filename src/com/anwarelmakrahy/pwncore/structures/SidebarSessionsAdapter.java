package com.anwarelmakrahy.pwncore.structures;

import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.console.ControlSession;

public class SidebarSessionsAdapter extends SessionsListAdapter {

	public SidebarSessionsAdapter(Context context, List<ControlSession> results) {
		super(context, results);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.sidebarsession_item,
					null);

			holder = new ViewHolder();
			holder.sessionType = (TextView) convertView
					.findViewById(R.id.sessionType);
			holder.sessionPeer = (TextView) convertView
					.findViewById(R.id.sessionPeer);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.sessionPeer.setText(itemDetailsrrayList.get(position).getPeer());
		holder.sessionType.setText("[" + itemDetailsrrayList.get(position).getId() +"] " + 
				WordUtils.capitalize(itemDetailsrrayList.get(position).getType()));

		return convertView;
	}
	
	private class ViewHolder {
		TextView sessionType;
		TextView sessionPeer;
	}
}
