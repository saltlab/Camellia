package com.clematis.selenium;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

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
		Thread.sleep(3000);

		try {
			assertEquals("2", driver.findElement(Byy.cssSelector("span#ss_n")).getText());
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
