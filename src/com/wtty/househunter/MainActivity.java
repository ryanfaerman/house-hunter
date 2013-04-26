package com.wtty.househunter;

import java.io.IOException;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, PropertyListFragment.PropertyListener {
	
	Context _context;
	String _where_clause;
	String _order_clause;
	PropertyListFragment _propertyFragment;
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current tab position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_context = this;
		

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
				.setTabListener(this));
		
		AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		Account[] list = manager.getAccounts();
		
		Log.i("TRACE", list[0].name);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void appendWhereClause(String clause) {
		if(_where_clause == null) {
			_where_clause = "";
		}
		if(!_where_clause.isEmpty()) {
			_where_clause += " AND ";
		}
		
		_where_clause += clause;
	}
	
	public void appendOrderClause(String clause) {
		if(_order_clause == null) {
			_order_clause = "";
		}
		if(!_order_clause.isEmpty()) {
			_order_clause += ", ";
		}
		
		_order_clause += clause;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		
	    switch (item.getItemId()) {
	        case R.id.show_all:
	        	_where_clause = "";
	    		_order_clause = "";
	            break;
	        case R.id.add_property:
	        	onSelection(0);
	        	break;
	        case R.id.sort_date_desc:
	        	_order_clause = PropertyDB.KEY_ROWID+" DESC";
	            break;
	        case R.id.sort_date_asc:
	        	_order_clause = PropertyDB.KEY_ROWID+" ASC";
	            break;
	        case R.id.filter_has_hoa:
	            appendWhereClause(PropertyDB.KEY_HOA+"='y'");
	            break;
	        case R.id.filter_hasnt_hoa:
	        	appendWhereClause(PropertyDB.KEY_HOA+"=='n' OR "+PropertyDB.KEY_HOA+" IS NULL");
	            break;
	        case R.id.filter_has_pool:
	        	appendWhereClause(PropertyDB.KEY_POOL+"='y'");
	            break;
	        case R.id.filter_hasnt_pool:
	        	appendWhereClause(PropertyDB.KEY_POOL+"=='n' OR "+PropertyDB.KEY_POOL+" IS NULL");
	            break;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	    if(_propertyFragment != null) {
	    	_propertyFragment.refreshList(_where_clause, _order_clause);
	    }
		
	    return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		
		Bundle args = new Bundle();
		Fragment fragment;
		switch (tab.getPosition()) {
		case 0:
			_propertyFragment = new PropertyListFragment();
			fragment = 	_propertyFragment;
			PropertyListFragment.setmyText("HELLO WORLD");
			break;
		
		case 1:
			String latitude = "26.269724";
			String longitude = "-80.246065";
			String address = "3211 NW 89th Way, Coral Springs, FL 33065";
			String uri = "geo:"+ latitude + "," + longitude + "?q="+address;
			startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
			
		default	:
			ContentValues values = new ContentValues();
			values.put(PropertyDB.KEY_ADDRESS, "3606 NW 84th Ave, Coral Springs, FL 33065");
			values.put(PropertyDB.KEY_NOTES, "Looks very promising");
			values.put(PropertyDB.KEY_POOL, "y");
			values.put(PropertyDB.KEY_SQFT, "2134");
			
			getContentResolver().insert(PropertyProvider.CONTENT_URI, values);

			fragment = new DummySectionFragment();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER,
					tab.getPosition() + 1);
			fragment.setArguments(args);
			break;
		}
		
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Create a new TextView and set its text to the fragment's section
			// number argument value.
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			textView.setText("Coming Soon");
			return textView;
		}
	}

	@Override
	public void onSelection(long data) {
		Intent _propertyActivity = new Intent(_context, PropertyActivity.class);
		_propertyActivity.putExtra("_id", data);
		Log.i("TRACE", "popping property intent");
		
		startActivityForResult(_propertyActivity, 0);
	}

	@Override
	public void onLongSelection(long id) {
		Geocoder _geocoder = new Geocoder(_context);
		
		if (!Geocoder.isPresent()){
			Log.i("B0RKED", "no geocoder - returning");
			return;
		}
		
		String _where_clause = "_id='"+id+"'";
		String[] projection = new String[] {
				PropertyDB.KEY_ADDRESS,
				PropertyDB.KEY_LAT,
				PropertyDB.KEY_LONG,
	        };
		
		String latitude = "26.269724";
		String longitude = "-80.246065";
		String address = "3211 NW 89th Way, Coral Springs, FL 33065";
		
		Cursor cur = getContentResolver().query(PropertyProvider.CONTENT_URI, projection, _where_clause, null, null);
		
		if (cur.moveToFirst()) {
        	Log.i("TRACE", "moved to first");
        	
        	int address_column = cur.getColumnIndex(PropertyDB.KEY_ADDRESS);
        	int lat_column = cur.getColumnIndex(PropertyDB.KEY_LAT);
        	int long_column = cur.getColumnIndex(PropertyDB.KEY_LONG);
        	
            do {
                // Get the field values
            	address = cur.getString(address_column);
            	latitude = cur.getString(lat_column);
            	longitude = cur.getString(long_column);
 
            } while (cur.moveToNext());

        }
		
		
		
		if(latitude == null || longitude == null || latitude.isEmpty() || longitude.isEmpty()) {
			try {
				List<Address> result = _geocoder.getFromLocationName(address, 1);
				if ((result == null)||(result.isEmpty())){	
					Log.i("B0RKED", "nothing found - returning");
					return;
				} else {
					latitude = String.valueOf(result.get(0).getLatitude());
					longitude = String.valueOf(result.get(0).getLongitude());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("B0RKED", "exeception so returning");
				return;
			}
		}
		Log.i("w3rkz", "geo: "+ latitude + "," + longitude);
		String uri = "geo:"+ latitude + "," + longitude + "?q="+address;
		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
		
	}

}
