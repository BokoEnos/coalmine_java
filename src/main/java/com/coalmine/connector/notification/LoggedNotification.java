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

/**
 * An application notification.
 */
public class LoggedNotification extends Notification {

	/** The application environment. E.g., production or staging */
	@SerializedName("app_environment")
	protected String applicationEnvironment;

	/** Free text message */
	protected String message;

	/** Stack trace as a String. */
	@SerializedName("stack_trace")
	protected String stackTrace;

	/** Thread that triggered the notification. */
	@SerializedName("thread_id")
	protected long threadId;

	/** Name of the machine that triggered the notification. */
	protected String hostname;

	/** Name of the class where the error occurred. */
	@SerializedName("class")
	protected String className;

	/** Severity of this notification. */
	protected Severity severity = Severity.ERROR;

	/** A map of server variables. */
	protected Map<String, String> server;

	/**
	 * A map of environment variables. Env is reserved for application state
	 * rather than system properties.
	 */
	protected Map<String, String> environment;

	/** File that generated the notification. */
	protected String file;

	/** The line number of the notification. */
	@SerializedName("line_number")
	protected int lineNumber;

	/** The method that generated the notification. */
	protected String method;

	/** Any HTTP parameters that existed when the notification was triggered. */
	protected String parameters;

	/** Remote IP address (for web apps). */
	protected String ipAddress;

	/** Referring resource (for web apps). */
	protected String referrer;

	/** The user agent of the visitor (for web apps). */
	@SerializedName("user_agent")
	protected String userAgent;

	/** URL of the resource that triggered the notification (for web apps). */
	protected String url;

	/** Responsible for converting self to JSON */
	protected transient Gson gson;

	/**
	 * Construct a Notification from an exception and the GSON implementation.
	 * 
	 * @param ex
	 *            The exception that was thrown
	 * @param gson
	 *            A custom GSON instantiation
	 */
	public LoggedNotification(Throwable ex, Gson gson) {
		this(gson);

		severity = Severity.ERROR;

		if (ex != null) {
			setException(ex);
		}
	}

	/**
	 * Construct a Notification with a thrown exception.
	 * 
	 * @param ex
	 *            The thrown exception
	 */
	public LoggedNotification(Throwable ex) {
		this(ex, new Gson());
	}

	/**
	 * Construct an empty Notification with a custom instantiation of GSON.
	 * 
	 * @param gson
	 *            A custom instantiation of GSON.
	 */
	public LoggedNotification(Gson gson) {
		this.gson = gson;

		threadId = Thread.currentThread().getId();

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// Silently ignore; not required
		}

		captureServer();
	}

	/**
	 * Construct an empty Notification.
	 */
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

	/**
	 * Set the notification's message.
	 * 
	 * @param message
	 *            The message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param method
	 *            The name of the method that triggered the notification
	 */
	public void setMethodName(String method) {
		this.method = method;
	}

	/**
	 * @param className
	 *            The name of the class that triggered the notification.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @param severity
	 *            The severity of the notification
	 */
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	/**
	 * @param environment
	 * 				The environment of the notification
	 */
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}
	
	/**
	 * Set the HTTP request object. This is only used by web apps.
	 * 
	 * @param request
	 *            The current HTTP request
	 */
	@SuppressWarnings("unchecked")
	public void setRequest(ServletRequest request) {

		StringBuilder sb = new StringBuilder();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			for (String value : request.getParameterValues(name)) {
				sb.append(name);
				sb.append("=");
				sb.append(value);
				sb.append("&");
			}
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
			referrer = http.getHeader("Referer");
			userAgent = http.getHeader("User-Agent");
			if (http.getRequestURL() != null) {
				url = http.getRequestURL().toString();
			}
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
			server.put(property.toString(),
					properties.getProperty(property.toString()));
		}
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public void setFile(String file) {
		this.file = file;
	}

	protected void setException(Throwable ex) {
		ex = getExceptionCause(ex);

		message = String.format("[%s] %s", ex.getClass().getSimpleName(), ex.getLocalizedMessage());

		setException(extractStackTraceElements(ex));
	}
	
	protected void setException(StackTraceElement[] lines) {
		if (lines.length > 0) {
			className = lines[0].getClassName();
			file = lines[0].getFileName();
			lineNumber = lines[0].getLineNumber();
			method = lines[0].getMethodName();
		}

		StringBuilder sb = new StringBuilder();
		for (StackTraceElement el : lines) {
			sb.append(el.toString());
			sb.append("\n");
		}

		stackTrace = sb.toString();
	}

	protected Throwable getExceptionCause(Throwable ex) {
		// Unwrap the exception and find the root cause.
		while (ex.getCause() != null) {
			ex = ex.getCause();
		}

		return ex;
	}

	protected StackTraceElement[] extractStackTraceElements(Throwable ex) {
		return ex.getStackTrace();
	}
}
