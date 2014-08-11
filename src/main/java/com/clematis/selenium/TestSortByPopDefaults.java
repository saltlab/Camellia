package com.clematis.selenium;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import com.clematis.core.WebDriverWrapper;
import com.clematis.jsmodify.JSExecutionTracer;

public class TestSortByPopDefaults {
	private static WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp(WebDriver parentDriver) throws Exception {
		//driver = new FirefoxDriver();
		driver = parentDriver;

		baseUrl = "http://localhost:9763";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testSortByPopDefaults() throws Exception {
		driver.get(baseUrl + "/store/assets/gadget");

		driver.findElement(Byy.cssSelector("i.icon-star")).click();
		
		assertEquals(12, driver.findElements(Byy.cssSelector(".asset-icon")).size());

		JavascriptExecutor jsx = (JavascriptExecutor) driver;
		
		jsx.executeScript("window.scrollBy(0,5000)", "");

		assertEquals(17, driver.findElements(Byy.cssSelector(".asset-icon")).size());

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
	
	/** Clematest jUnit wrappers **/
	private static void assertEquals(int s1, int s2){
		
//		((JavascriptExecutor) driver).executeScript("sendReally();");

		//TODO: changed counter for allowing assertions to be shown
		long assertionCutoff = (Long) (((JavascriptExecutor) driver).executeScript(
				"return traceCounter++;"/**/));
		
		assertionCutoff += JSExecutionTracer.getPageLoadBuffer();
		
		long timeStamp = (Long) (((JavascriptExecutor) driver).executeScript(
				"return Date.now();"/**/));
		System.out.println("[assertEquals]: " + assertionCutoff);

		try {
			org.junit.Assert.assertEquals(s1, s2);
		} catch (Error e) {
			WebDriverWrapper.flushAccesses(e.toString(), assertionCutoff, timeStamp);
			throw e;
		}
		WebDriverWrapper.flushAccesses(null, assertionCutoff, timeStamp);
	}
}
