package com.clematis.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.clematis.core.WebDriverWrapper;
import com.clematis.jsmodify.JSExecutionTracer;

public class MainViewTest {
	private static WebDriver driver;
	//private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();
	@Before
	public void setUp(WebDriver parentDriver) throws Exception {
		driver = parentDriver;
		//baseUrl = "http://localhost/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testMainView() throws Exception {
		driver.get("http://localhost:8888/?p=1");
		try {
			assertTrue(isElementPresent(Byy.cssSelector("div#theImage")));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(Byy.linkText("Hide  info")).click();
		// ERROR: Caught exception [ERROR: Unsupported command [getEval | window.document.getElementById("photoBoxes").style.display == "none" | ]]
		assertTrue(driver.findElement(Byy.id("photoBoxes")).isDisplayed() == false);
		Thread.sleep(700);

		driver.findElement(Byy.linkText("Show info")).click();
		// ERROR: Caught exception [ERROR: Unsupported command [getEval | window.document.getElementById("photoBoxes").style.display == "none" | ]]
		assertTrue(driver.findElement(Byy.id("photoBoxes")).isDisplayed());
		Thread.sleep(700);

		// ERROR: Caught exception [ERROR: Unsupported command [getEval | (window.document.getElementById("rateSelect").value % 5) + 1 | ]]
		WebElement select = driver.findElement(Byy.id("rateSelect"));
		int rating = Integer.parseInt(select.getAttribute("value"));
		int nextRating = rating % 5 + 1;
		System.out.println(nextRating);
		List<WebElement> allOptions = select.findElements(Byy.tagName("option"));
		for (WebElement option : allOptions) {
		    if(Integer.parseInt(option.getAttribute("value")) == nextRating)
		    {
		    	option.click();
		    	break;
		    }
		}
		try {
			System.out.println(driver.findElement(Byy.cssSelector("span#rateStatus")).getText());
			assertEquals("Your ratsing saved!", driver.findElement(Byy.cssSelector("span#rateStatus")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(Byy.xpath("//div[@id='Granny']/div[5]/div[2]/center/a/img")).click();
		try {
			assertTrue(driver.getCurrentUrl().matches("^http://localhost:8888/[\\s\\S]p=2$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(1400);

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
