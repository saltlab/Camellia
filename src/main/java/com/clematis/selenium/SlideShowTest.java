package com.clematis.selenium;

import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.*;
import static org.junit.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.clematis.core.WebDriverWrapper;
import com.clematis.jsmodify.JSExecutionTracer;

public class SlideShowTest {
	private static WebDriver driver;
	//private String baseUrl;
	private static StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp(WebDriver parentDriver) throws Exception {
		driver = parentDriver;
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public static void testSlideShow() throws Exception {
		driver.get("http://localhost:8888/?feat=slideshow");
		assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"), 30);
		try {
			assertTrue(isElementPresent(Byy.cssSelector("img#ss_photo")), 32);
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("1", driver.findElement(Byy.cssSelector("span#ss_n")).getText(), 37);
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		driver.findElement(Byy.linkText("Next")).click();
		try {
			assertEquals("2", driver.findElement(Byy.cssSelector("span#ss_n")).getText(), 44);
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(Byy.linkText("Next")).click();
		
		try {
			assertEquals("3", driver.findElement(Byy.cssSelector("span#ss_n")).getText(), 51);
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.invisibilityOfElementWithText(By.cssSelector("span#ss_n"), "3"));
		try {
			assertEquals("4", driver.findElement(By.cssSelector("span#ss_n")).getText(), 58);
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		driver.findElement(Byy.linkText("Previous")).click();
		try {
			assertEquals("3", driver.findElement(Byy.cssSelector("span#ss_n")).getText(), 65);
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(3000);
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			System.out.println(verificationErrorString);
			//fail(verificationErrorString);
		}
	}

	private static boolean isElementPresent(By by) { 
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/** Clematest jUnit wrappers **/
	private static void assertTrue(Boolean condition, int lineNumber){
		long assertionCutoff = (Long) (((JavascriptExecutor) driver).executeScript(
				"return traceCounter++;"/**/));
		assertionCutoff += JSExecutionTracer.getPageLoadBuffer();
		long timeStamp = (Long) (((JavascriptExecutor) driver).executeScript(
				"return Date.now();"/**/));
		try {
			org.junit.Assert.assertTrue(condition);
		} catch (Error e) {
			WebDriverWrapper.flushAccesses(e.toString(), assertionCutoff, timeStamp, lineNumber);
			throw e;
		}
		WebDriverWrapper.flushAccesses(null, assertionCutoff, timeStamp, lineNumber);
	}

	private static void assertEquals(String s1, String s2, int lineNumber){
		//TODO: changed counter for allowing assertions to be shown
		long assertionCutoff = (Long) (((JavascriptExecutor) driver).executeScript(
				"return traceCounter++;"/**/));
		assertionCutoff += JSExecutionTracer.getPageLoadBuffer();

		long timeStamp = (Long) (((JavascriptExecutor) driver).executeScript(
				"return Date.now();"/**/));

		try {
		   // org.junit.Assert.assertEquals(s1, s1);
		    org.junit.Assert.assertEquals(s1, s2);
		} catch (Error e) {
			WebDriverWrapper.flushAccesses(e.toString(), assertionCutoff, timeStamp, lineNumber);
			throw e;
		}
		WebDriverWrapper.flushAccesses(null, assertionCutoff, timeStamp, lineNumber);
	}
}

