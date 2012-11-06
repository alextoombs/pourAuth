package com.atoombs.pourauth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**This application will authenticate users to remotely pour set amounts of "medicine"
 * 	as set to different levels and stored in a local database.
 * 
 * @author Alex Toombs
 * @since July 20th, 2012
 * 
 * This application borrows heavily from the open source Eclipse NFC Plug-in
 * See website: http://code.google.com/p/nfc-eclipse-plugin/
 */
public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private Button readButton;
	private Button writeButton;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.first_screen);
		
		readButton = (Button)findViewById(R.id.readButton);
		writeButton = (Button)findViewById(R.id.writeButton);
		
      readButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
    			Log.i(TAG, "Going to read activity");
            	Intent intent = new Intent(MainActivity.this, NfcReadingActivity.class);
            	startActivity(intent);
            }
        });
      
      writeButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view){
  			Log.i(TAG, "Going to write activity");
          	Intent intent = new Intent(MainActivity.this, NfcWritingActivity.class);
          	startActivity(intent);
          }
      });
	}
}
