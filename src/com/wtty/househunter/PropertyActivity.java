package com.wtty.househunter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class PropertyActivity extends Activity {
	Context _context;
	String _address;
	String _notes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_property);
		_context = this;
		Log.i("PROPERTYACTIVITY", "hola");
		long id = getIntent().getLongExtra("_id", 0);
		
		Log.i("PROPERTYACTIVITY", String.valueOf(id));
		setTitle("Hello there tiny timmy!");
		
		String[] projection = new String[] {
			PropertyDB.KEY_ADDRESS,
			PropertyDB.KEY_NOTES
        };
		
		Log.i("TRACE", "built projection");
		Cursor cur = getContentResolver().query(PropertyProvider.CONTENT_URI, projection, null, null, "_id='"+id+"'");
		if (cur.moveToFirst()) {
        	Log.i("TRACE", "moved to first");
            int address_column = cur.getColumnIndex(PropertyDB.KEY_ADDRESS); 
            int notes_column = cur.getColumnIndex(PropertyDB.KEY_NOTES); 
            do {
                // Get the field values
            	_address = cur.getString(address_column);
            	setTitle(_address);
            	_notes = cur.getString(notes_column);
   
            } while (cur.moveToNext());

        }
		
		((EditText) findViewById(R.id.property_notes)).setText(_notes);
	};
}
