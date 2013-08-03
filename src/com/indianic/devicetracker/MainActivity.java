package com.indianic.devicetracker;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private final String TAG = this.getClass().getSimpleName();
	
	private final int PICK_CONTACT_REQUEST = 0;
	private SharedPreferences preferences;
	private Map<String, ?> numbers;
	private ArrayAdapter<Object> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initializeComponents();
		registerImsiAndImei();
	}
	
	private void initializeComponents() {
		
		preferences = getSharedPreferences(getString(R.string.pref_numbers), MODE_PRIVATE);
		numbers = preferences.getAll();
		
		final TextView txtHeader = (TextView) findViewById(R.id.activity_main_txt_configure_numbers);
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		txtHeader.setTypeface(typeface, Typeface.BOLD);
		
		final Button btnAddNumbers = (Button) findViewById(R.id.activity_main_btn_add_numbers);
		btnAddNumbers.setTypeface(typeface, Typeface.BOLD);
		
		final ListView numbersList = (ListView) findViewById(R.id.activity_main_list_numbers);
		ArrayList<Object> list = new ArrayList<Object>(numbers.values());
		
		adapter = new NumberAdapter(this, R.layout.row_activity_main, R.id.row_activity_main_txt_number, list);
		numbersList.setAdapter(adapter);
		
		btnAddNumbers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
			    pickContactIntent.setType(Phone.CONTENT_TYPE);
			    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
			}
		});
	}
	
	private void registerImsiAndImei() {
		
		final SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_imeiimsi), MODE_PRIVATE);
		final Editor editor = preferences.edit();
		final TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		if ( !preferences.contains(getString(R.string.imei)) ) {
			String imei = manager.getDeviceId();
			if ( !TextUtils.isEmpty(imei) && !imei.equalsIgnoreCase("null") ) {
				editor.putString(getString(R.string.imei), imei);
			}
		}
		
		if ( !preferences.contains(getString(R.string.imsi)) ) {
			String imsi = manager.getSubscriberId();
			if ( !TextUtils.isEmpty(imsi) && !imsi.equalsIgnoreCase("null") ) {
				editor.putString(getString(R.string.imsi), imsi);
			}
		}
		editor.commit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if ( resultCode == RESULT_OK && requestCode == PICK_CONTACT_REQUEST ) {
			final Uri contactUri = data.getData();
			final String[] projection = {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
			final Cursor cursor = getContentResolver()
	                    .query(contactUri, projection, null, null, null);
			Log.e(TAG, "Contact URI : " + contactUri);
			Log.e(TAG, "Path Segment : " + contactUri.getLastPathSegment());
			 
			if ( cursor != null && cursor.getCount() > 0 ) {
				cursor.moveToFirst();
				 
				//	Retrieve the phone number from the NUMBER column
				final String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
				final String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				
				Log.e(TAG, "Id : " + id);
				 
				final Editor editor = preferences.edit();
				editor.putString(String.valueOf(id), number);
				editor.commit();
				 
				adapter.add(number);
				if ( cursor != null ) {
					cursor.close();
				}
			}
		}
	}
}