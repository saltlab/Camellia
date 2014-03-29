package com.dyno.core.trace;

public class PropertyRead extends RWOperation {
	
	private String property;

	public String getProperty() {
		return property;
	}

	public void setProperty(String o) {
		property = o;
	}
}
