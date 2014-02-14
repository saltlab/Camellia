package com.dyno.jsmodify;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import com.crawljax.util.Helper;

/**
 * Reads an instrumentation array from the webbrowser and saves the contents in a JSON trace file.
 * 
 * @author Frank Groeneveld
 * @version $Id: JSExecutionTracer.java 6162 2009-12-16 13:56:21Z frank $
 */
public class JSExecutionTracer {

	private static String outputFolder;
	private static String traceFilename;

	private static final Logger LOGGER = Logger
	        .getLogger(JSExecutionTracer.class.getName());

	public static final String READWRITEDIRECTORY = "readswrites/";

	private static PrintStream output;

	private static int counter = 0;

	public JSExecutionTracer(String filename) {
		traceFilename = filename;
	}

	/**
	 * Initialize the plugin and create folders if needed.
	 * 
	 * @param browser
	 *            The browser.
	 */
	public static void preCrawling() {
		try {
			Helper.directoryCheck(getOutputFolder());
			output = new PrintStream(getOutputFolder() + getFilename());

			// Add opening bracket around whole trace
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println("{");
			System.setOut(oldOut);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void postCrawling() {
		try {
			// Add closing bracket
			PrintStream oldOut = System.out;
			System.setOut(output);
			System.out.println(" ");
			System.out.println("}");
			System.setOut(oldOut);

			/* close the output file */
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return Name of the file.
	 */
	public static String getFilename() {
		return traceFilename;
	}

	public static String getOutputFolder() {
		return Helper.addFolderSlashIfNeeded(outputFolder);
	}

	public void setOutputFolder(String absolutePath) {
		outputFolder = absolutePath;
	}

}
