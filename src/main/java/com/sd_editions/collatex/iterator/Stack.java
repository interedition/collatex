package com.sd_editions.collatex.iterator;

public interface Stack {

	Object peek();

	Object pop();

	void push(Object object);

	boolean isEmpty();
}