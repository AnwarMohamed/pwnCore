package com.anwarelmakrahy.pwncore.plugins;

import java.util.List;

import com.anwarelmakrahy.pwncore.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PluginsListAdapter extends BaseAdapter {
		
	private static List<pwnCorePlugin> pluginsList;
	private LayoutInflater layoutInflater;

	public PluginsListAdapter(Context context, List<pwnCorePlugin> results) {
		pluginsList = results;	
		layoutInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		try {
			return pluginsList.size();
		} catch (Exception e) {
			return 0;
		}
	}

	public Object getItem(int position) {
		return pluginsList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.plugin_item, null);
			
			holder = new ViewHolder();
			holder.pluginName = (TextView) convertView.findViewById(R.id.pluginName);
			holder.pluginPackage = (TextView) convertView.findViewById(R.id.pluginPackage);
			holder.pluginRoot = (TextView) convertView.findViewById(R.id.pluginRoot);
			holder.pluginDescription = (TextView) convertView.findViewById(R.id.pluginDescription);
			holder.pluginIcon = (ImageView) convertView.findViewById(R.id.pluginIcon);
			convertView.setTag(holder);
			
		} else {		
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		/*holder.pluginPackage.setText(pluginsList.get(position).getPackageName());
		holder.pluginName.setText(pluginsList.get(position).getTitle());
		holder.pluginIcon.setImageDrawable(pluginsList.get(position).getIconDrawable());
		
		if (pluginsList.get(position).getDescription().length() == 0)
			holder.pluginDescription.setVisibility(View.GONE);
		else
			holder.pluginDescription.setText(pluginsList.get(position).getDescription());*/
		
		return convertView;
	}

	private class ViewHolder {
		TextView pluginName;
		TextView pluginPackage;
		TextView pluginRoot;
		ImageView pluginIcon;
		TextView pluginDescription;
	}

}
