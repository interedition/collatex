package com.sd_editions.collatex.iterator;

public class ArrayIterator extends AbstractIterator {

	private Object[] array;
	private int index;

	public ArrayIterator(Object[] array) {
		this.array = array;
		this.index = 0;
	}

	public boolean doHasNext() {
		return index < array.length;
	}

	public Object doNext() {
		return array[index++];
	}
}