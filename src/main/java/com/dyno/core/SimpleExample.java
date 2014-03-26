package com.dyno.core;

import java.io.File;

import org.openqa.selenium.NoAlertPresentException;
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

import com.dyno.configuration.ProxyConfiguration;
import com.dyno.instrument.ProxyInstrumenter;
import com.dyno.jsmodify.JSExecutionTracer;
import com.dyno.jsmodify.JSModifyProxyPlugin;
import com.crawljax.util.Helper;

public class SimpleExample {

    //private static final String URL = "http://localhost:8888/phormer331/index.php";
	private static final String URL = "http://www.themaninblue.com/experiment/BunnyHunt/";

	private static String outputFolder = "";
	private static WebDriver driver;

	public static void main(String[] args) {
		try {

			outputFolder = Helper.addFolderSlashIfNeeded("clematis-output");

			JSExecutionTracer tracer = new JSExecutionTracer();
			tracer.setOutputFolder(outputFolder + "ftrace");
			// config.addPlugin(tracer);
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
}
