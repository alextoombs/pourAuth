package com.atoombs.pourauth;

import android.text.format.DateFormat;
import android.util.Log;

/**Patient class that stores each important field.
 * 
 * @author Alex Toombs
 * @since July 26th, 2012
 */
public class Patient {
	private double amt1;
	private double amt2;
	private String auth;
	private String timestamp;
	
	/**Default constructor
	 * 
	 * @param amt_1 amount of medicine 1 to pour
	 * @param amt_2 amount of medicine 2 to pour
	 * @param auth_user user authorized to pour for this patient
	 */
	public Patient(double amt_1, double amt_2, String auth_user) {
		// TODO Auto-generated constructor stub
		amt1 = amt_1;
		amt2 = amt_2;
		auth = auth_user;
		
		/**Create timestamp from current date, format to enter to db. */
		CharSequence dfCh = DateFormat.format("EEEE, MMMM dd, yyyy h:mm:ssaa" , new java.util.Date());
		String dateString = dfCh.toString();	
		
		timestamp = dateString;
		
		Log.i("Patient", "amt1: " + amt1 + ", amt2: " + amt2 + ", auth: " + auth);
	}
	
	public void updateTimestamp() {
		/**Create timestamp from current date, format to enter to db. */
		CharSequence dfCh = DateFormat.format("EEEE, MMMM dd, yyyy h:mm:ssaa" , new java.util.Date());
		String dateString = dfCh.toString();	
		
		timestamp = dateString;
	}
	
	//updates amt1
	public void updateAmt1(double amt_1) {
		amt1 = amt_1;
	}
	
	//updates amt2
	public void updateAmt2(double amt_2) {
		amt2 = amt_2;
	}
	
	//updates auth
	public void updateAuth(String newauth) {
		auth = newauth;
	}
	
	//accessor
	public double getAmt1() {
		return amt1;
	}
	
	//accessor
	public double getAmt2() {
		return amt2;
	}
	
	//accessor
	public String getAuth() {
		return auth;
	}
	
	//accessor
	public String getLastPour() {
		return timestamp;
	}

	/**Get this instance of Patient
	 * 
	 * @return this patient
	 */
	public Patient getPatient() {
		return this;
	}
}
