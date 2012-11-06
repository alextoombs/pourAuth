package com.atoombs.pourauth;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private final static String TAG = "GCMIntentService";
	private final static String senderId = "99682899393";
	
	public GCMIntentService() {
		super("GCMIntentService");
		Log.i(TAG, "GCM Passed");
	}
	
	/**
	 * @see com.google.android.gcm.GCMBaseIntentService#onError(android.content.Context, java.lang.String)
	 */
   @Override
    protected void onError(Context arg0, String errorID) {
        Log.e(TAG, errorID, null);
    }
   
	/**
	 * @see com.google.android.gcm.GCMBaseIntentService#onRegistered(android.content.Context, java.lang.String)
	 */
	@Override
	protected void onRegistered(Context context, String regId) {
		Log.i(TAG, "onRegistered: " + regId);
//		DeviceToken.registerDeviceForGCM(context, regId);
	}
	
	/**
	 * @see com.google.android.gcm.GCMBaseIntentService#onUnregistered(android.content.Context, java.lang.String)
	 */
	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.i(TAG, "onUnregistered: " + regId);
//        DeviceToken.unregisterDeviceFromGCM(context, regId);
	}
	
	/**
	 * @see com.google.android.gcm.GCMBaseIntentService#onMessage(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onMessage(Context context, Intent intent) {
	      Log.i(TAG, "Message is: " + intent.getStringExtra( "message" ) );

		// This is how to get values from the push message (data)
        String message = intent.getStringExtra("message");
		long timestamp = intent.getLongExtra("timestamp", -1);

		Intent notificationIntent = new Intent(context, PatientDbCreator.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	}
}
