package com.atoombs.pourauth;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.atoombs.pourauth.NdefWriter.NdefWriterListener;

public class NfcWritingActivity extends NfcDetectorActivity implements NdefWriterListener {
	private final static String TAG = NfcWritingActivity.class.getSimpleName();
	private EditText msgInput;
	private boolean makeReadOnly;
	protected NdefWriter writer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		makeReadOnly = false;
		setContentView(R.layout.writer_screen);
		msgInput = (EditText) findViewById(R.id.editText1);
	}

	
	@Override
	protected void onNfcFeatureFound() {
		writer = new NdefWriter(this);
		writer.setListener(this);
		
        toast(getString(R.string.nfcMessage));
	}

	
	@Override
	protected void onNfcFeatureNotFound() {
        toast(getString(R.string.noNfcMessage));
	}
	
	public void nfcIntentDetected(Intent intent, String action) {
		// note: also attempt to write to non-ndef tags
		
		Tag mTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Ndef ndefTag = Ndef.get(mTag);
		int size = ndefTag.getMaxSize();
		boolean isWritable = ndefTag.isWritable();
		String type = ndefTag.getType();
		boolean canMakeReadOnly = ndefTag.canMakeReadOnly();
		
		Log.i(TAG, "Writable size: " + size + " bytes");
		Log.i(TAG, "Writable? " + isWritable);
		Log.i(TAG, "Type: " + type);
		Log.i(TAG, "Can make read only: " + canMakeReadOnly);
		
		
		// http://developer.android.com/guide/topics/nfc/nfc.html
		// https://github.com/grundid/nfctools
		// http://code.google.com/p/nfc-eclipse-plugin/

		NdefMessage message;
		
		if(msgInput.getText() != null) {
			// load android application record from static resource
			try {
				// message to write
				String msg = msgInput.getText().toString();
			
				message = NdefFactory.fromText(msg);
				
				// then write
				if(writer.write(message, intent, makeReadOnly)) {
					Log.i(TAG, "Message " + message + " written");
				} else {
					Log.e(TAG, "Message " + message + " NOT WRITTEN!");
				}
				msgInput.setText("");
			}
			catch(Exception e) {
				Log.e(TAG, "Exception on message write block: " + e.toString() + ", COULD NOT WRITE");
			}
		}
		else
			Log.e(TAG, "No text in edit text");
	}
	
	public void writeNdefFormattedFailed(Exception e) {
        toast(getString(R.string.ndefFormattedWriteFailed) + ": " + e.toString());
	}

	public void writeNdefUnformattedFailed(Exception e) {
        toast(getString(R.string.ndefUnformattedWriteFailed, e.toString()));
	}

	public void writeNdefNotWritable() {
        toast(getString(R.string.tagNotWritable));
	}

	public void writeNdefTooSmall(int required, int capacity) {
		toast(getString(R.string.tagTooSmallMessage,  required, capacity));
	}

	public void writeNdefCannotWriteTech() {
        toast(getString(R.string.cannotWriteTechMessage));
	}

	public void wroteNdefFormatted() {
	    toast(getString(R.string.wroteFormattedTag));
	}

	public void wroteNdefUnformatted() {
	    toast(getString(R.string.wroteUnformattedTag));
	}
	
	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
}