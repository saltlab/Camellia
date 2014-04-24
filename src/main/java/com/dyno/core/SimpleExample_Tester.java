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

import com.dyno.configuration.AliasAnalyzer;
import com.dyno.configuration.ProxyConfiguration;
import com.dyno.configuration.TraceHelper;
import com.dyno.core.trace.ArgumentRead;
import com.dyno.core.trace.ArgumentWrite;
import com.dyno.core.trace.PropertyRead;
import com.dyno.core.trace.RWOperation;
import com.dyno.core.trace.ReturnStatementValue;
import com.dyno.core.trace.ReturnValueWrite;
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

	// Example arguments:       --server http://www.themaninblue.com/experiment/BunnyHunt/ --file bunnies.js --line 732 --variable positionY

	//--server http://www.themaninblue.com/experiment/BunnyHunt/ --file clouds.js --line 30 --variable cloud1

	// --server http://localhost:8080/test.html --file test.js --line 17 --variable original

	// --server http://localhost:8080/test.html --file test.js --line 22 --variable tt


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

	private ArrayList<RWOperation> theSlice = new ArrayList<RWOperation>();

	public void main(String[] args) {
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

			outputFolder = Helper.addFolderSlashIfNeeded("clematis-output");

			JSExecutionTracer tracer = new JSExecutionTracer();
			tracer.setOutputFolder(outputFolder + "ftrace");

			// Create a new instance of the firefox driver
			FirefoxProfile profile = new FirefoxProfile();
			// Instantiate proxy components
			ProxyConfiguration prox = new ProxyConfiguration();

			// Modifier responsible for parsing Ast tree
			ProxyInstrumenter s = new ProxyInstrumenter();

			// Add necessary files from resources
			s.setFileNameToAttach("/dyno.wrappers.js");
			s.setFileNameToAttach("/send.and.buffer.js");

			// Interface for Ast traversal
			JSModifyProxyPlugin p = new JSModifyProxyPlugin(s);
			p.excludeDefaults();

			// Set slicing criteria (as it was passed in by user)
			p.setTargetFile(FILE);
			p.setLineNo(LINE);
			p.setVariableName(VAR);

			Framework framework = new Framework();

			/* set listening port before creating the object to avoid warnings */
			Preferences.setPreference("Proxy.listeners", "127.0.0.1:" + prox.getPort());

			framework.setSession("FileSystem", new File("convo_model"), "");

			if (prox != null) {
				profile.setPreference("network.proxy.http", prox.getHostname());
				profile.setPreference("network.proxy.http_port", prox.getPort());
				profile.setPreference("network.proxy.type", prox.getType().toInt());
				/* use proxy for everything, including localhost */
				profile.setPreference("network.proxy.no_proxies_on", "");
			}

			/*
			 * For enabling Firebug with Clematis Replace '...' with the appropriate path to your
			 * Firebug installation
			 */
			// File file = new
			// File("/Users/.../Library/Application Support/Firefox/Profiles/zga73n4v.default/extensions/firebug@software.joehewitt.com.xpi");
			/*
			 * File file = new File(
			 * "/Users/Saba/Library/Application Support/Firefox/Profiles/b0dzzwrl.default/extensions/firebug@software.joehewitt.com.xpi"
			 * ); profile.addExtension(file);
			 * profile.setPreference("extensions.firebug.currentVersion", "1.8.1"); // Avoid startup
			 */// screen

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
			Iterator<RWOperation> it1 = all.iterator();

			ArrayList<RWOperation> readsToBeSliced = new ArrayList<RWOperation>();
			ArrayList<RWOperation> readsCompleted = new ArrayList<RWOperation>();
			ArrayList<RWOperation> potentialNewDependencies;


			// First one:
			RWOperation begin = new RWOperation();
			RWOperation next;
			begin.setLineNo(LINE);
			begin.setVariable(VAR);

			readsToBeSliced.add(begin);






			// 1  -  Get all instances of slicing criteria (all the reads for positionX on line _, etc.)
			// 2  -  Get last write for that instance
			// 3  -  Get all dependencies for that last write
			// 4  - repeat 1 for each new dependency
			while (readsToBeSliced.size() > 0) {
				next = readsToBeSliced.get(0);
				it1 = all.iterator();

				while (it1.hasNext()) {
					nextOp = it1.next();

					if (nextOp.getLineNo() == next.getLineNo() && nextOp.getVariable().equals(next.getVariable()) && (nextOp instanceof VariableRead || nextOp instanceof PropertyRead)) {
						// Relevant [READ] found! --> nextOp

						potentialNewDependencies = computeBackwardSlice(null, nextOp, nextOp.getVariable(), all, true);

						for (int g = 0; g < potentialNewDependencies.size(); g++) {
							System.out.println(potentialNewDependencies.get(g).getVariable());
						}

					}
				}
				readsCompleted.add(readsToBeSliced.remove(0));
			}


			ArrayList<String> vars = new ArrayList<String>();

			for (int d = 0; d < readsCompleted.size(); d++) {
				if (vars.indexOf(readsCompleted.get(d).getVariable()) == -1) {
					vars.add(readsCompleted.get(d).getVariable());
				}
			}

			String dataLines = "";

			for (int a = 0; a < theSlice.size(); a++) {
				dataLines += (theSlice.get(a).getLineNo()+1)+",";
			}

			// Save slice line numbers to file for visualization
			Helper.directoryCheck(p.getOutputFolder());
			Helper.checkFolderForFile("src/main/webapp/lineNumbers.txt");

			PrintStream oldOut = System.out;
			PrintStream outputVisual =
					new PrintStream("src/main/webapp/lineNumbers.txt");

			System.setOut(outputVisual);
			System.out.println(dataLines);
			System.setOut(oldOut);
			System.out.println(dataLines);

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


	private ArrayList<RWOperation> computeBackwardSlice(RWOperation top, RWOperation bottom, String name, ArrayList<RWOperation> all, boolean contained) {
		AliasAnalyzer aa = new AliasAnalyzer();
		int i = (top == null ? 0 : all.indexOf(top));
		RWOperation next = null;
		RWOperation nestedTop, nestedBottom;
		boolean found = false;
		ArrayList<RWOperation> potentialNewDependencies = new ArrayList<RWOperation>();
		
		
		System.out.println(name);
		System.out.println("Top: " + i);
		System.out.println("Bottom: " + bottom.getOrder());
		System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

		for (int j = all.indexOf(bottom); j >= i; j--) {
			next = all.get(j);

			/** 1 (ONE): Cut out function calls **/
			if (next instanceof ReturnStatementValue) {
				String alias = null;
				nestedTop = TraceHelper.getBeginningOfFunction((ReturnStatementValue) next, all);
				nestedBottom = next;

				// Skip the nested call when backwards slicing, it is taken care of in the above recursive call
				// j = index of ArgumentRead
				j = all.indexOf(nestedTop) + 1;

				// Check if the variable of interest is passed into a function by reference (could be altered there)
				for (int p = j; p >= i; p--) {
					// Possible for multiple references to variable passed in as separate arguments (but why?)
					if (all.get(p) instanceof ArgumentRead
							&& ((ArgumentRead) all.get(p)).getVariable().indexOf(name) == 0
							&& TraceHelper.isComplex(((ArgumentRead) all.get(p)).getValue())) {

						// get the local name in the called function (argument name)
						for (int q = j; q <= all.indexOf(nestedBottom); q++) {
							if (all.get(q) instanceof ArgumentWrite
									&& ((ArgumentWrite) all.get(q)).getFunctionName().equals(((ArgumentRead) all.get(p)).getFunctionName())
									&& ((ArgumentWrite) all.get(q)).getArgumentNumber() == ((ArgumentRead) all.get(p)).getArgumentNumber()) {
								alias = ((ArgumentWrite) all.get(q)).getVariable();

								//TODO: implement 'computeForwardSlice'
								// Compute forward slice of reference/argument
								computeForwardSlice(all.get(q), nestedBottom, alias, all);

								if (all.get(q).getChildren().size() > 0) {
									for (int o = q; o <= all.indexOf(nestedBottom); o++) {
										if (all.get(o).getSliceStatus() == true) {
											
											all.get(o).omitFromSlice();
											all.get(o).clearChildren();
											all.get(o).setParent(null);
											
											if (theSlice.indexOf(all.get(o)) == -1) {
												theSlice.add(all.get(o));
											}
											
											if (all.get(o) instanceof VariableWrite) {
												// NEW, TEST THIS
												// Need to slice all the dependencies for this write (this update through alias)
												ArrayList<RWOperation> deps = null;
												try {
													deps = TraceHelper.getDataDependencies(all, (VariableWrite) all.get(o));
												} catch (Exception e) {
													e.printStackTrace();
												}
												for (int r = 0; r < deps.size(); r++) {
													//computeBackwardSlice(null, bottom, all.get(o).getVariable(), all, true);
												}
											}
										}
									}
								}

								


								break;

							}

						}
						if (alias != null) break; 

					}
				}
				/** 2 (TWO): Return value from function is used in write-of-interest **/
			} else if (next instanceof ReturnValueWrite
					&& next.getVariable().indexOf(name) == 0) {
				// Compute backwards slice on return statement dependencies
				for (int r = all.indexOf(next); r >= i; r--) {
					if (all.get(r) instanceof ReturnStatementValue
							&& ((ReturnStatementValue) all.get(r)).getFunctionName().equals(((ReturnValueWrite) next).getFunctionName())) {
						nestedTop = TraceHelper.getBeginningOfFunction((ReturnStatementValue) all.get(r), all);

						// TODO: implement TraceHelper.getReturnDependencies
						ArrayList<String> rsDependencies = TraceHelper.getReturnDependencies(all, (ReturnStatementValue) all.get(r));

						for (int w = 0; w < rsDependencies.size(); w++) {
							// TODO: What if an argument influenced the return value? --> do we need to allow slicing to exit the function?
							computeBackwardSlice(nestedTop, all.get(r), rsDependencies.get(w), all, true /* or false ????*/);
						}
					}
				}
				/** 3 (OLD): Basic slicing **/
			} else if (next instanceof VariableWrite && ((VariableWrite) next).getVariable().equals(name)) {

				// UPWARDS
				if (next instanceof ArgumentWrite) {
					// Special case linking arguments from call to declaration
					for (int q = all.indexOf(next)-1; q >= 0; q--) {
						if (all.get(q) instanceof ArgumentRead 
								&& ((ArgumentRead) all.get(q)).getArgumentNumber() == ((ArgumentWrite) next).getArgumentNumber()
								&& ((ArgumentRead) all.get(q)).getFunctionName().equals(((ArgumentWrite) next).getFunctionName())
								&& ((ArgumentRead) all.get(q)).getValue().equals(((ArgumentWrite) next).getValue())) {


							if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, all.get(q)) == -1) {
								theSlice.add(all.get(q));
							}

							// Allowed to look through the parent/calling function for argument slice
							computeBackwardSlice(null, all.get(q), all.get(q).getVariable(), all, true);


							// Break from looking from Argument read
							found = true;
							break;
						}
					}
				} else {
					// Regular hard write (not argument write)

					try {
						potentialNewDependencies.addAll(TraceHelper.getDataDependencies(all, (VariableWrite) next));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (!(next instanceof VariableWriteAugmentAssign)) {
						// Previous writes are IRRELEVANT if it was NOT an augmented assignment i.e. -=, +=
						found = true;


					} else {
						// Add the augment assign line to the slice and continue looking for previous assign (non-augment)

						if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, next) == -1) {
							theSlice.add(next);
						}
					}
				}

				//Relevant <WRITE> found! --> 'searchingOp'
				// If the line is not part of the slice...add it
				if (found) {
					if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, next) == -1) {
						theSlice.add(next);
					}
					break;
				}
				/** 3.2 (OLD): Basic slicing **/
			} else if (next instanceof VariableWrite && ((VariableWrite) next).getVariable().indexOf(next.getVariable()) == 0
					&& ((VariableWrite) next).getVariable().indexOf(".") != -1) {
				try {
					potentialNewDependencies.addAll(TraceHelper.getDataDependencies(all, (VariableWrite) next));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, next) == -1) {
					theSlice.add(next);
				}
				/** 4 (FOUR) : Alias Detection/analysis  **/
			} else if (next instanceof VariableWrite
					&& TraceHelper.isComplex(((VariableWrite) next).getValue())) {
				// variable write covers property write as of now
				ArrayList<RWOperation> dependencies = new ArrayList<RWOperation>();

				try {
					dependencies = TraceHelper.getDataDependencies(all, (VariableWrite) next);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				// RHS Reading
				for (int b = 0; b < dependencies.size(); b++) {
					if (dependencies.get(b) instanceof VariableRead
							&& dependencies.get(b).getVariable().indexOf(top.getVariable()) == 0) {

						//	if (trace.get(i).getVariable().indexOf(".") == -1) {
						// Variable write

						AliasAnalyzer.getNextWrite(next, bottom, all);

						if (next.getChildren().size() > 0) {

							next.includeInSlice();

							// Clear kids
							for (int k = 0; k < next.getChildren().size(); k++) {
								next.getChildren().get(k).setParent(null);
							}
							next.clearChildren();

							// TODO: ADD TO SLICE ALL THE FLAGGED RWOOOOOOOOOOOOO
							for (int o = all.indexOf(next); o <= all.indexOf(bottom); o++) {
								if (all.get(o).getSliceStatus() == true) {
									all.get(o).omitFromSlice();
									if (theSlice.indexOf(all.get(o)) == -1) {
										theSlice.add(all.get(o));
									}

									if (all.get(o) instanceof VariableWrite) {
										// NEW, TEST THIS
										// Need to slice all the dependencies for this write (this update through alias)
										ArrayList<RWOperation> deps = null;
										try {
											deps = TraceHelper.getDataDependencies(all, (VariableWrite) all.get(o));
										} catch (Exception e) {
											e.printStackTrace();
										}
										for (int r = 0; r < deps.size(); r++) {
											computeBackwardSlice(null, bottom, all.get(o).getVariable(), all, true);
										}
									}
								}
							}
						}

						// Not sure about this, does a backwards slice of alias (overkill?)

						// Don't need this, only need forward slice for aliases?

						//computeBackwardSlice(next, bottom, next.getVariable(), all, false);


						break;
					} else if (dependencies.get(b) instanceof PropertyRead
							&& dependencies.get(b).getVariable().indexOf(bottom.getVariable()) == 0) {

						System.out.println("PropertyRead");

					}
				}
			}
		}

		return potentialNewDependencies;
	}

	private void computeForwardSlice(RWOperation top, RWOperation bottom, String name, ArrayList<RWOperation> all) {
		// TODO:

		AliasAnalyzer.getNextWrite(top, bottom, all);
	}




}



