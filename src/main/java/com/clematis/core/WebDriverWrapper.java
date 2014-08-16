package com.clematis.core;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.codehaus.jettison.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.ErrorHandler.UnknownServerException;
import org.openqa.selenium.JavascriptExecutor;

import com.clematis.jsmodify.JSExecutionTracer;

public class WebDriverWrapper extends org.openqa.selenium.firefox.FirefoxDriver {

	public static Vector<JSONObject> directAccesses = new Vector<JSONObject>();
	public static Vector<JSONObject> foundElements = new Vector<JSONObject>();

	public static Vector<JSONArray> assertionToAccess = new Vector<JSONArray>();
	public static Vector<JSONArray> assertionToElements = new Vector<JSONArray>();

	public static Vector<String> assertionOutcomes = new Vector<String>();
	public static Vector<Long> assertionCounters = new Vector<Long>();
	public static Vector<Long> assertionTimeStamps = new Vector<Long>();
	
	public static Vector<Integer> assertionLineNumber = new Vector<Integer>();
	
	private static long assertionCutTime = -1;
	private static long assertionCutCounter = -1;

	public static long getCutTime () {
		return assertionCutTime;
	}
	
	public static long getCutCounter () {
		return assertionCutCounter;
	}
	
	public WebDriverWrapper(FirefoxProfile fp) {
		super(fp);
	}

	public void get(String url) {
		System.out.println("[driver.get]:  " + url);
		super.get(url);
	}

	public String getCurrentUrl() {
		//System.out.println("[driver.getCurrentUrl]");
		return super.getCurrentUrl();
	}

	public String getTitle() {
		//System.out.println("[driver.getTitle]");
		return super.getTitle();	
	}

	/*public List<WebElement> findElements(com.clematis.selenium.By by) {
		System.out.println("[driver.findElements 1]");
		return driver.findElements(by.get);			
	}*/

	public List<WebElement> findElements(By by) {
		// TODO: get JSON representation for al returned elements like in 'findElement' (See below)
		//System.out.println("[driver.findElements 2]");
		List<WebElement> foundIts = super.findElements(by);	
		WebElement foundIt = null;
		String byMethod;

		for (int u = 0; u < foundIts.size(); u++) {
			try {
				foundIt = foundIts.get(u);

				try {
					JSONObject addMe = new JSONObject((String)((JavascriptExecutor) this).executeScript(
							"if (JsonML !== undefined) {"+
									"window.console.log('current counter  ' + traceCounter);"+
									"var parentNodeValue = JsonML.fromHTML(arguments[0]);"+
									"parentNodeValue[1].tagName = parentNodeValue[0].toLowerCase();"+
									"parentNodeValue[1].child = parentNodeValue[2];"+
									"parentNodeValue[1].xPath = getPathTo(arguments[0]);"+
									"parentNodeValue[1].counter = traceCounter;"+
									"return JSON.stringify(parentNodeValue[1]);"+
									"} else {"+
									"return null;"+
									"}",
									foundIt));
					addMe.put("counter", addMe.getInt("counter") + JSExecutionTracer.getPageLoadBuffer());

					if (addMe != null) {
						foundElements.add(addMe);
					}
				} catch (Exception e) {
					System.out.println("no element found");
				}
			} catch (NoSuchElementException e) {
				//Unable to locate element: {"method":"css selector","selector":"sspan#ss_n"}
				try {
					// Should have "method" and "selector"
					JSONObject culpritAccess = new JSONObject(e.getMessage().substring(
							e.getMessage().indexOf("{"),
							e.getMessage().indexOf("}")+1
							));

					byMethod = by.getClass().toString().substring("class org.openqa.selenium.By$By".length()+1);

					for (int i = 0; i < directAccesses.size(); i++) {
						if(((String)directAccesses.get(i).get("method")).contains(byMethod)
								&& ((String) directAccesses.get(i).get("selector")).equals((String)culpritAccess.get("selector"))) {
							directAccesses.get(i).put("fault", true);
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				throw e;
			} /*catch (JSONException je) {
				je.printStackTrace();
				return null;
			}*/
		}





		return foundIts;
	}

	/*public WebElement findElement(com.clematis.selenium.By by) {
		System.out.println("[driver.findElement 1]");
		return driver.findElement(by);		
	}*/

	public WebElement findElement(By by) {
		WebElement foundIt = null;
		String byMethod;

		try {
			foundIt = super.findElement(by);

			// Retrieve information on found element
			try {
				JSONObject addMe = new JSONObject((String)((JavascriptExecutor) this).executeScript(
						"if (JsonML !== undefined) {"+
								"window.console.log('current counter  ' + traceCounter);"+
								"var parentNodeValue = JsonML.fromHTML(arguments[0]);"+
								"parentNodeValue[1].tagName = parentNodeValue[0].toLowerCase();"+
								"parentNodeValue[1].child = parentNodeValue[2];"+
								"parentNodeValue[1].xPath = getPathTo(arguments[0]);"+
								"parentNodeValue[1].counter = traceCounter;"+
								"return JSON.stringify(parentNodeValue[1]);"+
								"} else {"+
								"return null;"+
								"}",
								foundIt));
				addMe.put("counter", addMe.getInt("counter") + JSExecutionTracer.getPageLoadBuffer());
				if (addMe != null) {
					foundElements.add(addMe);
				}
			} catch (Exception e) {
				System.out.println("no element found");
			}


			/*System.out.println(((JavascriptExecutor) this).executeScript(
				    "var e = arguments[0]; " +
				    "return e.innerText !== undefined ? e.innerText : e.textContent",
				    foundIt));*/

			return foundIt;
		} catch (NoSuchElementException e) {
			//Unable to locate element: {"method":"css selector","selector":"sspan#ss_n"}
			try {
				// Should have "method" and "selector"
				JSONObject culpritAccess = new JSONObject(e.getMessage().substring(
						e.getMessage().indexOf("{"),
						e.getMessage().indexOf("}")+1
						));

				byMethod = by.getClass().toString().substring("class org.openqa.selenium.By$By".length()+1);

				for (int i = 0; i < directAccesses.size(); i++) {
					if(((String)directAccesses.get(i).get("method")).contains(byMethod)
							&& ((String) directAccesses.get(i).get("selector")).equals((String)culpritAccess.get("selector"))) {
						directAccesses.get(i).put("fault", true);
					}
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			throw e;
		} /*catch (JSONException je) {
			je.printStackTrace();
			return null;
		}*/
	}

	public String getPageSource() {
		//System.out.println("[driver.getPageSource]");
		return super.getPageSource();
	}

	public void close() {
		//System.out.println("[driver.close]");
		super.close();
	}

	public void quit() {
		//System.out.println("[driver.quit]");
		super.quit();
	}

	public Set<String> getWindowHandles() {
		//System.out.println("[driver.getWindowHandles]");
		return super.getWindowHandles();
	}

	public String getWindowHandle() {
		//System.out.println("[driver.getWindowHandle]");
		return super.getWindowHandle();
	}

	public TargetLocator switchTo() {
		//System.out.println("[driver.switchTo]");
		return super.switchTo();
	}

	public Navigation navigate() {
		//System.out.println("[driver.navigate]");
		return super.navigate();
	}

	public Options manage() {
		//System.out.println("[driver.manage]");
		return super.manage();
	}

	public static void flushAccesses(String assertionMessage, long assertionCounter, long timeStamp, int lineNumber) {
		// Should be called when an assertion is executed in the Selenium test case.

		JSONArray newAssertion1 = new JSONArray();
		JSONArray newAssertion2 = new JSONArray();
		JSONObject nextOne;
		Iterator<JSONObject> it = directAccesses.iterator();
		Iterator<JSONObject> it2 = foundElements.iterator();

		while (it.hasNext()) {
			nextOne = it.next();
			newAssertion1.put(nextOne);
		}
		while (it2.hasNext()) {
			nextOne = it2.next();
			newAssertion2.put(nextOne);
		}

		// TODO: Might need to make sure empty arrays are included (assertion might have no accesses)
		assertionToAccess.add(newAssertion1);
		assertionToElements.add(newAssertion2);

		directAccesses.removeAllElements();
		foundElements.removeAllElements();

		assertionOutcomes.add(assertionMessage == null ? "true" : assertionMessage);
		assertionCounters.add(assertionCounter);
		assertionTimeStamps.add(timeStamp);
		
		assertionCutTime = timeStamp;
		assertionCutCounter = assertionCounter;
		assertionLineNumber.add(lineNumber);
	}

	public static void addDOMAccess (String type, String value, boolean found) {
		JSONObject addCss = new JSONObject();
		try {
			addCss.put("method", type);
			addCss.put("selector", value);
			directAccesses.add(addCss);
		} catch (JSONException e) {
			System.out.println("");
			e.printStackTrace();
		}
	}

}
