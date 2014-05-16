package com.clematis.instrument;

import java.io.File;
import java.io.IOException;
import java.util.List;


import org.apache.commons.io.FileUtils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;


public class SeleniumInstrumenter {

	private static boolean jUnitTestFlag = false;

	public static void main(String[] args) {

		File toBeParsed = new File("/Users/sheldon/Downloads/phormer331/test/SeleniumTests/SeleniumTestProject/src/phormer/test/SlideShowTest.java");
		processJavaFile(toBeParsed);
	}

	public static void processJavaFile(File file) {

		String source = null;
		try {
			source = FileUtils.readFileToString(file);
		} catch (IOException e) {
			System.out.println("Error reading file for parsing.");
			e.printStackTrace();
		}

		//	System.out.println("Sauce:  " + source.toString());

		//Document document = new Document(source);
		//IDocument asd = new IDocument();		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(source.toCharArray());
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.recordModifications();

		// to get the imports from the file
		List<ImportDeclaration> imports = unit.imports();
		/*	for (ImportDeclaration i : imports) {
			System.out.println(i.getName().getFullyQualifiedName());
		}*/

		// Create and add a new import
		final AST ast = unit.getAST();
		ImportDeclaration id = ast.newImportDeclaration();
		String classToImport = "org.openqa.selenium.chrome.ChromeDriver";
		id.setName(ast.newName(classToImport/*.split("\\.")*/));
		unit.imports().add(id); // add import declaration at end

		unit.accept(new ASTVisitor() {

			// Related nodes: NormalAnnotation, SingleMemberAnnotation
			@Override
			public boolean visit(MarkerAnnotation node) {
				System.out.println("MarkerAnnotation " + node.toString() + ".");
				if (node.getTypeName().toString().equals("Test")) {
					// Next node should be instrumented

				}
				// The next method declaration should be instrumented
				jUnitTestFlag = true;
				return true;
			}
			@Override
			public void endVisit(MarkerAnnotation node) {
				System.out.println("MarkerAnnotation " + node.toString() + " is visited");
			}

			//  FunctionDeclaration
			@Override
			public boolean visit(MethodDeclaration node) {
				System.out.println("Entering method " + node.getName().toString() + ".");
				return true;
			}
			@Override
			public void endVisit(MethodDeclaration node) {
				System.out.println("Method " + node.getName().toString() + " is visited");
				if (jUnitTestFlag == true) {
					jUnitTestFlag = false;
				}
			}

			@Override
			public boolean visit(MethodInvocation node) {
				// cssSelector, id, linkText, partialLinkText
				System.out.println("Invoking " + node.getName().toString() + ".");
				if (node.getName().toString().equals("cssSelector")
						|| node.getName().toString().equals("id")
						|| node.getName().toString().equals("linkText")
						|| node.getName().toString().equals("partialLinkText")) {

					// Only want to replace references to "By" with "Byy" (wrapper class)
					if (node.getExpression().toString().equals("By")) {
						SimpleName replacemantName = ast.newSimpleName("Byy");
						node.setExpression(replacemantName);
					}
				}
				return true;
			}

			@Override
			public void preVisit(ASTNode node)  { 
				System.out.println("PRE:  " + node.toString() + " " + node.getClass());
			}

			@Override
			public boolean visit(PrefixExpression node) {
				System.out.println("Prefix:  " + node.toString());
				return true;
			}
		});


		//gv.visit(unit.getRoot());

		System.out.println("Cleaning up and printing:");
		System.out.println("====================================");

		System.out.println(unit.toString());
		// to save the changed file
		/*  TextEdit edits = unit.rewrite(document, null);
	    edits.apply(document);
	    FileUtils.writeStringToFile(file, document.get());*/

		// to iterate through methods
		/*   List<AbstractTypeDeclaration> types = unit.types();
	    for (AbstractTypeDeclaration type : types) {
	        if (type.getNodeType() == ASTNode.TYPE_DECLARATION) {
	            // Class def found
	            List<BodyDeclaration> bodies = type.bodyDeclarations();
	            for (BodyDeclaration body : bodies) {
	                if (body.getNodeType() == ASTNode.METHOD_DECLARATION) {
	                    MethodDeclaration method = (MethodDeclaration)body;
	                    System.out.println("name: " + method.getName().getFullyQualifiedName());
	                }
	            }
	        }
	    }*/
	}
}
