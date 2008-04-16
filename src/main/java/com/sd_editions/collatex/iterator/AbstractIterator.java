package com.sd_editions.collatex.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractIterator implements Iterator {

  private Boolean hasNext;

  final public Object next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    hasNext = null;
    return doNext();
  }

  final public boolean hasNext() {
    if (hasNext == null) {
      hasNext = new Boolean(doHasNext());
    }
    return hasNext.booleanValue();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  protected abstract Object doNext();

  protected abstract boolean doHasNext();
}