package com.coalmine.connector;

import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coalmine.connector.notification.Notification;

/**
 * Non Blocking implementation of the Connector which extends the SimpleConnector. The implementation has a fixed notification capacity of 128 by default. 
 * When the capacity is reached it will drop all exceeding notifications
 * 
 */
public class NoneBlockingConnector extends SimpleConnector {
	
	private static final int DEFAULT_QUEUE_CAPACITY = 128;
	private static final Logger LOG = LoggerFactory.getLogger(NoneBlockingConnector.class);
	
	private final ArrayBlockingQueue<Notification> notificationQueue;
	private final NoneBlockingConnectorThread noneBlockingConnectorThread;
	
	public NoneBlockingConnector(String signature) {
		this(signature, DEFAULT_QUEUE_CAPACITY);
	}
	
	public NoneBlockingConnector(String signature, Integer queueCapacity) {
		super(signature);
		notificationQueue = new ArrayBlockingQueue<Notification>(queueCapacity);
		noneBlockingConnectorThread = new NoneBlockingConnectorThread();
	}
	
	@Override
	public boolean send(Notification notification) {
		try {
			return notificationQueue.add(notification);			
		} catch(IllegalStateException e) {
			LOG.warn("Notification queue is overflowing, skipping incoming notification (Increase queue capacity to track all notifications)");
			return false;
		}
	}
		
	@Override
	public void start() {
		noneBlockingConnectorThread.start();
	}
	
	@Override
	public void stop() {
		noneBlockingConnectorThread.interrupt();
	}
	
	private boolean sendInternal(Notification notification) {
		return super.send(notification);
	}
	
	private class NoneBlockingConnectorThread extends Thread {
		
		private boolean active = true;
		
		@Override
		public void run() {
			while(active) {
				try {
					Notification notification = notificationQueue.take();
					
			        try {
			        	sendInternal(notification);		            
			        } catch (Exception e) {
			            // ignore
			        }
					
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		
		public void interrupt() {
			active = false;
		}
	}
}
