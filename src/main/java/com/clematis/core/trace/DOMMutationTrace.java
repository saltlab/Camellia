package com.clematis.core.trace;

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

//import org.codehaus.jettison.json.JSONObject;
//import org.json.JSONException;

public class DOMMutationTrace extends TraceObject/* implements EpisodeSource */{
	private String mutationType;
	private String data;
	private String nodeName;
	private String nodeValue;
	private String nodeType;
	private JSONObject parentNodeValue = new JSONObject();
	private String relatedAssertions = "";

	public DOMMutationTrace() {
		super();
		setEpisodeSource(false);
	}

	public String getMutationType() {
		return mutationType;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setMutationType(String mutationType) {
		this.mutationType = mutationType;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeValue() {
		return nodeValue;
	}

	public void setNodeValue(String nodeValue) {
		this.nodeValue = nodeValue;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getParentNodeValue() {
		try {
	/*		if (parentNodeValue == null) {
				System.out.println("[DOMMutationTrace]: parentNodeValue is null");	
				return "";
			}*/
			return parentNodeValue.toString(4);
		} catch (JSONException e) {
			System.out.println("Error serializing parent node!");
			return "";
		}
	}


	/*public JSONObject getParentNodeValue() {
		return parentNodeValue;
	}*/

	/*	public void setParentNodeValue(String parentNodeValue) {
		if (parentNodeValue.contains("{") && parentNodeValue.contains("}")) {
			System.out.println("setParentNodeValue:   " + parentNodeValue);
			System.out.println(parentNodeValue.substring(parentNodeValue.indexOf("{"), parentNodeValue.lastIndexOf("}")+1));

			try {
				this.parentNodeValue = new JSONObject(parentNodeValue.substring(parentNodeValue.indexOf("{"), parentNodeValue.lastIndexOf("}")+1));
			} catch (JSONException e) {
				System.out.println("[DOMMutationTrace.setParentNodeValue]: Error setting parent node for DOM mutation.");
				e.printStackTrace();
			}
		} else {
			this.parentNodeValue = new JSONObject();
		}
		try {
			System.out.println("keys:    " + this.parentNodeValue.toString(4));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	/*	@JsonSetter("parentNodeValue")
	public void setParentNodeValue(final JSONObject parentNodeValue) {
		System.out.println("carr");
		try {
			System.out.println(parentNodeValue.toString(4));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.parentNodeValue = parentNodeValue;
	}*/

	@JsonSetter("parentNodeValue")
	public void setParentNodeValue(String parentNodeValue_string) {
		try {
			this.parentNodeValue = new JSONObject(parentNodeValue_string);
		} catch (JSONException e) {
			//e.printStackTrace();
			this.parentNodeValue = new JSONObject();
			try {
				parentNodeValue.put("value", parentNodeValue_string);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}


	public String getMutationAsJSON() {
		JSONObject returnObject = new JSONObject();
		try {
			returnObject.put("Mutation type", this.mutationType);
			returnObject.put("Node ID", this.parentNodeValue); 
			returnObject.put("Type of value changed", this.nodeName.replace("#", ""));
			returnObject.put("Content changed", this.nodeValue);
			returnObject.put("Story ID", this.getCounter());
			returnObject.put("Assertions", this.getRelatedAssertions());
			return returnObject.toString(4);
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	@JsonIgnore
	public JSONObject getParentNode() {
		return this.parentNodeValue;
	}

	@JsonSetter("relatedAssertions")
	public void mapToAssertion(String assertionNumber) {
		//this.relatedAssertions.add(assertionNumber);
		this.relatedAssertions += " " + assertionNumber;
	}

	public String getRelatedAssertions() {
		return this.relatedAssertions;
	}

	@JsonIgnore
	public ArrayList<Integer> getRelatesAssertionsAsArray() {
		ArrayList<Integer> returnMe = new ArrayList<Integer>();

		String[] rm = getRelatedAssertions().split(" ");

		for (int l = 0; l < rm.length; l++) {
			try {
				returnMe.add(Integer.parseInt(rm[l]));
			} catch (NumberFormatException nfe) {
				// Blank space, ignore
			}			
		}
		return returnMe;
	}

	/*
	 * public String getMutationAsJSON() { // JSONObject returnObject = new JSONObject(); JSONArray
	 * args = new JSONArray(); // Variable[] args.put(this.mutationType);
	 * args.put(this.parentNodeValue); args.put(this.nodeName.replace("#", ""));
	 * args.put(this.nodeValue); return args == null ? null : args.toString(); /* try {
	 * returnObject.put("Mutation type", this.mutationType); returnObject.put("Node ID",
	 * this.parentNodeValue); // returnObject.put("nodeType", this.nodeType);
	 * returnObject.put("Type of value changed", this.nodeName.replace("#", ""));
	 * returnObject.put("Content changed", this.nodeValue); } catch (JSONException e) {
	 * e.printStackTrace(); } return returnObject; }
	 */
}


