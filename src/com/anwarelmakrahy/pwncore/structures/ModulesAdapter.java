package com.anwarelmakrahy.pwncore.structures;

import java.util.LinkedList;
import java.util.List;

import com.anwarelmakrahy.pwncore.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ModulesAdapter extends BaseAdapter implements Filterable {
	
	private static List<ModuleItem> itemDetailsrrayList, originalFilter;
	private LayoutInflater l_Inflater;
	private Filter filter;

	public ModulesAdapter(Context context, List<ModuleItem> results) {
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

	@Override
	public Filter getFilter() {

		if (filter == null) {
	        filter =  new Filter() {
		        @SuppressWarnings("unchecked")
				@Override
		        protected void publishResults(CharSequence constraint, FilterResults results) {

		            if (results.count == 0) {
		            	itemDetailsrrayList  = originalFilter;
		            	notifyDataSetChanged();
		                //notifyDataSetInvalidated();
		            }
		            else {
		            	itemDetailsrrayList = (List<ModuleItem>) results.values;
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
	                }
	                else {
	                	List<ModuleItem> filteredList = new LinkedList<ModuleItem>();
		                for(int i=0; i<originalFilter.size(); i++) {
		                    ModuleItem item = originalFilter.get(i);
		
		                    if(item.getPath().toLowerCase().contains(constraint)) {
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
