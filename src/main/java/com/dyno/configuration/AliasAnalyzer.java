package com.dyno.configuration;

import java.util.ArrayList;
import java.util.Iterator;

import org.openqa.selenium.NotFoundException;

import com.dyno.core.trace.PropertyRead;
import com.dyno.core.trace.RWOperation;
import com.dyno.core.trace.VariableRead;
import com.dyno.core.trace.VariableWrite;

public class AliasAnalyzer {

	private boolean flag = false;
	private static RWOperation end;

	public ArrayList<String> getAllAliases(RWOperation bottom, RWOperation top, ArrayList<RWOperation> trace) {
		ArrayList<RWOperation> returnMe = new ArrayList<RWOperation>();
		ArrayList<RWOperation> dependencies;
		RWOperation newEnd;

		ArrayList<String> allAliases = new ArrayList<String>();

		if (end == null) {
			end = bottom;
		}

		System.out.println("READ Index: " + trace.indexOf(bottom));
		System.out.println("WRITE Index: " + trace.indexOf(top));

		if ((trace.indexOf(bottom) == -1 || trace.indexOf(top) == -1 /* both read and write should be in the trace */) 
				|| trace.indexOf(bottom) < trace.indexOf(top) /* read should be after write */) {
			System.out.println("[getAllAliases]: Invalid alias locator parameters");
			throw new NotFoundException();
		}


		for (int i = trace.indexOf(top) + 1; i < trace.indexOf(bottom); i++) {
			if (trace.get(i) instanceof VariableWrite
					&& TraceHelper.isComplex(((VariableWrite) trace.get(i)).getValue())) {
				// variable write covers property write as of now

				try {
					dependencies = TraceHelper.getDataDependencies(trace, (VariableWrite) trace.get(i));
				} catch (Exception e) {
					System.out.println("[getAllAliases]: Order of recorded reads is not as expected (Class AliasAnalyzer).");
					return allAliases;
				}

				// RHS Reading
				for (int j = 0; j < dependencies.size(); j++) {
					if (dependencies.get(j) instanceof VariableRead
							&& dependencies.get(j).getVariable().indexOf(top.getVariable()) == 0) {

						//	if (trace.get(i).getVariable().indexOf(".") == -1) {
						// Variable write
						returnMe.add(trace.get(i));
						allAliases.add(trace.get(i).getVariable());
						//						returnMe.addAll(getAllAliases(read, trace.get(i)));


						if (trace.get(i).getVariable().indexOf("xx") == 0) {
							System.out.println("wow");
						}

						newEnd = getNextWrite(trace.get(i), /*bottom,*/ trace);


						if (trace.get(i).getChildren().size() > 0) {

							trace.get(i).includeInSlice();

							// Clear kids
							for (int k = 0; k < trace.get(i).getChildren().size(); k++) {
								trace.get(i).getChildren().get(k).setParent(null);
							}
							trace.get(i).clearChildren();
						}


						// The alias doesn't last long
						if (newEnd != null) {
							// bottom to top
							allAliases.addAll(getAllAliases(newEnd, trace.get(i), trace));
						} else {
							allAliases.addAll(getAllAliases(bottom, trace.get(i), trace));
						}


						break;
					} else if (dependencies.get(j) instanceof PropertyRead
							&& dependencies.get(j).getVariable().indexOf(bottom.getVariable()) == 0) {

						System.out.println("PropertyRead");
						/*

						// Variable write
						returnMe.add(trace.get(i));
						allAliases.add(trace.get(i).getVariable());
						//						returnMe.addAll(getAllAliases(read, trace.get(i)));



						allAliases.addAll(getAllAliases(bottom, trace.get(i)));

						returnMe.add(trace.get(i));
						break;*/
					}
				}
			}
		}

		if (end.equals(bottom)) {
			end = null;
		}

		return allAliases;
	}


	private RWOperation getNextWrite(RWOperation start, ArrayList<RWOperation> trace) {
		RWOperation next;
		Iterator<RWOperation> it;
		String[] brokenDown = getPropChain(start.getVariable());
		String[] brokenDown2;

		System.out.println("=--=-=-=-=-=-=-=-=-=-=-=-");
		System.out.println(start.getOrder());
		System.out.println(start.getVariable());
		System.out.println("=--=-=-=-=-=-=-=-=-=-=-=-");
		System.out.println(this.end.getOrder());
		System.out.println(this.end.getVariable());


		/* TODO APRIL 15. if start.getVariable has a . in it.....look for last write for each parent property and base */
		if (getPropChain(start.getVariable()).length > 1) {
			getPrevWrite(start, trace);
		}

		for (int i = 0; i < brokenDown.length; i++) {

			for (int j = trace.indexOf(start) + 1; j <= trace.indexOf(end); j++) {
				next = trace.get(j);

				if (next instanceof VariableWrite) {

					brokenDown2 = getPropChain(next.getVariable());

					if (brokenDown2.length > brokenDown.length && compareStringArray(brokenDown2, brokenDown)) {
						// New write is more specific and equal

						// Flag this operation so we know to include the alias' origin/write in the slice
						next.setParent(start);
						start.addChild(next);

						next.includeInSlice();
						start.includeInSlice();

						// Reference to object (start) not overwritten, but is updated
					} else if (brokenDown2.length <= brokenDown.length && compareStringArray(brokenDown, brokenDown2)) {
						// Overwriting parent object...old reference is lost
						return next;

					}

				}
			}
		}
		return null;
	}

	private void getPrevWrite(RWOperation start, ArrayList<RWOperation> trace) {
		RWOperation next;
		Iterator<RWOperation> it;
		String[] brokenDown = getPropChain(start.getVariable());
		String baseName = "";
		String[] brokenDown2;

		/* TODO APRIL 15. if start.getVariable has a . in it.....look for last write for each parent property and base */


		for (int i = 0; i < brokenDown.length; i++) {
			if (i == 0) {
				baseName = brokenDown[i];
			} else {
				baseName += "."+ brokenDown[i];
			}

			for (int j = trace.indexOf(start) - 1 ; j >= 0; j--) {
				next = trace.get(j);

				if (next instanceof VariableWrite && next.getVariable().equals(baseName)) {
					start.setParent(next);
					next.addChild(start);

					next.includeInSlice();
					start.includeInSlice();
				}
			}
		}
	}

	private String[] getPropChain (String s) {
		String[] properties;
		properties = s.split("\\.");

		for (int k = 0; k < properties.length; k++) {
			if (properties[k].indexOf("(") != -1) {
				properties[k] = properties[k].substring(0, properties[k].indexOf("("));
			}
		}
		return properties;
	}

	private boolean compareStringArray(String[] longer, String[] shorter) {

		for (int i = 0; i < shorter.length; i++) {
			if (!shorter[i].equals(longer[i])) {
				return false;
			}
		}
		return true;
	}

}
