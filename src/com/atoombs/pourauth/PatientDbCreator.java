package com.atoombs.pourauth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

/**Provides a list of authorized users, each corresponding to a patient and their pour information.
 * Uses PatientDatabase class as a database to store this information.
 * 
*  @author Alex Toombs
*  @since July 25th, 2012 */
public class PatientDbCreator extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "PatientDbCreator";
    private boolean isClosed = false;
    
    private static final String SENDER_ID = "99682899393";
	
	private ArrayList<Patient> allPatients = new ArrayList<Patient>();
	private ArrayList<Patient> ownedPatients = new ArrayList<Patient>();
	
	private TextView authTv;
	private Spinner ownedPatientSpin;
	private Patient thisPatient;
	
	/**File of path to db */
	private File dbFile = new File("/data/data/com.atoombs.pourauth/databases/patients.db");
	public static String brk = "----------------------------\n";
	
	/**File of path to log pours */
	public File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/pours.txt");
	
	private Button newButton;
	private Button messageButton;
	private Button delButton;
	private Button writeButton;
	
    public PatientDatabase patsDb;
    private String authUser;
    @Override
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patsdb);
    	Log.i(TAG, "Logfile path: " + logFile.getPath());
    	authTv = (TextView)findViewById(R.id.authTv);

        Intent thisIntent = getIntent();
        authUser = thisIntent.getExtras().getString("auth");
        Log.i(TAG, "authUser: " + authUser);
  
        authTv.setText("Authorized as user: " + authUser + "!");
        
//        /**If previous DB version found, delete it. */
//        if(dbFile.exists())
//        	dbFile.delete();
        
        /**Starts the database and fills it with all the objects. */
        patsDb = new PatientDatabase(this);
        Log.i(TAG, "patsdb created");
        patsDb.open();
        isClosed = false;
        Log.i(TAG, "About to fill pats db");
        fillDb();
        
        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
	     // sets the app name in the intent
	     registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
	     registrationIntent.putExtra("sender", SENDER_ID);
	     startService(registrationIntent);
        
        GCMRegistrar.checkDevice( this );
        GCMRegistrar.checkManifest( this );
        final String regId = GCMRegistrar.getRegistrationId( this );
        if( regId.equals( "" ) ) {
            GCMRegistrar.register( this, SENDER_ID );
        }
        else {
            Log.i(TAG, "Already registered" );
        }
        
        if(thisIntent.getExtras().getBoolean("isEntered", false)) {
        	Log.i(TAG, "Entered patient");
        	String tempAmt1 = thisIntent.getExtras().getString("amt1");
        	String tempAmt2 = thisIntent.getExtras().getString("amt2");
        	
        	patsDb.open();
        	patsDb.enterPatient(authUser, tempAmt1, tempAmt2);
        	patsDb.close();
        }
        
        ownedPatientSpin = (Spinner) findViewById(R.id.patientSpin);
        delButton = (Button)findViewById(R.id.delButton);
        writeButton = (Button)findViewById(R.id.writeButton);
        newButton = (Button)findViewById(R.id.newButton);
        messageButton = (Button)findViewById(R.id.sendPatientMessage);
        
        printList();
        
        messageButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Log.i(TAG, "Sending message to phone");
        		//implement message code
        		sendMessageToPhone();
        	}
        });
        
        /**Clicking this button will show a new layout.  On this layout you can enter 2 double medicine amounts
         * to assign a new patient to the current auth user.
         */
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
            	Log.i(TAG, "Going to enter patient activity");
            	Intent intent = new Intent(PatientDbCreator.this, EnterPatientActivity.class);
            	intent.putExtra("auth", authUser);
            	startActivity(intent);
            }
        });
        
        writeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
            	Log.i(TAG, "Writing database now.");
            	writeDbToLog();
            }
        });
        
        delButton.setVisibility(View.GONE);
        delButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Log.i(TAG, "Deleting database now");
        		dbFile.delete();
        		ownedPatients.clear();
        		allPatients.clear();
        		
        		thisPatient = null; 		
                redrawSpinner();
        	}
        });
        
        populateLists();
        ownedPatientSpin.setOnItemSelectedListener(this);
    	ArrayAdapter<String> adapter = getOwnedPatientSpinner();
        ownedPatientSpin.setAdapter(adapter);
    }
    
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
        unregIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
        startService(unregIntent);
        GCMRegistrar.onDestroy(this);
    }
    
    public void onNothingSelected(AdapterView<?> arg0) {
        Log.d(TAG, "onNothingSelected()...");
    }
    
    /**Reaction to item selection of a spinner. */
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        if(arg0.getId() == R.id.patientSpin) {
        	Log.i(TAG, "In patient spin, arg2: " + arg2);
        	thisPatient = ownedPatients.get(arg2);
        }
    }
    
    /**Enter a patient into the database by passing its path as a String.
     * 
     * @param path auth authenticated user that may pour these medications
     * @param amt1 amount of medicine 1 to pour
     * @param amt2 amount of medicine 2 to pour
     */
    private void enterPatient(String auth, String amt1, String amt2) {
    	/**Actual data of the application to be input.  Constitutes one full entry. */
    	Log.i(TAG, "Entering patient with auth: " + auth + ", amt1: " + amt1 + ", amt2: " + amt2);
    	patsDb.open();
    	patsDb.enterPatient(auth, amt1, amt2);
    	patsDb.close();
    }
    
    /**Fill the database with all packages. This method parses through the list of PackageInfo objects, packages, and
     * 	for each PackageInfo pkg, it enters that patient into the database. */
    private void fillDb() {
    	if(!dbFile.exists()) {
			//fill with example patients
			enterPatient("Auth-001", "2.5", "3.0");
			enterPatient("Auth-003", "4.2", "1.8");
			enterPatient("Auth-002", "1.1", "0");
			enterPatient("Auth-002", "8.7", "12.2");
			enterPatient("Auth-002", "4.3", "8.8");
    	}
    	else
    		patsDb.close();    	
    	isClosed = true;
    }
    
    //call to repopulate lists and redraw spinner
    private void redrawSpinner() {
        ownedPatientSpin = (Spinner) findViewById(R.id.patientSpin);
        ownedPatientSpin.setOnItemSelectedListener(this);
        populateLists();
    	ArrayAdapter<String> adapter = getOwnedPatientSpinner();
        ownedPatientSpin.setAdapter(adapter);
    }
    
    /**Populate or repopulate the Lists of patients and owned patients. */
    private void populateLists() {
    	//clear list
    	allPatients.clear();
    	ownedPatients.clear();
    	
    	if(isClosed) {
    		patsDb.open();
    	}
    	
    	/**Get number of rows in the patient database. */
    	long rowNum = DatabaseUtils.queryNumEntries(patsDb.db, PatientDatabase.DATABASE_TABLE); 
    	
    	/**Create List of all patients, and list of all patients owned by authorized user at the time */
    	for(int i = 1; i <= rowNum; i++) {
        	Cursor c = patsDb.fetchPatient(i);
        	String thisRowAuth = c.getString(1);
        	Patient tempPatient = new Patient(Double.parseDouble(c.getString(3)), 
        				Double.parseDouble(c.getString(4)), thisRowAuth);
        	
        	allPatients.add(tempPatient);
        	
        	if(authUser.contains(thisRowAuth)) {
        		ownedPatients.add(tempPatient);
        	}
    	}
    	if(!isClosed) {
    		patsDb.close();
    		isClosed = true;
    	}
    }
    
    /**Get an adapter for a spinner over all patients owned by current authorized user.
     * 
     * @return adapter for patient select spinner.
     */
    private ArrayAdapter<String> getOwnedPatientSpinner() {
    	ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	
    	for(int j = 0; j < ownedPatients.size(); j++) {
    		adapter.add("Amt1: " + ownedPatients.get(j).getAmt1() + ", Amt2: " + ownedPatients.get(j).getAmt2());
    		
    		Log.i(TAG, "Added patient - Amt1: " + ownedPatients.get(j).getAmt1() + ", Amt2: " + ownedPatients.get(j).getAmt2());
    	}
    	return adapter;
    }
    
    private void printList() {
    	for(int k = 0; k < ownedPatients.size(); k++) {
    		Patient thisPat = ownedPatients.get(k);
    		Log.i(TAG, "ownedPatients # " + k + "- amt1: " + thisPat.getAmt1() + ", amt2: " + thisPat.getAmt2());
    	}
    	
    	for(int z = 0; z < allPatients.size(); z++) {
    		Patient thisPat = allPatients.get(z);
    		Log.i(TAG, "allPatients # " + z + "- amt1: " + thisPat.getAmt1() + ", amt2: " + thisPat.getAmt2());
    	}
    }
    
    //future
    private void sendMessageToPhone() {
    	try {
    		thisPatient.updateTimestamp();
    	}
    	catch(NullPointerException e) {
    		Log.e(TAG, "No patient selected, npe on sending message!");
    	}
    	//Code for sending message 
    	//use thisPatient
    }
    	
    /**Write the database to the log file. */
    private void writeDbToLog() {
		/**If file in /sdcard/ does not exist on SD card, create it. */
		if(!logFile.exists())
		{
			try 
			{
				logFile.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
    	
    	if(isClosed) {
    		patsDb.open();
    	}
    	/**Get number of rows in the patient database. */
    	long rowNum = DatabaseUtils.queryNumEntries(patsDb.db, PatientDatabase.DATABASE_TABLE); 	
		
		/**Print header for log file. */
		writeToSdLog("Database of patients with pour information: ", logFile);
    	writeToSdLog(brk, logFile);
    	
    	/**Iterate cursor through rows of the sole table in the database, dumping each line to sd card. */
    	for(int i = 1; i <= rowNum; i++) {
        	StringBuilder sb = new StringBuilder();
        	Cursor c = patsDb.fetchPatient(i);
    		
    		DatabaseUtils.dumpCurrentRow(c, sb);
    		writeToSdLog(sb, logFile);
    	}
    	writeToSdLog(brk, logFile);
    	
    	if(!isClosed) {
    		patsDb.close();
    		isClosed = true;
    	}
    }
    
    /**Writes a StringBuilder to the log file.
     * @param sb a StringBuilder passed to be written to the logfile.
     * @param lf the logFile in /sdcard/ that the StringBuilder is to be written to.
     */
    private void writeToSdLog(StringBuilder sb, File lf) {	
		/**If file in /data/ does not exist on SD card, create it. */
		if(!logFile.exists())
		{
			try 
			{
				logFile.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
    	
		try {
			/**Create a Buffered Writer and FileWriter to write to logFile. */
			BufferedWriter bw = new BufferedWriter(new FileWriter(lf,true));
			
			bw.append(sb);
			bw.newLine();
			bw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
    }
    
    /**Overridden method to allow Strings to be writen to logFile too.
     * @param str a String passed to be written to the logfile.
     * @param lf the logFile in /sdcard/ that the String is to be written to.
     */
    public static void writeToSdLog(String str, File lf) {
		try {
			/**Create a Buffered Writer and FileWriter to write to logFile. */
			BufferedWriter bw = new BufferedWriter(new FileWriter(lf,true));
			
			bw.append(str);
			bw.newLine();
			bw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
    }
    
    /**Get database. */
    public PatientDatabase getDatabase() {
    	return patsDb;
    }
}