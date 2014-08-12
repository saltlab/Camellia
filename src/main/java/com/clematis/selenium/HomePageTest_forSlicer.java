package com.clematis.selenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class HomePageTest_forSlicer {
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
		assertTrue(isElementPresent(By.cssSelector("div#Granny")));
		Thread.sleep(700);

		driver.findElement(By.linkText("Stories")).click();
		try {
			assertTrue(driver.getCurrentUrl().matches("^http://localhost:8888/[\\s\\S]mode=stories$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(By.partialLinkText("Default Category")).click();
		try {
			assertTrue(driver.findElement(By.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*category[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(By.partialLinkText("Default Story")).click();
		try {
			assertTrue(driver.findElement(By.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*story[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(By.linkText("SlideShow")).click();
		try {
			assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			System.out.println(verificationErrorString);
			fail(verificationErrorString);
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

}
