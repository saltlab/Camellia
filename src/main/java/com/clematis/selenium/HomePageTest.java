package com.clematis.selenium;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.clematis.core.WebDriverWrapper;
import com.clematis.jsmodify.JSExecutionTracer;

public class HomePageTest {
	private static WebDriver driver;
	//private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();
	@Before
	public void setUp(WebDriver parentDriver) throws Exception {
		driver = parentDriver;
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testHomePage() throws Exception {
		driver.get("http://localhost:8888/");
		assertTrue(isElementPresent(Byy.cssSelector("div#Granny")));
		Thread.sleep(700);

		driver.findElement(Byy.linkText("Stories")).click();
		try {
			assertTrue(driver.getCurrentUrl().matches("^http://localhost:8888/[\\s\\S]mode=stories$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(Byy.partialLinkText("Default Category")).click();
		try {
			assertTrue(driver.findElement(Byy.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*category[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(Byy.partialLinkText("Default Story")).click();
		try {
			assertTrue(driver.findElement(Byy.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*story[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(Byy.linkText("SlideShow")).click();
		try {
			assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		Thread.sleep(700);
		driver.findElement(Byy.linkText("Next")).click();
		Thread.sleep(700);

		try {
			assertEquals("1", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
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

	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	/** Clematest jUnit wrappers **/
	private static void assertTrue(Boolean condition){
		System.out.println("[assertTrue]: ");
		long assertionCutoff = (Long) (((JavascriptExecutor) driver).executeScript(
				"return traceCounter++;"/**/));
		assertionCutoff += JSExecutionTracer.getPageLoadBuffer();
		long timeStamp = (Long) (((JavascriptExecutor) driver).executeScript(
				"return Date.now();"/**/));
		try {
			org.junit.Assert.assertTrue(condition);
		} catch (Error e) {
			WebDriverWrapper.flushAccesses(e.toString(), assertionCutoff, timeStamp);
			throw e;
		}
		WebDriverWrapper.flushAccesses(null, assertionCutoff, timeStamp);
	}

	private static void assertEquals(String s1, String s2){
		//TODO: changed counter for allowing assertions to be shown
		long assertionCutoff = (Long) (((JavascriptExecutor) driver).executeScript(
				"return traceCounter++;"/**/));
		assertionCutoff += JSExecutionTracer.getPageLoadBuffer();

		long timeStamp = (Long) (((JavascriptExecutor) driver).executeScript(
				"return Date.now();"/**/));
		System.out.println("[assertEquals]: " + assertionCutoff);

		try {
		   // org.junit.Assert.assertEquals(s1, s1);
		    org.junit.Assert.assertEquals(s1, s2);
		} catch (Error e) {
			WebDriverWrapper.flushAccesses(e.toString(), assertionCutoff, timeStamp);
			throw e;
		}
		WebDriverWrapper.flushAccesses(null, assertionCutoff, timeStamp);
	}

}
