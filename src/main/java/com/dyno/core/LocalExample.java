package com.dyno.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Scope;

import com.google.common.io.Resources;

import com.crawljax.util.Helper;
import com.dyno.instrument.AstInstrumenter;
import com.dyno.instrument.FunctionTrace;
import com.dyno.instrument.ProxyInstrumenter;
import com.dyno.instrument.ProxyInstrumenter2;
import com.dyno.instrument.ReadWriteReplacer;
import com.dyno.jsmodify.JSModifyProxyPlugin;

public class LocalExample {
	
	private static String targetFile = "/short_bunnies.js";
	private static int tempLineNo = 7;
	private static String varName = "tt";
	
	// Definition scope finder
	private static ProxyInstrumenter2 ft = new ProxyInstrumenter2();
	private static ReadWriteReplacer wrr = new ReadWriteReplacer();
	
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
		ArrayList<AstNode> dataNodes = new ArrayList<AstNode>();
		Scope scopeOfInterest = null;

		/* initialize JavaScript context */
		Context cx = Context.enter();

		/* create a new parser */
		Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

		/* parse some script and save it in AST */
		ast = rhinoParser.parse(new String(input), scopename, 0);

		ft.setScopeName(targetFile);
		ft.setLineNo(tempLineNo);
		ft.setVariableName(varName);
		ft.start(new String(input));
		
		
		ast.visit(ft);
		scopeOfInterest = ft.getLastScopeVisited();

	//	System.out.println(scopeOfInterest.toSource());
		
		wrr.setScopeName(targetFile);
		wrr.setLineNo(tempLineNo);
		wrr.setVariableName(varName);
		wrr.start(new String(input));
		scopeOfInterest.visit(wrr);

		/* recurse through AST */
		
		//ast.getContainerFunction_or_Scope_for line
		
		//ast.getDefiningScope
		
		// if ast.getDefiningScope == ast.getContainerFunction_or_Scope_for, easy instrument all write in one funciton
		
		// otherwise, visit definiting funciton downwards replacing all writes to variable, then get its data depend,
		// and repeat for those!
		

		ast = wrr.finish(ast);
		
	/*	dataNodes = ft.getNextSliceStart();
		
		Iterator<AstNode> itt = dataNodes.iterator();
		AstNode nextNode;
		while(itt.hasNext()) {
			nextNode = itt.next();
			System.out.println("----------------------------------");
			System.out.println(Token.typeToName(nextNode.getType()));
			System.out.println(nextNode.getLineno());
			System.out.println(nextNode.toSource());
		}*/


		/* clean up */
		Context.exit();

		return ast.toSource();
		
	}

}
