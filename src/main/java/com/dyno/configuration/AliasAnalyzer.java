package com.dyno.configuration;

import java.util.ArrayList;

import org.openqa.selenium.NotFoundException;

import com.dyno.core.trace.PropertyRead;
import com.dyno.core.trace.RWOperation;
import com.dyno.core.trace.VariableRead;
import com.dyno.core.trace.VariableWrite;

public class AliasAnalyzer {

	ArrayList<RWOperation> trace;

	public AliasAnalyzer (ArrayList<RWOperation> trace) {
		this.trace = trace;
	}

	public ArrayList<String> getAllAliases(RWOperation read, RWOperation write) {
		ArrayList<RWOperation> returnMe = new ArrayList<RWOperation>();
		ArrayList<RWOperation> dependencies;

		ArrayList<String> allAliases = new ArrayList<String>();

		if ((trace.indexOf(read) == -1 || trace.indexOf(write) == -1 /* both read and write should be in the trace */) 
				|| trace.indexOf(read) < trace.indexOf(write) /* read should be after write */) {
			System.out.println("[getAllAliases]: Invalid alias locator parameters");
			throw new NotFoundException();
		}


		for (int i = trace.indexOf(write) + 1; i < trace.indexOf(read); i++) {
			if (trace.get(i) instanceof VariableWrite
					&& TraceHelper.isComplex(((VariableWrite) trace.get(i)).getValue())) {
				// variable write covers property write as of now

				try {
					dependencies = TraceHelper.getDataDependencies(trace, (VariableWrite) trace.get(i));
				} catch (Exception e) {
					System.out.println("[getAllAliases]: Order of recorded reads is not as expected (Class AliasAnalyzer).");
					return allAliases;
				}

				for (int j = 0; j < dependencies.size(); j++) {
					if (dependencies.get(j) instanceof VariableRead
							&& dependencies.get(j).getVariable().indexOf(read.getVariable()) == 0) {

						//	if (trace.get(i).getVariable().indexOf(".") == -1) {
						// Variable write
						returnMe.add(trace.get(i));
						allAliases.add(trace.get(i).getVariable());
//						returnMe.addAll(getAllAliases(read, trace.get(i)));

allAliases.addAll(getAllAliases(read, trace.get(i)));

						break;
					} else if (dependencies.get(j) instanceof PropertyRead
							&& dependencies.get(j).getVariable().indexOf(read.getVariable()) == 0) {



						// Variable write
						returnMe.add(trace.get(i));
						allAliases.add(trace.get(i).getVariable());
//						returnMe.addAll(getAllAliases(read, trace.get(i)));



						allAliases.addAll(getAllAliases(read, trace.get(i)));

						returnMe.add(trace.get(i));
						break;
					}
				}
			}
		}


		return allAliases;
	}

}
