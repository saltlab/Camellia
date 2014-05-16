package com.clematis.core.episode;

import java.util.ArrayList;
import java.util.Vector;

import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONObject;

//import org.codehaus.jettison.json.JSONObject;

import com.clematis.core.trace.DOMElementValueTrace;
import com.clematis.core.trace.DOMMutationTrace;
import com.clematis.core.trace.TraceObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
public class Episode {
	// private EpisodeSource source;
	private TraceObject source;
	private EpisodeTrace trace;
	private ArrayList<TraceObject> dom = new ArrayList<TraceObject>();
	private ArrayList<TraceObject> domAccesses = new ArrayList<TraceObject>();
	private boolean isBookmarked;
	private Vector<Integer> relatedAssertions = new Vector<Integer>();

	public Episode() {

	}
	
	public void addRelatedAssertion (int i) {
		this.relatedAssertions.add(i);
	}
	
	public Vector<Integer> getRelatedAssertions () {
		return relatedAssertions;
	}

	/*
	 * public Episode(EpisodeSource source) { trace = new EpisodeTrace(); }
	 */
	public Episode(TraceObject source) {
		trace = new EpisodeTrace();
		this.source = source;
	}

	public void addToTrace(TraceObject to) {
		trace.addToTrace(to);
	}

	/*
	 * public EpisodeSource getSource() { return source; } public void setSource(EpisodeSource
	 * source) { this.source = source; }
	 */public EpisodeTrace getTrace() {
		return trace;
	}

	public void setTrace(EpisodeTrace trace) {
		this.trace = trace;
	}

	@JsonIgnore
	public ArrayList<TraceObject> getDom() {
		return dom;
	}

	public void setDom(ArrayList<TraceObject> dom) {
		System.out.println("[setDom]");
		System.out.println(dom);
		this.dom = dom;
	}
	
	public ArrayList<TraceObject> getDomAccesses() {
		return domAccesses;
	}

	public void setDomAccesses(ArrayList<TraceObject> domA) {
		this.domAccesses = domA;
	}
	
	public void addDomAccess(TraceObject da) {
		this.domAccesses.add(da);				
	}
	
	public void addMutation(TraceObject mutation) {
	// TODO: Test	
		if (mutation instanceof DOMMutationTrace) {
			dom.add((DOMMutationTrace) mutation);
		} else if (mutation instanceof DOMElementValueTrace) {
			dom.add((DOMElementValueTrace) mutation);
		} else {
			System.out.println("[Episode.addMutation]: Argument is not a mutation object.");
			return;
		}				
	}

	public TraceObject getSource() {
		return source;
	}

	public void setSource(TraceObject source) {
		this.source = source;
	}
	
	public boolean getIsBookmarked() {
		return isBookmarked;
	}

	public void setIsBookmarked(boolean isBookmarked) {
		this.isBookmarked = isBookmarked;
	}
}
