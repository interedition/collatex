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

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

/**
 * Represent one Pair in an MVD
 *
 * @author Desmond Schmidt 18/8/07
 */
public class Match {
  Match parent;
  LinkedList<Match> children;
  private byte[] data;
  public Set<Witness> versions;
  public static int pairId = 1;
  /**
   * parent id if subject of a transposition
   */
  int id;

  /**
   * Create a basic pair
   *
   * @param versions its versions
   * @param data     its data
   */
  public Match(Set<Witness> versions, byte[] data) {
    this.versions = versions;
    this.data = data;
  }

  /**
   * Get the number of children we have
   *
   * @return the current size of the children list
   */
  public int numChildren() {
    return (children == null) ? 0 : children.size();
  }

  /**
   * Get an iterator over all the children of this pair.
   * The caller should have first tested if there are any
   * children, of course. This would not be a runtime but
   * a coding error, hence we just fail here with a
   * NullPointerException
   *
   * @return an iterator
   */
  public ListIterator<Match> getChildIterator() {
    return children.listIterator();
  }

  /**
   * Add a child pair to this parent to be. Children
   * don't have any data.
   *
   * @param child the child to add
   */
  public void addChild(Match child) {
    if (children == null)
      children = new LinkedList<Match>();
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
    if (children.size() == 0)
      children = null;
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
    return (parent != null) ? parent.length() : data.length;
  }

  /**
   * Return the size of the data used by this pair
   *
   * @return the size of the data only
   */
  int dataSize() {
    if (parent != null || isHint())
      return 0;
    else if (data == null) {
      System.out.println("null");
      return 0;
    } else
      return data.length;
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
    return children != null;
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
      sb.append(new String(parent.data));
      sb.append("]");
    } else if (children != null) {
      sb.append("{" + id + ":");
      sb.append(new String(data));
      sb.append("}");
      sb.append("; children=");
      for (int i = 0; i < children.size(); i++) {
        Match p = children.get(i);
        sb.append(p.toString());
        if (i < children.size() - 1)
          sb.append(",");
      }
    } else if (data != null)
      sb.append("'").append(new String(data)).append("'");
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
  public byte[] getData() {
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
  void setData(byte[] data) {
    this.data = data;
  }

  /**
   * Get the child of a parent
   *
   * @param v the version to look for a child in
   * @return the relevant pair or null
   */
  Match getChildInVersion(Witness v) {
    Match child = null;
    ListIterator<Match> iter = getChildIterator();
    while (iter.hasNext()) {
      Match q = iter.next();
      if (q.contains(v)) {
        child = q;
        break;
      }
    }
    return child;
  }
}
