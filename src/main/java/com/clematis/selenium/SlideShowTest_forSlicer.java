package com.clematis.selenium;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SlideShowTest_forSlicer {
	private WebDriver driver;
	//private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();
	@Before
	public void setUp(WebDriver parentDriver) throws Exception {
		driver = parentDriver;
		//baseUrl = "http://localhost/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testSlideShow() throws Exception {
		driver.get("http://localhost:8888/?feat=slideshow");

		assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		Thread.sleep(700);

		try {
			assertTrue(isElementPresent(By.cssSelector("img#ss_photo")));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		try {
			assertEquals("1", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		Thread.sleep(700);
		driver.findElement(By.linkText("Next")).click();
		Thread.sleep(700);

		try {
			assertEquals("2", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		
		Thread.sleep(700);
		driver.findElement(By.linkText("Next")).click();
		Thread.sleep(700);
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
}
