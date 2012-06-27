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

public class CoalmineFilter implements Filter {
	
	protected Connector connector;
	
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
	
	public void setVersion(String version) {
		connector.setVersion(version);
		Coalmine.setVersion(version);
	}
	
	public void setEnvironment(String environment) {
		connector.setApplicationEnvironment(environment);
		Coalmine.setEnvironment(environment);
	}
}
