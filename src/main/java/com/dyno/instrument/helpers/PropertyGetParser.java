package com.dyno.instrument.helpers;

import java.util.ArrayList;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;


public class PropertyGetParser {

	public PropertyGetParser () {

	}

	public static ArrayList<Name> getPropertyDependencies(PropertyGet rightSide) {
		System.out.println("[PropertyDependencies]: Entering");
		
		ArrayList<Name> p = new ArrayList<Name>();

		AstNode object = rightSide.getTarget();
		
		System.out.println(Token.typeToName(object.getType()));
		System.out.println(object.toSource());
		
		// Not used currently, naive case
		AstNode property = rightSide.getProperty();

		switch (object.getType()) {
		case org.mozilla.javascript.Token.NAME:  
			p.add((Name) object);
			break;
		case org.mozilla.javascript.Token.GETPROP:
			p.addAll(getPropertyDependencies((PropertyGet) object));			
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
