package com.clematis.instrument;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;

public class GenericVisitor extends ASTVisitor {
	
  List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
  List<ASTNode> otherNodes = new ArrayList<ASTNode>();

  @Override
  public boolean visit(MethodDeclaration node) {
    methods.add(node);
    return super.visit(node);
  }
  
  public boolean visit(ASTNode node) {
    
    if (node instanceof  NormalAnnotation) { 
        System.out.println(node.getClass());
        return super.visit((NormalAnnotation) node);
    } else if (node instanceof CompilationUnit) {
        return super.visit((CompilationUnit) node);
    } else {
    	System.out.println("Other node type:  " + node.getClass());
    }
    return true;
  }

  public List<MethodDeclaration> getMethods() {
    return methods;
  }
} 