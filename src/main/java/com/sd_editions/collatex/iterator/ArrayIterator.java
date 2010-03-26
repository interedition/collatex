package com.sd_editions.collatex.iterator;

public class ArrayIterator extends AbstractIterator {

  private final Object[] array;
  private int index;

  public ArrayIterator(Object[] array1) {
    this.array = array1;
    this.index = 0;
  }

  @Override
  public boolean doHasNext() {
    return index < array.length;
  }

  @Override
  public Object doNext() {
    return array[index++];
  }
}