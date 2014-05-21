package com.clematis.instrument;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodVisitor extends ASTVisitor {
    private MethodDeclaration m;

    //by add more visit method like the following below, then all king of statement can be visited.
    public boolean visit(MethodDeclaration node) {

        System.out.println("MethodDeclaration -- content:" + node.toString());

        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(node.getStartPosition());
        al.add(node.getLength());

        m = node;

        return false;
    }

    public MethodDeclaration getLastMethod() {
        return m;
    }

};