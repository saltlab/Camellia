package com.camellia.instrument.helpers;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Scope;

import com.camellia.instrument.AstInstrumenter;
import com.camellia.instrument.InstrumenterHelper;
import com.camellia.units.FunctionArgumentPair;

public class ParentFunctionFinder  extends AstInstrumenter {

	@Override
	public AstNode createNodeInFunction(FunctionNode function, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AstNode createNode(FunctionNode function, String postfix, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start(String node) {
		// TODO Auto-generated method stub

	}

	@Override
	public AstNode createPointNode(String objectAndFunction, int lineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean visit(AstNode node) {
		int tt = node.getType();

		// This won't work with minified JavaScript, but will work for now
		if (node.getLineno() == lookingForLineNumber) {
			 Scope s = findParentFunction(node);
			 
			 if (s instanceof FunctionNode) {
				 parentFunction = ((FunctionNode) s).getName();
			 } else {
				 parentFunction = "script";
			 }
			 
			 return false;
		}

		return true;
	}

	private Scope findParentFunction(AstNode node) {

		ArrayList<Scope> chain = InstrumenterHelper.getScopeChain(node);

		return chain.get(0);

	}

	private String parentFunction = "";

	public String getParentFunction() {
		return this.parentFunction;
	}

	private int lookingForLineNumber;

	public void setLineNumber (int fa) {
		this.lookingForLineNumber = fa;
	}
}
