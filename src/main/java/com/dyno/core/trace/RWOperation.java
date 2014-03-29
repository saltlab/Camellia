package com.dyno.core.trace;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class RWOperation implements Comparable<RWOperation> {

	private int order;
	private int lineNo;
	private String variable;
	//private int messageType;

	public int compareTo(RWOperation arg0) {
		if (order < arg0.getOrder()) {
			return -1;
		} else if (order > arg0.getOrder()) {
			return 1;
		}
		return 0;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int o) {
		order = o;
	}
	
	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int o) {
		lineNo = o;
	}
	
	public String getVariable() {
		return variable;
	}

	public void setVariable(String o) {
		variable = o;
	}
}
