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

public class SimpleConnector extends Connector {
	
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleConnector.class);
	
	public SimpleConnector(String signature) {
		super(signature);
	}
	
	@Override
	public boolean send(Notification notification) {
		
		if (!isSendable(notification)) {
			LOG.warn(
				String.format("Coalmine notifications are disabled for the current environment (%s). Enable this environment via connector.addEnabledEnvironment(\"%s\");"), applicationEnvironment, applicationEnvironment);
			return false;
		}
		
		try {
			return _send(notification);
		} catch (RuntimeException e) {
			LOG.error("Unable to send notification to Coalmine.", e);
			return false;
		}
	}
	
	private boolean _send(Notification notification) {
		notification.setApplicationEnvironment(applicationEnvironment);
		notification.setVersion(version);
		
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
}
