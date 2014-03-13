package com.dyno.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.sourceforge.htmlunit.corejs.javascript.Token;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;

import com.google.common.io.Resources;

import com.crawljax.util.Helper;
import com.dyno.instrument.AstInstrumenter;
import com.dyno.instrument.DependencyFinder;
import com.dyno.instrument.FunctionTrace;
import com.dyno.instrument.ProxyInstrumenter;
import com.dyno.instrument.ProxyInstrumenter2;
import com.dyno.instrument.ReadWriteReplacer;
import com.dyno.jsmodify.JSModifyProxyPlugin;
import com.dyno.units.SlicingCriteria;

public class LocalExample {

	//private static String targetFile = "/short_bunnies.js";
	private static String targetFile = "/testing.js";
	private static int tempLineNo = 1;
	private static String varName = "r";

	// Definition scope finder
	private static ProxyInstrumenter2 ft = new ProxyInstrumenter2();
	private static DependencyFinder wrr = new DependencyFinder();

	private static ArrayList<SlicingCriteria> remainingSlices = new ArrayList<SlicingCriteria>();
	private static ArrayList<SlicingCriteria> completedSlices = new ArrayList<SlicingCriteria>();

	public static void main(String[] args) {
		URL urlOfTarget = AstInstrumenter.class.getResource(targetFile);
		String jsFileContents;

		System.out.println("[LocalExample]: ");
		System.out.println(urlOfTarget);

		try {
			jsFileContents = Resources.toString(urlOfTarget, Charset.defaultCharset());
			asd(jsFileContents, targetFile);
		} catch (IOException e) {
			System.out.println("[LocalExample]: " + "Trouble reading local file.");
			e.printStackTrace();
		}


	}

	private static String asd(String input, String scopename) {
		AstRoot ast = null;
		Scope scopeOfInterest = null;
		ArrayList<Name> nextToSlice;
		Iterator<Name> varIterator;
		Name nextVar;
		Iterator<SlicingCriteria> itsc;
		Iterator<SlicingCriteria> itsc2;
		
		SlicingCriteria justFinished;

		/* initialize JavaScript context */
		Context cx = Context.enter();

		/* create a new parser */
		Parser rhinoParser = new Parser(new CompilerEnvirons(), cx.getErrorReporter());

		/* parse some script and save it in AST */
		ast = rhinoParser.parse(new String(input), scopename, 0);

		ft.setScopeName(targetFile);
		ft.setLineNo(tempLineNo);
		ft.setVariableName(varName);
		ft.start(new String(input));

		ast.visit(ft);
		scopeOfInterest = ft.getLastScopeVisited();

		remainingSlices.add(new SlicingCriteria(scopeOfInterest, varName));

		while (remainingSlices.size() > 0) {
			
			justFinished = new SlicingCriteria(remainingSlices.get(0).getScope(), remainingSlices.get(0).getVariable());

			// Set up parameters for instrumentation once scope if known
			wrr.setVariableName(justFinished.getVariable());
			wrr.setTopScope(justFinished.getScope());
			
			
			
			// Set up parameters for instrumentation once scope if known
			wrr.clearDataDependencies();
			wrr.setScopeName(targetFile);
			//wrr.setLineNo(tempLineNo);
			wrr.setVariableName(justFinished.getVariable());
			wrr.setTopScope(justFinished.getScope());
			wrr.start(new String(input));

			System.out.println(scopeOfInterest.toSource());
			
			// Start the instrumentation for a single variable
			scopeOfInterest.visit(wrr);

			// Tidy up code after all instance of variable have been instrumented
			ast = wrr.finish(ast);

			// The current slice should be the first in queue, move it to completed queue
			if (remainingSlices.get(0).equals(justFinished)) {
				completedSlices.add(remainingSlices.remove(0));
			} else {
				System.err.println("Error! Insturmentation exeucted out of order!");
			}

			// Get all the related variables to slice iteratively (E.g. LHS/RHS of assignments for initially sliced variable)
			nextToSlice =  wrr.getDataDependencies();
			System.out.println("Size of new vars to slice: " + nextToSlice.size());

			varIterator = nextToSlice.iterator();

			// Check if the variable is already queued for instrumentation
			while (varIterator.hasNext()) {
				nextVar = varIterator.next();

				//TODO: instrument instances of this variable in other JavaScript files if it is global:
				//      ft.setScopeName(targetFile);
				ft.setLineNo(nextVar.getLineno());
				ft.setVariableName(nextVar.getIdentifier());

				// Slicing criteria is made up of variable name and its context (line number, scope, etc.)
				// Determine scope of variable, find its delcaration
				ast.visit(ft);
				scopeOfInterest = ft.getLastScopeVisited();	

				itsc = remainingSlices.iterator();
				itsc2 = completedSlices.iterator();

				// Check if the recently discovered related variables had already been known
				SlicingCriteria newSlice = new SlicingCriteria(scopeOfInterest, nextVar.getIdentifier());
				SlicingCriteria queuedSlice;
				SlicingCriteria completedSlice;
				boolean alreadyInQueue = false;

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
					System.out.println("New Name: " + nextVar.getIdentifier() + " @ " + nextVar.getLineno());
				} else {
					System.out.println("Old Name: " + nextVar.getIdentifier() + " @ " + nextVar.getLineno());
				}

			}

			

		}


		itsc2 = completedSlices.iterator();
		System.out.println("Completed slicing criteria:");
		System.out.println("===========================");
		while (itsc2.hasNext()) {
			System.out.println(itsc2.next().getVariable());
		}


		// Which ever finish is used, make sure to initialize the global class counter











		/* clean up */
		Context.exit();

		return ast.toSource();

	}


	private boolean compareVariables(Name nameA, Name nameB, Scope scopeA, Scope scopeB) {
		boolean isSame = false;

		if (nameA.getIdentifier().equals(nameB.getIdentifier())
				&& scopeA.equals(scopeB)) {
			System.out.println();
			isSame = true;
		} else if (nameA.getIdentifier().equals(nameB.getIdentifier())
				&& !scopeA.equals(scopeB)) { 
			System.out.println("Names same, scopes different:");
			System.out.println("Scope A:");
			System.out.println(scopeA.toSource());
			System.out.println("Scope B:");
			System.out.println(scopeB.toSource());
		}
		return isSame;
	}

}
