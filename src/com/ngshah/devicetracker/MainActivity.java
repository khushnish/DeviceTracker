package com.ngshah.devicetracker;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdTargetingOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends DeviceAdminReceiver {
	
	public static class DeviceTrackerActivity extends Activity implements AdListener {
		
		private final String TAG = this.getClass().getSimpleName();
		static final int RESULT_ENABLE = 1;
		
		private final int PICK_CONTACT_REQUEST = 0;
		private SharedPreferences preferences;
		private Map<String, ?> numbers;
		private ArrayAdapter<Object> adapter;
		
		private Button btnEnableAdmin;
		private Button btnGooglePlayService;
		
		private DevicePolicyManager mDPM;
//		private ActivityManager mAM;
		private ComponentName mDeviceAdminSample;
		private AdLayout adView;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			
			initializeComponents();
			registerImsiAndImei();
		}
		
		private void initializeComponents() {
			
			AdRegistration.enableTesting(true);
			AdRegistration.enableLogging(true);
			
			adView = (AdLayout) findViewById(R.id.activity_main_view_border_ad_view);
			adView.setListener(this);

			try {
				AdRegistration.setAppKey("fe4fecbbff304e57b6405975185bae29");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			adView.loadAd(new AdTargetingOptions());
			
			preferences = getSharedPreferences(getString(R.string.pref_numbers), MODE_PRIVATE);
			numbers = preferences.getAll();
			
			final TextView txtHeader = (TextView) findViewById(R.id.activity_main_txt_configure_numbers);
			Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
			txtHeader.setTypeface(typeface, Typeface.BOLD);
			
			final Button btnAddNumbers = (Button) findViewById(R.id.activity_main_btn_add_numbers);
			btnAddNumbers.setTypeface(typeface, Typeface.BOLD);
			
			btnEnableAdmin = (Button) findViewById(R.id.activity_main_btn_enable_admin);
			btnEnableAdmin.setTypeface(typeface, Typeface.BOLD);
			
			btnGooglePlayService = (Button) findViewById(R.id.activity_main_btn_install_google_play_service);
			btnGooglePlayService.setTypeface(typeface, Typeface.BOLD);
			
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
			
			btnEnableAdmin.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
	                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
	                        mDeviceAdminSample);
	                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
	                        "Additional text explaining why this needs to be added.");
	                startActivityForResult(intent, RESULT_ENABLE);				
				}
			});
			
			btnGooglePlayService.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=com.google.android.gms"));
					startActivity(intent);
				}
			});
			
			mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
//			mAM = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
			mDeviceAdminSample = new ComponentName(this, MainActivity.class);
		}
		
		@Override
		protected void onResume() {
			super.onResume();
			
			checkAdminIsEnable();
			checkPlayServiceIsInstalled();
		}
		
		private void checkAdminIsEnable() {
			final boolean active = mDPM.isAdminActive(mDeviceAdminSample);
			
			if ( active ) {
				btnEnableAdmin.setVisibility(Button.GONE);
			}
		}
		
		private void checkPlayServiceIsInstalled() {
			if ( GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS ) {
				btnGooglePlayService.setVisibility(Button.GONE);
			}
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
			} else if ( requestCode == RESULT_ENABLE ) {
				 if (resultCode == Activity.RESULT_OK) {
	                 Log.i(TAG, "Admin enabled!");
	                 checkAdminIsEnable();
	             } else {
	                 Log.i(TAG, "Admin enable FAILED!");
	             }
			}
		}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			this.adView.destroy();
		}

		@Override
		public void onAdCollapsed(AdLayout arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAdExpanded(AdLayout arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAdFailedToLoad(AdLayout arg0, AdError arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAdLoaded(AdLayout arg0, AdProperties arg1) {
			// TODO Auto-generated method stub
			
		}
	}
}