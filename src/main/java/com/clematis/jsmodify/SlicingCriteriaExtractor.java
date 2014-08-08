package com.clematis.jsmodify;

import java.util.ArrayList;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import com.camellia.instrument.helpers.InfixExpressionParser;
import com.clematis.instrument.AstInstrumenter;

public class SlicingCriteriaExtractor extends AstInstrumenter {

	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
	private ErrorReporter errorReporter = compilerEnvirons.getErrorReporter();
	private ArrayList deps = new ArrayList<AstNode>();

	/**
	 * Construct without patterns.
	 */
	public SlicingCriteriaExtractor() {
		super();
	}

	/**
	 * Constructor with patterns.
	 * 
	 * @param excludes
	 *            List with variable patterns to exclude.
	 */
	public SlicingCriteriaExtractor(ArrayList<String> excludes) {
		super(excludes);
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstRoot parse(String code, int line) {
		Parser p = new Parser(compilerEnvirons, errorReporter);

		//System.out.println(code);
		return p.parse(code, null, line);
	}

	@Override
	public  boolean visit(AstNode node){
		int tt = node.getType();

		if (tt == org.mozilla.javascript.Token.ASSIGN) {
			deps.addAll(InfixExpressionParser.getOperandDependencies(((InfixExpression)node)));
			return false;
		} else {
			return true;  // process kids
		}
	}
	
	public ArrayList<AstNode> getDependencies() {
		return deps;
	}
	
	public void clearDependencies() {
		deps = new ArrayList<AstNode>();
	}

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public AstRoot finish(AstRoot node) {
		// Return 
		return node;
	}

	@Override
	public void start(String node) {
	}

	@Override
	public AstNode createNode(FunctionNode function, String postfix, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstNode createPointNode(String objectAndFunction, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}
}
