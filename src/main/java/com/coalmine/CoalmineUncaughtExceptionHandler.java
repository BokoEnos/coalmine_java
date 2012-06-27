package com.coalmine;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalmine.connector.Connector;
import com.coalmine.connector.notification.LoggedNotification;
import com.google.gson.Gson;

/**
 * Automatically send uncaught exceptions to Coalmine.
 */
public class CoalmineUncaughtExceptionHandler implements UncaughtExceptionHandler {

	/** The entity responsible for sending the notification. */
	protected Connector connector;
	
	/** Serialization library that makes our life easier. */
	protected Gson gson;
	
	/** Internal logger. */
	private static final Logger LOG = LoggerFactory.getLogger(CoalmineUncaughtExceptionHandler.class);
	
	/**
	 * Construct the UncaughtExceptionHandler with a Connector.
	 * 
	 * @param connector The connector responsible for sending notifications.
	 */
	public CoalmineUncaughtExceptionHandler(Connector connector) {
		this(connector, new Gson());
	}
	
	/**
	 * Construct with a Connector and Gson.
	 * 
	 * @param connector The connector responsible for sending notifications
	 * @param gson The GSON instantiation.
	 */
	public CoalmineUncaughtExceptionHandler(Connector connector, Gson gson) {
		this.connector = connector;
		this.gson = gson;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		LOG.error("Uncaught exception! This will be logged in Coalmine", ex);		
		connector.send(new LoggedNotification(ex, gson));
	}
}
