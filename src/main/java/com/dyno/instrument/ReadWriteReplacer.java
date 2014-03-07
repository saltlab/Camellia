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
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

public class ReadWriteReplacer extends AstInstrumenter {

	private static final String TOOLNAME = "_dyno";
	private static final String VARREAD = "_dynoRead";
	private static final String VARWRITE = "_dynoWrite";
	private static final String VARWRITEFUNCRET = "_dynoWriteReturnValue";
	private static final String PROPREAD = "_dynoReadProp";
	private static final String FUNCCALL = "_dynoFunc";

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

	/**
	 * Construct without patterns.
	 */
	public ReadWriteReplacer() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public ReadWriteReplacer(ArrayList<String> excludes) {
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


		if (tt == org.mozilla.javascript.Token.GETPROP) {
			//	handleProperty((PropertyGet) node);
		} else if (tt == org.mozilla.javascript.Token.VAR && node instanceof VariableDeclaration) {
			handleVariableDeclaration((VariableDeclaration) node);
		} else if (tt == org.mozilla.javascript.Token.ASSIGN) {
			handleAssignmentOperator((Assignment) node);
		} else if (tt == org.mozilla.javascript.Token.CALL) {
			handleFunctionCall((FunctionCall) node);
		} else if (tt == org.mozilla.javascript.Token.NAME && node.getLineno() == lineNo && ((Name) node).getIdentifier().equals(variableName)) {
			// Might need stricter check since target variable could appear multiple times on single line

			handleName((Name) node);
		} 



		return true;  // process kids
	}

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean targetIsChild(Name node) {
		SimpleSearcher ss = new SimpleSearcher();

		return ss.checkIfChild(node);
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
				.replaceAll("(\\n\\;\\n)", "\n\n")
				.replaceAll("\\.\\[", "[");

		System.out.println(isc);

		AstRoot iscNode = rhinoCreateNode(isc);


		// Return new instrumented node/code
		return iscNode;
	}

	@Override
	public void start(String node) {
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
		System.out.println(scope == null);
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

			if (leftSide.getType() == org.mozilla.javascript.Token.NAME && ((Name) leftSide).getIdentifier().equals(variableName)) {
				newBody = ("("+VARWRITE+"(\""+leftSide.toSource()+"\", "+node.getLineno()+"), "+rightSide.toSource()+")");

				newRightSide = parse(newBody);
				if (newRightSide != null) {
					nextInitializer.setInitializer(newRightSide);
				}
			}

		}
	}

	private void handleName(Name node) {
		String newBody;
		AstNode newTarget;
		AstNode parent = node.getParent();

		if (node.getParent().getType() == org.mozilla.javascript.Token.GETPROP) {
			System.out.println("GETPROP111");
			// If leading name/label e.g. 'document' in 'document.getElement()'
			if (parent.toSource().split("\\.")[0].equals(node.getIdentifier())) {
				System.out.println("111");

				newBody = VARREAD+"(\'"+node.getIdentifier()+"\', "+node.getLineno()+")";
			} else {
				newBody = parent.toSource().replaceFirst("."+node.getIdentifier(), "["+PROPREAD+"(\""+node.getIdentifier()+"\", "+node.getLineno()+")]");
			}
		} else if (node.getParent().getType() != org.mozilla.javascript.Token.VAR) {
			newBody = VARREAD+"(\'"+node.getIdentifier()+"\', " +node.getLineno()+")";
		} else {
			return;
		}
		newTarget = parse(newBody);
		if (newTarget != null) {
			node.setIdentifier(newBody);
		}

	}

	private void handleProperty(PropertyGet node) {
		System.out.println("[handleProperty]: " + node.toSource());

		String varBeingWritten = node.getLeft().toSource().split("\\.")[0];

		if (!varBeingWritten.equals(variableName)) {
			return;
		}

		AstNode newTarget;

		String newBody = "["+PROPREAD+"(\""+node.getProperty().toSource()+"\", "+node.getLineno()+")]";

		// Check if valid JavaScript to be safe
		newTarget = parse(newBody);

		//	node.removeChild();
		//	node.replaceChild(node.getRight(), newTarget);
		//	node.setRight(parse(node.getProperty().toSource()));
		//	node.setRight();
		//	node.setTarget(newTarget);
		Name tt = new Name();
		tt.setIdentifier(newBody);
		node.setProperty(tt);

		//node.setTarget(parse(""));
		//node.setOperator(org.mozilla.javascript.Token.EMPTY);
		//node.setTarget(parse("_clematestRemove"));
	}



	private void handleAssignmentOperator(Assignment node) {
		// Left & Right side
		AstNode leftSide = node.getLeft();
		AstNode rightSide = node.getRight();
		int rightRightSideType = rightSide.getType();

		// Replacement holders
		AstNode newRightSide;
		String newBody = "";
		String varBeingWritten = "";
		String varBeingRead = "";

		ArrayList<String> wrapperArgs = new ArrayList<String>();

		if (leftSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingWritten = leftSide.toSource().split("\\.")[0];
		} else if (leftSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingWritten = ((Name) leftSide).getIdentifier();
		}

		if (rightSide.getType() == org.mozilla.javascript.Token.GETPROP) {
			varBeingRead = rightSide.toSource().split("\\.")[0];
		} else if (rightSide.getType() == org.mozilla.javascript.Token.NAME) {
			varBeingRead = ((Name) rightSide).getIdentifier();
		}

		if (varBeingWritten.equals(variableName)) {
			// Variable of interest is being written to
			if (rightRightSideType == org.mozilla.javascript.Token.FUNCTION) {
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(getFunctionName((FunctionNode) rightSide));
				wrapperArgs.add(node.getLineno()+"");

				newBody = generateWrapper(VARWRITE, wrapperArgs);
			} else if (rightRightSideType == org.mozilla.javascript.Token.STRING 
					|| rightRightSideType == org.mozilla.javascript.Token.NUMBER
					|| rightRightSideType == org.mozilla.javascript.Token.NEG
					|| rightRightSideType == org.mozilla.javascript.Token.POS
					|| rightRightSideType == org.mozilla.javascript.Token.NULL
					// More options
					|| rightRightSideType == org.mozilla.javascript.Token.GETPROP
					|| rightRightSideType == org.mozilla.javascript.Token.NAME
					|| rightRightSideType == org.mozilla.javascript.Token.FALSE
					|| rightRightSideType == org.mozilla.javascript.Token.TRUE) {
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
			} else if (rightRightSideType == org.mozilla.javascript.Token.ADD) {
				// Need to iterate through all add items so we can backwards slice from those
				// These include string concats
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
			} else if (rightRightSideType == org.mozilla.javascript.Token.SUB) { 
				// Need to iterate through all items involve din the subtraction
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = rightSide.toSource().replaceFirst(rightSide.toSource(), generateWrapper(VARWRITE, wrapperArgs));
			} else if (rightRightSideType == org.mozilla.javascript.Token.CALL) {
				// Need to iterate through arguments to get data depends
				// Function being called may provide the control flow? and return type (therefore include the return statement in the slice?)
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = generateWrapper(VARWRITEFUNCRET, wrapperArgs);
			} else if (rightRightSideType == org.mozilla.javascript.Token.NEW) {
				wrapperArgs.add(varBeingWritten);
				wrapperArgs.add(rightSide.toSource());
				wrapperArgs.add(node.getLineno()+"");

				newBody = ("("+VARWRITE+"(\""+varBeingWritten+"\", "+node.getLineno()+"), "+rightSide.toSource()+")");
			} else {
				System.out.println("New right side type:" + Token.typeToName(rightRightSideType));
			}

			newRightSide = parse(newBody);

			if (newRightSide != null) {
				node.setRight(newRightSide);
			}
		} else if (varBeingRead.equals(variableName)) {
			// TODO: Thursday, March 6th, 2014
			// If variable of interest is not a primitive type (is a complex object), it could be changed by reference. Therefore, the variable being written to is a dependency?

		}
	}

	private String generateWrapper (String wrapperMethod, ArrayList<String> arguments) {
		String toBeReturned = wrapperMethod + "(";
		Iterator<String> it = arguments.iterator();
		String nextArgument;
		boolean first = true;

		while (it.hasNext()) {
			nextArgument = it.next();
			if (first) {
				nextArgument = "\"" + nextArgument.replaceAll("\"", "\'") + "\"";
				first = false;
			} else {
				nextArgument = ", " + nextArgument;
			}
			toBeReturned += nextArgument;
		}
		toBeReturned += ")";
		return toBeReturned;
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
		String targetMethod = target.toSource();
		String targetObject = "";
		AstNode newTarget = null;
		
		// TODO: fix this
		int lineNo = (node.getLineno() == 0 ? node.getParent().getParent().getLineno() : 0);

		int tt = target.getType();
		if (tt == org.mozilla.javascript.Token.GETPROP) {
			// Class specific function call, 33
			// E.g. document.getElementById, e.stopPropagation
			String[] methods = targetMethod.split("\\.");
			targetObject = methods[0];
			targetMethod = methods[methods.length-1];
			if (variableName.equals(targetObject)) {
				System.out.println("[handleFunctionCall]: " + targetMethod + " line " + target.getLineno());
				String newBody = target.toSource().replaceFirst("."+targetMethod, "["+FUNCCALL+"(\""+targetObject+"\",\""+targetMethod+"\", "+target.getLineno()+")]");
				newTarget = parse(newBody);
				newTarget.setLineno(target.getLineno());
			}
		}	
		if (newTarget != null) {
			node.setTarget(newTarget);
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

	private int lineNo = -1;

	public void setLineNo (int num) {
		this.lineNo = num-1;
	}

	public int getLineNo () {
		return this.lineNo + 1;
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