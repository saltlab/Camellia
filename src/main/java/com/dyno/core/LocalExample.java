package com.dyno.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;

import com.google.common.io.Resources;

import com.crawljax.util.Helper;
import com.dyno.instrument.AstInstrumenter;
import com.dyno.instrument.FunctionTrace;
import com.dyno.jsmodify.JSModifyProxyPlugin;

public class LocalExample {
	
	private static String targetFile = "/bunnies.js";
	private static FunctionTrace ft = new FunctionTrace();
	
	public static void main(String[] args) {
		URL urlOfTarget = AstInstrumenter.class.getResource(targetFile);
		String jsFileContents;
		
		System.out.println("[LocalExample]: ");
		System.out.println(urlOfTarget);
		
		try {
			jsFileContents = Resources.toString(urlOfTarget, Charset.defaultCharset());
			asd(jsFileContents, targetFile);
		} catch (IOException e) {
			System.out.println("[LocalExample]: " + "Trouble reading local file.");
			e.printStackTrace();
		}
		
		
	}
	
	private static String asd(String input, String scopename) {
		AstRoot ast = null;

		/* initialize JavaScript context */
		Context cx = Context.enter();

		/* create a new parser */
		Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

		/* parse some script and save it in AST */
		ast = rhinoParser.parse(new String(input), scopename, 0);

		// modifier.setScopeName(scopename);
		ft.setScopeName(targetFile);

		ft.start(new String(input));

		/* recurse through AST */
		ast.visit(ft);

		ast = ft.finish(ast);

		// todo todo todo do not instrument again if visited before
		StringTokenizer tokenizer = new StringTokenizer(scopename, "?");
		String newBaseUrl = "";
		if (tokenizer.hasMoreTokens()) {
			newBaseUrl = tokenizer.nextToken();
		}
		PrintStream output2;


		/* clean up */
		Context.exit();

		return ast.toSource();
		
	}

}
