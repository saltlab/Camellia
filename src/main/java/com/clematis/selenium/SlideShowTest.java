package com.clematis.selenium;

//import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;

import com.clematis.core.WebDriverWrapper;
import com.clematis.jsmodify.JSExecutionTracer;

public class SlideShowTest {
	private static WebDriver driver;
	//private String baseUrl;
	private static StringBuffer verificationErrors = new StringBuffer();



	@Before
	public void setUp(WebDriver parentDriver) throws Exception {
		//driver = new FirefoxDriver();
		driver = parentDriver;
		//baseUrl = "http://localhost/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public static void testSlideShow() throws Exception {
		driver.get("http://localhost:8888/?feat=slideshow");

		assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		Thread.sleep(700);

		
		Thread.sleep(700);
		driver.findElement(Byy.id("ss_playpause_link")).click();
		Thread.sleep(700);

		try {
			assertTrue(isElementPresent(Byy.cssSelector("img#ss_photo")));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		try {
			assertEquals("1", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		Thread.sleep(700);
		driver.findElement(Byy.linkText("Next")).click();
		Thread.sleep(700);

		try {
			assertEquals("2", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(1200);
		driver.findElement(Byy.linkText("Previous")).click();
		Thread.sleep(700);

		try {
			assertEquals("2", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}


		//Thread.sleep(30000);
		//driver.findElement(By.linkText("Back")).click();
		//assertEquals("TestGallery1", driver.getTitle());
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
			WebDriverWrapper.flushAccesses(false, assertionCutoff, timeStamp);
			throw e;
		}
		WebDriverWrapper.flushAccesses(true, assertionCutoff, timeStamp);
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
			WebDriverWrapper.flushAccesses(false, assertionCutoff, timeStamp);
			throw e;
		}
		WebDriverWrapper.flushAccesses(true, assertionCutoff, timeStamp);
	}
}

