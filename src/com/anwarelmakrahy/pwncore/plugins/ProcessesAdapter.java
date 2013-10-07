package com.anwarelmakrahy.pwncore.plugins;

import java.util.LinkedList;
import java.util.List;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.plugins.ProcessesActivity.ProcessItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ProcessesAdapter extends BaseAdapter implements Filterable {

	private static List<ProcessItem> itemDetailsrrayList, originalFilter;
	private LayoutInflater l_Inflater;
	private Filter filter;

	public ProcessesAdapter(Context context, List<ProcessItem> results)  {
		itemDetailsrrayList = results;
		originalFilter = results;
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
			convertView = l_Inflater.inflate(R.layout.process_item, null);

			holder = new ViewHolder();
			holder.procId = (TextView) convertView.findViewById(R.id.procId);
			holder.procName = (TextView) convertView.findViewById(R.id.procName);
			holder.procPath = (TextView) convertView.findViewById(R.id.procPath);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.procId.setText("[" + itemDetailsrrayList.get(position).getId() + "]");
		holder.procName.setText(itemDetailsrrayList.get(position).getName());
		
		if (itemDetailsrrayList.get(position).getPath() == null)
			holder.procPath.setVisibility(View.GONE);
		else
			holder.procPath.setText(itemDetailsrrayList.get(position).getPath());

		return convertView;
	}

	static class ViewHolder {
		TextView procId;
		TextView procName;
		TextView procPath;
	}

	@Override
	public Filter getFilter() {

		if (filter == null) {
			filter = new Filter() {
				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results.count == 0) {
						itemDetailsrrayList = originalFilter;
						notifyDataSetChanged();
					} else {
						itemDetailsrrayList = (List<ProcessItem>) results.values;
						notifyDataSetChanged();
					}
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					constraint = constraint.toString().toLowerCase();
					FilterResults newFilterResults = new FilterResults();

					if (constraint == null || constraint.length() == 0) {
						newFilterResults.count = originalFilter.size();
						newFilterResults.values = originalFilter;
					} else {
						List<ProcessItem> filteredList = new LinkedList<ProcessItem>();
						for (int i = 0; i < originalFilter.size(); i++) {
							ProcessItem item = originalFilter.get(i);

							if ((item.getPath() != null && 
									item.getPath().toLowerCase().contains(constraint)) ||
									item.getName().toLowerCase().contains(constraint) ||
									item.getId().contains(constraint)) {
								filteredList.add(item);
							}
						}
						newFilterResults.count = filteredList.size();
						newFilterResults.values = filteredList;
					}

					return newFilterResults;
				}
			};
		}
		return filter;
	}
}