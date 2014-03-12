package com.dyno.instrument.helpers;

import java.util.ArrayList;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;

public class PropertyGetParser {

	public PropertyGetParser () {

	}

	public static ArrayList<Name> getArgumentDependencies(PropertyGet node) {
		ArrayList<Name> p = new ArrayList<Name>();

		AstNode object = node.getTarget();
		
		// Not used currently, naive case
		AstNode property = node.getProperty();

		switch (object.getType()) {
		case org.mozilla.javascript.Token.NAME:  
			p.add((Name) object);
			break;
		case org.mozilla.javascript.Token.GETPROP:
			p.addAll(getArgumentDependencies((PropertyGet) object));
			break;
		case org.mozilla.javascript.Token.CALL:  
			p.addAll(FunctionCallParser.getArgumentDependencies((FunctionCall) object));
			break;
		default:
			System.out.println("[InfixExpression]: Error parsing Infix Expression. Unknown operand type. (getNames())");
			break;

		}
		
		return p;
	}

}
