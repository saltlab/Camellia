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

public class SimpleExample {

	// Example arguments:       --server http://www.themaninblue.com/experiment/BunnyHunt/ --file bunnies.js --line 732 --variable positionY

	//--server http://www.themaninblue.com/experiment/BunnyHunt/ --file clouds.js --line 30 --variable cloud1

	// --server http://localhost:8080/test.html --file test.js --line 17 --variable original

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

			outputFolder = Helper.addFolderSlashIfNeeded("clematis-output");

			JSExecutionTracer tracer = new JSExecutionTracer();
			tracer.setOutputFolder(outputFolder + "ftrace");
			tracer.preCrawling();

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

			Proxy proxy = new Proxy(framework);

			/* add the plugins to the proxy */
			proxy.addPlugin(p);

			framework.setSession("FileSystem", new File("convo_model"), "");

			/* start the proxy */
			proxy.run();

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

			driver = new FirefoxDriver(profile);
			WebDriverWait wait = new WebDriverWait(driver, 10);
			boolean sessionOver = false;

			// Use WebDriver to visit specified URL
			driver.get(URL);

			while (!sessionOver) {
				// Wait until the user/tester has closed the browser

				try {
					waitForWindowClose(wait);

					// At this point the window was closed, no TimeoutException
					sessionOver = true;
				} catch (TimeoutException e) {
					// 10 seconds has elapsed and the window is still open
					sessionOver = false;
				} catch (WebDriverException wde) {
					wde.printStackTrace();
					sessionOver = false;
				}
			}

			tracer.postCrawling();

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
			ArrayList<RWOperation> potentialNewDependencies;
			boolean found = false;

			AliasAnalyzer aa = new AliasAnalyzer();

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
						index = all.indexOf(nextOp);


						for (int a = index - 1; a >= 0; a--) {
							found = false;
							// Checking backwards from READ looking for WRITE
							searchingOp = all.get(a);

							/*if (searchingOp instanceof VariableWriteAugmentAssign && ((VariableWriteAugmentAssign) searchingOp).getVariable().equals(next.getVariable())) {

							} else*/
							

							
							if (searchingOp instanceof VariableWrite && ((VariableWrite) searchingOp).getVariable().equals(next.getVariable())) {
								potentialNewDependencies = new ArrayList<RWOperation>();
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

								if (searchingOp instanceof ArgumentWrite) {
									// Special case linking arguments from call to declaration
									for (int q = all.indexOf(searchingOp)-1; q >= 0; q--) {
										if (all.get(q) instanceof ArgumentRead 
												&& ((ArgumentRead) all.get(q)).getArgumentNumber() == ((ArgumentWrite) searchingOp).getArgumentNumber()
												&& ((ArgumentRead) all.get(q)).getFunctionName().equals(((ArgumentWrite) searchingOp).getFunctionName())
												&& ((ArgumentRead) all.get(q)).getValue().equals(((ArgumentWrite) searchingOp).getValue())) {
											potentialNewDependencies.add(all.get(q));

											// Add function call/argument pass as PARENT to this argument read
											nextOp.setParent(all.get(q));

											if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, all.get(q)) == -1) {
												theSlice.add(all.get(q));
											}

											// Break from looking from Argument read
											found = true;
											break;
										}
									}
								} else {
									potentialNewDependencies = TraceHelper.getDataDependencies(all, (VariableWrite) searchingOp);

									if (!(searchingOp instanceof VariableWriteAugmentAssign)) {
										// Previous writes are IRRELEVANT if it was NOT an augmented assignment i.e. -=, +=
										found = true;

										// For now, assume only augmented assignments don't result in complex values
										if (TraceHelper.isComplex(((VariableWrite) searchingOp).getValue())) {


											System.out.println("Is complex");
											System.out.println(searchingOp.getOrder());

											// Continue search based on RHS of assignment
											for (int f = 0; f < potentialNewDependencies.size(); f++) {
												potentialNewDependencies.get(f);


												// Find the hard write

											








												// get base of dependency
												// get writes for dependency (hard and soft/aug)
												// when u hit the hard...get the RHS and continue this loop for that depenedency

												// EACH of the above loops, go till the previous hard write for the dependency

												// FIND all aliases assigned from that write to this READ (next nextOP line 301 above)



												// IF the first right hand side is a base objct...we want all the writes for properties
												// AND all writes which tamper with the object's properties from parent's aliases

												// IF the right hand side is a propert read...want all changes from that property downwards (which could happen through parent aliases

												// REMEMBER ... aliases for the parent can assess the base object from properties

												/* e.g. var tt = {}
												 * 
												 * 		var ttt = tt;
												 * 
												 * 		tt.child = {};
												 * 
												 * 		
												 * 
												 *      var zz = tt.child;
												 *      
												 *		ttt.child.new = "yello";      <-- we need to capture this when slicing 'finish' below OR "zz" above OR "tt" above
												 *
												 *      var finish = zz;
												 * 
												 * 
												 * 
												 * 
												 */


												// get all aliases for base


											}
											

										
											
											ArrayList<String> aliases = aa.getAllAliases(nextOp, searchingOp, all);
											
											for (int ss = all.indexOf(searchingOp); ss < all.indexOf(nextOp); ss++) {
												if (all.get(ss).getSliceStatus() == true) {
													// Add to slice
													if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, all.get(ss)) == -1) {
														theSlice.add(all.get(ss));
													}
													all.get(ss).omitFromSlice();
												}
											}


											System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-");
											System.out.println("ALIASES:");
											for (int lk = 0; lk < aliases.size(); lk++) {
												System.out.println(aliases.get(lk));
											}

											/*	if () {

																					}*/
										}


									} else {
										// Add the augment assign line to the slice and continue looking for previous assign (non-augment)

										if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, searchingOp) == -1) {
											theSlice.add(searchingOp);
										}
									}


								}

								for (int d = 0; d < potentialNewDependencies.size(); d++) {

									// New dependency, add it to queue
									if (readsToBeSliced.indexOf(potentialNewDependencies.get(d)) == -1 
											&& readsCompleted.indexOf(potentialNewDependencies.get(d)) == -1) {
										readsToBeSliced.add(potentialNewDependencies.get(d));
									}

								}

								//Relevant <WRITE> found! --> 'searchingOp'
								// If the line is not part of the slice...add it
								if (found) {
									if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, searchingOp) == -1) {
										theSlice.add(searchingOp);
									}
									break;
								}
							} else if (searchingOp instanceof VariableWrite && ((VariableWrite) searchingOp).getVariable().indexOf(next.getVariable()) == 0
									&& ((VariableWrite) searchingOp).getVariable().indexOf(".") != -1) {
								System.out.println("Property write found");
								potentialNewDependencies = TraceHelper.getDataDependencies(all, (VariableWrite) searchingOp);
								
								for (int d = 0; d < potentialNewDependencies.size(); d++) {

									// New dependency, add it to queue
									if (readsToBeSliced.indexOf(potentialNewDependencies.get(d)) == -1 
											&& readsCompleted.indexOf(potentialNewDependencies.get(d)) == -1) {
										readsToBeSliced.add(potentialNewDependencies.get(d));
									}

								}
								if (TraceHelper.getIndexOfIgnoreOrderNumber(theSlice, searchingOp) == -1) {
									theSlice.add(searchingOp);
								}

							}


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
			System.out.println("%%%%%%%%%%%");
			for (int d = 0; d < vars.size(); d++) {
				System.out.println(vars.get(d));
			}
			System.out.println("%%%%%%%%%%%");


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

			System.out.println("------Fin--------");
			System.out.println(dataLines);


			//   story = new Story(domEventTraces, functionTraces, timingTraces, XHRTraces);
			//   story.setOrderedTraceList(sortTraceObjects());


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



