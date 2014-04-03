package com.dyno.core.trace;

import java.util.ArrayList;

public class VariableWrite extends RWOperation {
	private String value;
	private ArrayList<RWOperation> dataDependencies = new ArrayList<RWOperation>();

	public String getValue() {
		return value;
	}

	public void setValue(String o) {
		value = o;
	}

	public void addDataDependency(RWOperation a) {
		dataDependencies.add(a);
	}

	public void addDataDependencies(ArrayList<RWOperation> as) {
		dataDependencies.addAll(as);
	}

	public ArrayList<RWOperation> getDataDependencies(String o) {
		return dataDependencies;
	}
}
