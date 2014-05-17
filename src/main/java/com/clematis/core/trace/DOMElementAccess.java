package com.clematis.core.trace;

import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonSetter;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class DOMElementAccess extends TraceObject {

	private String accessor;
	private String parameter;
	private JSONObject element;
	private String xpath;
	
	public DOMElementAccess() {
		super();
		setEpisodeSource(false);
	}

	public void setAccessor(String a) {
		this.accessor = a;
	}


	public String getAccessor() {
		return this.accessor;
	}
	
	@JsonSetter("xpath")
	public void setXpath(String a) {
		this.xpath = a;
	}

	@JsonGetter("xpath")
	public String getXpath() {
		return this.xpath;
	}


	public void setParameter(String p) {
		this.parameter = p;
	}

	public String getParameter() {
		return this.parameter;
	}

	@JsonSetter("element")
	public void setElement(String p) {		
		try {
			this.element = new JSONObject(p);
		} catch (JSONException e) {
			System.out.println("Exception constructing JSONObject from string "
					+ p);
			e.printStackTrace();
		}
	}

	@JsonGetter("element")
	public String getElement() {
		return this.element.toString();
	}

}
