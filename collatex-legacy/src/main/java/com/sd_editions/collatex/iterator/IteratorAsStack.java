/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorAsStack implements Stack {

  private final Iterator<?> delegate;
  private Object topElement;
  private boolean topElementSet = false;

  public IteratorAsStack(Iterator<?> iterator) {
    this.delegate = iterator;
  }

  @Override
  public Object peek() {
    if (isEmpty()) throw new NoSuchElementException();
    return topElement;
  }

  @Override
  public Object pop() {
    Object result = peek();
    topElementSet = false;
    return result;
  }

  @Override
  @SuppressWarnings("unused")
  public void push(Object object) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty() {
    if (!topElementSet && delegate.hasNext()) {
      topElement = delegate.next();
      topElementSet = true;
    }
    return !topElementSet;
  }
}