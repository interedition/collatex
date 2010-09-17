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

package com.sd_editions.collatex.Block;

import java.util.HashMap;

import com.google.common.collect.Maps;

/**
 * Our basic block class.
 * This shouldn't be used itself, use one of its subclasses or create your own
 *
 */
@Deprecated
public abstract class Block {

  /* The Block's name */
  private String name;
  /* Any attributes associated with this block */
  private final HashMap<String, String> attributes;
  /* Basic XML like association with other Blocks */
  private Block firstChild;
  private Block lastChild;
  private Block nextSibling;
  private Block previousSibling;
  private Block startParent;
  private Block endParent;

  public Block() {
    this.attributes = Maps.newHashMap();
  }

  public String getName() {
    return this.name;
  }

  protected void setName(final String _name) {
    this.name = _name;
  }

  protected void setFirstChild(final Block _firstChild) {
    this.firstChild = _firstChild;
  }

  public Block getFirstChild() {
    return firstChild;
  }

  public boolean hasFirstChild() {
    return (this.firstChild != null);
  }

  protected void removeFirstChild() {
    this.firstChild = null;
  }

  protected void setLastChild(final Block _lastChild) {
    this.lastChild = _lastChild;
  }

  public Block getLastChild() {
    return this.lastChild;
  }

  public boolean hasLastChild() {
    return (this.lastChild != null);
  }

  protected void removeLastChild() {
    this.lastChild = null;
  }

  protected void setNextSibling(final Block _nextSibling) {
    this.nextSibling = _nextSibling;
  }

  public Block getNextSibling() {
    return this.nextSibling;
  }

  public boolean hasNextSibling() {
    return (this.nextSibling != null);
  }

  protected void removeNextSibling() {
    this.nextSibling = null;
  }

  protected void setPreviousSibling(final Block _previousSibling) {
    this.previousSibling = _previousSibling;
  }

  public Block getPreviousSibling() {
    return this.previousSibling;
  }

  public boolean hasPreviousSibling() {
    return (this.previousSibling != null);
  }

  protected void removePreviousSibling() {
    this.previousSibling = null;
  }

  protected void setStartParent(final Block _startParent) {
    this.startParent = _startParent;
  }

  public Block getStartParent() {
    return this.startParent;
  }

  public boolean hasStartParent() {
    return (this.startParent != null);
  }

  protected void removeStartParent() {
    this.startParent = null;
  }

  protected void setEndParent(final Block endParent) {
    this.endParent = endParent;
  }

  public Block getEndParent() {
    return this.endParent;
  }

  public boolean hasEndParent() {
    return (this.endParent != null);
  }

  protected void removeEndParent() {
    this.endParent = null;
  }

  public void setAttribute(final String key, final String value) {
    this.attributes.put(key, value);
  }

  public String getAttribute(final String key) {
    return this.attributes.get(key);
  }

  public String removeAttribute(final String key) {
    return this.attributes.remove(key);
  }

  public int numberOfAttributes() {
    return this.attributes.size();
  }

  public abstract void accept(IntBlockVisitor visitor);
}