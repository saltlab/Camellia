package com.camellia.units;

import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

public class SlicingCriteria {

	private Scope startScope;
	private String variableName;
	private boolean interTracking = false;
	
	public SlicingCriteria(Scope s, String n, boolean i) {
		this.startScope = s;
		this.variableName = n;
		this.interTracking = i;
	}
	
	public Scope getScope () {
		return startScope;
	}
	
	public String getVariable () {
		return variableName;
	}
	
	public void setInter (boolean t) {
		this.interTracking = t;
	}
	
	public boolean getInter () {
		return this.interTracking;
	}
	
	public boolean equals (SlicingCriteria compareTo) {
		if (compareTo.getVariable().indexOf(variableName) > -1
				&& compareTo.getScope().equals(startScope)) {
			return true;
		} else {
			return false;
		}
	}
}
