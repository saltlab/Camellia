package com.clematis.jsmodify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ast.AstRoot;
import org.openqa.selenium.WebDriver;
import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;
import org.owasp.webscarab.plugin.proxy.ProxyPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.json.JSONML;

import com.clematis.core.SimpleExample;
import com.clematis.instrument.AstInstrumenter;
import com.crawljax.util.Helper;
import com.google.common.io.Resources;

/**
 * The JSInstrument proxy plugin used to add instrumentation code to JavaScript files.
 */
public class JSModifyProxyPlugin extends ProxyPlugin {

	private List<String> excludeFilenamePatterns;
	private Vector<String> pastHTML = new Vector<String>();

	public static List<String> visitedBaseUrls; // /// todo todo todo todo **********
	public static String scopeNameForExternalUse; // //// todo ********** change this later

	private final JSASTModifier modifier;

	private boolean areWeRecording = true;

	private static String outputFolder = "";
	private static String jsFilename = "";
	private static String[] args;
	
	public String[] getArgs() {
		return this.args;
	}

	private static int loadEventCounter;


	/**
	 * Construct without patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 */
	public JSModifyProxyPlugin(JSASTModifier modify) {
		excludeFilenamePatterns = new ArrayList<String>();
		visitedBaseUrls = new ArrayList<String>();

		modifier = modify;

		outputFolder = Helper.addFolderSlashIfNeeded("clematis-output") + "js_snapshot";
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param modify
	 *            The JSASTModifier to run over all JavaScript.
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public JSModifyProxyPlugin(JSASTModifier modify, List<String> excludes) {
		excludeFilenamePatterns = excludes;
		modifier = modify;
	}

	public void excludeDefaults() {
		excludeFilenamePatterns.add(".*jquery[-0-9.]*.js?.*");
		excludeFilenamePatterns.add(".*jquery.*.js?.*");
		excludeFilenamePatterns.add(".*prototype.*js?.*");
		excludeFilenamePatterns.add(".*scriptaculous.*.js?.*");
		excludeFilenamePatterns.add(".*mootools.js?.*");
		excludeFilenamePatterns.add(".*dojo.xd.js?.*");
		excludeFilenamePatterns.add(".*trial_toolbar.js?.*");

		// Example application specific
		excludeFilenamePatterns.add(".*tabcontent.js?.*");

		excludeFilenamePatterns.add(".*toolbar.js?.*");
		excludeFilenamePatterns.add(".*jquery*.js?.*");

		// Don't instrument obfuscated code?
		excludeFilenamePatterns.add(".*min*.js?.*");
		excludeFilenamePatterns.add(".*bootstrap-tooltip*.js?.*");
		excludeFilenamePatterns.add(".*bootstrap-popover*.js?.*");
		excludeFilenamePatterns.add(".*handlebars*.js?.*");
		excludeFilenamePatterns.add(".*caramel.handlebars.client*.js?.*");
		excludeFilenamePatterns.add(".*caramel-client*.js?.*");


		// excludeFilenamePatterns.add(".*http://localhost:8888/phormer331/index.phpscript1?.*"); //
		// todo ???????

	}

	@Override
	public String getPluginName() {
		return "JSInstrumentPlugin";
	}

	@Override
	public HTTPClient getProxyPlugin(HTTPClient in) {
		return new Plugin(in);
	}

	private boolean shouldModify(String name) {
		/* try all patterns and if 1 matches, return false */
		for (String pattern : excludeFilenamePatterns) {
			if (name.matches(pattern)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method tries to add instrumentation code to the input it receives. The original input is
	 * returned if we can't parse the input correctly (which might have to do with the fact that the
	 * input is no JavaScript because the server uses a wrong Content-Type header for JSON data)
	 * 
	 * @param input
	 *            The JavaScript to be modified
	 * @param scopename
	 *            Name of the current scope (filename mostly)
	 * @return The modified JavaScript
	 */
	private synchronized String modifyJS(String input, String scopename) {

		System.out.println("<<<<");
		System.out.println("Scope: " + scopename);

		/***************/
		scopeNameForExternalUse = scopename; // todo todo todo todo
		/***************/

		if (!shouldModify(scopename)) {
			System.out.println("^ should not modify");
			System.out.println(">>>>");
			return input;
		}
		try {

			// Save original JavaScript files/nodes
			Helper.directoryCheck(getOutputFolder());
			setFileName(scopename);
			PrintStream output = new PrintStream(getOutputFolder() + getFilename());
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println(input);
			System.setOut(oldOut);

			PrintStream output_visual =
					new PrintStream("src/main/webapp/fish-eye-zoom-camera/" + getFilename());

			PrintStream oldOut2_visual = System.out;
			System.setOut(output_visual);
			System.out.println(input);
			System.setOut(oldOut2_visual);

			AstRoot ast = null;

			/* initialize JavaScript context */
			Context cx = Context.enter();

			/* create a new parser */
			Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

			/* parse some script and save it in AST */
			ast = rhinoParser.parse(new String(input), scopename, 0);

			// modifier.setScopeName(scopename);
			modifier.setScopeName(getFilename());

			modifier.start(new String(input));

			/* recurse through AST */
			ast.visit(modifier);

			ast = modifier.finish(ast);

			/****************************/
			// todo todo todo do not instrument again if visited before
			StringTokenizer tokenizer = new StringTokenizer(scopename, "?");
			String newBaseUrl = "";
			if (tokenizer.hasMoreTokens()) {
				newBaseUrl = tokenizer.nextToken();
			}
			PrintStream output2;
			try {
				output2 = new PrintStream("tempUrls.txt");
				PrintStream oldOut2 = System.out;
				System.setOut(output2);
				System.out.println("new newBaseUrl: " + newBaseUrl + "\n ---");
				boolean baseUrlExists = false;
				for (String str : visitedBaseUrls) {
					System.out.print(str);
					if (/* str.startsWith(newBaseUrl) || */str.equals(newBaseUrl)) {
						System.out.println(" -> exists");
						// System.setOut(oldOut2);
						baseUrlExists = true;
						// return input;
					}
					else {
						System.out.println();
					}
				}
				if (!baseUrlExists)
					visitedBaseUrls.add(newBaseUrl); //
				System.setOut(oldOut2);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/***************************/

			/* clean up */
			Context.exit();


			return ast.toSource();
		} catch (RhinoException re) {
			System.err.println(re.getMessage()
					+ "Unable to instrument. This might be a JSON response sent"
					+ " with the wrong Content-Type or a syntax error.");

			System.err.println("details: " + re.details());
			System.err.println("getLocalizedMessage: " + re.getLocalizedMessage());
			System.err.println("getScriptStackTrace: " + re.getScriptStackTrace());
			System.err.println("lineNumber: " + re.lineNumber());
			System.err.println("lineSource: " + re.lineSource());
			System.err.println("getCause: " + re.getCause());
			re.printStackTrace();

		} catch (IllegalArgumentException iae) {
			System.err.println("Invalid operator exception catched. Not instrumenting code.");

			System.err.println("getCause: " + iae.getCause());
			System.err.println("getLocalizedMessage: " + iae.getLocalizedMessage());
			System.err.println("getMessage: " + iae.getMessage());
			iae.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("Error saving original javascript files.");

			System.err.println("getMessage: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		System.err.println("Here is the corresponding buffer: \n" + input + "\n");

		return input;
	}

	private void setFileName(String scopename) {
		int index = scopename.lastIndexOf("/");
		jsFilename = scopename.substring(index + 1);
	}

	private static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	private static String getFilename() {
		return jsFilename;
	}

	/**
	 * This method modifies the response to a request.
	 * 
	 * @param response
	 *            The response.
	 * @param request
	 *            The request.
	 * @return The modified response.
	 */
	private Response createResponse(Response response, Request request) {
		Element newNodeToAdd;
		ArrayList<String> scriptNodesToCreate;

		if (request == null) {
			System.err.println("JSModifyProxyPlugin::createResponse: request is null");
			return response;
		}



		if (request.getURL() == null) {
			System.err.println("JSModifyProxyPlugin::createResponse: request url is null");
			return response;
		}
		else if (request.getURL().toString().isEmpty()) {
			System.err.println("JSModifyProxyPlugin::createResponse: request url is empty");
			return response;
		}
		else if (response == null) {
			System.err.println("JSModifyProxyPlugin::createResponse: response is null");
			return response;
		} else if (!request.getURL().toString().contains("-clematis")
				&& Integer.parseInt(response.getStatus()) == 404
				&& modifier.getFilesToPrepend().contains(request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/")))) {		
			return packageMessage(request, request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/")));	
			// Proxy can provide JavaScript and CSS specific to toolbar
		} else if (request.getURL().toString().contains("toolbar-clematis") && Integer.parseInt(response.getStatus()) == 404) {		
			return packageMessage(request,request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/toolbar-clematis/")));
			// Proxy can provide images for toolbar rendering
		} else if (request.getURL().toString().contains("/images-clematis/") && Integer.parseInt(response.getStatus()) == 404) {
			return packageMessage(request, request.getURL().toString().substring(request.getURL().toString().lastIndexOf("/images-clematis/")));
		}
		String type = response.getHeader("Content-Type");

		// Communication with client in regards to recording
		if (request.getURL().toString().contains("?beginrecord")) {
			areWeRecording = true;
			JSExecutionTracer.preCrawling();
			return response;
		}

		if (request.getURL().toString().contains("?stoprecord")) {
			areWeRecording = false;
			args = JSExecutionTracer.postCrawling();
			return response;
		}

		if (request.getURL().toString().contains("?thisisafunctiontracingcall")) {
			String rawResponse = new String(request.getContent());
			JSONArray buffer;
			JSONArray secondaryBuffer = new JSONArray();


			try {
				buffer = new JSONArray(rawResponse);

				for (int i = 0; i < buffer.length(); i++) {
					secondaryBuffer.put(buffer.getJSONObject(i));


				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			JSExecutionTracer.addPoint(secondaryBuffer);
			return response;
		}

		if (request.getURL().toString().contains("?loadednewpage")) {
			// TODO: Assumption, last saved HTML corresponds to this load event (need better mapping)
			try {
				String rawResponse = new String(request.getContent());
				JSONArray arrayWithLoadEvent = new JSONArray(rawResponse);
				JSONObject loadEvent;
				JSONArray entireDOMAsJSON;

				if (arrayWithLoadEvent.length() == 1) {
					loadEvent = arrayWithLoadEvent.getJSONObject(0);
					Document dom = Helper.getDocument(pastHTML.get(pastHTML.size()-1));
					// TODO: remove pastHTML first element
					loadEventCounter = loadEvent.getInt("counter");

					entireDOMAsJSON = documentBuilder(dom, loadEvent.getLong("timeStamp"));
					entireDOMAsJSON.put(0, loadEvent);
					JSExecutionTracer.addPoint(entireDOMAsJSON);

				} 
			} catch (JSONException e) { 
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return response;
		}

		if (type != null && type.contains("javascript")) {

			/* instrument the code if possible */
			response.setContent(modifyJS(new String(response.getContent()),
					request.getURL().toString()).getBytes());
		} else if (type != null && type.contains("html")) {
			try {
				//.add();
				long lDateTime = new Date().getTime();
				JSONArray temp = new JSONArray();

				JSONObject pageLoad = new JSONObject();
				pageLoad.put("messageType", "DOM_EVENT");
				pageLoad.put("timeStamp", lDateTime);
				pageLoad.put("eventType", "pageLoad");
				pageLoad.put("eventHandler", "");
				pageLoad.put("targetElement", "[\"DOCUMENT\", {\"data-type\":\"gadget\",\"data-id\":\"42b7    2a94-4774-4963-b09e-b4e05f9c7ba5\",\"class\":\"span3 asset\"}]");
				pageLoad.put("counter", JSExecutionTracer.getCounter());
				temp.put(pageLoad);


				Document dom = Helper.getDocument(new String(response.getContent()));


				JSONArray entireDOMAsJSON = documentBuilder(dom, lDateTime);
				for (int j = 0; j < entireDOMAsJSON.length(); j++) {
					// Add the HTML nodes as mutations
					temp.put(entireDOMAsJSON.get(j));
				}



				JSExecutionTracer.addPoint(temp);




				/* find script nodes in the html */
				NodeList nodes = dom.getElementsByTagName("script");

				for (int i = 0; i < nodes.getLength(); i++) {
					Node nType = nodes.item(i).getAttributes().getNamedItem("type");
					/* instrument if this is a JavaScript node */
					if ((nType != null && nType.getTextContent() != null && nType
							.getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();

						if (content.length() > 0) {
							String js = modifyJS(content, request.getURL() + "script" + i);
							nodes.item(i).setTextContent(js);
							continue;
						}
					}

					/* also check for the less used language="javascript" type tag */
					nType = nodes.item(i).getAttributes().getNamedItem("language");
					if ((nType != null && nType.getTextContent() != null && nType
							.getTextContent().toLowerCase().contains("javascript"))) {
						String content = nodes.item(i).getTextContent();
						if (content.length() > 0) {
							String js = modifyJS(content, request.getURL() + "script" + i);
							nodes.item(i).setTextContent(js);
						}

					}
				}


				// Add our JavaScript as script nodes instead of appending the file contents to existing JavaScript
				scriptNodesToCreate = modifier.getFilesToPrepend();
				for (int p = 0; p < scriptNodesToCreate.size(); p++) {
					newNodeToAdd = dom.createElement("script");					
					newNodeToAdd.setAttribute("src", scriptNodesToCreate.get(p));
					newNodeToAdd.setAttribute("language", "javascript");
					newNodeToAdd.setAttribute("type", "text/javascript");					
					if (dom.getElementsByTagName("meta").getLength() != 0 
							&& dom.getElementsByTagName("meta").item(0).getParentNode() == dom.getElementsByTagName("head").item(0)) {
						dom.getElementsByTagName("head").item(0).insertBefore(newNodeToAdd, dom.getElementsByTagName("meta").item(dom.getElementsByTagName("meta").getLength()-1));
					} else if (dom.getElementsByTagName("script").getLength() > 0) {
						dom.getElementsByTagName("script").item(0).getParentNode().insertBefore(newNodeToAdd, dom.getElementsByTagName("script").item(0));
					} else {
						if (dom.getElementsByTagName("HEAD").getLength() > 0) {
							NodeList nl = dom.getElementsByTagName("HEAD").item(0).getChildNodes();

						}

					}
				}


				// Inter-page recording (add extra JavaScript to enable recording right away)
				if (areWeRecording) {
					// Page probably changed and we were recording on previous page...so start recording immediately
					newNodeToAdd = dom.createElement("script");					
					newNodeToAdd.setAttribute("language", "javascript");
					newNodeToAdd.setAttribute("type", "text/javascript");
					newNodeToAdd.setTextContent("resumeRecording("+JSExecutionTracer.getCounter()+");");
					if (dom.getElementsByTagName("meta").getLength() != 0 
							&& dom.getElementsByTagName("meta").item(0).getParentNode() == dom.getElementsByTagName("head").item(0)) {
						dom.getElementsByTagName("head").item(0).insertBefore(newNodeToAdd, dom.getElementsByTagName("meta").item(dom.getElementsByTagName("meta").getLength()-1));
					}
				}






				/* only modify content when we did modify anything */
				if (nodes.getLength() > 0) {
					/* set the new content */
					response.setContent(Helper.getDocumentToByteArray(dom));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}/* else if (type.indexOf("image") == -1
				&& type.indexOf("css") == -1) {
			System.out.println(type);
		}*/
		/* return the response to the webbrowser */
		return response;
	}

	/**
	 * WebScarab plugin that adds instrumentation code.
	 */
	private class Plugin implements HTTPClient {

		private HTTPClient client;

		/**
		 * Constructor for this plugin.
		 * 
		 * @param in
		 *            The HTTPClient connection.
		 */
		public Plugin(HTTPClient in) {
			client = in;
		}

		//@Override
		public Response fetchResponse(Request request) throws IOException {


			Response response = this.client.fetchResponse(request);

			return createResponse(response, request);
		}

	}



	public static JSONArray documentBuilder(Document docdoc, long l) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		//DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		//DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		//Document document = docBuilder.parse(new File("document.xml"));
		//crawlAndConvert(document.getDocumentElement());

		return crawlAndConvert(docdoc.getDocumentElement(), l);
	}

	public static JSONArray crawlAndConvert(Node node, long l) {
		// do something with the current node instead of System.out
		//System.out.println(node.getNodeName());
		JSONArray returnMe = new JSONArray();
		JSONArray children = new JSONArray();
		JSONObject addMe;

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				//calls this method for all the children which is Element
				addMe = nodeToMutation(currentNode, l);
				if (addMe != null) {
					returnMe.put(addMe);
				}

				children = crawlAndConvert(currentNode, l);
				for (int j = 0; j < children.length(); j++) {
					try {
						returnMe.put(children.getJSONObject(j));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return returnMe;
	}

	private static JSONObject nodeToMutation(Node node, long l) {
		JSONObject mutation = null;
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node),
					new StreamResult(buffer));
			String str = buffer.toString();
			JSONArray jsonMLFormat = JSONML.toJSONArray(str);
			if (jsonMLFormat.length() > 1 
					&& jsonMLFormat.get(1) instanceof JSONObject
					&& jsonMLFormat.getJSONObject(1).toString().contains("{")
					&& jsonMLFormat.getJSONObject(1).toString().contains("}")) {
				//			System.out.println(jsonMLFormat.getJSONObject(1).toString(4));
				mutation = new JSONObject();
				mutation.put("messageType", "DOM_MUTATION");
				mutation.put("timeStamp", l);
				mutation.put("mutationType", "added");
				mutation.put("data", "");
				mutation.put("nodeName", "#"+node.getNodeName().toLowerCase());
				// TODO: fix these
				mutation.put("nodeType", 3);
				mutation.put("nodeValue", "#"+node.getNodeName().toLowerCase());
				mutation.put("parentNodeValue", jsonMLFormat.getJSONObject(1).toString());
				mutation.put("counter", loadEventCounter);


				/*			System.out.println(node.getNodeName().toLowerCase());
				System.out.println(node.getAttributes());
				NamedNodeMap attributes = node.getAttributes();
				attributes.getLength();
				for (int a = 0; a < attributes.getLength(); a++) {
					System.out.println(attributes.item(a));
				}
				System.out.println(node.getLocalName());
				System.out.println(node.getNodeType());
				System.out.println(node.getNodeValue());
				System.out.println(node.getPrefix());
				System.out.println(node.getTextContent());*/

				/* {
    "Assertions": "",
    "Content changed": "Tuesday, Nov 13th o\f 2012",
    "Mutation type": "removed",
    "Node ID": {
        "child": "Sunday, Nov 4th o\f 2012",
        "id": "ss_date",
        "tagName": "div"
    },
    "Story ID": 299,
    "Type of value changed": "text"
} */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mutation;
	}

	private Response packageMessage(Request request, String file) {
		Response intrResponse = new Response();
		intrResponse.setStatus("200");
		intrResponse.setVersion("HTTP/1.1");
		intrResponse.setRequest(request);
		intrResponse.setMessage("OK");
		intrResponse.setHeader("Connection", "close");

		try {
			intrResponse.setContent(Resources.toByteArray(AstInstrumenter.class.getResource(file)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(file);
			e.printStackTrace();
		} catch (NullPointerException npe) {
			System.out.println(file);
			npe.printStackTrace();
		}
		return intrResponse;
	}


	/*	send(JSON.stringify({
		messageType: "DOM_MUTATION",
		timeStamp: date,
		mutationType: "removed",
		data: removed.data,
		nodeName: removed.nodeName,
		nodeType: removed.nodeType,
		nodeValue: removed.nodeValue,
		parentNodeValue: removed.parentNodeValue,
		counter:     traceCounter++
	})); 
	 */
}
