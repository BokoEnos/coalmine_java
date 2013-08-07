package com.coalmine.connector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalmine.connector.notification.Notification;

/**
 * The simplest implementation of a Coalmine Connector. This is the class 
 * responsible for sending messages to the Coalmine API.
 * 
 * This connector honors rate throttling on the client side so that when a 
 * throttle event occurs, the client (this connector) will not send another 
 * message for the throttle period (value of Retry-After response header).
 */
public class SimpleConnector extends Connector {
	
	/** The system time that we were last throttled. */
	private Long lastThrottled;
	
	/**
	 * The number of seconds to wait (from lastThrottled) before sending another
	 * message to Coalmine. 
	 */
	private int throttleTimeout;
	
	/** Content type of the HTTP request. */
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
	
	/** Class level logger. */
	private static final Logger LOG = LoggerFactory.getLogger(SimpleConnector.class);

	/**
	 * The default number of seconds we will wait, if throttled, before 
	 * sending another. It is expected that a throttle response will include
	 * a Retry-After header with the number of seconds to wait before sending
	 * another message. If this is not set, or cannot be parsed, this value will
	 * be used.
	 */
	private static final int DEFAULT_THROTTLE_TIMEOUT = 60;
	
	public SimpleConnector(String signature) {
		super(signature);
	}
	
	@Override
	public void start() {
		
	}
	
	@Override
	public void stop() {
		
	}
	
	@Override
	public boolean send(Notification notification) {
		
		if (!isSendable(notification)) {
			LOG.warn(
				String.format("Coalmine notifications are disabled for the current environment (%s). Enable this environment via connector.addEnabledEnvironment(\"%s\");", 
						applicationEnvironment, applicationEnvironment));
			return false;
		}
		
		try {
			return _send(notification);
		} catch (RuntimeException e) {
			LOG.error("Unable to send notification to Coalmine.", e);
			return false;
		}
	}
	
	protected boolean isThrottled() {
		if (lastThrottled == null) {
			return false;
		}
		
		long diff = System.currentTimeMillis() - lastThrottled;
		if (diff > throttleTimeout * 1000) {
			lastThrottled = null;
			return false;
		}
		
		return true;
	}
	
	private boolean _send(Notification notification) {
		
		if (isThrottled()) {
			logThrottled();
			return false;
		}
		
		notification.setApplicationEnvironment(applicationEnvironment);
		notification.setVersion(version);
		if (getUserProvider() != null) {
			notification.setUserId(getUserProvider().getUser());
		}
		
		URL url = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(getUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type", CONTENT_TYPE);
			conn.setUseCaches(false);
			conn.setDoOutput(true); // Implicitly sets conn to POST
			conn.setConnectTimeout(timeout);
			
			Map<String, Object> params = notification.getQueryParams();
			params.put("signature", signature);
			StringBuilder queryBuilder = new StringBuilder();
			for (String key : params.keySet()) {
				queryBuilder.append(URLEncoder.encode(key, "UTF-8"));
				queryBuilder.append("=");
				if (params.get(key) != null) {
					queryBuilder.append(URLEncoder.encode(params.get(key).toString(), "UTF-8"));
				}
				queryBuilder.append("&");
			}
			
			String query = queryBuilder.toString();
			query = query.substring(0, query.length() - 1);
			
			LOG.debug(String.format("Sending %s to %s", query, getUrl()));
			DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
			writer.writeBytes(query);
			writer.flush ();
			writer.close ();
			
			if (conn.getResponseCode() == 200) {
				LOG.info("Successfully posted notification to Coalmine");
				return true;
			} else if (conn.getResponseCode() == 429) {
				logThrottled();				
				setTemporaryTimeout(conn.getHeaderField("Retry-After"));
				return false;
			}
			
			LOG.warn("Unable to communicate with the Coalmine server.");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		 
			br.close();
			LOG.warn("Response from Coalmine was: " + sb.toString());
		} catch (IOException e) {
			LOG.error("Unable to send notification to Coalmine", e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return false;
	}
	
	/**
	 * Set the temporary timeout, but take a string as a param. Convenience for
	 * accepting values directly from response headers.
	 * 
	 * @param strTimeoutSeconds
	 */
	protected void setTemporaryTimeout(String strTimeoutSeconds) {
		if (strTimeoutSeconds == null || strTimeoutSeconds.isEmpty()) {
			setTemporaryTimeout(DEFAULT_THROTTLE_TIMEOUT);
			return;
		}
		
		try {
			setTemporaryTimeout(Integer.parseInt(strTimeoutSeconds));
		} catch (NumberFormatException e) {
			LOG.warn("Unable to parse retry-after header value ({}) from throttled Coalmine response", strTimeoutSeconds);
			setTemporaryTimeout(DEFAULT_THROTTLE_TIMEOUT);
		}
	}
	
	/**
	 * Set the number of seconds to wait before sending another message to 
	 * Coalmine.
	 * 
	 * @param strTimeoutSeconds The number of seconds to wait
	 */
	protected void setTemporaryTimeout(int timeout) {
		lastThrottled = System.currentTimeMillis();
		throttleTimeout = timeout;
	}
	
	private void logThrottled() {
		LOG.warn("Application is being throttled by Coalmine. Notification will not be sent.");
	}
}
