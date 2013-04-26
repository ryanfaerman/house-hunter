package com.wtty.househunter;

import org.apache.http.conn.ManagedClientConnection;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class PropertyListFragment extends Fragment {
	
	private PropertyListener listener;
	SimpleCursorAdapter _cursorAdapter;
	ListView _propertyList;
	String _where_clause;
	String _order_clause;
	
	public interface PropertyListener {
		public void onSelection(long id);
		public void onLongSelection(long id);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		Log.i("TRACE", "PropertyListFragment initializing...");
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.property_list_fragment, container, false);
		
		Cursor _cursor = getProperties();
		getActivity().startManagingCursor(_cursor);
		
		_cursorAdapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_2, 
				_cursor,
				new String[] {PropertyDB.KEY_ADDRESS}, 
				new int[] {android.R.id.text1});
		
		_propertyList = (ListView) view.findViewById(R.id.property_list);
		_propertyList.setAdapter(_cursorAdapter);
		
		_propertyList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int pos,
					long id) {
				Log.i("ITEM SELECTED", String.valueOf(id));
				listener.onSelection(id);
				
			}
		});
		
		_propertyList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View view,
					int pos, long id) {
				listener.onLongSelection(id);
				
				return false;
			}
		});
		
		
		return view;
	}
	
	public static void setmyText(final String string) {
		Log.i("TRACE", string);
	}
	
	public void refreshList(final String where, final String order) {
		Log.i("TRACE", "refreshing list");
		_where_clause = where;
		_order_clause = order;
		
		Cursor _cursor = getProperties();
		Log.i("TRACE", "got properties - swapping cursors");
//		getActivity().startManagingCursor(_cursor);
		_cursorAdapter.swapCursor(_cursor);
//		_propertyList.setAdapter(_cursorAdapter);
		Log.i("TRACE", "done swapping and refreshing");
	}
	
	private Cursor getProperties() {
		Log.i("TRACE", "prepping query");
		String[] projection = new String[] {
			PropertyDB.KEY_ROWID,
			PropertyDB.KEY_ADDRESS
		};
		
		@SuppressWarnings("deprecation")
		Cursor cur = getActivity().managedQuery(PropertyProvider.CONTENT_URI, projection, _where_clause, null, _order_clause);
		Log.i("TRACE", "did query");
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
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("TRACE", "destroying fragment");
		super.onDestroy();
	}
}
