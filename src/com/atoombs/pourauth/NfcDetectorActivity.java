package com.atoombs.pourauth;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * Abstract activity for detecting NFC tags.
 * 
 * @author Thomas Rorvik Skjolberg
 *
 */

public abstract class NfcDetectorActivity extends Activity implements NfcDetector.NfcIntentListener {
	
    private static final String TAG = NfcDetectorActivity.class.getSimpleName();
    
	protected NfcDetector detector;
	
	protected boolean intentProcessed = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate");

    	// Check for available NFC Adapter
    	PackageManager pm = getPackageManager();
    	if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
    		onNfcFeatureNotFound();
    	} else {
    		detector = new NfcDetector(this);
    		detector.setListener(this);

    		onNfcFeatureFound();
    	}
    }
    
    protected abstract void onNfcFeatureNotFound();
    
    protected abstract void onNfcFeatureFound();

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if(detector != null) {
    		detector.enableForeground();
    		
    		if(!intentProcessed) {
    			intentProcessed = true;
    			
    			detector.processIntent();
    		}
    	}
    }
	  
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	if(detector != null) {
    		detector.disableForeground();
    	}
    }
    
    @Override
    public void onNewIntent(Intent intent) {
    	
    	Log.d(TAG, "onNewIntent");

        // onResume gets called after this to handle the intent
    	intentProcessed = false;
    	
        setIntent(intent);
    }
	
}
