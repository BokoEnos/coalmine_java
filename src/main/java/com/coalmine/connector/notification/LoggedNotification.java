package com.coalmine.connector.notification;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class LoggedNotification extends Notification {
	
	@SerializedName("app_environment")
	protected String applicationEnvironment;
	
	protected String message;
	
	@SerializedName("stack_trace")
	protected String stackTrace;
	
	@SerializedName("thread_id")
	protected long threadId;

	protected String hostname;
	
	/** Name of the class where the error occurred. */
	@SerializedName("class")
	protected String className;
	
	protected Severity severity = Severity.ERROR;
	
	protected Map<String, String> server;
	
	protected Map<String, String> environment;
	
	protected String file;
	
	@SerializedName("line_number")
	protected int lineNumber;
	
	protected String method;
	
	protected String parameters;
	
	protected String ipAddress;
	
	protected String referrer;
	
	@SerializedName("user_agent")
	protected String userAgent;
	
	protected String url;
	
	protected transient Gson gson;
		
	public LoggedNotification(Throwable ex, Gson gson) {
		this(gson);
		
		severity = Severity.ERROR;
		
		if (ex != null) {
			message  = String.format("[%s] %s", ex.getClass().getSimpleName(), ex.getLocalizedMessage());
					
			StackTraceElement[] lines = ex.getStackTrace();
			if (lines.length > 0) {
				className  = lines[0].getClassName();
				file       = lines[0].getFileName();
				lineNumber = lines[0].getLineNumber();
				method     = lines[0].getMethodName();
			}
			
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement el : lines) {
				sb.append(el.toString());
				sb.append("\n");
			}
			
			stackTrace = sb.toString();
		}
	}
	
	public LoggedNotification(Throwable ex) {
		this(ex, new Gson());
	}
	
	public LoggedNotification(Gson gson) {
		this.gson = gson;
		
		threadId = Thread.currentThread().getId();
		
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// Silently ignore; not required
		}
		
		captureServer();
		
		// TODO: Capture the process ID
	}
	
	public LoggedNotification() {
		this(new Gson()); 
	}
	
	@Override
	public Map<String, Object> getQueryParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("json", gson.toJson(this));
		return params;
	}
	
	@Override
	public void setApplicationEnvironment(String applicationEnvironment) {
		this.applicationEnvironment = applicationEnvironment;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setMethodName(String method) {
		this.method = method;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	@SuppressWarnings("unchecked")
	public void setRequest(ServletRequest request) {
		
		StringBuilder sb = new StringBuilder();
		Map<String, Object> params = request.getParameterMap();
		for (String key : params.keySet()) {
			sb.append(key);
			sb.append("=");
			sb.append(params.get(key));
		}
		
		parameters = sb.toString();
		if (parameters != null && parameters.length() > 1) {
			parameters = parameters.substring(0, parameters.length() - 1);
		}
		
		ipAddress = request.getRemoteAddr();
		
		environment = new HashMap<String, String>();
		environment.put("Character Encoding", request.getCharacterEncoding());
		environment.put("Content Type", request.getContentType());
		environment.put("Content Length", request.getContentLength() + "");
		environment.put("Local Address", request.getLocalAddr());
		environment.put("Local Name", request.getLocalName());
		environment.put("Local Port", request.getLocalPort() + "");
		environment.put("Protocol", request.getProtocol());
		environment.put("Remote Address", request.getRemoteAddr());
		environment.put("Remote Host", request.getRemoteHost());
		environment.put("Scheme", request.getScheme());
		environment.put("Server Name", request.getServerName());
		if (request.getLocale() != null) {
			environment.put("Locale", request.getLocale().toString());
		}
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest http = (HttpServletRequest) request;
			referrer  = http.getHeader("referer");
			userAgent = http.getHeader("User-Agent");
			url = http.getRequestURI();
			environment.put("Query String", http.getQueryString());
			environment.put("HTTP Method", http.getMethod());
			environment.put("Auth Type", http.getAuthType());

			Enumeration<String> headers = http.getHeaderNames();
			String name;
			while (headers.hasMoreElements()) {
				name = headers.nextElement();
				environment.put(name + " Header", http.getHeader(name));
			}
		}
	}
	
	protected void captureServer() {
		server = new HashMap<String, String>();
		Properties properties = System.getProperties();
		for (Object property : properties.keySet()) {
			server.put(property.toString(), properties.getProperty(property.toString()));
		}
	}
}
