package phormer.test;

import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SlideShowTest {
	private WebDriver driver;
	//private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();
	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		//baseUrl = "http://localhost/";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void testSlideShow() throws Exception {
		driver.get("http://localhost/?feat=slideshow");
		assertTrue(driver.getTitle().matches("^SlideShow[\\s\\S]*$"));
		driver.findElement(By.id("ss_playpause_link")).click();
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
		driver.findElement(By.linkText("Previous")).click();
		try {
			assertEquals("1", driver.findElement(By.cssSelector("span#ss_n")).getText());
		} catch (Error e) {
			verificationErrors.append(e.toString());
		}
		driver.findElement(By.linkText("Back")).click();
		assertEquals("TestGallery1", driver.getTitle());
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
}
