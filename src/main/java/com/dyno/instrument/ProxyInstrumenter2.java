package com.dyno.instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.NodeTransformer;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import com.gargoylesoftware.htmlunit.javascript.host.Document;

public class ProxyInstrumenter2 extends AstInstrumenter {

	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	private ErrorReporter errorReporter = compilerEnvirons.getErrorReporter();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;

	/**
	 * List with regular expressions of variables that should not be instrumented.
	 */
	private ArrayList<String> excludeList = new ArrayList<String>();
	private String src = "";

	private Scope lastScopeVisited = null;

	/**
	 * Construct without patterns.
	 */
	public ProxyInstrumenter2() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ProxyInstrumenter2(ArrayList<String> excludes) {
		super(excludes);
		excludeList = excludes;
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstRoot parse(String code) {
		Parser p = new Parser(compilerEnvirons, errorReporter);

		return p.parse(code, null, 0);
	}

	/**
	 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
	 * function.
	 * 
	 * @param f
	 *            The function node.
	 * @return The function name.
	 */
	protected String getFunctionName(FunctionNode f) {
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}

	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}

	static private int lineNo = -1;

	public void setLineNo (int num) {
		this.lineNo = num-1;
	}

	static private ArrayList<AstNode> dependencies = new ArrayList<AstNode>();

	public ArrayList<AstNode> getNextSliceStart() {
		return dependencies;
	}

	static private String variableName = null;

	public void setVariableName (String name) {
		this.variableName = name;
	}

	public Scope getLastScopeVisited () {
		return lastScopeVisited;
	}

	@Override
	public  boolean visit(AstNode node){
		boolean continueToChildren = true;
		int tt = node.getType();
		Scope definingScope = null;

		if (tt == org.mozilla.javascript.Token.NAME
				&& node.getLineno() == lineNo
				&& ((Name) node).getIdentifier().equals(variableName)) {
			// Starting point of slice

			definingScope = InstrumenterHelper.getDefiningScope((Name) node);

			if (definingScope.getType() == org.mozilla.javascript.Token.SCRIPT) {
				// Assume variable is defined in another JavaScript file and is therefore global

			}
			// At this point the defining scope should be identified, whether its global or a parent function

			// Below, replace getEnclosingFunction with a variable based on above if statement 

			/**     if (node.getEnclosingFunction() != null) {
                if (((Name) node).getDefiningScope() != node.getEnclosingFunction()) {
                    // TROUBLE!
                    System.out.println("TROUBLE 1");

                } else {
                    System.out.println("SIMPLE 1");

                }
            } else {
                // global? need to test
                System.out.println(Token.typeToName(node.getEnclosingScope().getType()));
                if (((Name) node).getDefiningScope() != node.getEnclosingScope()) {
                    // TROUBLE!
                    System.out.println("TROUBLE 2");
                } else {
                    System.out.println("SIMPLE 2");

                }
            }*/
			this.lastScopeVisited = definingScope;
			return false;

		}

		return continueToChildren;  // process kids
	}

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstNode createNode(FunctionNode function, String postfix, int lineNo) {
		String name;
		String code;

		name = getFunctionName(function);
		if (postfix == ":::EXIT") {
			postfix += lineNo;
		}

		/* only add instrumentation code if there are variables to log */

		/* TODO: this uses JSON.stringify which only works in Firefox? make browser indep. */
		/* post to the proxy server */
		code = "send(new Array('" + getScopeName() + "." + name + "', '" + postfix + "'));";

		return parse(code);
	}

	@Override
	public AstNode createPointNode(String shouldLog, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstRoot finish(AstRoot node) {
		// Adds necessary instrumentation to the root node src
		String isc = node.toSource().replaceAll("\\)]\\;+\\n+\\(", ")](")
				.replaceAll("\\)\\;\\n+\\(", ")(")
				.replaceAll("\\;\\n+\\;", ";")
				.replaceAll("\\;\\n+\\.", ".")
				.replaceAll("(\\n\\;\\n)", "\n\n")
				.replaceAll("\\.\\[", "[");

		System.out.println(isc);

		AstRoot iscNode = rhinoCreateNode(isc);

		// Return new instrumented node/code
		return iscNode;
	}

	@Override
	public void start(String node) {
		src = node;
	}
}
