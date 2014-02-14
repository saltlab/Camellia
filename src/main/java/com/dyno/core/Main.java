package com.dyno.core;

import java.io.IOException;

import org.openqa.selenium.WebDriver;

import com.dyno.jsmodify.JSExecutionTracer;
import com.crawljax.util.Helper;

public class Main {
	
	private static String outputFolder = "";
	private static WebDriver driver;
	private static JSExecutionTracer tracer;
	
    public void initialize() throws IOException {
    	outputFolder = Helper.addFolderSlashIfNeeded("instrumented");
    	
    	tracer = new JSExecutionTracer();
    	tracer.setOutputFolder(outputFolder + "js");
    }
	
    public static void main(String[] args) throws IOException {
        new Main().runMain(args);
    }
	
	void runMain(String[] args) throws IOException {
        parse(args);
        initialize();
        runDynoSlicer(args);
    }

    public void runDynoSlicer(String[] args) {
    	
    }
    
    private void parse(String[] options) {
    	//TODO:
    	
    	return;
    }

}
