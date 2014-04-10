package com.dyno.core;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

import com.google.common.io.Resources;

import com.dyno.instrument.AstInstrumenter;
import com.dyno.instrument.DependencyFinder;
import com.dyno.instrument.FunctionCallerDependencies;
import com.dyno.instrument.FunctionDeclarationInstrumenter;
import com.dyno.instrument.ProxyInstrumenter2;
import com.dyno.instrument.ReadWriteReplacer;
import com.dyno.units.ArgumentPassedIn;
import com.dyno.units.SlicingCriteria;

public class LocalExample {

	//private static String targetFile = "/short_bunnies.js";
	private String targetFile = "";
	private int tempLineNo = -1;
	private String varName = "";

	// Definition scope finder

	// SCOPE FIND
	private static ProxyInstrumenter2 sc = new ProxyInstrumenter2();

	private static ArrayList<SlicingCriteria> remainingSlices = new ArrayList<SlicingCriteria>();
	private static ArrayList<SlicingCriteria> completedSlices = new ArrayList<SlicingCriteria>();

	public void main(String[] args) {
		URL urlOfTarget = AstInstrumenter.class.getResource(targetFile);
		String jsFileContents;

		System.out.println("[LocalExample]: ");
		System.out.println(urlOfTarget);

		try {
			jsFileContents = Resources.toString(urlOfTarget, Charset.defaultCharset());
			instrument(jsFileContents, targetFile);
		} catch (IOException e) {
			System.out.println("[LocalExample]: " + "Trouble reading local file.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("[LocalExample]: " + "Something went wrong while instrumenting code.");
			e.printStackTrace();
		}
	}

	public void setTargetFile (String file) { 
		targetFile = "/" + file;
	}

	public void setLineNo (int l) { 
		tempLineNo = l;
	}

	public void setVariableName (String n) { 
		varName = n;
	}

	public String instrument(String input, String scopename) throws Exception {
		AstRoot ast = null;
		Scope scopeOfInterest = null;

		Iterator<SlicingCriteria> itsc2;
		Name start = new Name();
		SlicingCriteria justFinished;
		ArrayList<AstNode> varDeps;

		// TODO: TESTING THIS
		ArrayList<ArgumentPassedIn> logTheseArguments = new ArrayList<ArgumentPassedIn>();

		Scope definingScope;

		ArrayList<SlicingCriteria> possibleNextSteps;
		Iterator<AstNode> it;
		AstNode step;

		/* initialize JavaScript context */
		Context cx = Context.enter();

		/* create a new parser */
		Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

		/* parse some script and save it in AST */
		ast = rhinoParser.parse(new String(input), scopename, 0);

		// First criteria specified by user?
		start.setIdentifier(varName);
		start.setLineno(tempLineNo);
		definingScope = getDefiningScope(ast, start);

		remainingSlices.add(new SlicingCriteria(definingScope, varName));

		while (remainingSlices.size() > 0) {
			varDeps = new ArrayList<AstNode>();

			justFinished = new SlicingCriteria(remainingSlices.get(0).getScope(), remainingSlices.get(0).getVariable());

			// "UPWARDS"
			if (justFinished.getScope() instanceof FunctionNode 
					&& isArgument(justFinished.getVariable(), justFinished.getScope()) > -1) {
				// Need to find all places where the function is called ad(d argument number __ as a data dependency

				// Shouldn't be any duplicates at this point...remainingSlices are all unique?
				logTheseArguments.add(new ArgumentPassedIn((FunctionNode) justFinished.getScope(), justFinished.getVariable(), isArgument(justFinished.getVariable(), justFinished.getScope())));

				FunctionCallerDependencies fcd = new FunctionCallerDependencies();

				fcd.setFunctionName(((FunctionNode) justFinished.getScope()).getName());
				fcd.setArgumentNumber(isArgument(justFinished.getVariable(), justFinished.getScope()));
				fcd.setTopScope(((FunctionNode) justFinished.getScope()).getEnclosingScope());          
				fcd.clearDataDependencies();
				fcd.setScopeName(targetFile);

				fcd.getTopScope().visit(fcd);   

				varDeps.addAll(fcd.getDataDependencies());
				// get enclosing scope of the function delcaration...and find all calls to the function in there
			}

			// Get next variables dependencies
			varDeps.addAll(getDataDependencies(ast, justFinished));

			// Check if dependencies are new
			it = varDeps.iterator();
			possibleNextSteps = new ArrayList<SlicingCriteria>();

			while (it.hasNext()) {
				step = it.next();
				if (step instanceof Name) {
					possibleNextSteps.add(new SlicingCriteria(getDefiningScope(ast, (Name) step), ((Name) step).getIdentifier()));
				} else if (step instanceof KeywordLiteral && step.toSource().equals("this")) {
					possibleNextSteps.add(new SlicingCriteria(((KeywordLiteral) step).getEnclosingFunction(), "this"));
				}
			}

			// Add the new slicing criteria to queue (method checks against existing)
			addToQueue(possibleNextSteps);
		}

		// Actual instrumentation/augmentation of code after all dependencies known
		itsc2 = completedSlices.iterator();
		System.out.println("Completed slicing criteria:");
		System.out.println("===========================");

		SlicingCriteria next;

		while (itsc2.hasNext()) {
			next = itsc2.next();
			System.out.println(next.getVariable() + " ||  " + Token.typeToName(next.getScope().getType()));
		}

		// Which ever finish is used, make sure to initialize the global class counter
		ReadWriteReplacer inst = new ReadWriteReplacer();

		// Actual code augmentation/instrumentation happens here, at this point we know all the variables which must be tracked in the file
		/**    while (completedSlices.size() > 0) {			
            justFinished = new SlicingCriteria(completedSlices.get(0).getScope(), completedSlices.get(0).getVariable());

            // Set up parameters for instrumentation once scope if known
            inst.setVariableName(justFinished.getVariable());
            inst.setTopScope(justFinished.getScope());
            // NEW
            scopeOfInterest = justFinished.getScope();

            // Set up parameters for instrumentation once scope if known
            inst.setScopeName(targetFile);
            //wrr.setLineNo(tempLineNo);

            inst.start(new String(input));
            inst.setLineNo(tempLineNo);

            System.out.println("visiting! : " + justFinished.getVariable());
            System.out.println(Token.typeToName(justFinished.getScope().getType()));
            System.out.println("????????????");

            // Start the instrumentation for a single variable
            scopeOfInterest.visit(inst);

            // Tidy up code after all instance of variable have been instrumented
            //ast = ai.finish(ast);

            completedSlices.remove(0);
        }*/

		//inst.setRoot(ast);  see below line
		System.out.println(ast);
		inst.setVariablesOfInterest(completedSlices, ast);
		inst.setScopeName(targetFile);          
		inst.start(new String(input));

		ast.visit(inst);

		//ast = inst.finish(ast);



		// Instrument the function declarations! (track argument data flow)
		FunctionDeclarationInstrumenter fdi = new FunctionDeclarationInstrumenter();
		Iterator<ArgumentPassedIn> apiIt = logTheseArguments.iterator();
		while (apiIt.hasNext()) {	
			ArgumentPassedIn currentDeclarationToIntrument = apiIt.next();

			fdi.setArgumentName(currentDeclarationToIntrument.getArgument());
			fdi.setArgumentNumber(currentDeclarationToIntrument.getArgumentNumber());
			fdi.setTargetFunction(currentDeclarationToIntrument.getFunction());
			fdi.setScopeName(targetFile);


			System.out.println(currentDeclarationToIntrument.getArgument());
			System.out.println(currentDeclarationToIntrument.getArgumentNumber());
			System.out.println(currentDeclarationToIntrument.getFunction().getName());
			System.out.println("========");


			scopeOfInterest = currentDeclarationToIntrument.getFunction().getEnclosingScope();

			fdi.setTopScope(scopeOfInterest);
			fdi.start(new String(input));

			scopeOfInterest.visit(fdi);
		}


		ast = inst.finish(ast);

		System.out.println(ast.toSource());


		/* clean up */
		Context.exit();

		return ast.toSource();
	}

	private int isArgument(String varName2, Scope definingScope) throws Exception {
		if (definingScope instanceof FunctionNode) {
			List<AstNode> args = ((FunctionNode) definingScope).getParams();
			Iterator<AstNode> it = args.iterator();
			AstNode nextArg;
			int i = 0;

			while (it.hasNext()) {
				nextArg = it.next();

				if (nextArg instanceof Name) {
					if (((Name) nextArg).getIdentifier().equals(varName2)) {
						return i;	
					}
				} else {
					System.out.println("[isArgument]: Argument to Function defining scope is not 'Name'");
					throw new Exception();
				}
				i++;
			}
		}

		// Specified variable was not found function arguments
		return -1;
	}

	private ArrayList<AstNode> getDataDependencies (AstRoot ast, SlicingCriteria target) {
		// DEPENDENCY FIND
		DependencyFinder df = new DependencyFinder();

		ArrayList<AstNode> deps = new ArrayList<AstNode>();

		SlicingCriteria newSlice = new SlicingCriteria(target.getScope(), target.getVariable());

		// Set up parameters for instrumentation once scope if known
		df.setVariableName(target.getVariable());
		df.setTopScope(target.getScope());			
		df.clearDataDependencies();
		df.setScopeName(targetFile);

		System.out.println(df.getTopScope().toSource());

		// Start the instrumentation for a single variable
		df.getTopScope().visit(df);		

		System.out.println(df.getTopScope().toSource());


		// The current slice should be the first in queue, move it to completed queue
		if (remainingSlices.get(0).equals(newSlice)) {
			completedSlices.add(remainingSlices.remove(0));
		} else {
			System.err.println("Error! Insturmentation exeucted out of order!");
		}

		// Get all the related variables to slice iteratively (E.g. LHS/RHS of assignments for initially sliced variable)
		deps =  df.getDataDependencies();
		System.out.println("Size of new vars to slice: " + deps.size());

		return deps;
	}



	private static void addToQueue (ArrayList<SlicingCriteria> addThese) {
		Iterator<SlicingCriteria> itsc;
		SlicingCriteria queuedSlice;

		Iterator<SlicingCriteria> itsc2;
		SlicingCriteria completedSlice;

		Iterator<SlicingCriteria> itnew = addThese.iterator();
		SlicingCriteria newSlice;

		boolean alreadyInQueue;

		while (itnew.hasNext()) {
			newSlice = itnew.next();

			// Initialize iterator for each criteria check
			itsc = remainingSlices.iterator();
			itsc2 = completedSlices.iterator();

			// Check if the recently discovered related variables had already been known
			alreadyInQueue = false;

			// Already in queue
			while (itsc.hasNext()) {
				queuedSlice = itsc.next();
				if (queuedSlice.equals(newSlice)) {
					alreadyInQueue = true;
					break;
				}
			}

			// Already instrumented
			while (itsc2.hasNext()) {
				completedSlice = itsc2.next();
				if (completedSlice.equals(newSlice)) {
					alreadyInQueue = true;
					break;
				}
			}

			// Debugging:
			if (!alreadyInQueue) {
				remainingSlices.add(newSlice);
				System.out.println("New Name: " + newSlice.getVariable());
			} else {
				System.out.println("Old Name: " + newSlice.getVariable());
			}
		}
	}

	private Scope getDefiningScope(AstRoot ast, Name target) {
		sc.setScopeName(targetFile);
		sc.setLineNo(target.getLineno());
		sc.setVariableName(target.getIdentifier());

		if (target.getIdentifier().equals("this")) {
			System.out.println(target.getLineno());
			System.out.println(target.getIdentifier());
		}


		ast.visit(sc);

		try {
			System.out.println(sc.getLastScopeVisited().getLineno());
		} catch (NullPointerException e) {
	        System.out.println(target.getLineno());
	        System.out.println(target.getIdentifier());
		}
		
		return sc.getLastScopeVisited();
	}
}
