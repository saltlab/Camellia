package com.dyno.configuration;

import java.util.ArrayList;
import java.util.Iterator;

import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Scope;

import com.dyno.core.trace.ArgumentRead;
import com.dyno.core.trace.ArgumentWrite;
import com.dyno.core.trace.FunctionEnter;
import com.dyno.core.trace.PropertyRead;
import com.dyno.core.trace.RWOperation;
import com.dyno.core.trace.ReturnStatementValue;
import com.dyno.core.trace.VariableRead;
import com.dyno.core.trace.VariableWrite;
import com.dyno.instrument.ProxyInstrumenter2;

public class TraceHelper {

    public static int getIndexOfIgnoreOrderNumber(ArrayList<RWOperation> trace, RWOperation findMe){
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

    // OBSOLETE
    public static RWOperation getElementAtIndex(ArrayList<RWOperation> trace, int index) throws IndexOutOfBoundsException {
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

    public static ArrayList<RWOperation> getDataDependencies(ArrayList<RWOperation> trace, VariableWrite vw) throws Exception {
        int i = trace.indexOf(vw);
        ArrayList<RWOperation> deps = new ArrayList<RWOperation>();
        RWOperation current;
        boolean jumpAllowed = false;

        for (int j = i - 1; j >= 0; j--) {
            current = trace.get(j);

            // TODO: Might need better criteria for checking if write is dependent on read
            // (programmer might split assignment operation between mutliple lines)
            if (current instanceof VariableRead && current.getLineNo() == vw.getLineNo()) {
                deps.add(current);
            } else if (current instanceof PropertyRead && current.getLineNo() == vw.getLineNo()) {
                j = getAtomicIndex(trace, (PropertyRead) current);
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

        if (sc.getLastScopeVisited() != null) {
            System.out.println(sc.getLastScopeVisited().getLineno());
        }

        return sc.getLastScopeVisited();
    }

    private static int getAtomicIndex (ArrayList<RWOperation> trace, PropertyRead pr) throws Exception {
        int i = trace.indexOf(pr);
        String base;
        String base2;
        String prop;
        String prop2;
        String[] properties;
        String[] properties2;
        PropertyRead previousPropRead = pr;

        for (int j = i - 1; j >= 0; j--){
            if (trace.get(j) instanceof PropertyRead) {
                base = ((PropertyRead) trace.get(j)).getVariable();
                prop = ((PropertyRead) trace.get(j)).getProperty();
                properties = prop.split("\\.");

                for (int k = 0; k < properties.length; k++) {
                    if (properties[k].indexOf("(") != -1) {
                        properties[k] = properties[k].substring(0, properties[k].indexOf("("));
                    }
                    base += "."+properties[k];
                }

                prop2 = previousPropRead.getProperty();
                properties2 = prop2.split("\\.");
                base2 = previousPropRead.getVariable();
                for (int k = 0; k < properties2.length - 1; k++) {
                    if (properties2[k].indexOf("(") != -1) {
                        properties2[k] = properties2[k].substring(0, properties2[k].indexOf("("));
                    }
                    base2 += "."+properties2[k];
                }

                if (!base2.equals(base)) {
                    // Same operation still
                    throw new Exception();
                }
            } else if (trace.get(j) instanceof VariableRead) {
                return j;
            } else {
                System.out.println("[TraceHelper.getAtomicIndex]: Invalid RWOperation instead of PropertyRead/VariableRead.");
                throw new Exception();
            }
        }
        return i;
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

    public static RWOperation getEndOfFunction(ArgumentRead enter, ArrayList<RWOperation> trace) {
        // PRE: ArgumentRead enter (first argument) must have an ArgumentWrite succeeding it for guaranteed 
        //      correct behavior
        RWOperation next;
        int depth = -1;

        for (int i = trace.indexOf(enter); i < trace.size(); i++) {
            next = trace.get(i);

            if (next instanceof ReturnStatementValue
                    && enter.getFunctionName().equals(((ReturnStatementValue) next).getFunctionName())) {
                if (depth == 0) {
                    return next;
                } else {
                    depth--;
                }
            } else if (next instanceof ArgumentWrite) {
                // Group or single 'ArgumentWrite' means a function is entered
                int j;
                for (j = i; j < trace.size(); j++) {
                    if (!(trace.get(j) instanceof ArgumentWrite)) {
                        break;
                    }
                }
                depth++;
                // Continue search after the arguments have been initialized
                i = j - 1;
            }
        }


        return null;
    }


}
