package com.brousalis.mtm;

import java.net.*;

/**
 * BetaChecker provides the means to ensure users are runing the latest beta
 * 
 * @author Eric Stokes 10/28/2010
 * 
 */
public class BetaChecker {
	
	/**
	 * Static function that determines wether or not this is the most recent beta version.
	 * 
	 * @param isInBeta Is this version in beta
	 * @param betaCheckUrl The URL of the Beta Server
	 * @return True if the software is up to date, false if it is an old version
	 */
	public static Boolean isUpToDate(Boolean isInBeta, String betaCheckUrl) {
		if (isInBeta) {
			return NetUtils.getHTTPData(betaCheckUrl).equals("up_to_date");
		}
		return true;
	}
	
	/**
	 * Registers the current device with a registration server
	 * 
	 * @param registerUrl The location of the registration URL
	 * @param deviceID The ESN/IMEI/MEID of the device, whatever uniquely identifies it
	 * @param username The name of the user of this device
	 */
	public static void registerUser(String registerUrl, String deviceID, String username, String androidVersion, String network, String brand, String device, String manuf, String version) {
		NetUtils.getHTTPData(registerUrl + deviceID + "&user=" + URLEncoder.encode(username) + "&android=" + URLEncoder.encode(androidVersion) + "&network=" + URLEncoder.encode(network) + "&brand=" + URLEncoder.encode(brand) + "&hardware=" + URLEncoder.encode(device) + "&manuf=" + URLEncoder.encode(manuf) + "&version=" + URLEncoder.encode(version));
		
	}
	
	/**
	 * Check to see if this device is registered or banned
	 * 
	 * @param registerUrl
	 * @param deviceID
	 * @return "registered" if the device is in the database, "banned" if device has been manually removed from the database "not_registered" if the device has not yet registered with the server
	 */
	public static String checkUser(String registerUrl, String deviceID) {
		return NetUtils.getHTTPData(registerUrl + deviceID);
	}
}
