package com.clematis.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Hybrid_LoginAsAdmin_UpdateDescription {
	 private WebDriver driver;
	  private String baseUrl;
	  private boolean acceptNextAlert = true;
	  private StringBuffer verificationErrors = new StringBuffer();

	  @Before
	  public void setUp(WebDriver parentDriver) throws Exception {
	    driver = parentDriver;
	    baseUrl = "localhost:9763";
	    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	  }

	  @Test
	  public void testLoginAsAdmin() throws Exception {
		    driver.get(baseUrl + "/store/assets/gadget");
		    driver.findElement(By.linkText("Sign in")).click();
		    
		    Thread.sleep(2000);
		    
		    driver.findElement(By.id("username")).clear();
		    driver.findElement(By.id("username")).sendKeys("admin");
		    driver.findElement(By.id("password")).clear();
		    driver.findElement(By.id("password")).sendKeys("admin");
		    
		    
		    
		    driver.findElement(By.xpath("//button[@type='submit']")).click();
		    
		    
		    Thread.sleep(3000);
	    try {
	      assertEquals("admin", driver.findElement(By.cssSelector("ul.nav li a.dropdown-toggle")).getText());
	    } catch (Error e) {
	      verificationErrors.append(e.toString());
	    }
	    
	    driver.get(baseUrl + "/publisher/assets/gadget");
	    driver.findElement(By.xpath("//a[contains(text(),\"userAddedAsset\")]")).click();
	    driver.findElement(By.linkText("Edit")).click();
	    driver.findElement(By.id("overview_description")).clear();
	    driver.findElement(By.id("overview_description")).sendKeys("this is my user added asset 1 + added description");
	    driver.findElement(By.id("editAssetButton")).click();
	  }

	  @After
	  public void tearDown() throws Exception {
	    driver.quit();
	    String verificationErrorString = verificationErrors.toString();
	    if (!"".equals(verificationErrorString)) {
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

	  private boolean isAlertPresent() {
	    try {
	      driver.switchTo().alert();
	      return true;
	    } catch (NoAlertPresentException e) {
	      return false;
	    }
	  }

	  private String closeAlertAndGetItsText() {
	    try {
	      Alert alert = driver.switchTo().alert();
	      String alertText = alert.getText();
	      if (acceptNextAlert) {
	        alert.accept();
	      } else {
	        alert.dismiss();
	      }
	      return alertText;
	    } finally {
	      acceptNextAlert = true;
	    }
	  }
}
