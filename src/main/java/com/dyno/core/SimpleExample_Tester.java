package com.dyno.core;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.plugin.Framework;
import org.owasp.webscarab.plugin.proxy.Proxy;

import com.dyno.configuration.ProxyConfiguration;
import com.dyno.configuration.TraceHelper;
import com.dyno.core.trace.PropertyRead;
import com.dyno.core.trace.RWOperation;
import com.dyno.core.trace.VariableRead;
import com.dyno.core.trace.VariableWrite;
import com.dyno.core.trace.VariableWriteAugmentAssign;
import com.dyno.instrument.ProxyInstrumenter;
import com.dyno.jsmodify.JSExecutionTracer;
import com.dyno.jsmodify.JSModifyProxyPlugin;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import com.crawljax.util.Helper;

public class SimpleExample_Tester {

	public static final String SERVER_PREFIX2 = "--server";
	public static final String SERVER_PREFIX1 = "--s";

	public static final String LINE_PREFIX2 = "--line";
	public static final String LINE_PREFIX1 = "--l";

	public static final String FILE_PREFIX2 = "--file";
	public static final String FILE_PREFIX1 = "--f";

	public static final String VAR_PREFIX2 = "--v";
	public static final String VAR_PREFIX1 = "--variable";

	private static boolean urlProvided = false;
	private static boolean varProvided = false;
	private static boolean lineProvided = false;
	private static boolean fileProvided = false;
	private static String URL = "";
	private static String VAR = "";
	private static int LINE;
	private static String FILE = "";

	private static String outputFolder = "";
	private static WebDriver driver;

	public static void main(String[] args) {
		try {
			int argType = -1;

			System.out.println("args!");
			System.out.println(args.length);
			for (int ll = 0; ll < args.length; ll++) {
				System.out.println(args[ll]);
			}

			// Iterate through arguments
			for (String arg : args) {


				// Argument parsing
				switch (argType) {
				case 0:
					URL = arg;
					urlProvided = true;
					break;
				case 1:  
					VAR = arg;
					varProvided = true;
					break;
				case 2:  
					LINE = Integer.parseInt(arg);
					lineProvided = true;
					break;
				case 3:  
					FILE = arg;
					fileProvided = true;
					break;
				default:
					// Not an expected argument flag, ignore
					break;
				}
				argType = -1;

				// If previous argument was an argument flag, save the value
				argType = parse(arg);

			}

			if (urlProvided == false) {
				System.err.println("Invalid arguments. Please provide URL for target application as argument (E.g. --url http://localhost:8888/phormer331/index.php)");
				throw new IllegalArgumentException();
			} 

			if (fileProvided == false) {
				System.err.println("Invalid arguments. Please provide a filename for the slicing criteria for target application as argument (E.g. --file animate.js)");
				throw new IllegalArgumentException();
			} 

			if (lineProvided == false) {
				System.err.println("Invalid arguments. Please provide a line number for the slicing criteria (E.g. --line 8)");
				throw new IllegalArgumentException();
			} 

			if (varProvided == false) {
				System.err.println("Invalid arguments. Please provide the name of the variable for which we are computing a slice (E.g. --variable privateInt)");
				throw new IllegalArgumentException();
			} 

			ObjectMapper mapper = new ObjectMapper();
			// Register the module that serializes the Guava Multimap
			mapper.registerModule(new GuavaModule());

			Multimap<String, RWOperation> traceMap = mapper
					.<Multimap<String, RWOperation>> readValue(
							new File("clematis-output/ftrace/null"),
							new TypeReference<TreeMultimap<String, RWOperation>>() {
							});

			//Collection<RWOperation> properyReads = traceMap.get("PropertyRead");
			Collection<RWOperation> myOperations = traceMap.values();

			// Convert to ArrayList
			ArrayList<RWOperation> all = new ArrayList<RWOperation>();
			Iterator<RWOperation> it3 = myOperations.iterator();
			while (it3.hasNext()) {
				all.add(it3.next());
			}

			// Sort the ArrayList of read/write operations
			Comparator<RWOperation> comparator = new Comparator<RWOperation>() {
				public int compare(RWOperation c1, RWOperation c2) {
					return c1.getOrder() - c2.getOrder(); 
				}
			};
			Collections.sort(all, comparator); // use the comparator as much as u want

			RWOperation nextOp;
			RWOperation searchingOp;
			RWOperation searchingOp2;
			Iterator<RWOperation> it1 = all.iterator();
			int index;

			ArrayList<RWOperation> readsToBeSliced = new ArrayList<RWOperation>();
			ArrayList<RWOperation> readsCompleted = new ArrayList<RWOperation>();
			ArrayList<RWOperation> theSlice = new ArrayList<RWOperation>();

			// First one:
			RWOperation begin = new RWOperation();
			RWOperation next;
			begin.setLineNo(LINE);
			begin.setVariable(VAR);

			readsToBeSliced.add(begin);

			while (readsToBeSliced.size() > 0) {
				next = readsToBeSliced.get(0);

				while (it1.hasNext()) {
					nextOp = it1.next();

					//all.
					if (nextOp.getLineNo() == next.getLineNo() && nextOp.getVariable().equals(next.getVariable()) && (nextOp instanceof VariableRead || nextOp instanceof PropertyRead)) {
						System.out.println("Relevant [READ] found!");

						index = all.indexOf(nextOp);

						System.out.println(nextOp.getOrder());
						System.out.println(nextOp.getClass().toString());

						for (int a = index - 1; a >= 0; a--) {
							// Checking backwards from READ looking for WRITE
							searchingOp = all.get(a);


							/*if (searchingOp instanceof VariableWriteAugmentAssign && ((VariableWriteAugmentAssign) searchingOp).getVariable().equals(next.getVariable())) {

							} else*/
							if (searchingOp instanceof VariableWrite && ((VariableWrite) searchingOp).getVariable().equals(next.getVariable())) {

								/*
								 * TODO: Wednesday, If that write to 'VAR' is a reference (not primitive type), will need 
								 * to check if the 'right side' of the write/assignment is updated between THIS VariableWrite
								 * and the 'Relevant READ' since such an update would change the value of VAR before the
								 * 'Relevant READ' (since the reference is still live)
								 * 
								 * Need to check PropertyWrites only??
								 * 
								 * Need to integrate this into algorithm each time a variable's write is found...check
								 * for changes by reference if the written value is not primitive
								 * 
								 */

								//	((VariableWrite) searchingOp).addDataDependencies(TraceHelper.getDataDependencies(all, (VariableWrite) searchingOp));
								ArrayList<RWOperation> potentialNewDependencies = TraceHelper.getDataDependencies(all, (VariableWrite) searchingOp);

								for (int d = 0; d < potentialNewDependencies.size(); d++) {

									// New dependency, add it to queue
									if (readsToBeSliced.indexOf(potentialNewDependencies.get(d)) == -1 
											&& readsCompleted.indexOf(potentialNewDependencies.get(d)) == -1) {
										readsToBeSliced.add(potentialNewDependencies.get(d));
									}

								}

								System.out.println("Relevant <WRITE> found!");

								System.out.println(searchingOp.getOrder());
								System.out.println(searchingOp.getClass().toString());
								if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, searchingOp) == -1) {
									theSlice.add(searchingOp);
								}


								Iterator<RWOperation> depCheck = potentialNewDependencies.iterator();
								RWOperation nextDep;





								/*		for (int b = a - 1; b >= 0; b--) {
								searchingOp2 = TraceHelper.getElementAtIndex(all, b);

								if (searchingOp2.getLineNo() == searchingOp.getLineNo()) {
									System.out.println("Found an operation on the same line as the write");

									System.out.println(searchingOp2.getOrder());
									System.out.println(searchingOp2.getClass().toString());

								} else { 
									break;
								}
							}*/
								if (!(searchingOp instanceof VariableWriteAugmentAssign)) {
									// Previous writes are irrelevant if it was not an augmented assignment i.e. -=, +=
									break;
								}
							} 
						}

					}

					// 1  -  Get all instances of slicing criteria (all the reads for positionX on line _, etc.)


					// 2  -  Get last write for that instance

					// 3  -  Get all dependencies for that last write

					// 4  - repeat 1 for each new dependency
				}
				readsCompleted.add(readsToBeSliced.remove(0));
			}

			System.out.println(theSlice.size());

			for (int a = 0; a < theSlice.size(); a++) {
				System.out.println(theSlice.get(a).getClass());
				System.out.println(theSlice.get(a).getOrder());
				System.out.println(theSlice.get(a).getLineNo());
				System.out.println(theSlice.get(a).getVariable());
			}

			


			//   story = new Story(domEventTraces, functionTraces, timingTraces, XHRTraces);
			//   story.setOrderedTraceList(sortTraceObjects());

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	static boolean waitForWindowClose(WebDriverWait w) throws TimeoutException {
		// Function to check if window has been closed

		w.until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				try {
					return d.getWindowHandles().size() < 1;
				} catch (Exception e) {
					return true;
				}
			}
		});
		return true;
	}

	public static boolean isAlertPresent()
	{
		// Selenium bug where all alerts must be closed before
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException Ex) {
			return false;
		}
	}

	public static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	private static int parse(String arg) throws IllegalArgumentException {
		if (arg.equals(SERVER_PREFIX1) || arg.equals(SERVER_PREFIX2)) {
			return 0;
		} else if (arg.equals(VAR_PREFIX2) || arg.equals(VAR_PREFIX1)) {
			return 1;
		} else if (arg.equals(LINE_PREFIX1) || arg.equals(LINE_PREFIX2)) {
			return 2;
		} else if (arg.equals(FILE_PREFIX1) || arg.equals(FILE_PREFIX2)) {
			return 3;
		}
		return -1;
	}
}



