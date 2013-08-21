package com.ngshah.devicetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("DeviceTracker", "onReceive() called");
		Log.e("DeviceTracker", "Boot Completed");
		context.startService(new Intent(context, BootCompleteService.class));
	}
}
