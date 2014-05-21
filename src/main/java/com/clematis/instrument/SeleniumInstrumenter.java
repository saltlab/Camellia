package com.clematis.instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.commons.io.FileUtils;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.search.matching.PackageDeclarationLocator;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.util.Helper;


public class SeleniumInstrumenter {

    private static boolean jUnitTestFlag = false;

    public static void main(String[] args) {

        // File toBeParsed = new File("/Users/sheldon/Downloads/phormer331/test/SeleniumTests/SeleniumTestProject/src/phormer/test/SlideShowTest.java");
        File toBeParsed = new File("/Users/Sheldon/Camillia/src/main/java/com/clematis/selenium/SlideShowTest_before.java");
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
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setSource(source.toCharArray());
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        unit.recordModifications();
        System.out.println(unit.getPackage().getName().toString());


        Name newPackage = unit.getAST().newName("com.clematis.selenium");
        unit.getPackage().setName(newPackage);

        System.out.println(unit.getPackage().getName().toString());


        // Create and add a new import
        final AST ast = unit.getAST();
        ImportDeclaration id;

        // Add our wrapper classes to the test case definition
        ArrayList<String> classesToImport = new ArrayList<String>();	
        classesToImport.add("com.clematis.core.WebDriverWrapper");
        classesToImport.add("com.clematis.jsmodify.JSExecutionTracer");
        Iterator<String> it = classesToImport.iterator();
        while (it.hasNext()) {
            id = ast.newImportDeclaration();
            id.setName(ast.newName(it.next()/*.split("\\.")*/));
            unit.imports().add(id); // add import declaration at end
        }

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


        parser.setKind(ASTParser.K_COMPILATION_UNIT);


        MethodDeclaration attachMe1 = getMethodFromFile("src/main/resources/javawrappers/assertTrue.txt");
        MethodDeclaration attachMe2 = getMethodFromFile("src/main/resources/javawrappers/assertEquals.txt");
        System.out.println(attachMe1.toString());






        System.out.println(unit.toString());



        int appendBefore = unit.toString().lastIndexOf("}");
        String newSource = unit.toString().substring(0, appendBefore);
        newSource += attachMe1.toString();
        newSource += attachMe2.toString();
        newSource +="}";


        System.out.println(newSource);

        //   attached1.
        //   attached1.setBody(unit.getAST().newBlock());
        //  attached1.setBody(block1);


        System.out.println(unit.toString());

        try {
            File output = new File("src/main/java/com/clematis/selenium/output1.java");
            output.createNewFile();

            FileWriter fr = new FileWriter(output);


            fr.write(newSource);
            fr.flush();
            fr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        

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

    private static MethodDeclaration getMethodFromFile (String fileName) {
        MethodDeclaration returnMe = null;
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        ArrayList<String> fileContents = getFileContents(fileName);

        String sourceStart = "public class A {";
        //add a fake class A as a shell, to meet the requirement of ASTParser
        String sourceMiddle = "";
        for(String s : fileContents){ 
            s = s.trim();
            if(s.trim().length()>0 && !s.startsWith("---") && !s.startsWith("/") && !s.startsWith("*") )
                sourceMiddle += s.trim() + "\n";
        }
        String sourceEnd = "}";

        String source = sourceStart + sourceMiddle + sourceEnd;

        System.out.println(source);

        parser.setSource(source.toCharArray());
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        MethodVisitor astVisitor = new MethodVisitor();

        cu.accept(astVisitor);
        returnMe = astVisitor.getLastMethod();

        return returnMe;
    }

    private static ArrayList<String> getFileContents(String fileName) {
        ArrayList<String> fileAsArray = new ArrayList<String>();

        String s;
        String fileContents = "";
        try {
            File wrapper1 = new File(fileName);
            FileReader fr = new FileReader(wrapper1);
            BufferedReader br = new BufferedReader(fr); 

            // Read assertion accesses and results from file and instantiate JSONObject
            while((s = br.readLine()) != null) {
                fileAsArray.add(s);
                //fileContents += s;
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileAsArray;
    }
}
