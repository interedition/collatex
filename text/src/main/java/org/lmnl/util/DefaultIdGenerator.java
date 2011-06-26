package org.lmnl.util;

import org.lmnl.Annotation;
import org.lmnl.Range;

/**
 * A default implementation of an identifier generator.
 */
public class DefaultIdGenerator implements IdGenerator {
	private int id = 0;

	public String next(Range range) {
		return ("seg_" + range.getStart() + "_" + range.getEnd());
	}

	public synchronized String next(Annotation annotation) {
		return ("lay_" + id++);
	}

	public synchronized void reset() {
		id = 0;
	}
}
