package com.dyno.core.trace;

public class VariableRead extends RWOperation {
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String o) {
		value = o;
	}
}
