package com.sd_editions.collatex.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorAsStack implements Stack {

	private Iterator delegate;
	private Object topElement;
	private boolean topElementSet = false;

	public IteratorAsStack(Iterator iterator) {
		this.delegate = iterator;
	}

	public Object peek() {
		if (isEmpty())
			throw new NoSuchElementException();
		return topElement;
	}

	public Object pop() {
		Object result = peek();
		topElementSet = false;
		return result;
	}

	public void push(Object object) {
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty() {
		if (!topElementSet && delegate.hasNext()) {
			topElement = delegate.next();
			topElementSet = true;
		}
		return !topElementSet;
	}
}