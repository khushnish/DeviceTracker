package com.ngshah.devicetracker;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdTargetingOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

public class PlayRingActivity extends Activity implements AdListener {
	
	private Ringtone rt;
	private AdLayout adView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playring);
		
		boolean vibrate = getIntent().getBooleanExtra("vibrate", true);
		boolean ring = getIntent().getBooleanExtra("ring", true);
		
		playRingAndVibratePhone(vibrate, ring);
//		displayDialog();
		
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
	}
	
	private void playRingAndVibratePhone(boolean vibrate, boolean ring) {
		if ( vibrate ) {
			try {
				((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(12000);
				finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if ( ring ) {
			try {
				final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				int maxVolumeForDevice = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
				audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolumeForDevice, AudioManager.FLAG_ALLOW_RINGER_MODES);
				
				final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				rt = RingtoneManager.getRingtone(this, uri);
				rt.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onClickStop(View view) {
		stopPlaying();
	}
	
	private void displayDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setMessage("Stop Playing");
	    builder.setPositiveButton(R.string.stop, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   stopPlaying();
	               }
	           });
	    builder.setCancelable(false);
	    builder.create().show();
	}
	
	private void stopPlaying() {
		try {
			if ( rt != null && rt.isPlaying() ) {
				rt.stop();
			}
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.e("PlayRing", "onPause()");
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if ( pm.isScreenOn() ) {
			stopPlaying();
		}
	}

	@Override
	public void onAdCollapsed(AdLayout arg0) {
		
	}

	@Override
	public void onAdExpanded(AdLayout arg0) {
		
	}

	@Override
	public void onAdFailedToLoad(AdLayout arg0, AdError arg1) {
		
	}

	@Override
	public void onAdLoaded(AdLayout arg0, AdProperties arg1) {
		
	}
}