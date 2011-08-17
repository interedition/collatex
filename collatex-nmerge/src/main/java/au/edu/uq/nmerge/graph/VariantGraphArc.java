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
package au.edu.uq.nmerge.graph;

import au.edu.uq.nmerge.Errors;
import au.edu.uq.nmerge.exception.MVDException;
import au.edu.uq.nmerge.mvd.Match;
import au.edu.uq.nmerge.mvd.Witness;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

/**
 * An Arc is a fragment of data, a set of versions in a variant graph
 *
 * @author Desmond Schmidt
 */
public class VariantGraphArc {
  /**
   * the Adler32 modulus
   */
  static int MOD_ADLER = 65521;
  static String alphabet = new String(
          "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
  /**
   * the set of versions
   */
  public Set<Witness> versions;
  /**
   * the origin Node
   */
  VariantGraphNode from;
  /**
   * the destination Node
   */
  public VariantGraphNode to;
  /**
   * the data of this Arc
   */
  byte[] data;
  /**
   * usually empty list of transpose children
   */
  LinkedList<VariantGraphArc> children;
  /**
   * parent - can't be set as well as children
   */
  VariantGraphArc parent;
  /**
   * parent id if subject of a transposition
   */
  int id;
  /**
   * id counter: used for parent/child relationships
   */
  static int arcId = 1;

  /**
   * Create a child arc
   *
   * @param versions the set of versions for the child
   * @param parent   the parent arc whose data will be copied
   */
  public VariantGraphArc(Set<Witness> versions, VariantGraphArc parent) {
    this.versions = versions;
    // may attempt to create child of a child
    if (parent != null) {
      VariantGraphArc temp = parent;
      while (temp.parent != null)
        temp = temp.parent;
      if (temp != parent)
        parent = temp;
      this.parent = temp;
      parent.addChild(this);
    }
    //checkForData("m_s id=\"page");
  }

  /**
   * Get an error over the children. Shouldn't be called if no
   * children, hence we throw an exception
   *
   * @return a ListIterator over the arc's children
   * @throws MVDException
   */
  ListIterator<VariantGraphArc> childIterator() throws MVDException {
    if (children == null)
      throw new MVDException("no children to arc");
    return children.listIterator(0);
  }

  /**
   * Create a vanilla Arc
   *
   * @param versions the versions it belongs to
   * @param data     its data content
   */
  public VariantGraphArc(Set<Witness> versions, byte[] data) {
    this.versions = versions;
    this.data = data;
    //checkForData("m_s id=\"page");
  }

  /**
   * Set the to node
   *
   * @param to the new to node
   */
  public void setTo(VariantGraphNode to) {
    this.to = to;
  }

  /**
   * Set the from node
   *
   * @param from the new from node
   */
  public void setFrom(VariantGraphNode from) {
    this.from = from;
  }

  /**
   * Get the data of this arc
   *
   * @return the data as a byte array
   */
  public byte[] getData() {
    if (parent != null)
      return parent.data;
    else
      return data;
  }

  /**
   * Get the length of the data of this arc
   *
   * @return the length as an int
   */
  public int dataLen() {
    if (parent != null)
      return parent.data.length;
    else
      return data.length;
  }

  /**
   * Add one version to the arc
   *
   * @param version the version to add
   */
  public void addVersion(Witness version) {
    versions.add(version);
    if (to != null)
      to.addIncomingVersion(version);
    if (from != null)
      from.addOutgoingVersion(version);
  }

  /**
   * Add a child to the parent
   *
   * @param child the child arc
   */
  public void addChild(VariantGraphArc child) {
    if (children == null) {
      children = new LinkedList<VariantGraphArc>();
      id = VariantGraphArc.arcId++;
    }
    children.add(child);
    child.parent = this;
  }

  /**
   * Convert an Arc to a String for printing out
   *
   * @return the arc as a string
   */
  public String toString() {
    try {
      StringBuffer sb = new StringBuffer();
      if (from == null)
        sb.append("(0)");
      else if (from.isIncomingEmpty())
        sb.append("(s)");
      else
        sb.append("(" + from.nodeId + ")");
      sb.append(Iterables.toString(versions));
      byte[] realData = getData();
      String dataString = new String(realData);
      dataString = dataString.replace('\r', '/');
      dataString = dataString.replace('\n', '/');
      sb.append(": ");
      if (parent != null)
        sb.append("[" + parent.id + ":");
      else if (children != null)
        sb.append("{" + id + ":");
      sb.append("\"" + dataString + "\"");
      if (parent != null)
        sb.append("]");
      else if (children != null)
        sb.append("}");
      if (to == null)
        sb.append("(0)");
      else if (to.isOutgoingEmpty())
        sb.append("(e)");
      else
        sb.append("(" + to.nodeId + ")");
      return sb.toString();
    } catch (Exception e) {
      Errors.LOG.error(e.getMessage(), e);
      return "";
    }
  }

  /**
   * Split an arc into two. This can get complex because arcs may be children
   * of a transposed parent or the parent itself or just a plain vanilla arc.
   *
   * @param offset the offset into data pointing to the first byte of the rhs
   * @return an array of split arcs (or maybe just one)
   */
  VariantGraphArc[] split(int offset) throws MVDException {
    VariantGraphArc[] arcs = null;
    // handle simple cases first
    if (offset == 0 || offset == dataLen()) {
      arcs = new VariantGraphArc[1];
      arcs[0] = this;
    } else if (parent == null) {
      if (children == null)
        arcs = splitDataArc(offset);
      else
        arcs = splitParent(offset, null);
    } else
      arcs = splitChild(offset);
    return arcs;
  }

  /**
   * Tell our parent to split, and that will split us
   *
   * @param offset the offset at which to split
   * @return the split child arcs
   */
  private VariantGraphArc[] splitChild(int offset) throws MVDException {
    VariantGraphArc splitParent = parent;
    while (splitParent.parent != null)
      splitParent = parent;
    return splitParent.splitParent(offset, this);
  }

  /**
   * Split us, the parent, and adjust our children
   *
   * @param offset  the offset at which to split
   * @param desired if not null return the split arcs of this
   *                child, not of the parent
   * @return the split parent or child arc
   */
  private VariantGraphArc[] splitParent(int offset, VariantGraphArc desired) throws MVDException {
    VariantGraphArc[] arcs = splitDataArc(offset);
    for (int i = 0; i < children.size(); i++) {
      VariantGraphArc child = children.get(i);
      VariantGraphArc b = new VariantGraphArc(Sets.newHashSet(child.versions), arcs[0]);
      VariantGraphArc c = new VariantGraphArc(Sets.newHashSet(child.versions), arcs[1]);
      VariantGraphNode childFrom = child.from;
      VariantGraphNode childTo = child.to;
      childFrom.removeOutgoing(child);
      childTo.removeIncoming(child);
      childFrom.addOutgoing(b);
      childTo.addIncoming(c);
      VariantGraphNode n = new VariantGraphNode();
      n.addIncoming(b);
      n.addOutgoing(c);
      if (child == desired) {
        arcs = new VariantGraphArc[2];
        arcs[0] = b;
        arcs[1] = c;
      }
    }
    return arcs;
  }

  /**
   * This is called whenever we have to physically split an arc
   * that is not a child and has its own data
   *
   * @param offset point before which to split
   * @return an array of two split arcs
   */
  private VariantGraphArc[] splitDataArc(int offset) throws MVDException {
    VariantGraphArc[] arcs = new VariantGraphArc[2];
    byte[] leftData = new byte[offset];
    for (int i = 0; i < offset; i++) {
      leftData[i] = data[i];
    }
    arcs[0] = new VariantGraphArc(Sets.newHashSet(versions), leftData);
    byte[] rightData = new byte[dataLen() - offset];
    for (int i = offset, j = 0; i < dataLen(); i++, j++) {
      rightData[j] = data[i];
    }
    arcs[1] = new VariantGraphArc(Sets.newHashSet(versions), rightData);
    installSplit(arcs);
    return arcs;
  }

  /**
   * Replace this arc in the graph with two split ones
   *
   * @param arcs two arcs to replace this one
   */
  private void installSplit(VariantGraphArc[] arcs) throws MVDException {
    // now replace the existing arc with the two split ones
    from.replaceOutgoing(this, arcs[0]);
    VariantGraphNode inter = new VariantGraphNode();
    inter.addIncoming(arcs[0]);
    inter.addOutgoing(arcs[1]);
    to.replaceIncoming(this, arcs[1]);
  }

  /**
   * Required for membership tests in hashmaps and treemaps
   */
  public boolean equals(Object other) {
    VariantGraphArc otherArc = (VariantGraphArc) other;
    return versions.equals(otherArc.versions)
            && dataEquals(otherArc) && from == otherArc.from
            && to == otherArc.to;
  }

  /**
   * Is the data of the two arcs equal?
   *
   * @param otherArc the other arc to compare
   * @return true if they two arcs have same data
   */
  private boolean dataEquals(VariantGraphArc otherArc) {
    byte[] data1 = getData();
    byte[] data2 = otherArc.getData();
    if ((data1 == null && data2 != null) || (data1 != null && data2 == null))
      return false;
    else if (data1 == null && data2 == null)
      return true;
    else if (data1.length != data2.length)
      return false;
    else {
      for (int i = 0; i < data1.length; i++)
        if (data1[i] != data2[i])
          return false;
    }
    return true;
  }

  /**
   * Is this arc a hint?
   *
   * @return true if it is a hint
   */
  boolean isHint() {
    return versions.isEmpty();
  }

  /**
   * Convert and Arc to its pair form
   *
   * @param parents map of available parents
   * @param orphans map of available orphans
   * @return the pair
   */
  Match toPair(HashMap<VariantGraphArc, Match> parents, HashMap<VariantGraphArc, Match> orphans)
          throws MVDException {
    if (isHint())
      throw new MVDException("Ooops! hint detected!");
    Match p = new Match(versions, data);
    if (this.parent != null) {
      // we're a child - find our parent
      Match q = parents.get(parent);
      if (q != null) {
        q.addChild(p);
        // if this is the last child of the parent remove it
        if (parent.numChildren() == q.numChildren())
          parents.remove(parent);
      } else    // we're orphaned for now
        orphans.put(this, p);
    } else if (children != null) {
      // we're a parent
      for (int i = 0; i < children.size(); i++) {
        VariantGraphArc child = children.get(i);
        au.edu.uq.nmerge.mvd.Match r = orphans.get(child);
        if (r != null) {
          p.addChild(r);
          orphans.remove(child);
        }
      }
      if (p.numChildren() < this.numChildren())
        parents.put(this, p);
    }
    return p;
  }

  /**
   * Does the arc have no data?
   *
   * @return true if so
   */
  public boolean isEmpty() {
    return data.length == 0;
  }

  /**
   * Is this arc not attached to any node at the end?
   *
   * @return true if this is so
   */
  boolean unattached() {
    return to == null;
  }
  // extra routines for nmerge

  /**
   * Get the from node
   *
   * @return a Node possibly null
   */
  public VariantGraphNode getFrom() {
    return from;
  }

  /**
   * Get the to node
   *
   * @return a Node possibly null
   */
  public VariantGraphNode getTo() {
    return to;
  }

  /**
   * Remove a child from the parent
   *
   * @param child the child arc
   */
  public void removeChild(VariantGraphArc child) {
    assert children.contains(child) :
            "removeChild: child " + child + " not found!";
    children.remove(child);
    if (children.size() == 0)
      children = null;
  }

  /**
   * How many children do we have?
   *
   * @return the number of children in the child list
   */
  int numChildren() {
    return (children == null) ? 0 : children.size();
  }

  /**
   * Is this arc a parent?
   *
   * @return true if it has children
   */
  boolean isParent() {
    return children != null && children.size() > 0;
  }

  /**
   * Is this arc a child?
   *
   * @return true if it has a parent
   */
  boolean isChild() {
    return parent != null;
  }

  /**
   * We are dying. Pass our data on to our children. This
   * happens if we are being deleted.
   */
  public void passOnData() {
    ListIterator<VariantGraphArc> iter = children.listIterator();
    VariantGraphArc newParent = iter.next();
    newParent.data = this.data;
    newParent.setParent(null);
    while (iter.hasNext()) {
      VariantGraphArc a = iter.next();
      newParent.addChild(a);
    }
  }

  /**
   * Set the new parent in case of adoption
   *
   * @param parent the new parent
   */
  void setParent(VariantGraphArc parent) {
    this.parent = parent;
  }

  /**
   * Verify that our internal data is up to snuff
   *
   * @throws MVDException throw an exception if not
   */
  void verify() throws MVDException {
    if (data == null && parent == null)
      throw new MVDException("Arc data is null and shouldn't be");
  }

  /**
   * Do we have a child in the given version?
   *
   * @param version the version to test for
   * @return true if we have children and one of them has the version
   */
  boolean hasChildInVersion(Witness version) {
    if (children != null) {
      ListIterator<VariantGraphArc> iter = children.listIterator(0);
      while (iter.hasNext()) {
        VariantGraphArc child = iter.next();
        if (child.versions.contains(version))
          return true;
      }
      return false;
    } else
      return false;
  }
}