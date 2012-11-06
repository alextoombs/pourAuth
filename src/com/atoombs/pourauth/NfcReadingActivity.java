package com.atoombs.pourauth;

import java.util.List;

import org.nfctools.ndef.NdefContext;
import org.nfctools.ndef.NdefMessageDecoder;
import org.nfctools.ndef.Record;
import org.nfctools.ndef.wkt.records.TextRecord;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.atoombs.pourauth.NdefReader.NdefReaderListener;

public class NfcReadingActivity extends NfcDetectorActivity implements NdefReaderListener{
	private static final String TAG = NfcReadingActivity.class.getSimpleName();

	//Hardcoded array of Strings that are authenticated
	private static final String[] auths = {"Auth-001", "Auth-002", "Auth-003"};
	
	protected NdefReader reader;
	protected NdefMessage[] messages;
	
	private boolean isAuthorized;
	
	private Button authButton;
	private TextView readTv;
	
	private String authUser;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.reader_screen);
		
		isAuthorized = false;
		authButton = (Button)findViewById(R.id.authButton);
		authButton.setVisibility(View.GONE);
		
		readTv = (TextView) findViewById(R.id.readTv);
		
		/**When clicked, go to db creator activity, and add extra with which auth user. */
		authButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
    			Log.i(TAG, "Going to read activity");
            	Intent intent = new Intent(NfcReadingActivity.this, PatientDbCreator.class);
            	intent.putExtra("auth", authUser);
            	startActivity(intent);
            }
        });
	}
	@Override
	protected void onNfcFeatureFound() {
		reader = new NdefReader();
		reader.setListener(this);
	
        toast(getString(R.string.nfcMessage));
	}

	@Override
	protected void onNfcFeatureNotFound() {
        toast(getString(R.string.noNfcMessage));
	}
	
	public void nfcIntentDetected(Intent intent, String action) {
		Log.d(TAG, "nfcIntentDetected: " + action);
		
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
		
		if(reader.read(intent)) {
			// do something

			// show in log
			if(messages != null) {
				// iterate through all records in all messages (usually only one message)
				
				Log.d(TAG, "Found " + messages.length + " NDEF messages");

				NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
				for(int i = 0; i < messages.length; i++) {

					byte[] messagePayload = messages[0].toByteArray();
					
					
					// parse to records - byte to POJO
					List<Record> records = ndefMessageDecoder.decodeToRecords(messages[i].toByteArray());

					Log.d(TAG, "Message " + i + " is of size " + messagePayload.length + " and contains " + records.size() + " records"); // note: after combined chunks, if any.

					for(int k = 0; k < records.size(); k++) {
						Log.d(TAG, " Record " + k + " type " + records.get(k).getClass().getSimpleName());
					}
				}
			}
			
			// show in gui
			showList();
		} else {
			// do nothing(?)
			
			clearList();
		}
	}

	public void readNdefMessages(NdefMessage[] messages) {
		if(messages.length > 1) {
	        toast(getString(R.string.readMultipleNDEFMessage));
	        Log.i(TAG, "Multiple NDEF Messages; cannot process tag for authentication.");
		} else {
	        toast(getString(R.string.readSingleNDEFMessage));
		}		
		
		// save message
		this.messages = messages;
	}

	public void readNdefEmptyMessage() {
        toast(getString(R.string.readEmptyMessage));
	}

	public void readNonNdefMessage() {
	    toast(getString(R.string.readNonNDEFMessage));
	}

	public void toast(String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}

	private void showList() {
		if(messages != null && messages.length > 0) {
			
			// display the first message
			byte[] messagePayload = messages[0].toByteArray();
			
			// parse to records
			NdefMessageDecoder ndefMessageDecoder = NdefContext.getNdefMessageDecoder();
			List<Record> records = ndefMessageDecoder.decodeToRecords(messagePayload);
			
			// check for matched signatures for authorization
			for(String str: auths) {
				for(Record rec : records) {
					if(rec instanceof TextRecord) {
						TextRecord textRecord = (TextRecord)rec;
						Log.i(TAG, "This TextRecord: "  + textRecord.getText());
						if(str.equals(textRecord.getText())) {
							Log.i(TAG, "Above TextRecord matches an auth string, user is authorized");
							isAuthorized = true;
							authButton.setVisibility(View.VISIBLE);
							
							authUser = str;
						}
					}
				}
			}
			
			// show in gui
			ArrayAdapter<? extends Object> adapter = new NdefRecordAdapter(this, records);
			ListView listView = (ListView) findViewById(R.id.recordListView);
			listView.setAdapter(adapter);
			if(isAuthorized) {
				listView.setVisibility(View.GONE);
				readTv.setText("Your badge is authorized!  Please proceed to pour.");
			}
			else
				readTv.setText("Your tag is not authorized to pour.");
		} else {
			clearList();
		}
	}
	
	private void clearList() {
		ListView listView = (ListView) findViewById(R.id.recordListView);
		listView.setAdapter(null);
	}
}