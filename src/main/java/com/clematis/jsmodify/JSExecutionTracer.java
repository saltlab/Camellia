/*
 * Automatic JavaScript Invariants is a plugin for Crawljax that can be used to derive JavaScript
 * invariants automatically and use them for regressions testing. Copyright (C) 2010 crawljax.com
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.clematis.jsmodify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

import com.camellia.core.Main;
import com.camellia.core.SimpleExample2;
import com.clematis.core.WebDriverWrapper;
import com.clematis.core.episode.Episode;
import com.clematis.core.episode.Story;
import com.clematis.core.trace.DOMElementAccess;
import com.clematis.core.trace.DOMElementValueTrace;
import com.clematis.core.trace.DOMEventTrace;
import com.clematis.core.trace.DOMMutationTrace;
import com.clematis.core.trace.FunctionEnter;
import com.clematis.core.trace.FunctionCall;
import com.clematis.core.trace.FunctionExit;
import com.clematis.core.trace.SeleniumAssertionTrace;
import com.clematis.core.trace.TraceObject;
import com.clematis.visual.EpisodeGraph;
import com.clematis.visual.JSUml2Story;
import com.crawljax.util.Helper;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Reads an instrumentation array from the webbrowser and saves the contents in a JSON trace file.
 * 
 * @author Frank Groeneveld
 * @version $Id: JSExecutionTracer.java 6162 2009-12-16 13:56:21Z frank $
 */
public class JSExecutionTracer {

	private static final int ONE_SEC = 1000;

	private static String outputFolder;
	private static String traceFilename;

	private static JSONArray points = new JSONArray();

	private static final Logger LOGGER = Logger
			.getLogger(JSExecutionTracer.class.getName());

	public static final String FUNCTIONTRACEDIRECTORY = "functiontrace/";

	private static PrintStream output;

	// private Trace trace;
	private static Story story;
	private static ObjectMapper mapper = new ObjectMapper();
	static String theTime;
	private static int counter = 0;

	// Camellia linkage
	private static ArrayList<Name> slicingCriteria = new ArrayList<Name>();

	// Counter synchronizing code
	private static long pageLoadBuffer = 0;
	private static boolean pageLoadFlag = false;
	private static long pageLoadTime = -1;
	public static long getPageLoadBuffer () {
		return pageLoadBuffer;
	}

	// private ArrayList<TraceObject> sortedTraceList;
	// private ArrayList<Episode> episodeList;

	/**
	 * @param filename
	 */
	public JSExecutionTracer(String filename) {
		traceFilename = filename;
	}

	/**
	 * Initialize the plugin and create folders if needed.
	 * 
	 * @param browser
	 *            The browser.
	 */
	public static void preCrawling() {
		try {
			points = new JSONArray();

			Helper.directoryCheck(getOutputFolder());
			output = new PrintStream(getOutputFolder() + getFilename());

			// Add opening bracket around whole trace
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println("{");
			System.setOut(oldOut);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the JavaScript instrumentation array from the webbrowser and writes its contents in
	 * Daikon format to a file.
	 * 
	 * @param session
	 *            The crawling session.
	 * @param candidateElements
	 *            The candidate clickable elements.
	 */

	public void preStateCrawling() {

		String filename = getOutputFolder() + FUNCTIONTRACEDIRECTORY
				+ "jstrace-";

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		filename += dateFormat.format(date) + ".dtrace";

		try {

			LOGGER.info("Reading execution trace");

			LOGGER.info("Parsing JavaScript execution trace");

			// session.getBrowser().executeJavaScript("sendReally();");
			Thread.sleep(ONE_SEC);

			LOGGER.info("Saved execution trace as " + filename);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the story object to a JSON file on disk.
	 */
	public static void writeStoryToDisk() {

		mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
				Visibility.ANY));
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// to allow coercion of JSON empty String ("") to null Object value:
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			mapper.writeValue(new File("story.json"),
					story);

			/*		Story s1 = mapper.readValue(new File("story.json"),
			        Story.class);

			if (story.equals(s1)) {
				System.out.print("SUCCESS\n");
			}
			else
				System.out.print("SUCCESSS\n");*/
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Get a list with all trace files in the executiontracedirectory.
	 * 
	 * @return The list.
	 */
	public List<String> allTraceFiles() {
		ArrayList<String> result = new ArrayList<String>();

		/* find all trace files in the trace directory */
		File dir = new File(getOutputFolder() + FUNCTIONTRACEDIRECTORY);

		String[] files = dir.list();
		if (files == null) {
			return result;
		}
		for (String file : files) {
			if (file.endsWith(".dtrace")) {
				result.add(getOutputFolder() + FUNCTIONTRACEDIRECTORY + file);
			}
		}
		return result;
	}

	public static String[] postCrawling() {
		try {
			// Add closing bracket
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println(" ");
			System.out.println("}");
			System.setOut(oldOut);

			/* close the output file */
			output.close();

			return extraxtTraceObjects();
		} catch (Exception e) {
			e.printStackTrace();
			return new String[8];
		}
	}

	/**
	 * This method parses the JSON file containing the trace objects and extracts the objects
	 */
	@SuppressWarnings("unchecked")
	private static String[] extraxtTraceObjects() {
		String[] args = new String[8];
		
		try {
			// Declarations for reading back the written assertion summary
			String s;
			String fileContents = "";

			JSONObject testCaseSummary;
			Iterator<String> assertions;
			String assertionID;
			JSONObject singleAssertionSummary;
			Vector<TraceObject> relatedMutations;
			// For removing webdriver events
			Iterator<TraceObject> domEventIterator;
			Vector<TraceObject> webdriverEvents = new Vector<TraceObject>();
			TraceObject currentWebDriverEvent;

			ObjectMapper mapper = new ObjectMapper();
			// Register the module that serializes the Guava Multimap
			mapper.registerModule(new GuavaModule());

			Multimap<String, TraceObject> traceMap = mapper
					.<Multimap<String, TraceObject>> readValue(
							new File("clematis-output/ftrace/function.trace"),
							new TypeReference<TreeMultimap<String, TraceObject>>() {
							});

			Collection<TraceObject> timingTraces = traceMap.get("TimingTrace");
			Collection<TraceObject> domEventTraces = traceMap
					.get("DOMEventTrace");

			Collection<TraceObject> XHRTraces = traceMap.get("XHRTrace");
			Collection<TraceObject> functionTraces = traceMap
					.get("FunctionTrace");

			saveDirectAccessesToFile(WebDriverWrapper.assertionToAccess,
					WebDriverWrapper.assertionToElements,
					WebDriverWrapper.assertionOutcomes,
					WebDriverWrapper.assertionCounters,
					WebDriverWrapper.assertionTimeStamps,
					WebDriverWrapper.directAccesses);
			File file = new File("domAccesses.json");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr); 

			// Read assertion accesses and results from file and instantiate JSONObject
			while((s = br.readLine()) != null) {
				fileContents += s;
			}
			br.close();
			testCaseSummary = new JSONObject(fileContents);




			// Iterate through DOM events and remove webdriver events (e.g. "evaluate", etc.)
			domEventIterator = domEventTraces.iterator();
			while (domEventIterator.hasNext()) {
				currentWebDriverEvent = domEventIterator.next();
				if (currentWebDriverEvent instanceof DOMEventTrace && ((DOMEventTrace) currentWebDriverEvent).getEventType().contains("webdriver-")) {
					webdriverEvents.add(currentWebDriverEvent);
				}
			}
			domEventTraces.removeAll(webdriverEvents);

			// Convert assertions to traceobjects for injection into story
			ArrayList<TraceObject> seleniumAssertions = new ArrayList<TraceObject>();
			assertions = testCaseSummary.keys();
			int numberOfAssertions = 0;
			while (assertions.hasNext()) {
				assertions.next();
				numberOfAssertions++;
			}

			for (int a = 0; a < numberOfAssertions; a++) {
				//assertionID = assertions.next();
				singleAssertionSummary = testCaseSummary.getJSONObject(a+"");
				int currentAssertionCounter = singleAssertionSummary.getInt("counter");

				// Declare the new assertion trace object
				SeleniumAssertionTrace newAssertion = new SeleniumAssertionTrace();
				newAssertion.setCounter(currentAssertionCounter);
				newAssertion.setTimeStamp(singleAssertionSummary.getLong("timeStamp"));
				newAssertion.setOutcome(singleAssertionSummary.getString("outcome"));
				newAssertion.setAssertionID(a);
				// For front end display
				newAssertion.setId(7);
				seleniumAssertions.add(newAssertion);
			}


			story = new Story(domEventTraces, functionTraces, timingTraces, XHRTraces, seleniumAssertions);
			story.setOrderedTraceList(sortTraceObjects());

			story.setEpisodes(buildEpisodes());

			System.out.println("Total number of assertions:  "+ seleniumAssertions.size());
			System.out.println("# of trace objects: " + story.getOrderedTraceList().size());
			System.out.println("# of episodes: " + story.getEpisodes().size());

			ArrayList<Episode> bookmarkEpisodes = new ArrayList<Episode>();



			story.removeUselessEpisodes(bookmarkEpisodes);

			System.out.println("# of episodes after trimming: " + story.getEpisodes().size());


			// JavaScript episodes for JSUML2
			Helper.directoryCheck(outputFolder + "/sequence_diagrams/");
			PrintStream JSepisodes =
					new PrintStream(outputFolder + "/sequence_diagrams/allEpisodes.js");
			ArrayList<TraceObject> episodeMutations;
			ArrayList<TraceObject> episodeDomAccesses;
			ArrayList<TraceObject> episodeTrace;
			ArrayList<Integer> relatedAssertionsPerMutation;
			ArrayList<JSONObject> seleniumAccesses;
			JSONObject singleAccess;

			/*
			 * If assumming dom mutations can only be related to one assertions (each assertion means that past mutations have been tested)...check for webdriver events ("evaluate") to know which story counter to
			 *  cut and assertion off at 
			 * 
			 * 
			 * 
			 */
			int epNum = 0;
			// << EPISODE ITERATE >>
			for (Episode e : story.getEpisodes()) {
				if (e.getSource() instanceof SeleniumAssertionTrace) {
					// Add in an empty episodes
					designSequenceDiagram(e, JSepisodes);

					continue;
				}
				System.out.println("++++++++++++++++++++++++++++++++");
				System.out.println("EPISODE:  " + epNum);
				epNum++;
				episodeMutations = e.getDom();
				episodeDomAccesses = e.getDomAccesses();
				episodeTrace = e.getTrace().getTrace();
				int closestCounterEnter = -1;
				int closestCounterCall = -1;

				// Iterate through all assertions from selenium test case/suite run
				assertions = testCaseSummary.keys();
				// <<  ASSERTION ITERATE  >> 
				while (assertions.hasNext()) {
					assertionID = assertions.next();
					System.out.println("=================================");
					System.out.println("ASSERTION:  " + assertionID);


					singleAssertionSummary = testCaseSummary.getJSONObject(assertionID);

					// FINDING RELEVANT Mutations
					// <<  MUTATION ITERATE  >>
					relatedMutations = getRelevantTraceInformation(singleAssertionSummary.getJSONObject("elements"), e.getDom());

					// 1. Find parent episode of all mutants in "relatedMutations"
					// ^^ Dont need to find this? already known? see above loop
					if (relatedMutations.size() > 0) {
						e.addRelatedAssertion(Integer.parseInt(assertionID));



						int lineNumber = -1;

						@SuppressWarnings("unchecked")
						Iterator<String> elKeys = singleAssertionSummary.getJSONObject("elements").keys();


						// <<  cross EPISODE JS ACCESS with Selenium assertion ACCESSE(S)  >>

						while (elKeys.hasNext()) {
							// get selenium access for each mutation --> singleAccess
							singleAccess = singleAssertionSummary.getJSONObject("elements").getJSONObject(elKeys.next());
							//seleniumAccesses.add(singleAccess);2

							// look for dom accesses (JS) in this episode
							// << DOM ELEMEN ACCESS ITERATE >>
							for (int tt = 0; tt < episodeDomAccesses.size(); tt++) {



								if (compareHTMLElements(singleAccess, new JSONObject(((DOMElementAccess) episodeDomAccesses.get(tt)).getElement()))) {
									// link matches to closest Function enters

									// Reset variables when looking for closest 'call' and 'enter'
									closestCounterEnter = -1;
									closestCounterCall = -1;


									// closest ENTER and CALL to JS access
									for (int f = 0; f < episodeTrace.size(); f++) {
										if (episodeDomAccesses.get(tt).getCounter() > episodeTrace.get(f).getCounter()
												//&& episodeTrace.get(f).getCounter() - episodeDomAccesses.get(tt).getCounter() < closestCounter
												&& episodeTrace.get(f) instanceof FunctionEnter) {
											closestCounterEnter = episodeTrace.get(f).getCounter();
										} else if (episodeDomAccesses.get(tt).getCounter() > episodeTrace.get(f).getCounter()
												//&& episodeTrace.get(f).getCounter() - episodeDomAccesses.get(tt).getCounter() < closestCounter
												&& episodeTrace.get(f) instanceof FunctionCall
												&& episodeDomAccesses.get(tt).getCounter() - 1 == episodeTrace.get(f).getCounter()) {
											closestCounterCall = episodeTrace.get(f).getCounter();

										}
									}


									for (int f = 0; f < episodeTrace.size(); f++) {
										if (episodeTrace.get(f).getCounter() == closestCounterEnter) {
											((FunctionEnter) episodeTrace.get(f)).addAssertion(Integer.parseInt(assertionID));
											break;
										}
									}

									for (int ff = 0; ff < episodeTrace.size(); ff++) {
										if (episodeTrace.get(ff).getCounter() == closestCounterCall) {
											System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
											System.out.println("Slicing Criteria: ");
											System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
											System.out.println("Assertion: " + assertionID);
											System.out.println("THE ACCESS:  " + episodeDomAccesses.get(tt).getCounter());
											System.out.println("closest call: " + ((FunctionCall) episodeTrace.get(ff)).getTargetFunction() + "   " + ((FunctionCall) episodeTrace.get(ff)).getCounter());
											System.out.println("Line number to slice?:  " + ((FunctionCall) episodeTrace.get(ff)).getLineNo());
											System.out.println(".....................................");




											File getSliceCriteria = new File("src/main/webapp/fish-eye-zoom-camera/"+((FunctionCall) episodeTrace.get(ff)).getScopeName());
											FileReader fr2 = new FileReader(getSliceCriteria);
											BufferedReader br2 = new BufferedReader(fr2); 
											String webAppCode = "";

											// Read assertion accesses and results from file and instantiate JSONObject
											String s2;
											int lineCounter = 0;
											while((s2 = br2.readLine()) != null) {
												lineCounter++;

												if (lineCounter == ((FunctionCall) episodeTrace.get(ff)).getLineNo()) {
													// Have reached line of interest for extracting slicing criteria
													webAppCode += s2;
													break;
												}

											}
											br2.close();

											/* initialize JavaScript context */
											Context cx = Context.enter();

											/* create a new parser */
											CompilerEnvirons compilerEnvirons =  new CompilerEnvirons();
											compilerEnvirons.setRecordingLocalJsDocComments(true);
											compilerEnvirons.setAllowSharpComments(true);
											compilerEnvirons.setRecordingComments(true);
											compilerEnvirons.setOptimizationLevel(0);
											Parser rhinoParser = new Parser(compilerEnvirons, cx.getErrorReporter());

											/* parse some script and save it in AST */
											System.out.println(webAppCode);
											AstRoot ast = rhinoParser.parse(new String(webAppCode), ((FunctionCall) episodeTrace.get(ff)).getScopeName(), 0);


											int lineType = ast.getType();


											SlicingCriteriaExtractor sce = new SlicingCriteriaExtractor();
											ast.visit(sce);

											Iterator<AstNode> it = sce.getDependencies().iterator();
											AstNode next;
											while (it.hasNext()) {
												next = it.next();
												System.out.println(next.toSource());
												if (next instanceof Name) {
													next.setLineno(lineCounter);
													slicingCriteria.add((Name) next);
												}
											}
											sce.clearDependencies();

											break;
										}
									}





									// 4. call 'setAssertions' on that function enter 
									// 5. line 427 of JSUml2Story, change args for message in sequence diagram to string of affected assertions
								}
							}
						}


















					}

					// Create pic files for each episode's sequence diagram
				}
				designSequenceDiagram(e, JSepisodes);

				// Once all episodes have been saved to JS file, close
			}
			int episodeNumber = 0;
			for (Episode e : story.getEpisodes()) {
				if (e.getSource() instanceof com.clematis.core.trace.SeleniumAssertionTrace) {
					continue;
				}
				System.out.println("++++++++++++++++++++++++++");
				System.out.println("Episode numbre: " + episodeNumber);
				System.out.println("source: " + e.getSource().getClass());
				Vector<Integer> relatedAssertions = e.getRelatedAssertions();
				for (int r = 0; r < relatedAssertions.size(); r++) {
					System.out.println("        " + relatedAssertions.get(r));
				}
				episodeNumber++;
			}

			System.out.println(".....................................");
			System.out.println("-----------------------------------");
			Iterator<Name> finalIt = slicingCriteria.iterator();
			Name nameNext = null;

			while (finalIt.hasNext()) {
				nameNext = finalIt.next();
				System.out.println(nameNext.toSource() + ", line @:  " + nameNext.getLineno());
			}
			System.out.println(".....................................");
			System.out.println("-----------------------------------");


			if (nameNext != null) {
				args[0] = "--server";
				args[1] = "N/A";
				args[2] = "--file";
				args[3] = "phorm.js";
				args[4] = "--line";
				args[5] = (nameNext.getLineno()-1) + "";
				args[6] = "--variable";
				args[7] = nameNext.getIdentifier();

				System.out.println("Arguments to camellia");
				for (int i = 0; i < 8; i++) {
					System.out.print(args[i] + " ");
				}
				System.out.println("......");

			}

			// Make sure other browser session is killed here, will make new one to run test case for slicing portion

			JSepisodes.close();

			// Create graph containing all episodes with embedded sequence diagrams
			EpisodeGraph eg = new EpisodeGraph(getOutputFolder(), story.getEpisodes());
			eg.createGraph();
			writeStoryToDisk();
		} catch (Exception e) {
			e.printStackTrace();
			return args;
		}
		return args;
	}

	public static void designSequenceDiagram(Episode e, PrintStream jSepisodes) {
		// Given an episode (source, trace included), a pic file will be created
		// in clematis-output/ftrace/sequence_diagrams

		/*
		 * SequenceDiagram sd = new SequenceDiagram(getOutputFolder(), e); sd.createComponents();
		 * sd.createMessages(); sd.close();
		 */
		try {
			JSUml2Story jsu2story = new JSUml2Story(jSepisodes, e);
			jsu2story.createComponents();
			jsu2story.createMessages();
			jsu2story.close();

		} catch (FileNotFoundException e1) {
			System.out.println("Error initializing print stream for allEpisodes.js");
			e1.printStackTrace();
		} catch (IOException e2) {
			System.out.println("IOException while printing episodes to JS.");
			e2.printStackTrace();
		}

	}

	/**
	 * This method sorts all four groups of trace objects into one ordered list of trace objects
	 */
	private static ArrayList<TraceObject> sortTraceObjects() {
		ArrayList<TraceObject> sortedTrace = new ArrayList<TraceObject>();

		ArrayList<Collection<TraceObject>> allCollections =
				new ArrayList<Collection<TraceObject>>();

		if (story.getDomEventTraces().size() > 0) {
			allCollections.add(story.getDomEventTraces());
		}
		if (story.getFunctionTraces().size() > 0) {
			allCollections.add(story.getFunctionTraces());
		}
		if (story.getTimingTraces().size() > 0) {
			allCollections.add(story.getTimingTraces());
		}
		if (story.getXhrTraces().size() > 0) {
			allCollections.add(story.getXhrTraces());
		}
		if (story.getMutationsAsTraceObjects().size() > 0) {
			allCollections.add(story.getMutationsAsTraceObjects());
		}
		if (story.getSeleniumAssertions().size() > 0) {
			allCollections.add(story.getSeleniumAssertions());
		}

		if (allCollections.size() == 0) {
			System.out.println("No log");
			return null;
		}



		ArrayList<Integer> currentIndexInCollection = new ArrayList<Integer>();
		for (int i = 0; i < allCollections.size(); i++) {
			currentIndexInCollection.add(0);

		}

		while (true) {
			int currentMinArray = 0;

			for (int i = 0; i < allCollections.size(); i++) {
				TraceObject traceObj =
						Iterables.get(allCollections.get(i), currentIndexInCollection.get(i));
				TraceObject currObj =
						Iterables.get(allCollections.get(currentMinArray),
								currentIndexInCollection.get(currentMinArray));
				if (traceObj.getCounter() < currObj.getCounter())
					currentMinArray = i;
			}

			sortedTrace.add(Iterables.get(allCollections.get(currentMinArray),
					currentIndexInCollection.get(currentMinArray)));

			currentIndexInCollection.set(currentMinArray,
					currentIndexInCollection.get(currentMinArray) + 1);
			if (currentIndexInCollection.get(currentMinArray) >= allCollections.get(
					currentMinArray).size()) {
				allCollections.remove(currentMinArray);
				currentIndexInCollection.remove(currentMinArray);
				if (allCollections.size() == 0)
					break;
			}
		}

		return sortedTrace;
	}

	private static ArrayList<Episode> buildEpisodes() {
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		int i, j, previousEpisodeEnd = 0;

		if (story == null)
			return episodes;

		for (i = 0; i < story.getOrderedTraceList().size(); i++) {
			// Iterate through all TraceObjects and identify episodes
			TraceObject sourceTraceObj = story.getOrderedTraceList().get(i);


			if (sourceTraceObj.isEpisodeSource()) {
				// && !(sourceTraceObj.getClass().toString().contains("TimeoutCallback"))
				// && !(sourceTraceObj.getClass().toString().contains("XMLHttpRequestResponse"))) {
				// Simple case
				// If the TraceObject is the beginning of an episode
				// i.e. DOMEvent, XHRRequest, create an episode
				Episode episode = new Episode(sourceTraceObj);

				if (sourceTraceObj instanceof SeleniumAssertionTrace) {
					// Add the newly created episode to the list of episodes
					episodes.add(episode);
					// Update i to the end of the newly created episode
					previousEpisodeEnd = i;
					continue;
				}

				for (j = i + 1; j < story.getOrderedTraceList().size(); j++) {
					// Go through the succeeding TraceObjects looking for the
					// end of the episode (as indicated by another episode starter
					// (DOMEvent, TimingEvent, etc.)

					TraceObject currentTraceObj = story.getOrderedTraceList().get(j);


					if (currentTraceObj instanceof DOMEventTrace && ((DOMEventTrace) currentTraceObj).getEventType().equals("pageLoad")) {
						// Special case: page load, start new episode
						break;
					} else if (currentTraceObj instanceof SeleniumAssertionTrace) {
						break;
					} else if (!currentTraceObj.isEpisodeSource() 
							|| (currentTraceObj.isEpisodeSource() && Math.abs(currentTraceObj.getTimeStamp() - sourceTraceObj.getTimeStamp()) <= 80)) {
						// If the succeeding TraceObject is not the beginning of
						// another episode, add it to the current episode
						episode.addToTrace(currentTraceObj);

						if ((currentTraceObj instanceof DOMMutationTrace) || (currentTraceObj instanceof DOMElementValueTrace)) {
							// Attach DOM mutations as properties
							episode.addMutation(currentTraceObj);
						} else if (currentTraceObj instanceof DOMElementAccess) {

							episode.addDomAccess(currentTraceObj);

						}
					} else /*if (currentTraceObj.isEpisodeSource() && Math.abs(currentTraceObj.getTimeStamp() - sourceTraceObj.getTimeStamp()) > 80)*/ {
						// End of current episode, break out of inner-loop
						break;
					} 
				}

				// Add the newly created episode to the list of episodes
				episodes.add(episode);
				// Update i to the end of the newly created episode
				i = j - 1;
				previousEpisodeEnd = i;

			} else if (sourceTraceObj.getClass().toString().contains("TimeoutCallback")
					|| sourceTraceObj.getClass().toString().contains("XMLHttpRequestResponse")) {
				// Special case
				// TimeoutCallback is triggered after the callback function
				// of a timeout has completed. Therefore, have to search backwards in
				// Episode.
				// e.g. FunctionEnter -> FunctionEnter -> FuntionExit -> FunctionExit ->
				// TimeoutCallback
				// As opposed to DOMEvent:
				// DOMEvent -> FunctionEnter -> FunctionEnter -> FuntionExit -> FunctionExit

				Episode episode = new Episode(sourceTraceObj);

				for (j = previousEpisodeEnd + 1; j < i; j++) {
					// Iterate from end of last episode to this TimeoutCallback
					TraceObject currentTraceObj = story.getOrderedTraceList().get(j);
					episode.addToTrace(currentTraceObj);
				}

				// Add the newly created episode to the list of episodes
				episodes.add(episode);
				previousEpisodeEnd = i;

			}

		}
		return episodes;
	}

	/**
	 * @return Name of the file.
	 */
	public static String getFilename() {
		return traceFilename;
	}

	public static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

	/**
	 * Dirty way to save program points from the proxy request threads. TODO: Frank, find cleaner
	 * way.
	 * 
	 * @param string
	 *            The JSON-text to save.
	 */
	public static void addPoint(JSONArray string) {

		JSONArray buffer = null;
		JSONObject targetAttributes = null;
		JSONObject targetElement = null;
		String JSONLabel = new String();
		int i;
		int oldCounter = 0;

		try {
			//	buffer = new JSONArray(string);
			buffer = string;


			/* save the current System.out for later usage */
			PrintStream oldOut = System.out;
			/* redirect it to the file */
			System.setOut(output);

			for (i = 0; i < buffer.length(); i++) {

				if (points.length() > 0) {
					// Add comma after previous trace object
					System.out.println(",");
				}

				points.put(buffer.getJSONObject(i));

				if (buffer.getJSONObject(i).has("args")
						&& ((String) buffer.getJSONObject(i).get("messageType"))
						.contains("FUNCTION_ENTER")) {
					try {
						JSONArray args = (JSONArray) buffer.getJSONObject(i).get("args");
						String newValue = args.toString();
						buffer.getJSONObject(i).remove("args");
						buffer.getJSONObject(i).put("args", newValue);
					} catch (JSONException jse) {
						// argument is not a JSON object
						continue;
					}
				}
				if (buffer.getJSONObject(i).has("returnValue")
						&&
						!buffer.getJSONObject(i).get("returnValue").getClass().toString()
						.contains("Null")) {
					try {
						JSONObject rv = (JSONObject) buffer.getJSONObject(i).get("returnValue");
						String newValue = rv.toString();
						buffer.getJSONObject(i).remove("returnValue");
						buffer.getJSONObject(i).put("returnValue", newValue);
					} catch (JSONException jse) {
						// argument is not a JSON object
						continue;
					}
				}
				if (buffer.getJSONObject(i).has("targetElement")) {

					JSONArray extractedArray = new JSONArray(buffer
							.getJSONObject(i).get("targetElement").toString());
					try {
						targetAttributes = extractedArray.getJSONObject(1);
						String targetType = extractedArray.get(0).toString();

						targetElement = new JSONObject("{\"elementType\":\""
								+ targetType + "\",\"attributes\":"
								+ targetAttributes.toString() + "}");

					} catch (Exception e) {
						// targetElement is not usual DOM element
						// E.g. DOMContentLoaded
						if (buffer.getJSONObject(i).has("eventType")
								&& buffer.getJSONObject(i).get("eventType")
								.toString().contains("ContentLoaded")) {
							targetElement = new JSONObject(
									"{\"elementType\":\"DOCUMENT\",\"attributes\":\"-\"}");
						} else {
							targetElement = new JSONObject(
									"{\"elementType\":\"UNKNOWN\",\"attributes\":\"-\"}");
						}
					}
					buffer.getJSONObject(i).remove("targetElement");
					buffer.getJSONObject(i).put("targetElement", targetElement.toString());
				}

				// Insert @class key for Jackson mapping
				if (buffer.getJSONObject(i).has("messageType")) {
					String mType = buffer.getJSONObject(i).get("messageType")
							.toString();


					if (mType.contains("DOM_EVENT") 
							&& buffer.getJSONObject(i).getString("eventType").equals("pageLoad")) {
						pageLoadFlag = true;
						pageLoadTime = buffer.getJSONObject(i).getLong("timeStamp");
					} else {
						pageLoadFlag = false;
					}

					// Maybe better to change mType to ENUM and use switch
					// instead of 'if's
					if (mType.contains("FUNCTION_CALL")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.FunctionCall");
						JSONLabel = "\"FunctionTrace\":";
					} else if (mType.contains("FUNCTION_ENTER")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.FunctionEnter");
						JSONLabel = "\"FunctionTrace\":";
					} else if (mType.contains("FUNCTION_EXIT")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.FunctionExit");
						JSONLabel = "\"FunctionTrace\":";
					} else if (mType.contains("RETURN_STATEMENT")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.FunctionReturnStatement");
						JSONLabel = "\"FunctionTrace\":";
					} else if (mType.contains("DOM_EVENT")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.DOMEventTrace");
						JSONLabel = "\"DOMEventTrace\":";



					} else if (mType.contains("DOM_MUTATION")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.DOMMutationTrace");
						JSONLabel = "\"DOMEventTrace\":";

						if (buffer.getJSONObject(i).getLong("timeStamp") == pageLoadTime) {
							pageLoadBuffer++;
							pageLoadFlag = true;
						}
					} else if (mType.contains("DOM_ELEMENT_VALUE")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.DOMElementValueTrace");
						JSONLabel = "\"DOMEventTrace\":";
					} else if (mType.contains("TIMEOUT_SET")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.TimeoutSet");
						JSONLabel = "\"TimingTrace\":";
					} else if (mType.contains("TIMEOUT_CALLBACK")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.TimeoutCallback");
						JSONLabel = "\"TimingTrace\":";
					} else if (mType.contains("XHR_OPEN")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.XMLHttpRequestOpen");
						JSONLabel = "\"XHRTrace\":";
					} else if (mType.contains("XHR_SEND")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.XMLHttpRequestSend");
						JSONLabel = "\"XHRTrace\":";
					} else if (mType.contains("XHR_RESPONSE")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.XMLHttpRequestResponse");
						JSONLabel = "\"XHRTrace\":";
					} else if (mType.contains("DOM_ACCESS")) {
						buffer.getJSONObject(i).put("@class",
								"com.clematis.core.trace.DOMElementAccess");
						//JSONLabel = "\"DOMAccess\":";
						JSONLabel = "\"DOMEventTrace\":";
					}
					// messageType obsolete
					buffer.getJSONObject(i).remove("messageType");

					//if (pageLoadFlag == false) {
					oldCounter = (Integer) buffer.getJSONObject(i).get("counter");
					buffer.getJSONObject(i).remove("counter");
					buffer.getJSONObject(i).put("counter", oldCounter + pageLoadBuffer);
					//}

				}

				System.out.print(JSONLabel + "["
						+ buffer.getJSONObject(i).toString(2) + "]");



			}

			/* Restore the old system.out */
			System.setOut(oldOut);


			if (i > 0) {
				//	counter = buffer.getJSONObject(buffer.length()-1).getInt("counter")+1 - pageLoadBuffer;
				counter = oldCounter+1;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private static void saveDirectAccessesToFile (Vector<org.codehaus.jettison.json.JSONArray> saveMe,
			Vector<org.codehaus.jettison.json.JSONArray> foundElements,
			Vector<String> assertionOutcomes,
			Vector<Long> assertionCounters,
			Vector<Long> assertionTimestamps,
			Vector<org.json.JSONObject> directAccesses) {

		// Accesses via By for the assertion
		Iterator<org.codehaus.jettison.json.JSONArray> eachAssertion = saveMe.iterator();
		// Elements retrieved by 'driver' for the assertion
		Iterator<org.codehaus.jettison.json.JSONArray> eachAssertion2 = foundElements.iterator();

		// Entries from the above iterators
		org.codehaus.jettison.json.JSONArray listOfAccesses;
		org.codehaus.jettison.json.JSONArray listOfElements;

		JSONObject objectToPrint = new JSONObject();
		JSONObject assertionObject;
		JSONObject accessObject;
		JSONObject elementObject;

		// File to write test case summary
		File output = new File("domAccesses.json");
		FileWriter fw = null;
		int assertionNumber = 0;

		try {
			fw = new FileWriter(output);
			// TODO: cope with exceptions being thrown during test case?
			while (eachAssertion.hasNext() && eachAssertion2.hasNext()) {
				assertionObject = new JSONObject();
				accessObject = new JSONObject();
				elementObject = new JSONObject();

				listOfAccesses = eachAssertion.next();
				for (int i = 0; i < listOfAccesses.length(); i++) {
					accessObject.put(i+"", (JSONObject) listOfAccesses.get(i));
				}

				listOfElements = eachAssertion2.next();
				for (int i = 0; i < listOfElements.length(); i++) {
					elementObject.put(i+"", (JSONObject) listOfElements.get(i));
				}

				assertionObject.put("outcome", assertionOutcomes.get(assertionNumber));
				assertionObject.put("counter", assertionCounters.get(assertionNumber));
				assertionObject.put("timeStamp", assertionTimestamps.get(assertionNumber));
				assertionObject.put("accesses", accessObject);
				assertionObject.put("elements", elementObject);

				objectToPrint.put(assertionNumber+"", assertionObject);
				assertionNumber++;
			}
			if (directAccesses.size() > 0) {
				System.out.println("trouble at " + assertionNumber);
				assertionObject = new JSONObject();
				accessObject = new JSONObject();
				elementObject = new JSONObject();

				for (int j = 0; j < directAccesses.size(); j++) {
					accessObject.put(j+"", (JSONObject) directAccesses.get(j));
				}


				for (int j = 0; j < WebDriverWrapper.foundElements.size(); j++) {
					elementObject.put(j+"", (JSONObject) WebDriverWrapper.foundElements.get(j));
				}

				assertionObject.put("outcome", "incomplete");
				assertionObject.put("accesses", accessObject);
				assertionObject.put("elements", elementObject);

				objectToPrint.put(assertionNumber+"", assertionObject);
				assertionNumber++;
			}
			fw.write(objectToPrint.toString(4));
			fw.close();
		} catch (org.json.JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.codehaus.jettison.json.JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static Vector<TraceObject> getRelevantTraceInformation(JSONObject assertionAccesses, Collection<TraceObject> traceMutations) throws Exception {
		Vector<TraceObject> returnMe = new Vector<TraceObject>();
		Iterator<String> elKeys = assertionAccesses.keys();
		JSONObject currentEl;
		Collection<TraceObject> allMutations = traceMutations;
		Iterator<TraceObject> mutationIterator;
		TraceObject currentMutation;
		int seleniumAccessCounter = -1;

		//ArrayList<TraceObject> allAccesses = s1.getDOMAccesses();
		int m = 0;
		while (elKeys.hasNext()){
			currentEl = new JSONObject(assertionAccesses.getJSONObject(elKeys.next()).toString());
			seleniumAccessCounter = currentEl.getInt("counter");
			currentEl.remove("counter");

			mutationIterator = allMutations.iterator();

			while (mutationIterator.hasNext()) {
				currentMutation = mutationIterator.next();
				if (currentMutation.getCounter() > seleniumAccessCounter) {
					// Ignore those mutations after the assertion
					continue;
				}


				//TODO: Current comparison is lazy, should compare as objects instead of strings
				if (currentMutation instanceof DOMMutationTrace && 
						commpareJSON(((DOMMutationTrace)currentMutation).getParentNode(), currentEl)) {
					returnMe.add(currentMutation);
				} else if (currentMutation instanceof DOMMutationTrace &&
						((DOMMutationTrace)currentMutation).getParentNode().has("id") &&
						(currentEl).has("id") &&
						((DOMMutationTrace)currentMutation).getParentNode().getString("id").equals(currentEl.getString("id"))) {
					returnMe.add(currentMutation);

				} else if (currentMutation instanceof DOMMutationTrace &&
						((DOMMutationTrace)currentMutation).getParentNode().has("xPath") &&
						(currentEl).has("xPath") &&
						((DOMMutationTrace)currentMutation).getParentNode().getString("xPath").equals(currentEl.getString("xPath"))) {
					returnMe.add(currentMutation);

				}
			}
		}
		System.out.println("returning size:  " + returnMe.size());
		return returnMe;
	}

	@SuppressWarnings("unchecked")
	private static boolean commpareJSON(JSONObject jso1, JSONObject jso2) {
		boolean returnMe = true;
		Iterator<String> jso1keys = jso1.sortedKeys();
		Iterator<String> jso2keys = jso2.sortedKeys();
		String key1;
		String key2;		
		try {
			while (jso1keys.hasNext()) {
				if (!jso2keys.hasNext()) {
					// Number of keys between two object is not even
					returnMe = false;
					break;
				}
				key1 = jso1keys.next();
				key2 = jso2keys.next();
				if (jso1.get(key1) instanceof JSONObject && jso2.get(key2) instanceof JSONObject) {
					// JSON objects, special case, recusively call this method (commpareJSON)
					if (!commpareJSON(jso1.getJSONObject(key1), jso2.getJSONObject(key2))) {
						returnMe = false;
						break;
					}
				} else if (jso1.get(key1) instanceof String && jso2.get(key2) instanceof String) {

					// value is String
					if (!jso1.get(key1).equals(jso2.get(key2))) {
						returnMe = false;
						break;
					}
				} else {
					// value is some other primitive (Integer, Double)
					if (jso1.get(key1) != jso2.get(key2)) {
						returnMe = false;
						break;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnMe;
	}

	private static boolean compareHTMLElements(JSONObject elem1, JSONObject elem2) {
		// Limitation, cannot track xPath for removed nodes (already removed from DOM tree)
		// Overcome: Save xPath to elements when initially added? that way they are saved in 'summary' array of MutationObserver?
		try {
			if (elem1.has("id") && elem2.has("id") && elem1.getString("id").equals(elem2.getString("id"))) {
				return true;
			} else if (elem1.has("xPath") && elem2.has("xPath") && elem1.getString("xPath").equals(elem2.getString("xPath"))) {

				return true;
			}
		} catch (JSONException e) {
			System.out.println("Can't see object");
		}
		return false;
	}

	public static int getCounter() {
		return counter;
	}

}
