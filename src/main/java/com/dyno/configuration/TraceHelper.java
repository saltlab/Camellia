package com.dyno.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Scope;
import org.openqa.selenium.NotFoundException;

import com.dyno.core.trace.FunctionEnter;
import com.dyno.core.trace.PropertyRead;
import com.dyno.core.trace.RWOperation;
import com.dyno.core.trace.VariableRead;
import com.dyno.core.trace.VariableWrite;
import com.dyno.instrument.ProxyInstrumenter2;

public class TraceHelper {

	public static int getIndexOf(Collection<RWOperation> trace, RWOperation findMe) throws NotFoundException{
		int i = 0;
		Iterator<RWOperation> it = trace.iterator();

		while (it.hasNext()) {
			if (it.next().getOrder() == findMe.getOrder()) {
				return i;
			}
			i++;
		}

		// Something is wrong, should be present in trace
		System.err.println("Read/Write operation not found in trace.");
		throw new NotFoundException();

	}

	public static int getIndexOfIgnoreOrderNumber(Collection<RWOperation> trace, RWOperation findMe){
		int i = 0;
		Iterator<RWOperation> it = trace.iterator();
		RWOperation next;

		while (it.hasNext()) {
			next = it.next();

			if (next.getClass().equals(findMe.getClass())
					&& next.getLineNo() == findMe.getLineNo()
					&& next.getVariable().equals(findMe.getVariable())) {
				return i;
			}

			i++;
		}

		return -1;

	}

	public static RWOperation getElementAtIndex(Collection<RWOperation> trace, int index) throws IndexOutOfBoundsException {
		int i = 0;
		Iterator<RWOperation> it = trace.iterator();
		RWOperation next;

		while (it.hasNext()) {
			next = it.next();

			if (i == index) {
				return next;
			}
			i++;
		}

		System.err.println("Invalid index when searching trace.");
		throw new IndexOutOfBoundsException();
	}

	public static ArrayList<RWOperation> getDataDependencies(Collection<RWOperation> trace, VariableWrite vw) {
		int i = getIndexOf(trace, vw);
		ArrayList<RWOperation> deps = new ArrayList<RWOperation>();
		RWOperation current;
		boolean jumpAllowed = false;

		for (int j = i - 1; j >= 0; j--) {
			current = getElementAtIndex(trace, j);

			// TODO: Might need better criteria for checking if write is dependent on read
			// (programmer might split assignment operation between mutliple lines)
			if ((current instanceof VariableRead || current instanceof PropertyRead /*|| current instanceof FunctionReturn*/)
					&& current.getLineNo() == vw.getLineNo()) {
				deps.add(current);
			} else if (current instanceof FunctionEnter) {
				// Line number is allow to change (!= vw.getLineNo())

				// set a flag?
				jumpAllowed = true;

			} else if (current.getLineNo() != vw.getLineNo() && !jumpAllowed) {
				break;
			}
		}
		return deps;
	}
	
	public static Scope getDefiningScope(AstRoot ast, String name, int lineNo) {
	    ProxyInstrumenter2 sc = new ProxyInstrumenter2();

        sc.setLineNo(lineNo);
        sc.setVariableName(name); 

        ast.visit(sc);
        
        System.out.println(name);
        System.out.println(lineNo);
                
        System.out.println(sc.getLastScopeVisited().getLineno());
        
        return sc.getLastScopeVisited();
    }
	
	public static boolean isComplex(String value) {
		// Aliases are irrelevant if the assigned type is primitive
		if (value.equals("[object Number]")
			|| value.equals("[object String]")
					|| value.equals("[object Null]")
					|| value.equals("[object Undefined]")) {
			 return false;
		}
		return true;
	}

}
