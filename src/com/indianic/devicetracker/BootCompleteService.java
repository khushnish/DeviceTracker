package com.indianic.devicetracker;

import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.NeighboringCellInfo;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
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
			//	Send SMS on Registered Numbers
			
			//	IMEI and IMSI
			sendImeiAndImsiInfo();
			
			//	Network
			sendNetworkInfo();
			
			//	Location
			sendLocationInfo();
		}
	}
	
	private void sendImeiAndImsiInfo() {
		sendSMS("IMEI is : " + imei + "\nIMSI is : " + imsi);
	}
	
	private void sendNetworkInfo() {
		
		final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		final GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
		
		final String networkOperator = telephonyManager.getNetworkOperator();
		final String mcc = networkOperator.substring(0, 3);
		final String mnc = networkOperator.substring(3);
		
		final StringBuilder builder = new StringBuilder();
		builder.append("Country : " + telephonyManager.getNetworkCountryIso());
		builder.append("\nNetwork Name : " + telephonyManager.getNetworkOperatorName());
		
		if ( cellLocation != null ) {
			builder.append("\nCell Id : " + cellLocation.getCid());
			builder.append("\nLAC : " + cellLocation.getLac());
		}
		builder.append("\nMNC : " + mnc);
		builder.append("\nMCC : " + mcc);
		
		final List<NeighboringCellInfo> neighboringList = telephonyManager.getNeighboringCellInfo();
		
		if ( neighboringList != null && neighboringList.size() > 0 ) {
			String stringNeighboring = "\nNeighboring List- Lac : Cid : RSSI\n";
			for ( int i = 0; i < neighboringList.size(); i++ ) {
				
				String dBm;
				int rssi = neighboringList.get(i).getRssi();
				if ( rssi == NeighboringCellInfo.UNKNOWN_RSSI ) {
					dBm = "Unknown RSSI";
				} else {
					dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
				}
				
				stringNeighboring = stringNeighboring
						+ String.valueOf(neighboringList.get(i).getLac()) +" : "
						+ String.valueOf(neighboringList.get(i).getCid()) +" : "
						+ String.valueOf(neighboringList.get(i).getPsc()) +" : "
						+ String.valueOf(neighboringList.get(i).getNetworkType()) +" : "
						+ dBm +"\n";
			}
			builder.append(stringNeighboring);
		}
		sendSMS(builder.toString());
	}
	
	private void sendLocationInfo() {
		
	}
	
	private void sendSMS (String content) {
		final SmsManager sms = SmsManager.getDefault();
		final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_numbers), MODE_PRIVATE);
		final Map<String, ?> numers = sharedPreferences.getAll();
		
		for (Map.Entry<String, ?> entry : numers.entrySet()) {
		    Log.e("BootCompleteReceiver", "Key = " + entry.getKey() + ", Value = " + entry.getValue());
		    sms.sendTextMessage(String.valueOf(entry.getValue()), null, content, null, null);
		}
	}
}