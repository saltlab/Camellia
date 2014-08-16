package com.clematis.selenium;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.inject.internal.Function;

public class SlideShowTest_before {
	private WebDriver driver;
	//private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testSlideShow() throws Exception {
		driver.get("http://localhost:8888/?feat=slideshow");
		assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		try {
			assertTrue(isElementPresent(By.cssSelector("img#ss_photo")));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		try {
			assertEquals("1", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		driver.findElement(By.linkText("Next")).click();
		try {
			assertEquals("2", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		driver.findElement(By.linkText("Next")).click();
		try {
			assertEquals("3", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}

		(new WebDriverWait(driver, 10)).until(ExpectedConditions.invisibilityOfElementWithText(By.cssSelector("span#ss_n"), "3"));
		try {
			assertEquals("4", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		driver.findElement(By.linkText("Previous")).click();		
		try {
			assertEquals("3", driver.findElement(By.cssSelector("span#ss_n")).getText());
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
