package com.coalmine.connector.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

import com.coalmine.Coalmine;
import com.coalmine.connector.Connector;
import com.coalmine.connector.NoneBlockingConnector;
import com.coalmine.connector.SimpleConnector;
import com.coalmine.connector.notification.LoggedNotification;
import com.coalmine.connector.notification.Severity;

public class CoalmineAppender extends AppenderBase<ILoggingEvent> {

	/**
	 * Parameters
	 */
	private String signature;
	private String environment;
	private String version;
	
	private Level level = Level.ERROR;
	private boolean async = true;
	
	private Connector connector;
	
	@Override
	protected void append(ILoggingEvent event) {
		
		// skip lower levels
		if (!event.getLevel().isGreaterOrEqual(level)) {
			return;
		}
		
		// Avoid sending messages about ourself. This prevents infinite loops.
		if (event.getLoggerName().contains("com.coalmine")) {
			return;
		}
		
		LoggedNotification notification = generateBaseNotification(event);
		
		notification.setSeverity(getSeverity(event.getLevel()));
		notification.setMessage(event.getMessage());
		
		notification.setEnvironment(event.getMDCPropertyMap());
		
		connector.send(notification);
	}
	
	@Override
	public void start() {		

		// fallback on Coalmine static config when local config is unavailable
		if(async) {
			this.connector = new NoneBlockingConnector(signature == null ? Coalmine.getSignature() : signature);
		} else {			
			this.connector = new SimpleConnector(signature == null ? Coalmine.getSignature() : signature);
		}
		
		this.connector.setApplicationEnvironment(environment == null ? Coalmine.getEnvironment() : environment);
		this.connector.setVersion(version == null ? Coalmine.getVersion() : version);
		this.connector.start();		
		
		super.start();
	}
	
	@Override
	public void stop() {
		this.connector.stop();
		super.stop();
	}
	
	protected LoggedNotification generateBaseNotification(ILoggingEvent event) {
		
		LoggedNotification notification = new LoggedNotification();
		
		// no information known
		if(event.getThrowableProxy() == null) {
			return notification;
		}
		
		StackTraceElementProxy[] lines = event.getThrowableProxy().getStackTraceElementProxyArray();
		
		if(lines != null && lines.length > 0) {
			notification.setClassName(lines[0].getStackTraceElement().getClassName());
			notification.setFile(lines[0].getStackTraceElement().getFileName());
			notification.setLineNumber(lines[0].getStackTraceElement().getLineNumber());
			notification.setMethodName(lines[0].getStackTraceElement().getMethodName());
		}

		StringBuilder sb = new StringBuilder();
		for (StackTraceElementProxy el : lines) {
			sb.append(el.toString());
			sb.append("\n");
		}

		notification.setStackTrace(sb.toString());
		
		return notification;
	}
	
	protected Severity getSeverity(Level level) {
		if (level == Level.WARN) {
			return Severity.WARN;
		} else if (level == Level.INFO) {
			return Severity.INFO;
		} else if (level == Level.DEBUG || level == Level.TRACE) {
			return Severity.DEBUG;
		}
		
		return Severity.ERROR;
	}
	
	public void setAsync(boolean async) {
		this.async = async;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
}
