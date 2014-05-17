package com.clematis.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.owasp.webscarab.model.Preferences;
import org.owasp.webscarab.plugin.Framework;
import org.owasp.webscarab.plugin.proxy.Proxy;

import com.clematis.core.configuration.ProxyConfiguration;
import com.clematis.instrument.FunctionTrace;
import com.clematis.jsmodify.JSExecutionTracer;
import com.clematis.jsmodify.JSModifyProxyPlugin;
import com.clematis.selenium.Hybrid_LoginAsAdmin_UpdateDescription;
import com.clematis.selenium.SlideShowTest;
import com.clematis.selenium.TestSortByPopDefaults;
import com.crawljax.util.Helper;

public class SimpleExample {

	// private static final String URL = "http://localhost:8080/same-game/same-game.html";
	// private static final String URL = "http://localhost:8080/example_webapplication/index.html";

	//	private static final String URL = "http://localhost:8080/study_application/index.html";
	//	private static final String URL = "http://localhost:8888/phormer331/index.php";

	//	private static final String URL = "http://10.162.207.43:8000/sap/bi/launchpad/explorer?itemId=nhl-data%3ACOMPLETE_NHL_PLAYER_STATS&type=DATASET";
	//	private static final String URL = "http://localhost:8080/study_application/index.html";

	private static String outputFolder = "";
	private static WebDriverWrapper driver;

	public static void main(String[] args) {
		try {

			outputFolder = Helper.addFolderSlashIfNeeded("clematis-output");

			JSExecutionTracer tracer = new JSExecutionTracer("function.trace");
			tracer.setOutputFolder(outputFolder + "ftrace");
			// config.addPlugin(tracer);
			tracer.preCrawling();
			System.setProperty("webdriver.chrome.driver", "/Users/sheldon/Downloads/chromedriver 2");

			// Create a new instance of the firefox driver
			FirefoxProfile profile = new FirefoxProfile();
			// Instantiate proxy components
			ProxyConfiguration prox = new ProxyConfiguration();

			// Modifier responsible for parsing Ast tree
			FunctionTrace s = new FunctionTrace();

			// Add necessary files from resources

			s.setFileNameToAttach("/esprima.js");
			s.setFileNameToAttach("/esmorph.js");

			// s.setFileNameToAttach("/jquery-1.9.1.js");
			// s.setFileNameToAttach("/jquery-ui-1.10.2.custom.js");
			// s.setFileNameToAttach("/jquery.tipsy.js");
			// s.setFileNameToAttach("/trial_toolbar.js");
			// s.setFileNameToAttach("/toolbar.js");
			s.setFileNameToAttach("/addvariable.js");
			s.setFileNameToAttach("/asyncLogger.js");
			s.setFileNameToAttach("/applicationView.js");
			s.setFileNameToAttach("/eventlistenersMirror.js");
			s.setFileNameToAttach("/jsonml-dom.js");
			s.setFileNameToAttach("/domMutations.js");
			s.setFileNameToAttach("/mutation_summary.js");
			s.instrumentDOMModifications();

			// Interface for Ast traversal
			JSModifyProxyPlugin p = new JSModifyProxyPlugin(s);
			p.excludeDefaults();

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

				profile.setPreference("network.proxy.ssl", prox.getHostname());
				profile.setPreference("network.proxy.ssl_port", prox.getPort());

				profile.setPreference("network.proxy.type", prox.getType().toInt());
				/* use proxy for everything, including localhost */
				profile.setPreference("network.proxy.no_proxies_on", "localhost");
				//profile.setAcceptUntrustedCertificates(true);
			}



			




			/*
			 * For enabling Firebug with Clematis Replace '...' with the appropriate path to your
			 * Firebug installation
			 */
			//			 File file = new File("/Users/.../Library/Application Support/Firefox/Profiles/zga73n4v.default/extensions/firebug@software.joehewitt.com.xpi");
			//			File file = new File("/Users/sheldon/Library/Application Support/Firefox/Profiles/zga73n4v.default/extensions/firebug@software.joehewitt.com.xpi");
			//			profile.addExtension(file);
			//			profile.setPreference("extensions.firebug.currentVersion", "1.8.1"); // Avoid startup
			// screen
			//	/Users/sheldon/Library/Application\ Support/Firefox/Profiles/zga73n4v.default

			//TODO: changed to WebDriverWrapper from firefoxdriver
			//driver = new FirefoxDriver(profile);

			FirefoxProfile profile2 = new FirefoxProfile(new File("/Users/sheldon/Library/Application Support/Firefox/Profiles/zga73n4v.default"));

			if (prox != null) {
				profile2.setPreference("network.proxy.http", prox.getHostname());
				profile2.setPreference("network.proxy.http_port", prox.getPort());

				profile2.setPreference("network.proxy.ssl", prox.getHostname());
				profile2.setPreference("network.proxy.ssl_port", prox.getPort());

				profile2.setPreference("network.proxy.type", prox.getType().toInt());
				/* use proxy for everything, including localhost */
				profile2.setPreference("network.proxy.no_proxies_on", "");
				//profile2.setAcceptUntrustedCertificates(true);
			}
			
	/*		File p12 = new File("./certs/" + prox.getHostname() +":"+ "9763" + ".p12");
			InputStream is = new FileInputStream(p12);
			System.out.println(is.available());
*/
			driver = new WebDriverWrapper(profile2);
			WebDriverWait wait = new WebDriverWait(driver, 10);
			boolean sessionOver = false;

			// Use WebDriver to visit specified URL
			//		driver.get(URL);

			//SlideShowTest HPT = new SlideShowTest();
			//		TestSortByPopDefaults HPT = new TestSortByPopDefaults();
			Hybrid_LoginAsAdmin_UpdateDescription HPT = new Hybrid_LoginAsAdmin_UpdateDescription();
			try {
				HPT.setUp(driver);
				//HPT.testSlideShow();
				//		HPT.testSortByPopDefaults();
				HPT.testLoginAsAdmin();
			} catch (NoSuchElementException e) {
				System.out.println("Error executing test case:");
				e.printStackTrace();
			}


			if (driver instanceof JavascriptExecutor) {
				System.out.println("doing sendRecordStop");
				((JavascriptExecutor) driver).executeScript("sendRecordStop();");
			}

			//saveDirectAccessesToFile(driver.assertionToAccess, driver.assertionToElements, driver.assertionOutcomes, driver.directAccesses);

			HPT.tearDown();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}



	public static boolean isAlertPresent()
	{
		// Selenium bug where all alerts must be closed before
		// driver.execute(String) can be executed
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
}
