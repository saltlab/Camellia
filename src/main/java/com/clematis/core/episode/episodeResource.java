package com.clematis.core.episode;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.clematis.core.trace.DOMElementValueTrace;
import com.clematis.core.trace.DOMEventTrace;
import com.clematis.core.trace.DOMMutationTrace;
import com.clematis.core.trace.FunctionCall;
import com.clematis.core.trace.FunctionEnter;
import com.clematis.core.trace.FunctionExit;
import com.clematis.core.trace.FunctionReturnStatement;
import com.clematis.core.trace.FunctionTrace;
import com.clematis.core.trace.SeleniumAssertionTrace;
import com.clematis.core.trace.TimeoutCallback;
import com.clematis.core.trace.TimeoutSet;
import com.clematis.core.trace.TimingTrace;
import com.clematis.core.trace.TraceObject;
import com.clematis.core.trace.XMLHttpRequestOpen;
import com.clematis.core.trace.XMLHttpRequestResponse;
import com.clematis.core.trace.XMLHttpRequestSend;
import com.clematis.core.trace.XMLHttpRequestTrace;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

@Path("/clematis-api")
@Produces({ "application/json" })
public class episodeResource {

	private Story s1;
	private Story s2;

	private long lastModified = -1;

	private File f1 = null;
	private File f2 = null;

	private ObjectMapper mapper = new ObjectMapper();
	private Map<String, Episode> episodeMap = new HashMap<String, Episode>(200);
	private Map<String, Episode> episodeMap2 = new HashMap<String, Episode>(200);

	public void configureObjectMapper() {
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


	public String initialize() {
		int i;

		configureObjectMapper();
		try {
			f2 = new File("story.json");

			// Used cached/saved story, no need to reinitialize
			if (f2.lastModified() == lastModified) {
				return "successfully intialized story";
			}

			this.s1 = mapper.readValue(f2,
					Story.class);

			/*	this.s2 = mapper.readValue(new File("story2.json"),
					Story.class);*/
			lastModified = f2.lastModified();

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
		f1 = f2;

		return "successfully intialized story";

	}

	@GET
	@Path("/episodes/howmany")
	@Produces(MediaType.APPLICATION_JSON)
	public int NumberOfEpisodes() {
		initialize();
		return this.s1.getEpisodes().size();
	}

	@GET
	@Path("/episodes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Episode> getEpisodes() {
		initialize();
		return this.s1.getEpisodes();

	}

	@GET
	@Path("/episodes/bookmarked")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Integer> getBookmarks() {
		initialize();
		List<Integer> b1 = new ArrayList<Integer>();
		for (int i = 0; i < this.s1.getEpisodes().size(); i++) {
			if (this.s1.getEpisodes().get(i).getIsBookmarked() == true) {
				b1.add(i);
			}
		}
		return b1;

	}

	@GET
	@Path("/episodes/pretty")
	@Produces(MediaType.APPLICATION_JSON)
	public String getEpisodesPretty() {
		initialize();
		try {
			return mapper.writeValueAsString(this.s1.getEpisodes());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("/episodes/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Episode getEpisode(@PathParam("id") String id) {
		initialize();
		return episodeMap.get(id);
	}

	@GET
	@Path("/episodes/{id}/DOM")
	@Produces(MediaType.APPLICATION_JSON)
	public String getEpisodeDom(@PathParam("id") String id)
	{
		initialize();

		if (episodeMap.get(id).getDom() == null) {
			return "DOM is NULL!";
		}
		else {
			return episodeMap.get(id).getDom().toString();
		}
	}

	@GET
	@Path("/episodes/{id}/source")
	@Produces(MediaType.APPLICATION_JSON)
	public TraceObject getEpisodeSource(@PathParam("id") String id) {
		initialize();
		if (episodeMap.get(id).getSource() instanceof SeleniumAssertionTrace) {
			return (SeleniumAssertionTrace)	episodeMap.get(id).getSource();
		} else if (episodeMap.get(id).getSource() instanceof DOMEventTrace) {
			return (DOMEventTrace)	episodeMap.get(id).getSource();
		} else {
			return 	episodeMap.get(id).getSource();
		}
	}

	@GET
	@Path("/episodes/{id}/trace")
	@Produces(MediaType.APPLICATION_JSON)
	public EpisodeTrace getEpisodeTrace(@PathParam("id") String id) {
		initialize();
		return episodeMap.get(id).getTrace();
	}

	// ///////////////////Resources to get information about traces.////////////////
	/*
	 * @GET
	 * @Path("/episodes/{id}/trace/{type}")
	 * @Produces(MediaType.APPLICATION_JSON) public List<TraceObject> getStuff(@PathParam("id")
	 * String id, @PathParam("type") String type) { intialize(); List<FunctionTrace> functionTraces
	 * = new ArrayList<FunctionTrace>(); List<DOMMutationTrace> DOMMutationTraces = new
	 * ArrayList<DOMMutationTrace>(); List<DOMElementValueTrace> DOMElementValueTraces = new
	 * ArrayList<DOMElementValueTrace>(); List<XMLHttpRequestTrace> XMLHttpRequestTraces = new
	 * ArrayList<XMLHttpRequestTrace>(); List<TimingTrace> TimingTraces = new
	 * ArrayList<TimingTrace>(); List<DOMEventTrace> DOMEventTraces = new
	 * ArrayList<DOMEventTrace>(); for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
	 * if (to instanceof FunctionTrace) { functionTraces.add((FunctionTrace) to); } else if (to
	 * instanceof DOMMutationTrace) { DOMMutationTraces.add((DOMMutationTrace) to); } else if (to
	 * instanceof DOMElementValueTrace) { DOMElementValueTraces.add((DOMElementValueTrace) to); }
	 * else if (to instanceof XMLHttpRequestTrace) { XMLHttpRequestTraces.add((XMLHttpRequestTrace)
	 * to); } else if (to instanceof TimingTrace) { TimingTraces.add((TimingTrace) to); } else if
	 * (to instanceof DOMEventTrace) { DOMEventTraces.add((DOMEventTrace) to); } } if (type ==
	 * "functionTrace") { return functionTraces; } else if (type == "DOMMutationTrace") { return
	 * DOMMutationTraces; } }
	 */
	@GET
	@Path("/episodes/{id}/trace/functionTrace")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionTrace> getFunctionTrace(@PathParam("id") String id) {
		initialize();
		List<FunctionTrace> functionTraces = new ArrayList<FunctionTrace>();

		for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
			if (to instanceof FunctionTrace) {
				functionTraces.add((FunctionTrace) to);
			}
		}
		return functionTraces;
	}

	@GET
	@Path("/episodes/{id}/trace/DOMMutationTrace")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DOMMutationTrace> getDOMMutationTrace(@PathParam("id") String id) {
		initialize();
		List<DOMMutationTrace> DOMMutationTraces = new ArrayList<DOMMutationTrace>();

		for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
			if (to instanceof DOMMutationTrace) {
				DOMMutationTraces.add((DOMMutationTrace) to);
			}
		}
		return DOMMutationTraces;
	}

	@GET
	@Path("/episodes/{id}/trace/DOMElementValueTrace")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DOMElementValueTrace> getDOMElementValueTrace(@PathParam("id") String id) {
		initialize();
		List<DOMElementValueTrace> DOMElementValueTraces = new ArrayList<DOMElementValueTrace>();

		for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
			if (to instanceof DOMElementValueTrace) {
				DOMElementValueTraces.add((DOMElementValueTrace) to);
			}
		}
		return DOMElementValueTraces;
	}

	@GET
	@Path("/episodes/{id}/trace/XMLHttpRequestTrace")
	@Produces(MediaType.APPLICATION_JSON)
	public List<XMLHttpRequestTrace> getXMLHttpRequestTrace(@PathParam("id") String id) {
		initialize();
		List<XMLHttpRequestTrace> XMLHttpRequestTraces = new ArrayList<XMLHttpRequestTrace>();

		for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
			if (to instanceof XMLHttpRequestTrace) {
				XMLHttpRequestTraces.add((XMLHttpRequestTrace) to);
			}
		}
		return XMLHttpRequestTraces;
	}

	@GET
	@Path("/episodes/{id}/trace/TimingTrace")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TimingTrace> getTimingTrace(@PathParam("id") String id) {
		initialize();
		List<TimingTrace> TimingTraces = new ArrayList<TimingTrace>();

		for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
			if (to instanceof TimingTrace) {
				TimingTraces.add((TimingTrace) to);
			}
		}
		return TimingTraces;
	}

	@GET
	@Path("/episodes/{id}/trace/DOMEventTrace")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DOMEventTrace> getDOMEventTrace(@PathParam("id") String id) {
		initialize();
		List<DOMEventTrace> DOMEventTraces = new ArrayList<DOMEventTrace>();

		for (TraceObject to : episodeMap.get(id).getTrace().getTrace()) {
			if (to instanceof DOMEventTrace) {
				DOMEventTraces.add((DOMEventTrace) to);
			}
		}
		return DOMEventTraces;
	}

	// //////////////////////////////////////////////////////////////////////////////////

	// ///////////////////Resources to get information about function traces.////////////////
	@GET
	@Path("/episodes/{id}/trace/functionTrace/FunctionCall")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionCall> getFunctionCall(@PathParam("id") String id) {
		initialize();
		List<FunctionCall> FunctionCalls = new ArrayList<FunctionCall>();

		for (TraceObject to : getFunctionTrace(id)) {
			if (to instanceof FunctionCall) {
				FunctionCalls.add((FunctionCall) to);
			}
		}
		return FunctionCalls;
	}

	@GET
	@Path("/episodes/{id}/trace/functionTrace/FunctionEnter")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionEnter> getFunctionEnter(@PathParam("id") String id) {
		initialize();
		List<FunctionEnter> FunctionEnters = new ArrayList<FunctionEnter>();

		for (TraceObject to : getFunctionTrace(id)) {
			if (to instanceof FunctionEnter) {
				FunctionEnters.add((FunctionEnter) to);
			}
		}
		return FunctionEnters;
	}

	@GET
	@Path("/episodes/{id}/trace/functionTrace/FunctionExit")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionExit> getFunctionExit(@PathParam("id") String id) {
		initialize();
		List<FunctionExit> FunctionExits = new ArrayList<FunctionExit>();

		for (TraceObject to : getFunctionTrace(id)) {
			if (to instanceof FunctionExit) {
				FunctionExits.add((FunctionExit) to);
			}
		}
		return FunctionExits;
	}

	@GET
	@Path("/episodes/{id}/trace/functionTrace/FunctionReturnStatement")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionReturnStatement> getFunctionReturnStatement(@PathParam("id") String id) {
		initialize();
		List<FunctionReturnStatement> FunctionReturnStatements =
				new ArrayList<FunctionReturnStatement>();

		for (TraceObject to : getFunctionTrace(id)) {
			if (to instanceof FunctionReturnStatement) {
				FunctionReturnStatements.add((FunctionReturnStatement) to);
			}
		}
		return FunctionReturnStatements;
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	// ///////////////////Resources to get information about timing traces.////////////////

	@GET
	@Path("/episodes/{id}/trace/TimingTrace/TimeoutCallback")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TimeoutCallback> getTimeoutCallback(@PathParam("id") String id) {
		initialize();
		List<TimeoutCallback> TimeoutCallbacks = new ArrayList<TimeoutCallback>();

		for (TraceObject to : getTimingTrace(id)) {
			if (to instanceof TimeoutCallback) {
				TimeoutCallbacks.add((TimeoutCallback) to);
			}
		}
		return TimeoutCallbacks;
	}

	@GET
	@Path("/episodes/{id}/trace/TimingTrace/TimeoutSet")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TimeoutSet> getTimeoutSet(@PathParam("id") String id) {
		initialize();
		List<TimeoutSet> TimeoutSets = new ArrayList<TimeoutSet>();

		for (TraceObject to : getTimingTrace(id)) {
			if (to instanceof TimeoutSet) {
				TimeoutSets.add((TimeoutSet) to);
			}
		}
		return TimeoutSets;
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	// ///////////////////Resources to get information about XMLHTTPRequest traces.////////////////

	@GET
	@Path("/episodes/{id}/trace/XMLHttpRequestTrace/XMLHttpRequestOpen")
	@Produces(MediaType.APPLICATION_JSON)
	public List<XMLHttpRequestOpen> getXMLHttpRequestOpen(@PathParam("id") String id) {
		initialize();
		List<XMLHttpRequestOpen> XMLHttpRequestOpens = new ArrayList<XMLHttpRequestOpen>();

		for (TraceObject to : getXMLHttpRequestTrace(id)) {
			if (to instanceof XMLHttpRequestOpen) {
				XMLHttpRequestOpens.add((XMLHttpRequestOpen) to);
			}
		}
		return XMLHttpRequestOpens;
	}

	@GET
	@Path("/episodes/{id}/trace/XMLHttpRequestTrace/XMLHttpRequestResponse")
	@Produces(MediaType.APPLICATION_JSON)
	public List<XMLHttpRequestResponse> getXMLHttpRequestResponse(@PathParam("id") String id) {
		initialize();
		List<XMLHttpRequestResponse> XMLHttpRequestResponses =
				new ArrayList<XMLHttpRequestResponse>();

		for (TraceObject to : getXMLHttpRequestTrace(id)) {
			if (to instanceof XMLHttpRequestResponse) {
				XMLHttpRequestResponses.add((XMLHttpRequestResponse) to);
			}
		}
		return XMLHttpRequestResponses;
	}

	@GET
	@Path("/episodes/{id}/trace/XMLHttpRequestTrace/XMLHttpRequestSend")
	@Produces(MediaType.APPLICATION_JSON)
	public List<XMLHttpRequestSend> getXMLHttpRequestSend(@PathParam("id") String id) {
		initialize();
		List<XMLHttpRequestSend> XMLHttpRequestSends = new ArrayList<XMLHttpRequestSend>();

		for (TraceObject to : getXMLHttpRequestTrace(id)) {
			if (to instanceof XMLHttpRequestSend) {
				XMLHttpRequestSends.add((XMLHttpRequestSend) to);
			}
		}
		return XMLHttpRequestSends;
	}

	@GET
	@Path("/story/timingTraces")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TraceObject> getTimingTraces() {
		initialize();
		return this.s1.getTimingTraces();
	}

	@GET
	@Path("/story/domEventTraces")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TraceObject> getDomEventTraces() {
		initialize();
		return this.s1.getDomEventTraces();
	}

	@GET
	@Path("/story/XHRTraces")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TraceObject> getXHRTraces() {
		initialize();
		return this.s1.getXhrTraces();
	}

	@GET
	@Path("/story/functionTraces")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TraceObject> getFunctionTraces() {
		initialize();
		return this.s1.getFunctionTraces();
	}

	// need to find which episodes have timeouts, then need to find corresponding callbacks
	@GET
	@Path("/story/causalLinks")
	@Produces(MediaType.APPLICATION_JSON)
	public List<causalLinks> episodesContainTimeouts() {
		System.out.println("[causalLinks]");

		initialize();
		List<causalLinks> causalLinkss = new ArrayList<causalLinks>();

		for (int i = 0; i < episodeMap.size(); i++) {
			String strI = "" + i;
			getTimeoutSet(strI);
			// if episode contains a timeout, find the corresponding callback
			if (getTimeoutSet(strI).size() > 0) {
				for (int x = 0; x < getTimeoutSet(strI).size(); x++) {
					for (int z = 0; z < episodeMap.size(); z++) {
						String strZ = "" + z;
						if (getTimeoutCallback(strZ).size() > 0)
						{
							for (int zz = 0; zz < getTimeoutCallback(strZ).size(); zz++) {
								if (getTimeoutSet(strI).get(x).getId() == getTimeoutCallback(strZ)
										.get(zz).getId()) {
									causalLinkss.add(new causalLinks(i, z));
								}
							}
						}
					}
				}
			}

			if (getXMLHttpRequestOpen(strI).size() > 0) {
				for (int x = 0; x < getXMLHttpRequestOpen(strI).size(); x++) {
					for (int z = 0; z < episodeMap.size(); z++) {
						String strZ = "" + z;
						if (getXMLHttpRequestResponse(strZ).size() > 0)
						{
							for (int zz = 0; zz < getXMLHttpRequestResponse(strZ).size(); zz++) {
								if (getXMLHttpRequestOpen(strI).get(x).getId() == getXMLHttpRequestResponse(
										strZ)
										.get(zz).getId()) {
									causalLinkss.add(new causalLinks(i, z));
								}
							}
						}
					}
				}
			}

			if (episodeMap.get(strI).getRelatedAssertions().size() > 0) {
				Vector<Integer> assertionsToLink = episodeMap.get(strI).getRelatedAssertions();
				// Iterate through related assertions

				for (int k = 0; k < assertionsToLink.size(); k++) {
					// Find them in  story, add a link
					for (int j = 0; j < s1.getEpisodes().size(); j++) {

						if (s1.getEpisodes().get(j).getSource() instanceof SeleniumAssertionTrace
								&& ((SeleniumAssertionTrace) s1.getEpisodes().get(j).getSource()).getAssertionID() == assertionsToLink.get(k)) {
							causalLinkss.add(new causalLinks(i, j));
						}
					}
				}
			}
		}


		return causalLinkss;

	}

	@GET
	@Path("/story/sequenceDiagram")
	@Produces(MediaType.APPLICATION_JSON)
	public String getsequenceDiagram() {
		initialize();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		for (Episode e : this.s1.getEpisodes()) {
			// Create pic files for each episode's sequence diagram
			com.clematis.jsmodify.JSExecutionTracer.designSequenceDiagram(e, ps);
		}
		String output = null;
		try {
			output = os.toString("UTF8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// System.out.println(output);
		ps.close();
		return output;
	}

	@GET
	@Path("/story/sequenceDiagram2")
	@Produces(MediaType.APPLICATION_JSON)
	public String getsequenceDiagram2() {
		String output = null;
		try {
			output =
					new Scanner(new File(
							"clematis-output/ftrace/sequence_diagrams/allEpisodes.js"))
			.useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("" + output);
		return output;
	}

	@GET
	@Path("/story/compareStories")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<String> getDifference() {
		initialize();

		ArrayList<String> returnMe = new ArrayList<String>();

		ArrayList<Episode> s1Episodes = this.s1.getEpisodes();
		ArrayList<Episode> s2Episodes = this.s2.getEpisodes();

		Episode s1CurrentEpisode;
		Episode s2CurrentEpisode;

		for (int i = 0; i < s1Episodes.size(); i++) {
			s1CurrentEpisode = s1Episodes.get(i);
			for (int j = 0; j < s2Episodes.size(); j++) {
				s2CurrentEpisode = s2Episodes.get(j);
				// Look for episode in other story that has same source
				if (compareEpisodes(s1CurrentEpisode, s2CurrentEpisode)) {

				}



			}
		}

		s1CurrentEpisode = s1Episodes.get(0);
		s2CurrentEpisode = s2Episodes.get(0);

		/*	returnMe.add((s1CurrentEpisode.getSource() instanceof DOMEventTrace ? "DOMEventTrace\n" : "not DOMEventTrace\n"));
		returnMe.add((s1CurrentEpisode.getSource() instanceof TimingTrace ? "TimingTrace\n" : "not TimingTrace\n"));
		returnMe.add((s1CurrentEpisode.getSource() instanceof XMLHttpRequestTrace ? "XMLHttpRequestTrace\n" : "not XMLHttpRequestTrace\n"));


		returnMe.add((s2CurrentEpisode.getSource() instanceof DOMEventTrace ? "DOMEventTrace\n" : "not DOMEventTrace\n"));
		returnMe.add((s2CurrentEpisode.getSource() instanceof TimingTrace ? "TimingTrace\n" : "not TimingTrace\n"));
		returnMe.add((s2CurrentEpisode.getSource() instanceof XMLHttpRequestTrace ? "XMLHttpRequestTrace\n" : "not XMLHttpRequestTrace\n"));

		returnMe.add(s1CurrentEpisode.getSource().getClass().toString());
		returnMe.add(s2CurrentEpisode.getSource().getClass().toString());*/

		returnMe.add(s1CurrentEpisode.getDom().toString());
		returnMe.add(s2CurrentEpisode.getDom().toString());

		return returnMe;
	}

	private boolean compareEpisodes(Episode e1, Episode e2) {

		TraceObject to1 = e1.getSource();
		TraceObject to2 = e2.getSource();

		if (to1.getClass().toString() == to2.getClass().toString()) {

		} else {

		}

		return false;
	}

	private ArrayList<JSONObject> compareSources(TraceObject to1, TraceObject to2) {
		ArrayList<JSONObject> sourceDiff = new ArrayList<JSONObject>();
		try {

			if (to1.getClass().toString() != to2.getClass().toString()) {
				// Check that the types are the same first and foremost (DOMEventTrace, TimingTrace, etc.)
				JSONObject sourceType = new JSONObject();
				sourceType.put("class", new JSONObject());
				((JSONObject) sourceType.get("class")).put("1", to1.getClass().toString());
				((JSONObject) sourceType.get("class")).put("2", to2.getClass().toString());
				sourceDiff.add(sourceType);			
				return sourceDiff;
			}

			// TODO: Double check these types
			if (to1.getClass().toString().contains("DOMEventTrace")) {
				// DOM Event
				if (((DOMEventTrace) to1).getEventType() != ((DOMEventTrace) to2).getEventType()) {
					// type
					JSONObject eventType = new JSONObject();
					eventType.put("eventType", new JSONObject());
					((JSONObject) eventType.get("eventType")).put("1", to1.getClass().toString());
					((JSONObject) eventType.get("eventType")).put("2", to2.getClass().toString());
					sourceDiff.add(eventType);			
					return sourceDiff;
				} 
				/*		if () {
					// handler

				}
				if () {
					// target

				}*/
			} else if (to1.getClass().toString().contains("TimingTrace")) {
				// setTimeout callback
				/*		if () {
					// callback

				} 
				if () {
					// timeout id

				} */

			} else if (to1.getClass().toString().contains("XMLHttpRequestTrace")) {
				// XMLHttpRequest Response
				/*		if () {
					// xhrid

				} 
				if () {
					// callback

				}
				if () {
					// response

				}*/
			} else {
				// Unsupported source type
				System.out.println("[compareSources]: Unsupported episode source.");
				JSONObject sourceType = new JSONObject();
				sourceType.put("type", new JSONObject());
				((JSONObject) sourceType.get("type")).put("1", to1.getClass().toString());
				((JSONObject) sourceType.get("type")).put("2", to2.getClass().toString());

				sourceDiff.add(sourceType);			
				return sourceDiff;
			}
		} catch (JSONException e) {
			System.out.println("[compareSources]: Error diffing sources.");
			e.printStackTrace();
		}

		return sourceDiff;
	}


	@GET
	@Path("/assertion/failure/Level1")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getFailureLevel1() {

		try {

			JSONObject testCaseSummary = getTestCaseSummary();
			Iterator<String> keys = testCaseSummary.keys();
			String nextString = "";
			JSONObject nextAssertion;

			while (keys.hasNext()) {
				nextString = keys.next();
				nextAssertion = testCaseSummary.getJSONObject(nextString);
				// Found the failure
				if (nextAssertion.has("outcome") && !nextAssertion.get("outcome").equals("true")) {
					// Should have level 2 function names added to the summary at this point
					return nextAssertion.getJSONArray("level2");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new JSONArray();
	}

	@GET
	@Path("/assertion/failure/domelement")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getDependentElement() {
		JSONObject returnMe = new JSONObject();
		JSONObject nextAssertion = null;

		try {
			JSONObject testCaseSummary = getTestCaseSummary();
			Iterator<String> keys = testCaseSummary.keys();
			String nextString = "";

			while (keys.hasNext()) {
				nextString = keys.next();
				nextAssertion = testCaseSummary.getJSONObject(nextString);
				// Found the failure
				if (nextAssertion.has("outcome") && !nextAssertion.get("outcome").equals("true")) {
					// Latest access/element returned is the dependency
					JSONObject elements = nextAssertion.getJSONObject("elements");
					Iterator<String> elementIDs = elements.keys();
					int currentCounter = -1;
					String nextID = "";

					while (elementIDs.hasNext()) {
						nextID = elementIDs.next();
						if (elements.getJSONObject(nextID).getInt("counter") > currentCounter) {
							currentCounter = elements.getJSONObject(nextID).getInt("counter");
							returnMe = elements.getJSONObject(nextID);
						}
					}

				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (returnMe.has("counter")) {
			returnMe.remove("counter");
		}
		if (returnMe.has("child")) {
			returnMe.remove("child");
		}
		
		// Append Selenium/test case failure message
		try {
			returnMe.put("message", nextAssertion.get("outcome"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return returnMe;
	}

	private JSONObject getTestCaseSummary() {
		try {
			File file = new File("domAccesses.json");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr); 
			String ss = "";
			String fileContents = "";

			// Read assertion accesses and results from file and instantiate JSONObject
			while((ss = br.readLine()) != null) {
				fileContents += ss + "\n";
			}
			br.close();

			return new JSONObject(fileContents);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject();
	}

}
