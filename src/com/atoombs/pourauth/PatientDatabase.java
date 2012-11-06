package com.atoombs.pourauth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

/**Class to create a database that stores patient pour information.
 * @author Alex Toombs
 * @since July 25th, 2012
 * 
 * Written with help from Google's Notepad database tutorial, Paul Berman's UA code, 
 * 		www.vogella.com, and 'Programming Android' by Mednieks et al
 */
public class PatientDatabase {
	public static final String KEY_ROWID = "_id";
	public static final String AUTH = "authorized_user";
	public static final String LAST_POUR = "last_pour";
	public static final String AMT_1 = "amount_one";
	public static final String AMT_2 = "amount_two";
	
	
	public static final int ID_COL = 0;
	public static final int AUTH_COL = 1;
	public static final int PAT_COL = 2;
	public static final int TIME_COL = 3;
	public static final int AMT_1_COL = 4;
	public static final int AMT_2__COL = 5;
	
	private static final String TAG = "PatientDatasbase";
    private DatabaseHelper dbHelper;
    public SQLiteDatabase db;
	
	private static final String DATABASE_NAME = "patients.db";
	public static final String DATABASE_TABLE = "pats";
	private static final int DATABASE_VERSION = 1;
	
	/**Database creation SQL statement. */
	private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " (" 
						+ KEY_ROWID + " INTEGER PRIMARY KEY ASC, " 
						+ AUTH  + " TEXT, " 
						+ LAST_POUR + " TEXT, "
						+ AMT_1+ " TEXT, "
						+ AMT_2 + " TEXT);";
	
	private final Context mCtx;
	
    public static class DatabaseHelper extends SQLiteOpenHelper {
    	public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
    		Log.i(TAG, "Constructing the DatabaseHelper...");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            Log.i(TAG, "DatabaseHelper onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS patients");
            //update later
            onCreate(db);
        }
    }

    /**Constructor which takes the context to allow the database to be created.
     * @param ctx the Context within which to work
     * */
    public PatientDatabase(Context ctx) {
    	this.mCtx = ctx;
    	Log.i(TAG, "Constructing the database...");
    }
    
    /**Attempt to open the database.  If it can't be created,
     * throws an SQL exception.
     * @return AppsInfoDb the database you created
     * @throws SQLException in case the database cannot be created
     */
    public PatientDatabase open() throws SQLException {
    	Log.i(TAG, "Opening the database...");
        dbHelper = new DatabaseHelper(mCtx);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    /**Close the dbHelper class that assisted in creating the database. */
    public void close() {
    	Log.i(TAG, "Closing the database...");
        dbHelper.close();
    }
    
    /**Create a new patient entry.
     * 
     * @param path auth authenticated user that may pour these medications
     * @param amt1 amount of medicine 1 to pour
     * @param amt2 amount of medicine 2 to pour
     * @return row id or -1 for failure
     */
    public long enterPatient(String auth, String amt1, String amt2) {
    	Log.i(TAG, "Enter a new database element in the database class...");
    	ContentValues args = new ContentValues();
        args.put(AUTH, auth);
        args.put(AMT_1, amt1);
        args.put(AMT_2, amt2);
        
		/**Create timestamp from current date, format to enter to db. */
		CharSequence dfCh = DateFormat.format("EEEE, MMMM dd, yyyy h:mm:ssaa" , new java.util.Date());
		String dateString = dfCh.toString();		
		args.put(LAST_POUR, dateString);
        
    	Log.i(TAG, "Return the row ID of the new element");
    	long rowid = db.insert(DATABASE_TABLE, null, args);
    	Log.i(TAG, "Row id is: " + rowid);
    	return rowid;
    }
    
    /**Deletes a patient that had been entered into the database (deletes whole row)
     * @param row id of deleted patient
     * @return true if deleted, false otherwise
     */
    public boolean deletePatient(long rowId) {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**Query the database and retrieve all patients in it.
     * 
     * @return Cursor over all of the patients in the database
     */
    public Cursor retrieveAll() {
    	Log.i(TAG, "Retrieve all patients in the database...");
    	return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, AUTH, LAST_POUR, AMT_1, 
    						AMT_2}, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor at the patient with the entered row id
     * 
     * @param rowId row id of patient to retrieve
     * @return Cursor points to patient to be retrieved if it is found, could return null if nothing found
     * @throws SQLException thrown if patient could not be found/retrieved
     */
    public Cursor fetchPatient(long rowId) throws SQLException {
    	Log.i(TAG, "Fetch a certain patient in the database...");
        Cursor mCursor =
            db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, AUTH, LAST_POUR, AMT_1, 
					AMT_2}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        else {
        	Log.e(TAG, "Cursor is null");
        }
        return mCursor;
    }

    /**
     * Update the patient with the parameters supplied.  patient is specified using the row id.
     * 
     * @param rowId row id of patient to update
     * @param path auth authenticated user that may pour these medications
     * @param amt1 amount of medicine 1 to pour
     * @param amt2 amount of medicine 2 to pour
     * @return true if the patient was successfully updated, false otherwise
     */
    public boolean updatePatient(long rowId, String auth, String amt1, String amt2) {
    	Log.i(TAG, "Attempting to update an app");
        ContentValues args = new ContentValues();
        args.put(AUTH, auth);
        args.put(AMT_1, amt1);
        args.put(AMT_2, amt2);

        boolean wasSuccessful = db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        if(wasSuccessful) {
        	Log.i(TAG, "Updating the patient was successful");
        }
        else {
        	Log.i(TAG, "Updating the patient was not successful");
        }
        return wasSuccessful;
    }
    
    /**Updates when the patient was last poured medicine.
     * 
     * @param rowId id of patient to update
     * @return whether or not the update was successful
     */
    public boolean updateLastPour(long rowId) {
    	ContentValues args = new ContentValues();
    	
		/**Create timestamp from current date, format to enter to db. */
		CharSequence dfCh = DateFormat.format("EEEE, MMMM dd, yyyy h:mm:ssaa" , new java.util.Date());
		String dateString = dfCh.toString();
		
		args.put(LAST_POUR, dateString);
		
        boolean wasSuccessful = db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
        if(wasSuccessful) {
        	Log.i(TAG, "Updating the patient was successful");
        }
        else {
        	Log.i(TAG, "Updating the patient was not successful");
        }
        return wasSuccessful;
    }
}