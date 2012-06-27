package com.coalmine.connector.logging;

import java.util.logging.Level;

import com.coalmine.connector.Connector;

/**
 * GAE log Handlers must manually manage the level to prevent SecurityException
 * from being thrown.
 */
public class GaeCoalmineHandler extends CoalmineHandler {

	/** The minimum acceptable level to send to Coalmine. */
	protected Level level;
	
	/**
	 * Constructor
	 */
	public GaeCoalmineHandler(Connector connector) {
		super(connector);
		level = Level.WARNING;
	}
	
	/**
	 * We override to avoid security conflicts on Google App Engine.
	 */
	@Override
	public void setLevel(Level level) {
		this.level = level;
	}
	
	/**
	 * We override to avoid security conflicts on Google App Engine.
	 */
	@Override
	public Level getLevel() {
		return level;
	}
}
