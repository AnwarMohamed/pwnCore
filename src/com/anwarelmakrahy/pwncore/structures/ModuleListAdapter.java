package com.anwarelmakrahy.pwncore.structures;

import java.util.List;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.R.id;
import com.anwarelmakrahy.pwncore.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ModuleListAdapter extends BaseAdapter {
	
	private static List<ModuleItem> itemDetailsrrayList;
	private LayoutInflater l_Inflater;

	public ModuleListAdapter(Context context, List<ModuleItem> results) {
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
			convertView = l_Inflater.inflate(R.layout.mainlist_item, null);
			
			holder = new ViewHolder();
			holder.txt_itemTitle = (TextView) convertView.findViewById(R.id.moduleItemTitle);
			holder.txt_itemDir = (TextView) convertView.findViewById(R.id.moduleItemDir);

			convertView.setTag(holder);
			
		} else {		
			holder = (ViewHolder) convertView.getTag();
		}
		
		String[] path = itemDetailsrrayList.get(position).getPath().split("/");
		Integer path_size = path.length;
			
		holder.txt_itemTitle.setText(path[path_size-1]);
		holder.txt_itemDir.setText(itemDetailsrrayList.get(position).getPath());
		
		return convertView;
	}

	static class ViewHolder {
		TextView txt_itemTitle;
		TextView txt_itemDir;

	}
}
