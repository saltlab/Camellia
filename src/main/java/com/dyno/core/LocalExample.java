package com.dyno.core;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

import com.google.common.io.Resources;

import com.dyno.instrument.AstInstrumenter;
import com.dyno.instrument.DependencyFinder;
import com.dyno.instrument.ProxyInstrumenter2;
import com.dyno.instrument.ReadWriteReplacer;
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

	public String instrument(String input, String scopename) {
		AstRoot ast = null;
		Scope scopeOfInterest = null;

		Iterator<SlicingCriteria> itsc2;
		Name start = new Name();
		SlicingCriteria justFinished;
		ArrayList<Name> varDeps;

		ArrayList<SlicingCriteria> possibleNextSteps;
		Iterator<Name> it;
		Name step;

		/* initialize JavaScript context */
		Context cx = Context.enter();

		/* create a new parser */
		Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

		/* parse some script and save it in AST */
		ast = rhinoParser.parse(new String(input), scopename, 0);

		// First criteria specified by user?
		start.setIdentifier(varName);
		start.setLineno(tempLineNo);
		remainingSlices.add(new SlicingCriteria(getDefiningScope(ast, start), varName));

		while (remainingSlices.size() > 0) {

			justFinished = new SlicingCriteria(remainingSlices.get(0).getScope(), remainingSlices.get(0).getVariable());

			// Get next variables dependencies
			varDeps = getDataDependencies(ast, justFinished);

			// Check if dependencies are new
			it = varDeps.iterator();
			possibleNextSteps = new ArrayList<SlicingCriteria>();

			while (it.hasNext()) {
				step = it.next();
				possibleNextSteps.add(new SlicingCriteria(getDefiningScope(ast, step), step.getIdentifier()));
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
		while (completedSlices.size() > 0) {			
			justFinished = new SlicingCriteria(completedSlices.get(0).getScope(), completedSlices.get(0).getVariable());

			// Set up parameters for instrumentation once scope if known
			inst.setVariableName(justFinished.getVariable());
			inst.setTopScope(justFinished.getScope());
			// NEW
			scopeOfInterest = justFinished.getScope();

			// Set up parameters for instrumentation once scope if known
			inst.setScopeName(targetFile);
			//wrr.setLineNo(tempLineNo);
			inst.setVariableName(justFinished.getVariable());
			inst.setTopScope(justFinished.getScope());
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
		}

		ast = inst.finish(ast);

		/* clean up */
		Context.exit();

		return ast.toSource();
	}

	private ArrayList<Name> getDataDependencies (AstRoot ast, SlicingCriteria target) {
		// DEPENDENCY FIND
		DependencyFinder df = new DependencyFinder();

		ArrayList<Name> deps = new ArrayList<Name>();

		SlicingCriteria newSlice = new SlicingCriteria(target.getScope(), target.getVariable());

		// Set up parameters for instrumentation once scope if known
		df.setVariableName(target.getVariable());
		df.setTopScope(target.getScope());			
		df.clearDataDependencies();
		df.setScopeName(targetFile);

		// Start the instrumentation for a single variable
		df.getTopScope().visit(df);			

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

		ast.visit(sc);
		return sc.getLastScopeVisited();
	}
}
