package com.ngshah.devicetracker;

import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class BootCompleteService extends Service {
	
	private SharedPreferences preferences;
	private TelephonyManager manager;
	private String imei = "";
	private String imsi = "";
	
	@Override
	public void onCreate() {
		super.onCreate();
		preferences = getSharedPreferences(getString(R.string.pref_imeiimsi), MODE_PRIVATE);
		manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		boolean isImeiAvailable = false;
		boolean isImsiAvailable = false;
		
		for (int i = 0; i <= 5000; ++i) {
			
			isImeiAvailable = checkImeiIsAvailable();
			isImsiAvailable = checkImsiIsAvailable();
			
			if ( isImeiAvailable && isImsiAvailable ) {
				break;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		checkSimIsChanged();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private boolean checkImeiIsAvailable() {
		try {
			imei = manager.getDeviceId();
			if ( !TextUtils.isEmpty(imei) && !imei.equals("null")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean checkImsiIsAvailable() {
		try {
			imsi = manager.getSubscriberId();
			if ( !TextUtils.isEmpty(imsi) && !imsi.equals("null")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void checkSimIsChanged() {
		if ( !imsi.equals(preferences.getString(getString(R.string.imsi), "")) ) {
			
			final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_numbers), MODE_PRIVATE);
			final Map<String, ?> numers = sharedPreferences.getAll();
			
			//	Send SMS on Registered Numbers
			SendSMS sendSMS;
			for (Map.Entry<String, ?> entry : numers.entrySet()) {
			    Log.e("BootCompleteReceiver", "Key = " + entry.getKey() + ", Value = " + entry.getValue());
			    
			    sendSMS = new SendSMS(this, String.valueOf(entry.getValue()));
				
				//	IMEI and IMSI
				sendSMS.sendImeiAndImsi();
				
				//	Network
				sendSMS.sendNetworkInfo();
				
				//	Location
				sendSMS.sendLocation();
			}
		}
	}
}