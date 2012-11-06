package com.atoombs.pourauth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EnterPatientActivity extends Activity {
	private String authUser;
	private static final String TAG = "EnterPatientActivity";
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(TAG, "Going to enter patient screen");
    	setContentView(R.layout.enter_patient);
    	
    	Intent thisIntent = getIntent();
        authUser = thisIntent.getExtras().getString("auth");
    	
    	//enter new patient
    	Button entryButton = (Button)findViewById(R.id.entryButton);
    	
    	entryButton.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View view) {
            	EditText amt1Etxt = (EditText)findViewById(R.id.amt1EditTxt);
            	EditText amt2Etxt = (EditText)findViewById(R.id.amt2EditTxt);
            	
            	String amt1Str = amt1Etxt.getText().toString();
            	String amt2Str = amt2Etxt.getText().toString();

            	Log.i(TAG, "Entering new patient for auth user " + authUser + " with amt1,amt2: " + amt1Str + ", " + amt2Str);
            	amt1Etxt.setText("");
            	amt2Etxt.setText("");
            	
            	Log.i(TAG, "Going to back to pats db activity");
            	Intent intent = new Intent(EnterPatientActivity.this, PatientDbCreator.class);
            	intent.putExtra("auth", authUser);
            	intent.putExtra("amt1", amt1Str);
            	intent.putExtra("amt2", amt2Str);
            	intent.putExtra("isEntered", true);
            	startActivity(intent);
    		}
    	});
	}
}
