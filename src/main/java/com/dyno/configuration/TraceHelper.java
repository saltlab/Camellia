package com.dyno.configuration;

import java.util.Collection;
import java.util.Iterator;

import org.openqa.selenium.NotFoundException;

import com.dyno.core.trace.RWOperation;

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

}
