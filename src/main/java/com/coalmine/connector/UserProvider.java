package com.coalmine.connector;

/**
 * Interface to provide access to the user that is currently logged in. Implement
 * based on your authentication scheme. Pass the implementation to the Connector.
 * The Connector will call the getUserId method when a notification is being sent.
 */
public interface UserProvider {

	/**
	 * Fetch the ID of the user that is currently logged in. The ID is of type
	 * String can be anything you choose. For example, this could be the PK ID of
	 * the user, the username, etc. Whatever is returned is what will be displayed
	 * in the Coalmine UI.
	 * 
	 * @return A unique identifier for the user.
	 */
	String getUser();
}
