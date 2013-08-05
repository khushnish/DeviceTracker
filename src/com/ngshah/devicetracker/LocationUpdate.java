package com.ngshah.devicetracker;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.ngshah.devicetracker.utils.LocationImpl;

public class LocationUpdate implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {
	
	private LocationImpl locationImpl;
	private Context context;
	
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static final int MILLISECONDS_PER_SECOND = 1000;
//    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
//    private static final long UPDATE_INTERVAL =
//            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    
	public LocationUpdate(Context context, LocationImpl locationImpl) {
		this.locationImpl = locationImpl;
		this.context = context;
		
		mLocationClient = new LocationClient(context, this, this);
	        
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(
	                LocationRequest.PRIORITY_HIGH_ACCURACY);
//		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		mLocationRequest.setNumUpdates(3);
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e("Location", "onConnectionFailed");
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult((Activity)context, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.e("Location", "Connected");
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		Log.e("Location", "DisConnected");
	}

	@Override
	public void onLocationChanged(Location location) {
		String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
		Log.e("Location", msg);
		
		locationImpl.updateLocation(location);
		
		notify();
	}
	
	protected Location getLastLocation() {
		try {
			if ( mLocationClient != null && mLocationClient.isConnected() ) {
				return mLocationClient.getLastLocation();			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}