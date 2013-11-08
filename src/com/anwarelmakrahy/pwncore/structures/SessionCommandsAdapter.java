package com.anwarelmakrahy.pwncore.structures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class SessionCommandsAdapter extends BaseAdapter implements Filterable {

	private static Map<String, SessionCommand> itemDetailsrrayList, originalFilter;
	private LayoutInflater l_Inflater;
	private Filter filter;

	//private Map<String, SessionCommand> filtered = new HashMap<String, SessionCommand>();
	
	public SessionCommandsAdapter(Context context, Map<String, SessionCommand> results) {
		//filterMap(results);
		itemDetailsrrayList = results;//filtered;
		originalFilter = results;//filtered;
		l_Inflater = LayoutInflater.from(context);
	}

	/*private void filterMap(Map<String, SessionCommand> all) {
		filtered.clear();
		for (Map.Entry<String, SessionCommand> entry : all.entrySet()) {
			if (entry.getValue().isImplemented())
				filtered.put(entry.getKey(), entry.getValue());
		}
	}*/
	
	public int getCount() {
		try {
			return itemDetailsrrayList.size();
		} catch (Exception e) {
			return 0;
		}
	}

	public Object getItem(int position) {
		return itemDetailsrrayList.values().toArray()[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.sessioncommand_item, null);

			holder = new ViewHolder();
			holder.txt_itemTitle = (TextView) convertView.findViewById(R.id.commandTitle);
			holder.txt_itemDisc = (TextView) convertView.findViewById(R.id.commandDisc);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txt_itemTitle.setText(((SessionCommand)(itemDetailsrrayList.values().toArray()[position])).getTitle());
		holder.txt_itemDisc.setText(((SessionCommand)(itemDetailsrrayList.values().toArray()[position])).getDescription());

		return convertView;
	}

	static class ViewHolder {
		TextView txt_itemTitle;
		TextView txt_itemDisc;

	}

	@Override
	public Filter getFilter() {

		if (filter == null) {
			filter = new Filter() {
				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {

					if (results.count == 0) {
						itemDetailsrrayList = originalFilter;
						notifyDataSetChanged();
						// notifyDataSetInvalidated();
					} else {
						itemDetailsrrayList = (Map<String, SessionCommand>) results.values;
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
						Map<String, SessionCommand> filteredList = new HashMap<String, SessionCommand>();
						for (int i = 0; i < originalFilter.size(); i++) {
							SessionCommand item = (SessionCommand)(originalFilter.values().toArray()[i]);

							if (item.getTitle().toLowerCase().contains(constraint) ||
									item.getDescription().toLowerCase().contains(constraint)) {
								filteredList.put(item.getCodename(), item);
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
