package com.indianic.devicetracker;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.NeighboringCellInfo;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.indianic.devicetracker.utils.Common;

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
		for (String content: smsContent) {
			Log.e(TAG, "Content is : " + content);
			if ( content.equalsIgnoreCase(Common.prefixes[0]) ) {	//	IMEI
				Log.e(TAG, "Sending IMEI");
				final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				sendSMS("IMEI is : " + telephonyManager.getDeviceId());
			} else if ( content.equalsIgnoreCase(Common.prefixes[1]) ) {	//	IMSI
				Log.e(TAG, "Sending IMSI");
				final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				sendSMS("IMSI is : " + telephonyManager.getSubscriberId());
			} else if ( content.equalsIgnoreCase(Common.prefixes[2]) ||
					content.equalsIgnoreCase(Common.prefixes[3]) ) {	//	IMEI & IMSI
				Log.e(TAG, "Sending IMEI & IMSI");
				final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//				String myIMSI = android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMSI);
				sendSMS("IMEI is : " + telephonyManager.getDeviceId() + "\nIMSI is : " + telephonyManager.getSubscriberId());
			} else if ( content.equalsIgnoreCase(Common.prefixes[4]) ||
					content.equalsIgnoreCase(Common.prefixes[5]) || 
					content.equalsIgnoreCase(Common.prefixes[6]) ||
					content.equalsIgnoreCase(Common.prefixes[7])) {	//	CELL LOCATION && CELL INFO
				
				Log.e(TAG, "Sending CELL LOCATION");
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
			} else if ( content.equalsIgnoreCase(Common.prefixes[8]) ||
					content.equalsIgnoreCase(Common.prefixes[9]) ||
					content.equalsIgnoreCase(Common.prefixes[10]) ) {		//	LOCATION INFO
				
			} else if ( content.equalsIgnoreCase(Common.prefixes[11]) ||
					content.equalsIgnoreCase(Common.prefixes[12]) ||
					content.equalsIgnoreCase(Common.prefixes[13]) ||
					content.equalsIgnoreCase(Common.prefixes[14]) ||
					content.equalsIgnoreCase(Common.prefixes[15]) ||
					content.equalsIgnoreCase(Common.prefixes[16])) {	//	WIPE DATA
				
			} else if ( content.equalsIgnoreCase(Common.prefixes[17]) ) {	//	RING
//				final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
//				int maxVolumeForDevice = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
//				audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolumeForDevice, AudioManager.FLAG_ALLOW_RINGER_MODES);
				
//				final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//				final Ringtone rt = RingtoneManager.getRingtone(this, uri);
//				rt.play();
				
				MediaPlayer player = MediaPlayer.create(this,
						RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
				player.start();
			} else if ( content.equalsIgnoreCase(Common.prefixes[18]) ) {	//	VIBRATE
				((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(800);
			} else if ( content.equalsIgnoreCase(Common.prefixes[19]) ||
					content.equalsIgnoreCase(Common.prefixes[20])) {	//	RING AND VIBRATE
				((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(800);
				
			}
		}
		
		stopSelf();
	}
	
	private void sendSMS (String content) {
		final SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(senderNumber, null, content, null, null);
	}
}