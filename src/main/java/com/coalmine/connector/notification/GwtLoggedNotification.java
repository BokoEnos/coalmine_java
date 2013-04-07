package com.coalmine.connector.notification;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

public class GwtLoggedNotification extends LoggedNotification {

	protected static final String[] GWT_IGNORE_FILES = {"StackTraceCreator.java", "Exceptions.java"};

	public GwtLoggedNotification(Throwable ex, Gson gson) {
		super(ex, gson);
	}
	
	public GwtLoggedNotification(Throwable ex) {
		super(ex);
	}
	
	public GwtLoggedNotification(Gson gson) {
		super(gson);
	}
	
	public GwtLoggedNotification() {
		super();
	}
	
	@Override
	protected StackTraceElement[] extractStackTraceElements(Throwable ex) {
		List<StackTraceElement> lines = new LinkedList<StackTraceElement>();
		for (StackTraceElement line : super.extractStackTraceElements(ex)) {
			if (!isGwtOverhead(line)) {
				lines.add(line);
			}
		}
		
		return lines.toArray(new StackTraceElement[0]);
	}
	
	protected boolean isGwtOverhead(StackTraceElement line) {
		
		for (String fileName : GWT_IGNORE_FILES) {
			if (fileName.equals(line.getFileName())) {
				return true;
			}
		}
		
		return false;
	}
}
