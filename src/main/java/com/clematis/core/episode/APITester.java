package com.clematis.core.episode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONException;

import com.clematis.core.trace.DOMElementValueTrace;
import com.clematis.core.trace.DOMMutationTrace;
import com.clematis.core.trace.TraceObject;
import com.clematis.visual.EpisodeGraph;
import com.clematis.visual.JSUml2Story;
import com.crawljax.util.Helper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.collect.Iterables;

public class APITester {

	private static Story s1;
	private static Story s2;
	private static ObjectMapper mapper = new ObjectMapper();
	private static Map<String, Episode> episodeMap = new HashMap<String, Episode>(200);
	private static Map<String, Episode> episodeMap2 = new HashMap<String, Episode>(200);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		initialize();

		try {
			File file = new File("domAccesses.json");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr); 
			String s;
			String fileContents = "";
			JSONObject testCaseSummary;
			Iterator<String> assertions;
			String assertionID;
			JSONObject singleAssertionSummary;
			Iterator<TraceObject> mutationIterator;
			TraceObject currentMutation;
			Vector<TraceObject> relatedMutations;
			Story story;

			// Read assertion accesses and results from file and instantiate JSONObject
			while((s = br.readLine()) != null) {
				fileContents += s;
			}
			br.close();
			testCaseSummary = new JSONObject(fileContents);

			//TODO: Thursday, november 27th ---- testCaseSummary is JSONObject with assertion results and accesses, use to find relevant information in trace

			s1.getDomEventTraces().addAll(s1.getDomMutations().values());

			// Iterate through all assertions from test case/suite run
			assertions = testCaseSummary.keys();
			while (assertions.hasNext()) {
				assertionID = assertions.next();
				singleAssertionSummary = (JSONObject) testCaseSummary.get(assertionID);

				// FINDING RELEVANT TraceObjects
				relatedMutations = getRelevantTraceInformation2(singleAssertionSummary.getJSONObject("elements"));

				for (int k = 0; k < relatedMutations.size(); k++) {
					((DOMMutationTrace) (relatedMutations.get(k))).mapToAssertion(assertionID);
					System.out.println("Counter:  " + ((DOMMutationTrace) (relatedMutations.get(k))).getCounter());
				}



				if (((String) singleAssertionSummary.get("outcome")).contains("false")) {
					// Assertion was a failure, find relevant JS from Clematis trace

					// get the assertion object "accesses"

					// for each assertion 

					// get its "method" e.g. cssselector

					// get its argument e.g. span#ss_n

					// look for related information in trace (DOMAccess and DOMMutation)

				} else if (((String) singleAssertionSummary.get("outcome")).contains("incomplete")) {
					// Test case did not complete due to exception (ElementNotFound), find what happened to the expected element

				}
			}



			story = new Story(s1.getDomEventTraces(), s1.getFunctionTraces(), s1.getTimingTraces(), s1.getXhrTraces(), null);
			story.setOrderedTraceList(sortTraceObjects(story));
			story.setEpisodes(buildEpisodes(story));

			// JavaScript episodes for JSUML2
			PrintStream JSepisodes =
					new PrintStream("allEpisodes.js");

			for (Episode e : story.getEpisodes()) {
				// Create pic files for each episode's sequence diagram
				designSequenceDiagram(e, JSepisodes);
			}

			// Once all episodes have been saved to JS file, close
			JSepisodes.close();

			// Create graph containing all episodes with embedded sequence diagrams
			EpisodeGraph eg = new EpisodeGraph("~/clematis/", story.getEpisodes());
			eg.createGraph();

			mapper.writeValue(new File("story.json"),
					story);
			mapper.writeValue(new File("story_old.json"),
					s1);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.codehaus.jettison.json.JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		ArrayList<Episode> s1Episodes = s1.getEpisodes();
		//ArrayList<Episode> s2Episodes = s2.getEpisodes();


		/*	for (int i = 0; i < s1Episodes.size(); i++) {
			System.out.println(s1Episodes.get(i).getClass());
			mutationIterator = s1Episodes.get(i).getDom().iterator();
			while (mutationIterator.hasNext()) {
				currentMutation = mutationIterator.next();
				if (currentMutation != null) {

					System.out.println("Retain class?    " + currentMutation.getClass());
					System.out.println(((DOMMutationTrace) currentMutation).getMutationAsJSON());
				} 
			}
		}

		s1.getEpisodes();*/
		System.out.println("DoNe!");
	}

	private static void intialize() {
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance()
				.withFieldVisibility(
						Visibility.ANY));
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// to allow coercion of JSON empty String ("") to null Object value:
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		try {
			System.out.println("Mapper 1");
			s1 = mapper.readValue(new File("story.json"),
					Story.class);
			/*	System.out.println("Mapper 2");
			s2 = mapper.readValue(new File("story2.json"),
					Story.class);*/
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < s1.getEpisodes().size(); i++) {
			episodeMap.put(Integer.toString(i), s1.getEpisodes().get(i));
		}

		/*for (int j = 0; j < s2.getEpisodes().size(); j++) {
			episodeMap2.put(Integer.toString(j), s2.getEpisodes().get(j));
		}*/
	}

	public static String initialize() {
		int i;

		configureObjectMapper();
		try {
			s1 = mapper.readValue(new File("story.json"),
					Story.class);
			/*	this.s2 = mapper.readValue(new File("story2.json"),
					Story.class);*/
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (i = 0; i < s1.getEpisodes().size(); i++) {
			episodeMap.put(Integer.toString(i), s1.getEpisodes().get(i));
		}

		/*for (i = 0; i < s2.getEpisodes().size(); i++) {
			episodeMap2.put(Integer.toString(i), s2.getEpisodes().get(i));
		}*/

		return "successfully intialized story";

	}

	public static void configureObjectMapper() {
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance()
				.withFieldVisibility(
						Visibility.ANY));
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// to allow coercion of JSON empty String ("") to null Object value:
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	}



	@SuppressWarnings("unchecked")
	private static Vector<TraceObject> getRelevantTraceInformation2(JSONObject allElements) throws Exception {
		Vector<TraceObject> returnMe = new Vector<TraceObject>();
		Iterator<String> elKeys = allElements.keys();
		JSONObject currentEl;
		ArrayList<TraceObject> allMutations = s1.getDomEventTraces();
		ArrayList<TraceObject> allAccesses = s1.getDOMAccesses();

		while (elKeys.hasNext()){
			currentEl = allElements.getJSONObject(elKeys.next());
			System.out.println(currentEl.toString());
			for (int i = 0; i < allMutations.size(); i++) {
				//TODO: Current comparison is lazy, should compare as objects instead of strings
				System.out.println(allMutations.get(i).getClass().toString());
				if (allMutations.get(i) instanceof DOMMutationTrace && 
						((DOMMutationTrace)allMutations.get(i)).getParentNode().toString().equals(currentEl.toString())) {
					System.out.println("==============");
					System.out.println("found one?");
					System.out.println(currentEl.toString());
					System.out.println(((DOMMutationTrace)allMutations.get(i)).getParentNode().toString());
					System.out.println("==============");
					returnMe.add(allMutations.get(i));
				} 
			}
		}

		// Check for related accesses in the trace
		/*	for (int j = 0; j < allAccesses.size(); j++) {

		}
		 */
		return returnMe;
	}


	private static ArrayList<TraceObject> sortTraceObjects(Story story) {
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

	private static ArrayList<Episode> buildEpisodes(Story story) {
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

				for (j = i + 1; j < story.getOrderedTraceList().size(); j++) {
					// Go through the succeeding TraceObjects looking for the
					// end of the episode (as indicated by another episode starter
					// (DOMEvent, TimingEvent, etc.)

					TraceObject currentTraceObj = story.getOrderedTraceList().get(j);

					if (Math.abs(currentTraceObj.getTimeStamp() - sourceTraceObj.getTimeStamp()) < 80) {
						// If the succeeding TraceObject is not the beginning of
						// another episode, add it to the current episode
						episode.addToTrace(currentTraceObj);

						if ((currentTraceObj instanceof DOMMutationTrace) || (currentTraceObj instanceof DOMElementValueTrace)) {
							// Attach DOM mutations as properties
							episode.addMutation(currentTraceObj);
						}
					} else {
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
}
