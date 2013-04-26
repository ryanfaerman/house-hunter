package com.wtty.househunter;

import java.io.IOException;
import java.util.List;

import android.R.bool;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class PropertyActivity extends Activity {
	Context _context;
	String _address;
	String _notes;
	String _latitude;
	String _longitude;
	String _sqft;
	String _bedrooms;
	String _bathrooms;
	String _pool;
	String _hoa;
	String _state;
	String _where_clause;
	
	EditText _property_address;
	EditText _property_notes;
	EditText _property_lat;
	EditText _property_long;
	EditText _property_sqft;
	EditText _property_bedrooms;
	EditText _property_bathrooms;
	CheckBox _property_pool;
	CheckBox _property_hoa;
	EditText _property_state;
	
	Boolean _new_property = false;
	long _id = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_property);
		_context = this;
		Log.i("TRACE", "hola");
		_id = getIntent().getLongExtra("_id", 0);
		
		
		
		Log.i("TRACE", String.valueOf(_id));
		setTitle("New Property");
		
		_property_address = (EditText) findViewById(R.id.property_address);
		_property_notes = (EditText) findViewById(R.id.property_notes);
//		_property_lat = (EditText) findViewById(R.id.property_latitude);
//		_property_long = (EditText) findViewById(R.id.property_longitude);
		_property_sqft = (EditText) findViewById(R.id.property_sqft);
		_property_bedrooms = (EditText) findViewById(R.id.property_bedrooms);
		_property_bathrooms = (EditText) findViewById(R.id.property_bathrooms);
		_property_pool = (CheckBox) findViewById(R.id.property_pool);
		_property_hoa = (CheckBox) findViewById(R.id.property_hoa);
//		_property_state = (EditText) findViewById(R.id.property_state);
		
		_new_property = (_id == 0);
		
		if(!_new_property) {
			load_data();
			findViewById(R.id.map_button).setVisibility(View.VISIBLE);
		}
		
//		findViewById(R.id.cancel_button).setOnClickListener(cancelClickListener);
		findViewById(R.id.save_button).setOnClickListener(saveClickListener);
		findViewById(R.id.map_button).setOnClickListener(mapClickListener);
		
	};
	
	View.OnClickListener cancelClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Log.i("TRACE", "canceling quote");
			populate_fields();
		}
	};
	
	View.OnClickListener mapClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Log.i("TRACE", "mapping property");
			
			Geocoder _geocoder = new Geocoder(_context);
			
			if (!Geocoder.isPresent()){
				Log.i("B0RKED", "no geocoder - returning");
				return;
			}
			
			
			
			
			if(_latitude == null || _longitude == null || _latitude.isEmpty() || _longitude.isEmpty()) {
				try {
					List<Address> result = _geocoder.getFromLocationName(_address, 1);
					if ((result == null)||(result.isEmpty())){	
						Log.i("B0RKED", "nothing found - returning");
						return;
					} else {
						_latitude = String.valueOf(result.get(0).getLatitude());
						_longitude = String.valueOf(result.get(0).getLongitude());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("B0RKED", "exeception so returning");
					return;
				}
			}
			Log.i("w3rkz", "geo: "+ _latitude + "," + _longitude);
			String uri = "geo:"+ _latitude + "," + _longitude + "?q="+_address;
			startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
		}
	};
	
	View.OnClickListener saveClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			_where_clause = "_id='"+_id+"'";
			Log.i("TRACE", "saving quote");
			ContentValues values = new ContentValues();
			
			values.put(PropertyDB.KEY_ADDRESS, _property_address.getText().toString());
			values.put(PropertyDB.KEY_NOTES, _property_notes.getText().toString());
//			values.put(PropertyDB.KEY_LAT, _property_lat.getText().toString());
//			values.put(PropertyDB.KEY_LONG, _property_long.getText().toString());
			values.put(PropertyDB.KEY_SQFT, _property_sqft.getText().toString());
			values.put(PropertyDB.KEY_BEDROOMS, _property_bedrooms.getText().toString());
			values.put(PropertyDB.KEY_BATHROOMS, _property_bathrooms.getText().toString());
			
			
			String has_pool = "n";
			if(_property_pool.isChecked()) {
				has_pool = "y";
			}
			
			String has_hoa = "n";
			if(_property_hoa.isChecked()) {
				has_hoa = "y";
			}
			
			values.put(PropertyDB.KEY_POOL, has_pool);
			values.put(PropertyDB.KEY_HOA, has_hoa);
//			values.put(PropertyDB.KEY_STATE, _property_state.getText().toString());
			
			if(_new_property) {
				Uri new_record = getContentResolver().insert(PropertyProvider.CONTENT_URI, values);
				_id = Long.valueOf(new_record.getPathSegments().get(1));
				_new_property = false;
				_where_clause = "_id='"+_id+"'";
				findViewById(R.id.map_button).setVisibility(View.VISIBLE);
			} else {
				getContentResolver().update(PropertyProvider.CONTENT_URI, values, _where_clause, null);
			}
			
			
			load_data();
			
			Toast.makeText(_context, "Property Saved!", Toast.LENGTH_SHORT).show();
		}
	};
	
	
	void load_data() {
		String[] projection = new String[] {
				PropertyDB.KEY_ADDRESS,
				PropertyDB.KEY_NOTES,
				PropertyDB.KEY_LAT,
				PropertyDB.KEY_LONG,
				PropertyDB.KEY_SQFT,
				PropertyDB.KEY_BEDROOMS,
				PropertyDB.KEY_BATHROOMS,
				PropertyDB.KEY_POOL,
				PropertyDB.KEY_HOA,
				PropertyDB.KEY_STATE
	        };
			
			Log.i("TRACE", "built projection");
			Cursor cur = getContentResolver().query(PropertyProvider.CONTENT_URI, projection, _where_clause, null, null);
			if (cur.moveToFirst()) {
	        	Log.i("TRACE", "moved to first");
	        	
	        	int address_column = cur.getColumnIndex(PropertyDB.KEY_ADDRESS);
	        	int notes_column = cur.getColumnIndex(PropertyDB.KEY_NOTES);
	        	int lat_column = cur.getColumnIndex(PropertyDB.KEY_LAT);
	        	int long_column = cur.getColumnIndex(PropertyDB.KEY_LONG);
	        	int sqft_column = cur.getColumnIndex(PropertyDB.KEY_SQFT);
	        	int bedrooms_column = cur.getColumnIndex(PropertyDB.KEY_BEDROOMS);
	        	int bathrooms_column = cur.getColumnIndex(PropertyDB.KEY_BATHROOMS);
	        	int pool_column = cur.getColumnIndex(PropertyDB.KEY_POOL);
	        	int hoa_column = cur.getColumnIndex(PropertyDB.KEY_HOA);
	        	int state_column = cur.getColumnIndex(PropertyDB.KEY_STATE);
	        	
	            do {
	                // Get the field values
	            	Log.i("TRACE", String.valueOf(sqft_column));
	            	_address = cur.getString(address_column);
	            	_notes = cur.getString(notes_column);
	            	_latitude = cur.getString(lat_column);
	            	_longitude = cur.getString(long_column);
	            	_sqft = cur.getString(sqft_column);
	            	_bedrooms = cur.getString(bedrooms_column);
	            	_bathrooms = cur.getString(bathrooms_column);
	            	_pool = cur.getString(pool_column);
	            	_hoa = cur.getString(hoa_column);
	            	_state = cur.getString(state_column);
	   
	            } while (cur.moveToNext());

	        }
			setTitle(_address);
			populate_fields();
	}
	
	void populate_fields() {

		_property_address.setText(_address);
		_property_notes.setText(_notes);
//		_property_lat.setText(_lat);
//		_property_long.setText(_long);
		_property_sqft.setText(_sqft);
		_property_bedrooms.setText(_bedrooms);
		_property_bathrooms.setText(_bathrooms);
		
		boolean has_pool = !(_pool == null || _pool.contentEquals("n"));
		boolean has_hoa = !(_hoa == null || _hoa.contentEquals("n"));
		_property_pool.setChecked(has_pool);
		_property_hoa.setChecked(has_hoa);
//		_property_state.setText(_state);
		
	}
	
	@Override
	public void onDestroy() {
		Log.i("TRACE", "destroying property activity");
		super.onDestroy();
	}
}
