package com.dyno.units;

import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

public class SlicingCriteria {

	private static Scope startScope;
	private String variableName;
	
	public SlicingCriteria(Scope s, String n) {
		this.startScope = s;
		this.variableName = n;
	}
	
	public static Scope getScope () {
		return startScope;
	}
	
	public String getVariable () {
		return variableName;
	}
	
	public boolean equals (SlicingCriteria compareTo) {
		if (compareTo.getVariable().equals(variableName)
				&& compareTo.getScope().equals(startScope)) {
			return true;
		} else {
			return false;
		}
	}
}
