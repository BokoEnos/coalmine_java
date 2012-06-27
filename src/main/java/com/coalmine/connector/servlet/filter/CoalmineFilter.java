package com.coalmine.connector.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.coalmine.Coalmine;
import com.coalmine.connector.Connector;
import com.coalmine.connector.SimpleConnector;
import com.coalmine.connector.notification.LoggedNotification;

/**
 * A servlet filter that allows for easy integration of Coalmine by only using
 * a web.xml file. For example:
 * 
 * 		<filter>
 *			<filter-name>coalmine</filter-name>
 *			<filter-class>com.coalmine.connector.servlet.filter.CoalmineFilter</filter-class>
 *			<!-- Required: The signature assigned to this application by Coalmine. -->
 *			<init-param>
 *       		<param-name>signature</param-name>
 *       		<param-value>MY_COALMINE_SIGNATURE</param-value>
 *   		</init-param>
 *   		<!-- Optional: The environment of the application. Defaults to "Production" -->
 *   		<init-param>
 *       		<param-name>environment</param-name>
 *       		<param-value>Production</param-value>
 *   		</init-param>
 *   		<!-- Optional: The version of this application. Defaults to "1.0.0" -->
 *   		<init-param>
 *       		<param-name>version</param-name>
 *       		<param-value>1.0.0</param-value>
 *   		</init-param>
 *		</filter>
 *		<filter-mapping>
 *			<filter-name>coalmine</filter-name>
 *			<url-pattern>/*</url-pattern>
 *		</filter-mapping>
 */
public class CoalmineFilter implements Filter {
	
	/** The Coalmine connector responsible for communicating with the Coalmine service. */
	protected Connector connector;
	
	/**
	 * Init the Filter. Read configuration from web.xml
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		connector = new SimpleConnector(config.getInitParameter("signature"));
		Coalmine.setSignature(config.getInitParameter("signature"));
		
		String environment = config.getInitParameter("environment");
		if (environment != null && !environment.isEmpty()) {
			setEnvironment(environment);
		}
		
		String version = config.getInitParameter("version");
		if (version != null && !version.isEmpty()) {
			setVersion(version);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		try {
			chain.doFilter(request, response);
		} catch (RuntimeException ex) {
			LoggedNotification notification = new LoggedNotification(ex);
			notification.setRequest(request);
			connector.send(notification);
			throw ex;
		}
	}

	@Override
	public void destroy() {
		// Do nothing
	}
	
	/**
	 * @param version The version of the application
	 */
	public void setVersion(String version) {
		connector.setVersion(version);
		Coalmine.setVersion(version);
	}
	
	/**
	 * @param environment The application environment
	 */
	public void setEnvironment(String environment) {
		connector.setApplicationEnvironment(environment);
		Coalmine.setEnvironment(environment);
	}
}
