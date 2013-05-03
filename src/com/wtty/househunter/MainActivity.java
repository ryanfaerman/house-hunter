package com.wtty.househunter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, PropertyListFragment.PropertyListener {
	
	Context _context;
	Messenger _messenger;
	String _where_clause;
	String _order_clause = PropertyDB.KEY_ROWID+" DESC";
	HashMap<String, String> _where_clauses = new HashMap<String, String>();
	HashMap<String, String> _opposite_where_clauses = new HashMap<String, String>();
	HashMap<String, String> _order_clauses = new HashMap<String, String>();
	PropertyListFragment _propertyFragment = new PropertyListFragment();
	PropertyListFragment _flipFragment = new PropertyListFragment();
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
		
		Log.i("TRACE", "Number of Records: " + String.valueOf(propertyCount()));
		
		if(propertyCount() == 0) {
			dialog();
		}
		

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
				.setTabListener(this));
//		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
//				.setTabListener(this));
		
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
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		
	    switch (item.getItemId()) {
	        case R.id.show_all:
	    		_where_clauses.clear();
	    		_order_clauses.clear();
	            break;
	        case R.id.add_property:
	        	onSelection(0);
	        	break;
	        case R.id.sort_date:
	        	String dir = "asc";
	        	if(item.getTitle().equals("Newest First")) {
	        		item.setTitle("Oldest First");
	        		dir = "desc";
	        	} else {
	        		item.setTitle("Newest First");
	        		dir = "asc";
	        	}
	        	_order_clause = PropertyDB.KEY_ROWID+" "+dir;
	            break;
	        case R.id.filter_has_hoa:
	        	item.setChecked(!item.isChecked());
	        	if(item.isChecked()) {
	        		_where_clauses.put("hoa", PropertyDB.KEY_HOA+"='y'");
	        		_opposite_where_clauses.put("hoa", PropertyDB.KEY_HOA+"='n' OR "+PropertyDB.KEY_HOA+" IS NULL");
	        	} else {
	        		_opposite_where_clauses.put("hoa", PropertyDB.KEY_HOA+"='y'");
	        		_where_clauses.put("hoa", PropertyDB.KEY_HOA+"='n' OR "+PropertyDB.KEY_HOA+" IS NULL");
	        	}
	        	
	            break;
	        case R.id.filter_has_pool:
	        	item.setChecked(!item.isChecked());
	        	if(item.isChecked()) {
	        		_where_clauses.put("pool", PropertyDB.KEY_POOL+"='y'");
	        		_opposite_where_clauses.put("pool", PropertyDB.KEY_POOL+"='n' OR "+PropertyDB.KEY_POOL+" IS NULL");
	        	} else {
	        		_opposite_where_clauses.put("pool", PropertyDB.KEY_POOL+"='y'");
	        		_where_clauses.put("pool", PropertyDB.KEY_POOL+"='n' OR "+PropertyDB.KEY_POOL+" IS NULL");
	        	}
	        	
	            break;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }

	    if(_propertyFragment != null) {
	    	Log.i("TRACE", "propfrag has dem values: "+String.valueOf(_where_clauses.size()));
	    	_propertyFragment.refreshList(combine(_where_clauses.values().toArray(), " AND "), _order_clause);
	    }
	    if(_flipFragment != null) {
	    	Log.i("TRACE", "flipfrahg has dem values: "+String.valueOf(_where_clauses.size()));
//	    	_flipFragment.refreshList(combine(_opposite_where_clauses.values().toArray(), " AND "), _order_clause);
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
//			_propertyFragment = new PropertyListFragment();
			if(_propertyFragment == null) {
				Log.i("TRACE", "bananananana");
			}
			if(!_where_clauses.isEmpty()) {
				_propertyFragment.refreshList(combine(_where_clauses.values().toArray(), " AND "), _order_clause);
			}
			
			fragment = 	_propertyFragment;
			break;
		
//		case 1:
//			
//			if(!_opposite_where_clauses.isEmpty()) {
//				Log.i("TRACE", "prented something happened hur");
////				_flipFragment.refreshList(combine(_opposite_where_clauses.values().toArray(), " AND "), _order_clause);
//			}
//			
//			fragment = 	_flipFragment;
//			break;
//			
		default	:
		
//			try {
//				SearchRequest sr = new SearchRequest();
//				sr.execute(new URL("http://www.zillow.com/webservice/GetSearchResults.htm?zws-id=X1-ZWz1djyaxz82dn_86zet&address=3211+NW+89th+way&citystatezip=Coral%20Springs%2C+FL"));
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
	public void onLongSelection(final long id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Confirm Deletion");
		alert.setMessage("Are you sure you want to delete this property?");

		// Set an EditText view to get user input 
//		final EditText input = new EditText(this);
//		alert.setView(input);

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
//		  String value = input.getText().toString();
		  Log.i("TRACE", "PLEASE HELP ME");
		  // Do something with value!
		  	
		  	getContentResolver().delete(PropertyProvider.CONTENT_URI, "_id='"+ String.valueOf(id) +"'", null);
		  	if(_propertyFragment != null) {
		    	Log.i("TRACE", "propfrag has dem values: "+String.valueOf(_where_clauses.size()));
		    	_propertyFragment.refreshList(combine(_where_clauses.values().toArray(), " AND "), _order_clause);
		    }
		  }
		});

		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
		
	}
	
	public int propertyCount() {
		Cursor countCursor = getContentResolver().query(PropertyProvider.CONTENT_URI,
				new String[] {"count(*) as count"},
				null, null, null);
		countCursor.moveToFirst();
		return countCursor.getInt(0);
	}
	
	public void dialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Welcome to House Hunter");
		alert.setMessage("House Hunter is here to help you.\n\nWould you like to add a home?");

		// Set an EditText view to get user input 
//		final EditText input = new EditText(this);
//		alert.setView(input);

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
//		  String value = input.getText().toString();
		  Log.i("TRACE", "PLEASE HELP ME");
		  // Do something with value!
		  	onSelection(0);
		  }
		});

		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}
	
	public String combine(Object[] s, String glue)
	{	
		  int k=s.length;
		  if (k==0)
		    return null;
		  StringBuilder out=new StringBuilder();
		  out.append(s[0].toString());
		  for (int x=1;x<k;++x)
		    out.append(glue).append(s[x].toString());
		  return out.toString();
		}
	
	
	
}
