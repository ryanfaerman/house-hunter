package com.wtty.househunter;

import org.apache.http.conn.ManagedClientConnection;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PropertyListFragment extends Fragment {
	
	private PropertyListener listener;
	
	public interface PropertyListener {
		public void onSelection(long id);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		Log.i("TRACE", "PropertyListFragment initializing...");
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.property_list_fragment, container, false);
		
		Cursor _cursor = getProperties();
		getActivity().startManagingCursor(_cursor);
		
		ListAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_2, 
				_cursor,
				new String[] {PropertyDB.KEY_ADDRESS}, 
				new int[] {android.R.id.text1});
		
		ListView propertyList = (ListView) view.findViewById(R.id.property_list);
		propertyList.setAdapter(adapter);
		
		propertyList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long id) {
				Log.i("ITEM SELECTED", String.valueOf(id));
				listener.onSelection(id);
				
			}
});
		return view;
	}
	
	private Cursor getProperties() {
		Log.i("TRACE", "prepping query");
		String[] projection = new String[] {
			PropertyDB.KEY_ROWID,
			PropertyDB.KEY_ADDRESS
		};
		
		@SuppressWarnings("deprecation")
		Cursor cur = getActivity().managedQuery(PropertyProvider.CONTENT_URI, projection, null, null, null);
		
		return cur;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i("TRACE", "attaching PropertyListFragment");
		try {
			listener = (PropertyListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement PropertyListener");
		}
	}
}
