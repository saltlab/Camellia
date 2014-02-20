package com.dyno.core;

import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.WebDriver;

import com.dyno.jsmodify.JSExecutionTracer;

public class Main {
    public static final String CHARSET_PREFIX = "encoding";
    public static final String SERVER_PREFIX1 = "-s";
    public static final String SERVER_PREFIX2 = "--server";
    public static final String VERSION_PREFIX1 = "-V";
    public static final String VERSION_PREFIX2 = "--version";
    public static final String LOCAL_PREFIX1 = "-l";
    public static final String LOCAL_PREFIX2 = "--local";
    public static final Properties properties = new Properties();
	
	private static String outputFolder = "";
	private static WebDriver driver;
	private static JSExecutionTracer tracer;
	
    public void initialize() throws IOException {
    	tracer = new JSExecutionTracer();
    	tracer.setOutputFolder(outputFolder + "js");
    }
    
    private boolean printVersion;
    private boolean isServer;
    private boolean isLocal;
	
    public static void main(String[] args) throws IOException {
        new Main().runMain(args);
    }
	
	void runMain(String[] args) throws IOException {
        parse(args);
        initialize();
        runDynoSlicer(args);
    }

    public void runDynoSlicer(String[] args) {
    	 
    	
    	if (isServer) {
    		// Rename to server example?
    		SimpleExample.main(args);
    	} else if (isLocal) {
    		LocalExample.main(args);
    	} else {
    		System.out.println("Unsupported Mode, choose either local or server.");
    	}
    	
    }
    
    public Main parse(String[] args) {
        for (String arg : args) {
            parse(arg);
        }
        if (!validOptions()) {
            System.err.println("Invalid options!");
            System.exit(1);
        }
        return this;
    }
    
    private void parse(String arg) {
        if (arg.equals(SERVER_PREFIX1) || arg.equals(SERVER_PREFIX2)) {
        	isServer = true;
        } else if (arg.equals(VERSION_PREFIX1) || arg.equals(VERSION_PREFIX2)) {
            printVersion = true;
        } else if (arg.equals(LOCAL_PREFIX1) || arg.equals(LOCAL_PREFIX2)) {
            isLocal = true;
        }
    }
    
    private boolean validOptions() {
        if (isServer && isLocal) {
            return false;
        }
        return isServer || isLocal || printVersion;
    }

}
