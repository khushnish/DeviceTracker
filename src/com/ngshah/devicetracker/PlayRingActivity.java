package com.ngshah.devicetracker;

import android.app.Activity;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;

public class PlayRingActivity extends Activity {
	
	private Ringtone rt; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playring);
		
		boolean vibrate = getIntent().getBooleanExtra("vibrate", true);
		boolean ring = getIntent().getBooleanExtra("ring", true);
		
		playRingAndVibratePhone(vibrate, ring);
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
		try {
			rt.stop();
			finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
