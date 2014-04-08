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
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import com.dyno.instrument.helpers.FunctionCallParser;
import com.dyno.instrument.helpers.InfixExpressionParser;
import com.dyno.instrument.helpers.ObjectLiteralParser;
import com.dyno.instrument.helpers.PropertyGetParser;

public class DependencyFinder extends AstInstrumenter {

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

	private ArrayList<AstNode> dataDependencies = new ArrayList<AstNode>();
	private ArrayList<Name> controlDependencies = new ArrayList<Name>();

	/**
	 * Construct without patterns.
	 */
	public DependencyFinder() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public DependencyFinder(ArrayList<String> excludes) {
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

	public ArrayList<AstNode> getDataDependencies () {
		return this.dataDependencies;
	}

	public void clearDataDependencies () {
		this.dataDependencies = new ArrayList<AstNode>();
	}

	private static Scope topMost;

	public void setTopScope(Scope s) {
		this.topMost = s;
	}

	public Scope getTopScope() {
		return topMost;
	}

	@Override
	public  boolean visit(AstNode node){
		int tt = node.getType();

		/*	if (tt == org.mozilla.javascript.Token.GETPROP
				|| tt == org.mozilla.javascript.Token.CALL
				|| tt == org.mozilla.javascript.Token.NAME) {
			System.out.println(Token.typeToName(node.getType()) + " : " + node.toSource());
		}*/


		if (tt == org.mozilla.javascript.Token.VAR && node instanceof VariableDeclaration) {
			// TODO:   uncomment this vv

			handleVariableDeclaration((VariableDeclaration) node);
		} else if (tt == org.mozilla.javascript.Token.ASSIGN
				|| tt == org.mozilla.javascript.Token.ASSIGN_ADD
				|| tt == org.mozilla.javascript.Token.ASSIGN_SUB) {
			// TODO:

			handleAssignmentOperator((Assignment) node);
		}

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
				.replaceAll("\\;\\n+\\;", ";")
				.replaceAll("\\;\\n+\\.", ".")
				.replaceAll("\\;\\n+\\,", ",")
				.replaceAll("\\ \\.", " ")
				.replaceAll("(\\n\\;\\n)", "\n\n")
				//	.replaceAll("(\\n)", "\n\n")  // <-- just for spacing, might not be needed
				.replaceAll("\\.\\[", "[");

		System.out.println(isc);

		AstRoot iscNode = rhinoCreateNode(isc);


		// Return new instrumented node/code
		return iscNode;
	}

	@Override
	public void start(String node) {
	}

	public void start() {
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

	protected String[] getVariablesNamesInFunction(Scope scope) {
		TreeSet<String> result = new TreeSet<String>();

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

	private String getFunctionNodeName(FunctionNode node){
		AstNode parent = node.getParent();
		String name = node.getName();

		if (name == "" && parent.getType() == org.mozilla.javascript.Token.ASSIGN) {
			name = parent.toSource().substring(0,parent.toSource().indexOf(node.toSource()));
			name = name.substring(name.lastIndexOf(".")+1,name.indexOf("="));
		}
		return name;
	}

	private String getIdentifier(String variableName, AstNode node) {
		String[] localVariables;
		boolean found = false;
		FunctionNode enclosingFunction = node.getEnclosingFunction();
		ArrayList<String> scopePath = new ArrayList<String>();
		Iterator<String> scopeIt;
		String returnMe = "";

		while (enclosingFunction != null) {
			scopePath.add(getFunctionName(enclosingFunction));
			localVariables = getVariablesNamesInFunction(enclosingFunction);

			for (int j = 0; j < localVariables.length; j++) {
				if (localVariables[j].equals(variableName)) {
					found = true;
					break;
				}
			}

			if (found) {
				scopeIt = scopePath.iterator();
				while (scopeIt.hasNext()) {
					returnMe += scopeIt.next() + "-";
				}
				return returnMe + variableName;
			}
			enclosingFunction = node.getEnclosingFunction();
		}  
		return "global-" + variableName;
	}

	// TODO: mark left side with scope + variable name (right now its just scope but we are only interested in that
	//       given variable at the most local level)
	//ArrayList<String> myClosure = getClosure(node);
	private void handleVariableDeclaration(VariableDeclaration node) {
		List<VariableInitializer> vi = node.getVariables();
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
			
			if (rightSide == null) {
				// Variable declaration without assignment e.g. "var i;"
				continue;
			}
			// Just in case
			newBody = rightSide.toSource();


			if (leftSide.getType() == org.mozilla.javascript.Token.NAME && ((Name) leftSide).getIdentifier().equals(variableName)) {
				//	newBody = ("("+VARWRITE+"(\""+leftSide.toSource()+"\", "+node.getLineno()+"), "+rightSide.toSource()+")");


				//dataDependencies.add((Name) leftSide);

			} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME 
					&& ((Name) rightSide).getIdentifier().equals(variableName)) {


				if (leftSide.getType() == org.mozilla.javascript.Token.NAME) {
					dataDependencies.add((Name) leftSide);

				} else if (leftSide.getType() == org.mozilla.javascript.Token.GETPROP) {
					dataDependencies.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) leftSide));

				}


			} else if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP 
					&& ((PropertyGet) rightSide).getTarget().toSource().equals(variableName)) {

				if (leftSide.getType() == org.mozilla.javascript.Token.NAME) {
					dataDependencies.add((Name) leftSide);

				} else if (leftSide.getType() == org.mozilla.javascript.Token.GETPROP) {
					dataDependencies.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) leftSide));

				}


			}
			/*SimpleSearcher ss = new SimpleSearcher();
			ss.setVariableName(variableName);
			ss.visit(rightSide);
			boolean found = ss.getFound();

			System.out.println("Element found in here? " + found);*/

			newRightSide = parse(newBody);
			if (newRightSide != null) {
				nextInitializer.setInitializer(newRightSide);
			}

		}
	}



	private void handleAssignmentOperator(InfixExpression node) {
		System.out.println("[handleAssignmentOperator]: " + node.toSource());

		// Left & Right side
		AstNode leftSide = node.getLeft();
		AstNode rightSide = node.getRight();
		int rightSideType = rightSide.getType();
		int leftSideType = leftSide.getType();

		// Holders
		String varBeingWritten = "";
		String varBeingRead = "";

		// Get the name of the object being written to
		if (leftSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingWritten = leftSide.toSource().split("\\.")[0];
		} else if (leftSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingWritten = ((Name) leftSide).getIdentifier();
		} else {
			System.out.println("[handleAssignmentOperator]: Left hand side of assignment is not NAME or GETPROP, illegal?");
		}

		// Get the name of the object being read from
		if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingRead = rightSide.toSource().split("\\.")[0];
		} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingRead = ((Name) rightSide).getIdentifier();
		} else if (rightSide.getType() == org.mozilla.javascript.Token.OBJECTLIT) {
			System.out.println("[handleAssignmentOperator]: Right hand side of assignment is Object literal...to be implemented");
		} else if (rightSide.getType() == org.mozilla.javascript.Token.STRING
				|| rightSide.getType() == org.mozilla.javascript.Token.NUMBER) {
			System.out.println("[handleAssignmentOperator]: Right hand side of assignment is primitive type...no dependency");
			// No dependency
		} else {
			System.out.println("[handleAssignmentOperator]: Right hand side of assignment is not NAME or GETPROP, function?");
			System.out.println(rightSide.toSource());
		}


		// LHS = variable of interest
		if (varBeingWritten.equals(variableName)) {

			// Variable of interest is being written to
			if (rightSideType == org.mozilla.javascript.Token.FUNCTION) {
				// No data dependencies added

			} else if (rightSideType == org.mozilla.javascript.Token.CALL) {
				// Must slice return statement of function (could be defined elsewhere)
				// Must add arguments (see handleFunctionCall


				dataDependencies.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) rightSide));

				// If method call, must add base object to data dependencies

			} else if (rightSideType == org.mozilla.javascript.Token.NAME) {
				// Add name to data dependencies (variableName, lineNo)

				dataDependencies.add((Name) rightSide);

			} else if (rightSideType == org.mozilla.javascript.Token.GETPROP) {
				// Need to check if there is over lap with CALL (method calls, which do they fall under)


				dataDependencies.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) rightSide));



				// Must add base object to data dependencies
				// LVL add property as data dependency 'this.prop'
			} else if (rightSideType == org.mozilla.javascript.Token.ADD) {

				// Investigate how to get all variables in the add/concatination
				InfixExpression addOperation = ((InfixExpression) rightSide);

				System.out.println("Class: " + rightSide.getClass().toString());
				System.out.println("Operation: " + Token.typeToName(addOperation.getOperator()));
				System.out.println("Right: " + Token.typeToName(addOperation.getRight().getType()));
				System.out.println("Left: " + Token.typeToName(addOperation.getLeft().getType()));

				dataDependencies.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) rightSide));


			} else if (rightSideType == org.mozilla.javascript.Token.SUB) {
				InfixExpression subOperation = ((InfixExpression) rightSide);

				dataDependencies.addAll(InfixExpressionParser.getOperandDependencies((InfixExpression) rightSide));

				// Investigate how to get all variables in the sub
			} else if (rightSideType == org.mozilla.javascript.Token.NEG) {

				// Investigate how to get all variables in the sub
			} else if (rightSideType == org.mozilla.javascript.Token.POS) {

				// Investigate how to get all variables in the sub
			} else if (rightSideType == org.mozilla.javascript.Token.OBJECTLIT) {

				dataDependencies.addAll(ObjectLiteralParser.getArgumentDependencies((ObjectLiteral) rightSide));

				// Check RHS of property assignments for data dependencies (Name, Function Call, etc.)

			} else if (rightSideType == org.mozilla.javascript.Token.STRING
					|| rightSideType == org.mozilla.javascript.Token.NUMBER
					|| rightSideType == org.mozilla.javascript.Token.NULL
					|| rightSideType == org.mozilla.javascript.Token.FALSE
					|| rightSideType == org.mozilla.javascript.Token.TRUE) {
				// Don't care, no new dependencies
			} else {
				System.out.println("[handleAssignmentOperator]: Unknown right hand side type.");
			}
			// RIGHT = variable of interest
		} else if (varBeingRead.equals(variableName)) {
			// Variable of interest is being written to
			if (leftSideType == org.mozilla.javascript.Token.NAME) {
				// Add name to data dependencies (variableName, lineNo)

			} else if (leftSideType == org.mozilla.javascript.Token.GETPROP) {
				// Need to check if there is over lap with CALL (method calls, which do they fall under)

				// Must add base object to data dependencies
				// LVL add property as data dependency 'this.prop'
			} else {
				System.out.println("[handleAssignmentOperator]: Unknown left hand side type.");
			}
		}


	}

	static private ArrayList<AstNode> dependencies = new ArrayList<AstNode>();

	public ArrayList<AstNode> getNextSliceStart() {
		return dependencies;
	}

	private String variableName = null;

	public void setVariableName (String name) {
		this.variableName = name;
	}

	public String setVariableName () {
		return this.variableName;
	}
}
