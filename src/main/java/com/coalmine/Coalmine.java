package com.coalmine;

public class Coalmine {

	protected static String signature;
	
	protected static String environment;
	
	protected static String version;
	
	public static final void setSignature(String signature) {
		Coalmine.signature = signature;
	}
	
	public static final String getSignature() {
		return signature;
	}
	
	public static final void setEnvironment(String environment) {
		Coalmine.environment = environment;
	}
	
	public static final String getEnvironment() {
		return environment;
	}
	
	public static final void setVersion(String version) {
		Coalmine.version = version;
	}
	
	public static final String getVersion() {
		return version;
	}
}
