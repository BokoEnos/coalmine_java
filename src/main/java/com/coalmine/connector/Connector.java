package com.coalmine.connector;

import com.coalmine.connector.notification.Notification;

/**
 * Responsible for sending notifications to the Coalmine service.
 */
public abstract class Connector {

	static final String DEFAULT_API_URL = "https://coalmineapp.com/notify";
	
	static final int DEFAULT_TIMEOUT = 5000;
	
	protected String url;
	
	protected int timeout;
	
	protected String signature;
	
	protected String applicationEnvironment = "Development";
	
	protected String version = "1.0.0";
	
	public Connector(String signature) {
		this.signature = signature;
		setUrl(DEFAULT_API_URL);
		setTimeout(DEFAULT_TIMEOUT);
	}
	
	public abstract boolean send(Notification notification);
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public void setApplicationEnvironment(String environment) {
		this.applicationEnvironment = environment;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}
