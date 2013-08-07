package com.ngshah.devicetracker;

import java.util.List;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.NeighboringCellInfo;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ngshah.devicetracker.utils.Common;
import com.ngshah.devicetracker.utils.LocationImpl;

public class SendSMS implements LocationImpl, LocationListener {
	
	private final String TAG = this.getClass().getSimpleName();

	private Context context;
	private String senderNumber = "";
	private boolean isLocationAvailable = false;
	private LocationManager locationManager;
	
	public SendSMS(Context context, String senderNumber) {
		this.context = context;
		this.senderNumber = senderNumber;
	}
	
	protected void sendImei() {
		final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		sendSMS("IMEI is : " + telephonyManager.getDeviceId());
	}
	
	protected void sendImsi() {
		final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		sendSMS("IMSI is : " + telephonyManager.getSubscriberId());
	}
	
	protected void sendImeiAndImsi() {
		final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		sendSMS("IMEI is : " + telephonyManager.getDeviceId() + "\nIMSI is : " + telephonyManager.getSubscriberId());
	}
	
	protected void sendNetworkInfo() {
		
		final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
	
	protected void sendLocation() {
		LocationUpdate locationUpdate = null;
		if ( GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS ) {
			locationUpdate = new LocationUpdate(context, this);
			synchronized (locationUpdate) {
				try {
					locationUpdate.wait(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if ( !isLocationAvailable ) {
			Log.e(TAG, "Play Service GPS DATA NOT Available");
			
			Location location = null;
			if ( locationUpdate != null ) {
				location = locationUpdate.getLastLocation();
			}
			if ( location != null) {
				updateLocation(location);
			} else {
				Log.e(TAG, "Trying GPS Location");
				locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				
				if ( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, this);
					synchronized (locationManager) {
						try {
							locationManager.wait(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else {
					Log.e(TAG, "GPS provider not Available");
				}
				
				if ( !isLocationAvailable &&
						locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
					synchronized (locationManager) {
						try {
							Log.e(TAG, "Trying Network Location");
							locationManager.wait(10000);
							if ( !isLocationAvailable ) {
								locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this);
								locationManager.wait(10000);
								Log.e(TAG, "No Network Location");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else {
					Log.e(TAG, "Network provider not Available");
				}
				
				if ( !isLocationAvailable ) {
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					
					if ( !isLocationAvailable && location != null ) {
						Log.e(TAG, "Got GPS LAST Location Available");
						updateLocation(location);
					} else {
						Log.e(TAG, "Trying Network LAST Location Available");
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						
						if ( !isLocationAvailable && location != null ) {
							Log.e(TAG, "Got Network LAST Location Available");
							updateLocation(location);
						} else {
							Log.e(TAG, "Send SMS Location Not getting");
							sendSMS(context.getString(R.string.location_not_available));
						}
					}
				} else {
					Log.e(TAG, "Location NOT Available");
				}
				locationManager.removeUpdates(this);
			}
		} else {
			Log.e(TAG, "Location Available");
		}
	}
	
	protected void wipeData(){
		final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		boolean isEnabled = devicePolicyManager.isAdminActive(new ComponentName(context, MainActivity.class));
		
		if ( isEnabled ) {
			devicePolicyManager.wipeData(0);
		}
	}
	
	protected void lockScreen(String content) {
		final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		boolean isEnabled = devicePolicyManager.isAdminActive(new ComponentName(context, MainActivity.class));
		
		Log.e(TAG, "Enabled : " + isEnabled);
		if ( isEnabled ) {
			String newPassword = content;
			newPassword = newPassword.replace(Common.prefixes[21], "").trim();
			newPassword = newPassword.replace(Common.prefixes[22], "").trim();
			newPassword = newPassword.replace(Common.prefixes[23], "").trim();
			newPassword = newPassword.replace(Common.prefixes[24], "").trim();
			
			if ( TextUtils.isDigitsOnly(newPassword) ) {
				
				devicePolicyManager.setPasswordQuality(new ComponentName(context, MainActivity.class), DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
				devicePolicyManager.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
				devicePolicyManager.lockNow();
			} else {
				sendSMS(context.getString(R.string.numeric_numbers_only));
			}
		}
	}
	
	@Override
	public void updateLocation(Location location) {
		
		isLocationAvailable = true;
		
		final StringBuilder builder = new StringBuilder();
		builder.append(location.getLatitude()).append(",").append(location.getLongitude());
		
		if ( location.hasAltitude() ) {
			builder.append("\n").append("Altitude:").append(location.getAltitude());
		}
		
		if ( location.hasAccuracy() ) {
			builder.append("\n").append("Accuracy:").append(location.getAccuracy());
		}
		
		if ( location.hasSpeed() ) {
			builder.append("\n").append("Speed:").append(location.getSpeed()).append(" meters/second");
		}
			
		sendSMS(builder.toString());
	}
	
	private void sendSMS (String content) {
		final SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(senderNumber, null, content, null, null);
	}

	@Override
	public void onLocationChanged(Location location) {
		
		Log.e(TAG, "onLocationChanged : Lat : " + location.getLatitude() + 
				", Long : " + location.getLongitude());
		
		isLocationAvailable = true;
		locationManager.removeUpdates(this);
		if ( location != null ) {
			updateLocation(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.e(TAG, "onProviderDisabled : " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.e(TAG, "onProviderEnabled : " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.e(TAG, "onStatusChanged : " + provider);
	}
}