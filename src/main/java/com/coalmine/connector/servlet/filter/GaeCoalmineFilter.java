package com.coalmine.connector.servlet.filter;

import java.util.logging.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.coalmine.connector.logging.GaeCoalmineHandler;

/**
 * A CoalmineFilter which automatically collects the environment and version from
 * the GAE environment variables.
 * 
 * This filter also adds a JUL Handler since GAE uses JUL. Any messages logged as
 * WARNING or SEVERE are automatically sent to Coalmine. You can disable this behavior by
 * adding the below to your web.xml filter.
 * 
 * <init-param>
 * 		<init-name>jul-handler</init-name>
 * 		<init-value>false</init-value>
 * </init-param>
 */
public class GaeCoalmineFilter extends CoalmineFilter {

	/** Class member just so that the log does not get garbage collected. */
	protected Logger _log;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);
		
		String environment = config.getInitParameter("environment");
		if (environment == null || environment.isEmpty()) {
			environment = System.getProperty("com.google.appengine.runtime.environment");
			setEnvironment(environment);
		}
		
		String version = config.getInitParameter("version");
		if (version == null || version.isEmpty()) {
			version = System.getProperty("com.google.appengine.application.version");
			setVersion(version);
		}
		
		// GAE uses JUL so we provide an easy way to hook into those log messages
		// By default we always do this, but allow the client to disable with a config param
		if (!"false".equals(config.getInitParameter("jul-handler"))) {
			_log = Logger.getLogger("");
			_log.addHandler(new GaeCoalmineHandler(connector));
		}
	}
}
