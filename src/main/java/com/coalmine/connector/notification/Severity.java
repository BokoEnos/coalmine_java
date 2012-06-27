package com.coalmine.connector.notification;

public enum Severity {

	/** Critical. App has crashed as a result. */
	ERROR,
	
	/** Failure, but the app is able to move on. */
	WARN,
	
	/** Important info about the app. */
	INFO,
	
	/** Information that will help debug possible future ERROR or WARNs */
	DEBUG,
	
	/** Any other kind of information. */
	TRACE
}
