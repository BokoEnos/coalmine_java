package com.coalmine.connector.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalmine.Coalmine;
import com.coalmine.connector.Connector;
import com.coalmine.connector.SimpleConnector;
import com.coalmine.connector.notification.LoggedNotification;
import com.coalmine.connector.notification.Severity;

/**
 * A java.util.logging Handler to send log messages to Coalmine. By default,
 * only WARNING and higher are sent. Use setLevel() to change the minimum level.
 */
public class CoalmineHandler extends Handler {

	/** The entity responsible for sending notifications to Coalmine. */
	protected Connector connector;
	
	private static final Logger LOG = LoggerFactory.getLogger(CoalmineHandler.class);
	
	/**
	 * Construct a handler with a Connector. Default level to WARNING and above.
	 * 
	 * @param connector The Connector to send Coalmine notifications to
	 */
	public CoalmineHandler(Connector connector) {
		this.connector = connector;
		
		// Default to only accepting WARNING or above.
		try {
			setLevel(Level.WARNING);
		} catch (SecurityException e) {
			LOG.error("Unable to set severity level", e);
		}
	}
	
	/**
	 * Default constructor so that you can declare this handler in your logging.properties.
	 * 
	 * For example:
	 * 
	 * 		# Set some default levels
	 * 		com.company.level = ALL
	 *		.level = WARNING
	 *
	 *		# Use the default JUL handler and the CoalmineHandler
	 *		handlers = java.util.logging.ConsoleHandler,com.coalmine.connector.logging.CoalmineHandler
	 *
	 */
	public CoalmineHandler() {
		this(new SimpleConnector(Coalmine.getSignature()));
		connector.setApplicationEnvironment(Coalmine.getEnvironment());
		connector.setVersion(Coalmine.getVersion());
	}

	@Override
	public void flush() {
		// TODO Queue up messages to send to Coalmine
	}

	@Override
	public void publish(LogRecord record) {
		
		if (getLevel().intValue() > record.getLevel().intValue()) {
			return;
		}
		
		// Avoid sending messages about ourself. This prevents infinite loops.
		if (record.getLoggerName().contains("com.coalmine")) {
			return;
		}
		
		// TODO: Queue this up for later sending instead.
		
		LoggedNotification notification = buildLoggedNotification(record.getThrown());
		notification.setSeverity(getSeverity(record.getLevel()));
		notification.setMessage(record.getMessage());
		notification.setMethodName(record.getSourceMethodName());
		notification.setClassName(record.getSourceClassName());
		notification.setThreadId(record.getThreadID());
		connector.send(notification);
	}
	
	@Override
	public void close() throws SecurityException {
		// Do nothing
	}
	
	protected LoggedNotification buildLoggedNotification(Throwable ex) {
		return new LoggedNotification(ex);
	}
	
	protected Severity getSeverity(Level level) {
		if (level == Level.WARNING) {
			return Severity.WARN;
		} else if (level == Level.INFO) {
			return Severity.INFO;
		} else if (level == Level.FINE || level == Level.FINER || level == Level.FINEST) {
			return Severity.DEBUG;
		}
		
		return Severity.ERROR;
	}
}
