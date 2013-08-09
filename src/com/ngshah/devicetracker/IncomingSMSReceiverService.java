package com.ngshah.devicetracker;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.ngshah.devicetracker.utils.Common;

public class IncomingSMSReceiverService extends Service {
	
	private final String TAG = this.getClass().getSimpleName();
	private ArrayList<String> smsContent;
	private String senderNumber = "";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		smsContent = intent.getStringArrayListExtra("smsContent");
		senderNumber = intent.getStringExtra("senderNumber");
		
		Log.e(TAG, "smsContent size : " + smsContent.size());
		Log.e(TAG, "senderNumber : " + senderNumber);
		
		checkContent();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	
	private void checkContent() {
		SendSMS sendSMS = new SendSMS(this, senderNumber);
		
		for (String content: smsContent) {
			content = content.toString().toUpperCase(Locale.getDefault());
			Log.e(TAG, "Content is : " + content);
			if ( content.equalsIgnoreCase(Common.prefixes[0]) ) {	//	IMEI
				Log.e(TAG, "Sending IMEI");
				sendSMS.sendImei();
			} else if ( content.equalsIgnoreCase(Common.prefixes[1]) ) {	//	IMSI
				Log.e(TAG, "Sending IMSI");
				sendSMS.sendImsi();
			} else if ( content.equalsIgnoreCase(Common.prefixes[2]) ||
					content.equalsIgnoreCase(Common.prefixes[3]) ) {	//	IMEI & IMSI
				Log.e(TAG, "Sending IMEI & IMSI");
				sendSMS.sendImeiAndImsi();
			} else if ( content.equalsIgnoreCase(Common.prefixes[4]) ||
					content.equalsIgnoreCase(Common.prefixes[5]) || 
					content.equalsIgnoreCase(Common.prefixes[6]) ||
					content.equalsIgnoreCase(Common.prefixes[7])) {	//	CELL LOCATION && CELL INFO
				
				Log.e(TAG, "Sending CELL LOCATION");
				sendSMS.sendNetworkInfo();
			} else if ( content.equalsIgnoreCase(Common.prefixes[8]) ||
					content.equalsIgnoreCase(Common.prefixes[9]) ||
					content.equalsIgnoreCase(Common.prefixes[10]) ) {		//	LOCATION INFO
				sendSMS.sendLocation();
			} else if ( content.equalsIgnoreCase(Common.prefixes[11]) ||
					content.equalsIgnoreCase(Common.prefixes[12]) ||
					content.equalsIgnoreCase(Common.prefixes[13]) ||
					content.equalsIgnoreCase(Common.prefixes[14]) ||
					content.equalsIgnoreCase(Common.prefixes[15]) ||
					content.equalsIgnoreCase(Common.prefixes[16])) {	//	WIPE DATA
				Log.e(TAG, "WIPE DATA ");
				sendSMS.wipeData();
			} else if ( content.equalsIgnoreCase(Common.prefixes[17]) ) {	//	RING
				playRingAndVibratePhone(false, true);
			} else if ( content.equalsIgnoreCase(Common.prefixes[18]) ) {	//	VIBRATE
				playRingAndVibratePhone(true, false);
			} else if ( content.equalsIgnoreCase(Common.prefixes[19]) ||
					content.equalsIgnoreCase(Common.prefixes[20])) {	//	RING AND VIBRATE
				playRingAndVibratePhone(true, true);
			} else if ( content.contains(Common.prefixes[21]) ||
					content.contains(Common.prefixes[22]) || 
					content.contains(Common.prefixes[23]) ||
					content.contains(Common.prefixes[24])) {	//	Screen Lock
				
				Log.e(TAG, "Locking Screen ");
				sendSMS.lockScreen(content);
			} else if ( content.contains(Common.prefixes[25]) ||
					content.contains(Common.prefixes[26]) || 
					content.contains(Common.prefixes[27]) ||
					content.contains(Common.prefixes[28]) ) {	//	Enable App Launcher
				final ComponentName componentToDisable = new ComponentName(getPackageName(),
						  getPackageName() + ".MainActivity$DeviceTrackerActivity");
				getPackageManager().setComponentEnabledSetting(
						componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				Log.e(TAG, "App Enable");
			} else if ( content.contains(Common.prefixes[29]) ||
					content.contains(Common.prefixes[30]) || 
					content.contains(Common.prefixes[31]) ||
					content.contains(Common.prefixes[32]) ) {	//	Disable App Launcher
				final ComponentName componentToDisable = new ComponentName(getPackageName(),
						getPackageName() + ".MainActivity$DeviceTrackerActivity");
				getPackageManager().setComponentEnabledSetting(
						 componentToDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				Log.e(TAG, "App Disable");
			}
		}

		stopSelf();
	}
	
	private void playRingAndVibratePhone(boolean vibrate, boolean ring) {
		
		final Intent playRingIntent = new Intent(this, PlayRingActivity.class);
		playRingIntent.putExtra("vibrate", vibrate);
		playRingIntent.putExtra("ring", ring);
		startActivity(playRingIntent);
	}
}