package com.clematis.selenium;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MainViewTest_forSlicer {
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
			assertTrue(isElementPresent(By.cssSelector("div#theImage")));
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(By.linkText("Hide  info")).click();
		// ERROR: Caught exception [ERROR: Unsupported command [getEval | window.document.getElementById("photoBoxes").style.display == "none" | ]]
		assertTrue(driver.findElement(By.id("photoBoxes")).isDisplayed() == false);
		Thread.sleep(700);

		driver.findElement(By.linkText("Show info")).click();
		// ERROR: Caught exception [ERROR: Unsupported command [getEval | window.document.getElementById("photoBoxes").style.display == "none" | ]]
		assertTrue(driver.findElement(By.id("photoBoxes")).isDisplayed());
		Thread.sleep(700);

		// ERROR: Caught exception [ERROR: Unsupported command [getEval | (window.document.getElementById("rateSelect").value % 5) + 1 | ]]
		WebElement select = driver.findElement(By.id("rateSelect"));
		int rating = Integer.parseInt(select.getAttribute("value"));
		int nextRating = rating % 5 + 1;
		System.out.println(nextRating);
		List<WebElement> allOptions = select.findElements(By.tagName("option"));
		for (WebElement option : allOptions) {
		    if(Integer.parseInt(option.getAttribute("value")) == nextRating)
		    {
		    	option.click();
		    	break;
		    }
		}
		Thread.sleep(1300);

		try {
			assertEquals("Your rating saved!", driver.findElement(By.cssSelector("span#rateStatus")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		Thread.sleep(700);

		driver.findElement(By.xpath("//div[@id='Granny']/div[5]/div[2]/center/a/img")).click();
		try {
			assertTrue(driver.getCurrentUrl().matches("^http://localhost:8888/[\\s\\S]p=2$"));
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
