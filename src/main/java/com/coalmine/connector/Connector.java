package com.coalmine.connector;

import java.util.HashSet;
import java.util.Set;

import com.coalmine.connector.notification.Notification;

/**
 * Responsible for sending notifications to the Coalmine service.
 */
public abstract class Connector {

	static final String DEFAULT_API_URL = "https://coalmineapp.com/notify";
	
	@SuppressWarnings("serial")
	static final Set<String> DEFAULT_ENABLED_ENVIRONMENTS = new HashSet<String>(){{
		add("production");
		add("staging");		
	}};
	
	static final int DEFAULT_TIMEOUT = 5000;
	
	protected String url;
	
	protected int timeout;
	
	protected String signature;
	
	protected String applicationEnvironment = "Production";
	
	protected String version = "1.0.0";
	
	protected Set<String> enabledEnvironments;
	
	protected UserProvider userProvider;
	
	public Connector(String signature) {		
		this(signature, DEFAULT_ENABLED_ENVIRONMENTS);
	}
	
	public Connector(String signature, Set<String> enabledEnvironments) {
		this.signature = signature;
		this.enabledEnvironments = enabledEnvironments;
		
		setUrl(DEFAULT_API_URL);
		setTimeout(DEFAULT_TIMEOUT);
	}
	
	public abstract void start();
	public abstract void stop();
	
	public abstract boolean send(Notification notification);
	
	public void addEnabledEnvironment(String env) {
		if (env == null || env.isEmpty()) {
			throw new IllegalArgumentException("Invalid environment");
		}
		
		enabledEnvironments.add(env.toLowerCase());
	}
	
	public void setUserProvider(UserProvider userProvider) {
		this.userProvider = userProvider;
	}
	
	public UserProvider getUserProvider() {
		return userProvider;
	}
	
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
	
	protected boolean isSendable(Notification notification) {
		return enabledEnvironments.contains(applicationEnvironment.toLowerCase());
	}
}
