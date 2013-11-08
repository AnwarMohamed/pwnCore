package com.anwarelmakrahy.pwncore.structures;

import java.util.ArrayList;

import com.anwarelmakrahy.pwncore.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SidebarAdapter extends BaseAdapter {

	private static ArrayList<SidebarItem> itemDetailsrrayList;
	private LayoutInflater l_Inflater;

	public SidebarAdapter(Context context, ArrayList<SidebarItem> results) {
		itemDetailsrrayList = results;
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return itemDetailsrrayList.size();
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
			convertView = l_Inflater.inflate(R.layout.layout_item, null);

			holder = new ViewHolder();
			holder.txt_itemTitle = (TextView) convertView
					.findViewById(R.id.title);
			holder.itemImage = (ImageView) convertView.findViewById(R.id.photo);
			holder.txt_itemCounter = (TextView) convertView
					.findViewById(R.id.count);
			holder.txt_itemHeader = (TextView) convertView
					.findViewById(R.id.header);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (itemDetailsrrayList.get(position).isHeader()) {
			holder.txt_itemTitle.setVisibility(View.INVISIBLE);
			holder.txt_itemCounter.setVisibility(View.INVISIBLE);
			holder.itemImage.setVisibility(View.INVISIBLE);
			holder.txt_itemHeader.setVisibility(View.VISIBLE);

			holder.txt_itemTitle.setHeight(0);
			holder.txt_itemCounter.setHeight(0);
			holder.itemImage.setMaxHeight(0);
			holder.txt_itemHeader.setHeight(70);

			holder.txt_itemHeader.setText(itemDetailsrrayList.get(position)
					.getTitle());
		}

		else {

			holder.txt_itemTitle.setText(itemDetailsrrayList.get(position)
					.getTitle());
			holder.itemImage.setBackgroundResource(itemDetailsrrayList.get(
					position).getImage());
			holder.txt_itemHeader.setVisibility(View.INVISIBLE);

			if (itemDetailsrrayList.get(position).showCount())
				holder.txt_itemCounter
						.setText(Integer.toString(itemDetailsrrayList.get(
								position).getCount()));
			else {
				holder.txt_itemCounter.setVisibility(View.INVISIBLE);
			}
		}

		if (!itemDetailsrrayList.get(position).getClickable()) {
			// convertView.setClickable(false);
			// convertView.setFocusable(false);
			// convertView.setEnabled(false);
			// convertView.setEnabled(isEnabled(((Integer)
			// convertView.getTag()).intValue()));

		}
		return convertView;
	}

	static class ViewHolder {
		TextView txt_itemTitle;
		ImageView itemImage;
		TextView txt_itemCounter;
		TextView txt_itemHeader;
	}
}
