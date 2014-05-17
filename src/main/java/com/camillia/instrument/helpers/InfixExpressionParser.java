package com.camillia.instrument.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableDeclaration;

import com.camillia.instrument.AstInstrumenter;

public class InfixExpressionParser {//extends AstInstrumenter {

	public InfixExpressionParser () {

	}

	public static ArrayList<AstNode> /* ArrayList<AstNode> */ getOperandDependencies(InfixExpression node) {

		ArrayList<AstNode> d = new ArrayList<AstNode>();
		ArrayList<AstNode> operands = new ArrayList<AstNode>();
		AstNode operand;
		Iterator<AstNode> it;

		// Un-used right now
		//int operationType = node.getOperator();

		operands.add(node.getLeft());
		operands.add(node.getRight());

		it = operands.iterator();

		while (it.hasNext()) {
			operand = it.next();
			switch (operand.getType()) {
			case org.mozilla.javascript.Token.ADD:  
				// Call recursively (e.g. var a = b + c + d)
				d.addAll(getOperandDependencies((InfixExpression) operand));
				break;
			case org.mozilla.javascript.Token.SUB:
				d.addAll(getOperandDependencies((InfixExpression) operand));

				break;
			case org.mozilla.javascript.Token.NAME:  
				d.add((Name) operand);
				break;
			case org.mozilla.javascript.Token.THIS:  
				d.add((KeywordLiteral) operand);
				break;
			case org.mozilla.javascript.Token.GETPROP:  
				d.addAll(PropertyGetParser.getPropertyDependencies((PropertyGet) operand));
				break;
			case org.mozilla.javascript.Token.NUMBER:  
			case org.mozilla.javascript.Token.STRING:  
				System.out.println("[InfixExpression]: Don't care infix (String or Number)");
				break;
			default:
				System.out.println("[InfixExpression]: Error parsing Infix Expression. Unknown operand type. (getNames())");
				break;
			}
		}



		return d;
	}

}
