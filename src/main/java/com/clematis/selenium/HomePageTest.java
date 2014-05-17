package com.clematis.selenium;

//import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.support.ui.Select;

public class HomePageTest {
	private WebDriver driver;
	//private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();
	
	public HomePageTest(WebDriver driver2) {
		driver = driver2;
	}

	@Before
	public void setUp() throws Exception {
		//driver = new FirefoxDriver();
		//baseUrl = "http://localhost/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testHomePage() throws Exception {
		driver.get("http://localhost:8888/");
		assertTrue(isElementPresent(By.cssSelector("div#Granny")));
		Thread.sleep(2000);
		driver.findElement(By.linkText("Stories")).click();
		try {
			assertTrue(driver.getCurrentUrl().matches("^http://localhost:8888/\\S*?mode=stories$"));
		} catch (Error e) {
			System.out.println("Assertion 1: " + e.toString());
			verificationErrors.append(e.toString());
		}
		Thread.sleep(2000);
		driver.findElement(By.partialLinkText("Default Category")).click();
		try {
			assertTrue(driver.findElement(By.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*category[\\s\\S]*$"));
		} catch (Error e) {
			System.out.println("Assertion 2: " + e.toString());
			verificationErrors.append(e.toString());
		}
		Thread.sleep(2000);
		driver.findElement(By.partialLinkText("Default Story")).click();
		try {
			assertTrue(driver.findElement(By.cssSelector("div.midInfo")).getText().matches("^[\\s\\S]*story[\\s\\S]*$"));
		} catch (Error e) {
			System.out.println("Assertion 3: " + e.toString());
			verificationErrors.append(e.toString());
		}
		Thread.sleep(2000);
		driver.findElement(By.linkText("SlideShow")).click();
		try {
			assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		} catch (Error e) {
			System.out.println("Assertion 4: " + e.toString());
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

	private boolean isElementPresent(org.openqa.selenium.By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
}
