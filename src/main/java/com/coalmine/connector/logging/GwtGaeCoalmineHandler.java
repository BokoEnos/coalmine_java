package com.coalmine.connector.logging;

import com.coalmine.connector.Connector;
import com.coalmine.connector.notification.GwtLoggedNotification;
import com.coalmine.connector.notification.LoggedNotification;

public class GwtGaeCoalmineHandler extends GaeCoalmineHandler {

	public GwtGaeCoalmineHandler(Connector connector) {
		super(connector);
	}
	
	protected LoggedNotification buildLoggedNotification(Throwable ex) {
		return new GwtLoggedNotification(ex);
	}

}
