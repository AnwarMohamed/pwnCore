package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TargetHostListAdapter extends BaseAdapter {

	private static ArrayList<TargetHostItem> itemDetailsrrayList;
	private LayoutInflater l_Inflater;

	public TargetHostListAdapter(Context context, ArrayList<TargetHostItem> results) {
		itemDetailsrrayList = results;
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return itemDetailsrrayList.size();
	}

	public Object getItem(int position) {
		return itemDetailsrrayList.get(position);
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.targethost_item, null);
			
			holder = new ViewHolder();
			holder.txt_itemHost = (TextView) convertView.findViewById(R.id.targetHost);
			holder.txt_itemOS = (TextView) convertView.findViewById(R.id.targetOS);
			holder.txt_itemImage = (ImageView) convertView.findViewById(R.id.targetImage);
			holder.txt_itemImageBack = (ImageView) convertView.findViewById(R.id.targetImageBack);

			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
			holder.txt_itemHost.setText(itemDetailsrrayList.get(position).getHost());	
			holder.txt_itemOS.setText("Target OS: " + itemDetailsrrayList.get(position).getOS());
			holder.txt_itemImageBack.setImageResource(R.drawable.computer);
			holder.txt_itemImage.setImageResource(getImageResFromOS(itemDetailsrrayList.get(position).getOS()));

		return convertView;
	}

	static class ViewHolder {
		TextView txt_itemHost;
		TextView txt_itemOS;
		ImageView txt_itemImage;
		ImageView txt_itemImageBack;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private int getImageResFromOS(String os) {
		for (int i=0; i<osTitles.length; i++)
			if (osTitles[i].equals(os))
				return osImages[i];
		return osImages[0];
	}
	
	public static String[] osTitles = { "Unknown", "Windows", "Linux", "Android", "Cisco IOS", "FreeBSD", "NetBSD", "Mac OS X", "OpenBSD", "Printer", "Solaris"};
	private static int[] osImages = { R.drawable.unknown, R.drawable.windows7, R.drawable.linux, R.drawable.android, R.drawable.cisco, R.drawable.bsd, R.drawable.bsd, 
								R.drawable.macosx, R.drawable.bsd, R.drawable.printer, R.drawable.solaris};

}