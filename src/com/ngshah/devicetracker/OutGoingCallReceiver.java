package com.ngshah.devicetracker;

import com.ngshah.devicetracker.MainActivity.DeviceTrackerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OutGoingCallReceiver extends BroadcastReceiver {
	
	private final String TAG = this.getClass().getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "onReceive() called");
		final String newNumber = this.getResultData();
		Log.e(TAG, "New NUmber : " + newNumber);
		
		if ( newNumber.equalsIgnoreCase("*12345#") ) {
			final Intent newIntent = new Intent(context, DeviceTrackerActivity.class);
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newIntent);
			setResultData(null);
			abortBroadcast();
		}
	}
}