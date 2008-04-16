package com.sd_editions.collatex.iterator;

public class ArrayIterator extends AbstractIterator {

  private Object[] array;
  private int index;

  public ArrayIterator(Object[] array) {
    this.array = array;
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