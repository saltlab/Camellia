package com.dyno.instrument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

public class ProxyInstrumenter extends AstInstrumenter {

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

	private ArrayList<String> closureStack = new ArrayList<String>();

	/**
	 * Construct without patterns.
	 */
	public ProxyInstrumenter() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ProxyInstrumenter(ArrayList<String> excludes) {
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

		//System.out.println(code);
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

	@Override
	public  boolean visit(AstNode node){
		int tt = node.getType();

		//System.out.println(node.toSource());
		//	System.out.println(Token.typeToName(tt));

		if (tt == org.mozilla.javascript.Token.VAR && node instanceof VariableDeclaration) {
			System.out.println("FIRST_ASSIGN " + node.getLineno());
			System.out.println(node.toSource());
			handleVariableDeclaration((VariableDeclaration) node);			
		} else if (tt == org.mozilla.javascript.Token.VAR) {
			//System.out.println("What are you?");
			//System.out.println(Token.typeToName(tt));
		} else if (tt == org.mozilla.javascript.Token.SCRIPT) {
			System.out.println(node.getEnclosingScope());
		}



		/*	if (tt == org.mozilla.javascript.Token.FUNCTION) {
			System.out.println("FUNCTION");
		} else if (tt == org.mozilla.javascript.Token.CALL ) {
			System.out.println("CALL");
		} else if (tt == org.mozilla.javascript.Token.RETURN) {
			System.out.println("RETURN");
		}*/
		return true;  // process kids
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
				.replaceAll("\\;\\n+\\;", ";");
		
		
		AstRoot iscNode = rhinoCreateNode(isc);
		System.out.println(iscNode.toSource());

		
		// Return new instrumented node/code
		return iscNode;
	}

	@Override
	public void start(String node) {
		src=node;
	}

	/**
	 * Returns all variables in scope.
	 * 
	 * @param func
	 *            The function.
	 * @return All variables in scope.
	 */
	protected String[] getVariablesNamesInScope(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();

		do {
			/* get the symboltable for the current scope */
			Map<String, Symbol> t = scope.getSymbolTable();

			if (t != null) {
				for (String key : t.keySet()) {
					/* read the symbol */
					Symbol symbol = t.get(key);
					/* only add variables and function parameters */
					if (symbol.getDeclType() == Token.LP || symbol.getDeclType() == Token.VAR) {
						result.add(symbol.getName());
					}
				}
			}

			/* get next scope (upwards) */
			scope = scope.getEnclosingScope();
		} while (scope != null);

		/* return the result as a String array */
		return result.toArray(new String[0]);
	}

	/**
	 * Check if we should instrument this variable by matching it against the exclude variable
	 * regexps.
	 * 
	 * @param name
	 *            Name of the variable.
	 * @return True if we should add instrumentation code.
	 */
	protected boolean shouldInstrument(String name) {
		if (name == null) {
			return false;
		}

		/* is this an excluded variable? */
		for (String regex : excludeList) {
			if (name.matches(regex)) {
				return false;
			}
		}
		return true;
	}

	public ArrayList<String> getExcludeList() {
		return this.excludeList;
	}

	private ArrayList<String> getClosure(AstNode node){
		ArrayList<String> parentClosures = new ArrayList<String>();
		Scope cParent = node.getEnclosingScope();

		while (cParent != null) {
			if (cParent.getType() == org.mozilla.javascript.Token.FUNCTION) {
				parentClosures.add(getFunctionNodeName((FunctionNode) cParent));
			} else if (cParent.getType() == org.mozilla.javascript.Token.SCRIPT) {
				// Top level of JavaScript file
				parentClosures.add(getScopeName());
			}
			cParent = cParent.getEnclosingScope();
		}
		if (parentClosures.size() == 0) {
			parentClosures.add(getScopeName());
		}
		return parentClosures;
	}

	private ArrayList<String> getClosureOld(AstNode node){
		ArrayList<String> parentClosures = new ArrayList<String>();
		AstNode cParent = null;

		cParent = node;

		while (cParent.getParent() != null) {
			cParent = cParent.getParent();
			if (cParent.getType() == org.mozilla.javascript.Token.FUNCTION) {
				parentClosures.add(getFunctionNodeName((FunctionNode) cParent));
			} else if (cParent.getType() == org.mozilla.javascript.Token.SCRIPT) {
				// Top level of JavaScript file
				parentClosures.add(getScopeName());
			}
		}
		return parentClosures;
	}

	private String getFunctionNodeName(FunctionNode node){
		AstNode parent = node.getParent();
		String name = node.getName();

		if (name == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
			name = parent.toSource().substring(0,parent.toSource().indexOf(node.toSource()));
			name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
		}
		return name;
	}

	private void handleVariableDeclaration(VariableDeclaration node) {
		int lineNo = node.getLineno();
		List<VariableInitializer> vi = node.getVariables();
		String currentBody = null;
		Iterator<VariableInitializer> varIt = vi.iterator();
		VariableInitializer nextInitializer;
		AstNode leftSide;
		AstNode rightSide;
		AstNode newRightSide;
		String newBody;

		while (varIt.hasNext()) {
			nextInitializer = varIt.next();
			leftSide = nextInitializer.getTarget();
			rightSide = nextInitializer.getInitializer();

			newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), 
					"_clematest.write("+leftSide.toSource()+", "+rightSide.toSource()+", "+node.getLineno()+")");//,"+rightSide.toSource());
			System.out.println("--- NAME: " + newBody);
			newRightSide = parse(newBody);
			if (newRightSide != null) {
				nextInitializer.setInitializer(newRightSide);
			}
		}


		ArrayList<String> myClosure = getClosure(node);
		System.out.println("//1//: " + myClosure);


		/*

		targetBody = target.toSource();
		String newBody = target.toSource().replaceFirst(targetBody, "FCW("+targetBody+",'"+targetBody+"',"+lineNo+")");
		System.out.println("--- NAME: " + newBody);
		newTarget = parse(newBody);


		 */





	}

	private void handleFunction(FunctionNode node) {

		// Store information on function declarations
		AstNode parent = node.getParent();
		String name = node.getName();
		String body = node.toSource();
		int[] range = {node.getBody().getAbsolutePosition()+1,node.getEncodedSourceEnd()-1};
		int hash = node.hashCode();	
		int type = node.getType();
		int lineNo = node.getLineno()+1;
		String arguments = new String();

		if(node.getParamCount() > 0){
			List<AstNode> params = node.getParams();
			for (AstNode pp: params) {
				arguments +=  "," + pp.toSource();
			}
			arguments = arguments.replaceFirst(",", "");
		} else {
			arguments = "";
		}

		if (node.getFunctionType() == FunctionNode.FUNCTION_EXPRESSION) {
			// Complicated Case
			if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.COLON) {
				// Assignment Expression					
				name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
				name = name.substring(0,name.indexOf(":"));
			} else if (node.getName() == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
				name = node.getParent().toSource().substring(0,node.getParent().toSource().indexOf(node.toSource()));
				name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
			}
		} else {
			if (node.getFunctionType() == FunctionNode.FUNCTION_STATEMENT) {
				System.out.println("* " + node.getName());
			}
			// unrecognized;
			System.out.println("Unrecognized function name at " + lineNo);
		}		
	}

	private void updateAllLineNo(AstNode body) {

		AstNode lastChild = (AstNode) body.getLastChild();

		if (lastChild == null) {
			// No children
			return;
		}

		while (true) {
			// Update line number of immediate children
			lastChild.setLineno(lastChild.getLineno()+body.getLineno());

			// Call recursively for grandchildren, greatgrandchildren, etc.
			updateAllLineNo(lastChild);

			if (body.getChildBefore(lastChild) != null) {
				lastChild = (AstNode) body.getChildBefore(lastChild);
			} else {
				break;
			}
		} 
	}

	private void handleFunctionCall(FunctionCall node) {
		// Store information on function calls
		AstNode target = node.getTarget();
		String targetBody = target.toSource();
		int[] range = {0,0};
		int lineNo = -1;
		if (node.getParent().toSource().indexOf("FCW(") > -1) {
			lineNo = node.getParent().getParent().getParent().getLineno() +1;
		} else {
			lineNo = node.getLineno()+1;
		}
		AstNode newTarget = null;

		range[0] = node.getAbsolutePosition();
		range[1] = node.getAbsolutePosition()+node.getLength();

		if (target.toSource().indexOf("FCW") == 0) {
			// We don't want to instrument out code (dirty way)
			return;
		}


		int tt = target.getType();
		if (tt == org.mozilla.javascript.Token.NAME) {
			// Regular function call, 39
			// E.g. parseInt, print, startClock
			targetBody = target.toSource();
			String newBody = target.toSource().replaceFirst(targetBody, "FCW("+targetBody+",'"+targetBody+"',"+lineNo+")");
			System.out.println("--- NAME: " + newBody);
			newTarget = parse(newBody);

		} else if (tt == org.mozilla.javascript.Token.GETPROP) {
			// Class specific function call, 33
			// E.g. document.getElementById, e.stopPropagation
			String[] methods = targetBody.split("\\.");
			range[0] += targetBody.lastIndexOf(methods[methods.length-1])-1;
			targetBody = methods[methods.length-1];

			String newBody = target.toSource().replaceFirst("."+targetBody, "[FCW(\""+targetBody+"\", "+lineNo+")]");
			System.out.println("--- PROP: " + newBody);
			newTarget = parse(newBody);
		} else {
			if (tt == org.mozilla.javascript.Token.GETELEM) {
				System.out.println("====== " + org.mozilla.javascript.Token.GETELEM + " - " + targetBody);
			}
			else if (tt == org.mozilla.javascript.Token.LP) {
				System.out.println("====== " + org.mozilla.javascript.Token.LP + " - " + targetBody);
			}
			else if (tt == org.mozilla.javascript.Token.THIS) {
				System.out.println("====== " + org.mozilla.javascript.Token.THIS + " - " + targetBody);
			}
			else
				System.out.println("======");
		}
		if (newTarget != null) {
			newTarget.setLineno(node.getTarget().getLineno());
			node.setTarget(newTarget);
		}
		else {
			System.out.println("NEW TARGET NULL +++ " + node.getTarget());
		}
	}

	private void handleReturn(ReturnStatement node) {
		// return statements

		int lineNo = node.getLineno()+1;
		AstNode newRV;

		if (node.getReturnValue() != null) {
			// Wrap return value
			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", '" + node.getReturnValue().toSource().replace("'", "\\'")+ "' ," + lineNo +");");
			//			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", '" + 'a' + "' ," + lineNo +");");
			//			newRV = parse("RSW("+ node.getReturnValue().toSource() + ", \"val\" ," + lineNo +");");
			newRV.setLineno(node.getReturnValue().getLineno());

		} else {
			// Return value is void
			newRV = parse("RSW(" + lineNo +")");
			newRV.setLineno(node.getLineno());
		}

		updateAllLineNo(newRV);
		node.setReturnValue(newRV);
	}

}
