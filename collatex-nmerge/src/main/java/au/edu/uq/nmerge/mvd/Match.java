/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
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
package au.edu.uq.nmerge.mvd;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Represent one Pair in an MVD
 *
 * @author Desmond Schmidt 18/8/07
 */
public class Match<T> {
  public static int pairId = 1;

  /**
   * parent id if subject of a transposition
   */
  private int id;

  private Match parent;
  private List<Match> children = Lists.newArrayList();

  public Set<Witness> versions;
  private List<T> data;

  /**
   * Create a basic pair
   *
   * @param versions its versions
   * @param data     its data
   */
  public Match(Set<Witness> versions, List<T> data) {
    this.versions = versions;
    this.data = data;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public List<Match> getChildren() {
    return Collections.unmodifiableList(children);
  }

  /**
   * Get the number of children we have
   *
   * @return the current size of the children list
   */
  public int numChildren() {
    return children.size();
  }

  /**
   * Add a child pair to this parent to be. Children
   * don't have any data.
   *
   * @param child the child to add
   */
  public void addChild(Match child) {
    children.add(child);
    child.setParent(this);
  }

  /**
   * Remove a child pair. If this was our only child, stop
   * being a parent.
   *
   * @param child the child to remove
   */
  public void removeChild(Match child) {
    children.remove(child);
  }

  /**
   * Set the pair's parent i.e. make this a child
   *
   * @param parent the parent to be
   */
  public void setParent(Match parent) {
    this.parent = parent;
  }

  /**
   * Just get the length of the data, even if it is transposed.
   *
   * @return the length of the pair in bytes
   */
  int length() {
    return (parent != null) ? parent.length() : data.size();
  }

  /**
   * Does this pair contain the given version?
   *
   * @param version the version to test
   * @return true if version intersects with this pair
   */
  public boolean contains(Witness version) {
    return versions.contains(version);
  }

  /**
   * Is this pair really a hint?
   *
   * @return true if it is, false otherwise
   */
  public boolean isHint() {
    return versions.isEmpty();
  }

  /**
   * Is this pair a child, i.e. the object of a transposition?
   *
   * @return true if it is, false otherwise
   */
  public boolean isChild() {
    return parent != null;
  }

  /**
   * Is this pair a parent i.e. the subject of a transposition?
   *
   * @return true if it is, false otherwise
   */
  public boolean isParent() {
    return !children.isEmpty();
  }

  /**
   * Convert a pair to a human-readable form
   *
   * @return the pair as a String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(versions + ": ");
    if (parent != null) {
      sb.append("[" + parent.id + ":");
      sb.append(Iterables.toString(parent.data));
      sb.append("]");
    } else if (children != null) {
      sb.append("{" + id + ":");
      sb.append(Iterables.toString(data));
      sb.append("}");
      sb.append("; children=").append(Iterables.toString(children));
    } else if (data != null)
      sb.append("'").append(Iterables.toString(data)).append("'");
    else
      sb.append("null");
    return sb.toString();
  }

  /**
   * Get the parent of this child pair.
   *
   * @return the parent
   */
  public Match getParent() {
    return parent;
  }

  /**
   * Get the data of this pair
   *
   * @return this pair's data or that of its parent
   */
  public List<T> getData() {
    if (parent != null)
      return parent.getData();
    else
      return data;
  }

  /**
   * Set the data of this pair. Not to be used publicly!
   *
   * @param data the new data for this pair
   */
  void setData(List<T> data) {
    this.data = data;
  }

  /**
   * Get the child of a parent
   *
   * @param v the version to look for a child in
   * @return the relevant pair or null
   */
  Match getChildInVersion(Witness v) {
    for (Match q : children) {
      if (q.contains(v)) {
        return q;
      }
    }
    return null;
  }
}
