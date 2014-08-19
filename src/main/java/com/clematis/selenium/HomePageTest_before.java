package com.clematis.selenium;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class HomePageTest_before {
	private WebDriver driver;
	private StringBuffer verificationErrors = new StringBuffer();
	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testHomePage() throws Exception {
		driver.get("http://localhost:8888/");
		assertTrue(isElementPresent(By.cssSelector("div#Granny")));
		driver.findElement(By.linkText("Stories")).click();
		try {
			assertTrue(driver.getCurrentUrl().matches("^http://localhost:8888/[\\s\\S]mode=stories$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(By.partialLinkText("Default Category")).click();
		try {
			assertTrue(driver.findElement(By.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*category[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(By.linkText("SlideShow")).click();
		try {
			assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(Byy.linkText("Next")).click();
		try {
			assertEquals("2", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(Byy.linkText("Previous")).click();
		try {
			assertEquals("1", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
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
