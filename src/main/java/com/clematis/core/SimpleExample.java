package com.clematis.core;

import java.io.File;
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
import com.clematis.selenium.SlideShowTest;
import com.clematis.selenium.TestSortByPopDefaults;

import com.crawljax.util.Helper;

public class SimpleExample {

    private static String outputFolder = "";
    private static WebDriverWrapper driver;

    public static void main(String[] args) {
        try {

            outputFolder = Helper.addFolderSlashIfNeeded("clematis-output");

            JSExecutionTracer tracer = new JSExecutionTracer("function.trace");
            tracer.setOutputFolder(outputFolder + "ftrace");
            // config.addPlugin(tracer);
            tracer.preCrawling();

            // Create a new instance of the firefox driver
            FirefoxProfile profile = new FirefoxProfile();
            // Instantiate proxy components
            ProxyConfiguration prox = new ProxyConfiguration();

            // Modifier responsible for parsing Ast tree
            FunctionTrace s = new FunctionTrace();

            // Add necessary files from resources

            s.setFileNameToAttach("/esprima.js");
            s.setFileNameToAttach("/esmorph.js");
            s.setFileNameToAttach("/addvariable.js");
            s.setFileNameToAttach("/asyncLogger.js");
            s.setFileNameToAttach("/applicationView.js");
            s.setFileNameToAttach("/instrumentDOMEvents.js");
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

              //  profile.setPreference("network.proxy.ssl", prox.getHostname());
              //  profile.setPreference("network.proxy.ssl_port", prox.getPort());

                profile.setPreference("network.proxy.type", prox.getType().toInt());
                /* use proxy for everything, including localhost */
                profile.setPreference("network.proxy.no_proxies_on", "");
                //profile.setAcceptUntrustedCertificates(true);
            }

            driver = new WebDriverWrapper(profile);
            WebDriverWait wait = new WebDriverWait(driver, 10);
            boolean sessionOver = false;

            // Use WebDriver to visit specified URL
            //		driver.get(URL);

            SlideShowTest HPT = new SlideShowTest();
            //TestSortByPopDefaults HPT = new TestSortByPopDefaults();
            try {
                HPT.setUp(driver);
                //HPT.testSortByPopDefaults();
                HPT.testSlideShow();
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
