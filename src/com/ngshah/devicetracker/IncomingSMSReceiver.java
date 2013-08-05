package com.ngshah.devicetracker;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ngshah.devicetracker.utils.Common;

public class IncomingSMSReceiver extends BroadcastReceiver {
	
	private final String TAG = this.getClass().getSimpleName();
	private Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if ( intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED") ) {
			this.context = context;
			Log.i(TAG, "SMS Received");
			final Bundle bundle = intent.getExtras();
			if (bundle != null && bundle.containsKey("pdus")) {
			    final Object[] pdus = (Object[]) bundle.get("pdus");
			    final SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[0]);
			    String senderNumber = sms.getOriginatingAddress();
			    
			    Log.i(TAG, "Sender Number : " + senderNumber);
			    senderNumber = contactExists(senderNumber);
			    
			    final ArrayList<String> smsContent = new ArrayList<String>();
			    
			    if ( !senderNumber.equalsIgnoreCase("") ) {
			    	 Log.i(TAG, "Contact Exists : ");
				    if ( senderIsAvailable(senderNumber) ) {
				    	
				    	final SmsMessage [] messages = new SmsMessage[pdus.length];
				    	String msgContent;
				    	for (int i = 0; i < messages.length; i++) {
				    		msgContent = "";
				    		messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				    		msgContent = messages[i].getMessageBody();
				    		 
				    		for (int j = 0; j < Common.prefixes.length; j++) {
				    			if ( msgContent.toUpperCase(Locale.getDefault()).contains(Common.prefixes[j]) ) {
				    				Log.i(TAG, "Aborting broadcast...");
				    				abortBroadcast();
				    				smsContent.add(msgContent);
				    			}
				    		}
				    	}
				    	
				    	if ( smsContent.size() > 0 ) {
					    	final Intent smsReceiverService = new Intent(context, IncomingSMSReceiverService.class);
					    	smsReceiverService.putStringArrayListExtra("smsContent", smsContent);
					    	smsReceiverService.putExtra("senderNumber", senderNumber);
					    	context.startService(smsReceiverService);
				    	}
				    }
			    } else {
			    	Log.i(TAG, "Contact does not Exists : ");
			    }
			}
		}
	}
	
	private boolean senderIsAvailable(String senderNumber) {
		
		final SharedPreferences preferences = 
				context.getSharedPreferences(context.getString(R.string.pref_numbers), Context.MODE_PRIVATE);
		
		final Map<String,?> keys = preferences.getAll();
		
		for ( Map.Entry<String,?> entry : keys.entrySet() ) {
            Log.d("map values",entry.getKey() + ": " + 
                    entry.getValue().toString());
            if ( entry.getValue().toString().equalsIgnoreCase(senderNumber) ) {
            	Log.i(TAG, "Contact also Exists in our system: ");
            	return true;
            }
		}
		return false;
	}
	
	public String contactExists(String number) {
		/// number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Log.e(TAG, "lookupUri : " + lookupUri);
		final String[] mPhoneNumberProjection = { PhoneLookup._ID, PhoneLookup.NUMBER, PhoneLookup.LOOKUP_KEY };
		final Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
		
		try {
		   if ( cur.moveToFirst() ) {
			   String newId = cur.getString(cur.getColumnIndex(PhoneLookup._ID));
			   String newNumber = cur.getString(cur.getColumnIndex(PhoneLookup.NUMBER));
			   Log.e(TAG, "New ID is : " + newId);
			   Log.e(TAG, "Number is : " + newNumber);
			   
		      return newNumber;
		   }
		} finally {
			if (cur != null)
				cur.close();
		}
		return "";
	}
}