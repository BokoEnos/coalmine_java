package com.coalmine.connector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.coalmine.connector.notification.LoggedNotification;
import com.coalmine.connector.notification.Notification;

public class SimpleConnectorTest {

	protected Connector connector;
	
	@Before
	public void setUp() {
		connector = new SimpleConnector("AtestSignature");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddEnabledEnvironmentWithNullEnv() {
		connector.addEnabledEnvironment(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddEnabledEnvironmentWithEmptyString() {
		connector.addEnabledEnvironment("");
	}
	
	@Test
	public void testIsSendableWhenSendable() {
		Notification notification = new LoggedNotification();
		assertTrue(connector.isSendable(notification));
	}
	
	@Test
	public void testIsSendableWhenNotSendable() {
		connector.setApplicationEnvironment("development");
		assertFalse(connector.isSendable(new LoggedNotification()));
	}
	
	@Test
	public void testIsSendableIsNotCaseSensitive() {
		connector.setApplicationEnvironment("PrOductioN");
		assertTrue(connector.isSendable(new LoggedNotification()));
	}
}
