package com.clematis.selenium;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.clematis.core.WebDriverWrapper;

public class Byy extends By {
	/*  
	 * By.ByClassName 
     * By.ByCssSelector 
	 * By.ById 
	 * By.ByLinkText 
	 * By.ByName 
	 * By.ByPartialLinkText 
	 * By.ByTagName 
	 * By.ByXPath 
	 * TODO: Some of these wrappers not created!*/

	public static org.openqa.selenium.By cssSelector (String selector) {
		WebDriverWrapper.addDOMAccess("cssSelector", selector, /* found ? */ true);
		return org.openqa.selenium.By.cssSelector(selector);
	}

	public static org.openqa.selenium.By id (String selector) {
		WebDriverWrapper.addDOMAccess("id", selector, true);
		return org.openqa.selenium.By.id(selector);
	}

	public static org.openqa.selenium.By linkText (String selector) {
		WebDriverWrapper.addDOMAccess("linkText", selector, true);
		return org.openqa.selenium.By.linkText(selector);
	}
	
	public static org.openqa.selenium.By name (String selector) {
		WebDriverWrapper.addDOMAccess("name", selector, true);
		return org.openqa.selenium.By.name(selector);
	}

	public static org.openqa.selenium.By partialLinkText(String string) {
		WebDriverWrapper.addDOMAccess("partialLinkText", string, true);
		return org.openqa.selenium.By.partialLinkText(string);
	}

	@Override
	public List<WebElement> findElements(SearchContext context) {
		System.out.println("[findElements]:  To Be Implemented: " + context.toString() );
		return null;
	}




}
