package com.coalmine.connector.notification;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Base Notification for sending information to Coalmine. Notifications come in
 * two major forms: LoggedNotification (application event notifications) and
 * Version notifications (e.g., my app was just deployed).
 */
public abstract class Notification {

	/** Name of the application environment (e.g., production, staging, etc). */
	protected String applicationEnvironment;
	
	/** Application version. Optional. Example: 1.0.0 */
	protected String version;
	
	@SerializedName("user_id")
	protected String userId;
	
	/**
	 * Get the fields to send to Coalmine.
	 * 
	 * @return A map of fields to values to send to Coalmine
	 */
	public abstract Map<String, Object> getQueryParams();

	/**
	 * Set the application environment
	 * 
	 * @param applicationEnvironment The application environment
	 */
	public void setApplicationEnvironment(String applicationEnvironment) {
		this.applicationEnvironment = applicationEnvironment;
	}
	
	/**
	 * Set the application version
	 * 
	 * @param version The application version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
