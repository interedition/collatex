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
import au.edu.uq.nmerge.mvd.Witness;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Simple representation of a variant graph, or subgraph thereof
 *
 * @author Desmond Schmidt 24/10/08
 */
public class VariantGraph<T> {
  /**
   * the special start and end nodes that define the graph
   */
  VariantGraphNode<T> start, end;
  /**
   * distance of the (sub)graph from the start of the new version
   */
  int position;
  /**
   * subset of versions applicable to this subgraph
   */
  Set<Witness> constraint;
  /**
   * maximum length of this graph
   */
  int maxLen;
  /**
   * total length of all bytes stored in all arcs
   */
  int totalLen;
  static int MIN_OVERLAP_LEN = 10;

  /**
   * Basic constructor
   */
  public VariantGraph() {
    start = new VariantGraphNode<T>();
    end = new VariantGraphNode<T>();
    this.constraint = Sets.newHashSet();
    maxLen = -1;
  }

  /**
   * This constructor makes a sub-graph out of part of a larger graph
   *
   * @param start      the node which will function as start and
   *                   may have incoming arcs that will be ignored
   * @param end        the node which will function as end and
   *                   which may have outgoing arcs that will be ignored
   * @param constraint graph only covers these versions and ignores all others
   * @param position   the position from the start of the new version
   */
  public VariantGraph(VariantGraphNode<T> start, VariantGraphNode<T> end, Set<Witness> constraint, int position) {
    this.start = start;
    this.end = end;
    this.position = position;
    this.constraint = Sets.newHashSet(constraint);
    this.maxLen = maxLength();
  }

  /**
   * Add an unaligned arc to the graph, attached to the start and end only
   *
   * @param data     the data of the single version it will hold
   * @param version  the ID of that version
   * @param position the position of the arc
   * @return the special, unaligned arc
   */
  public VariantGraphSpecialArc<T> addSpecialArc(List<T> data, Witness version, int position)
          throws MVDException {
    VariantGraphSpecialArc<T> a = new VariantGraphSpecialArc<T>(Sets.newHashSet(version), data, position);
    start.addOutgoing(a);
    end.addIncoming(a);
    // ensure this is clear
    this.constraint.remove(version);
    // not part of the constraint set yet
    return a;
  }

  /**
   * Get the data of the specified version
   *
   * @param version the id of the version to read
   * @return the version's data as a byte array
   */
  List<T> getVersion(Witness version) {
    VariantGraphNode<T> temp = start;
    int len = 0;
    while (temp != null && !temp.equals(end)) {
      VariantGraphArc<T> a = temp.pickOutgoingArc(version);
      len += a.dataLen();
      temp = a.to;
    }
    final List<T> versionData = Lists.newArrayListWithExpectedSize(len);
    temp = start;
    while (temp != null && !temp.equals(end)) {
      VariantGraphArc<T> a = temp.pickOutgoingArc(version);
      versionData.addAll(a.getData());
      temp = a.to;
    }
    return versionData;
  }

  /**
   * Find the maximum length of this graph in bytes by following
   * each path and finding the length of the longest one
   *
   * @return the maximum length of the graph
   */
  private int maxLength() {
    HashMap<Witness, Integer> lengths = new HashMap<Witness, Integer>();
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    HashSet<VariantGraphNode<T>> printed = new HashSet<VariantGraphNode<T>>();
    queue.add(start);
    while (!queue.isEmpty()) {
      VariantGraphNode<T> node = queue.poll();
      ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs(this);
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        List<T> data = a.getData();
        // calculate total length
        totalLen += data.size();
        for (Witness i : a.versions) {
          if (lengths.containsKey(i)) {
            Integer value = lengths.get(i);
            lengths.put(i, value.intValue() + data.size());
          } else
            lengths.put(i, data.size());
        }
        a.to.printArc(a);
        printed.add(a.to);
        if (a.to != end && a.to.allPrintedIncoming(constraint)) {
          queue.add(a.to);
          a.to.reset();
        }
      }
    }
    // clear printed
    Iterator<VariantGraphNode<T>> iter2 = printed.iterator();
    while (iter2.hasNext()) {
      VariantGraphNode<T> n = iter2.next();
      n.reset();
    }
    /// Find the maximum length version
    Integer max = new Integer(0);
    Set<Witness> keys = lengths.keySet();
    Iterator<Witness> iter = keys.iterator();
    while (iter.hasNext()) {
      Witness key = iter.next();
      Integer value = lengths.get(key);
      if (value.intValue() > max.intValue())
        max = value;
    }
    return max.intValue();
  }

  /**
   * What is the maximum length of this graph?
   *
   * @return the length
   */
  int length() {
    if (maxLen == -1)
      maxLen = maxLength();
    return maxLen;
  }

  /**
   * Get the total length of all bytes in the graph
   *
   * @return the number of bytes in the graph
   */
  int totalLen() {
    if (maxLen == -1)
      maxLen = maxLength();
    return totalLen;
  }

  /**
   * Add a version to the constraint set, i.e. adopt it. Also turn
   * all special arcs into ordinary arcs.
   *
   * @param version the version to adopt
   */
  public void adopt(Witness version) throws Exception {
    constraint.add(version);
    VariantGraphNode<T> temp = start;
    while (temp != end) {
      VariantGraphArc<T> a = temp.pickOutgoingArc(version);
      assert a != null : "Couldn't find outgoing arc for version " + version;
      if (a instanceof VariantGraphSpecialArc<?>) {
        VariantGraphArc<T> b = new VariantGraphArc<T>(a.versions, a.data);
        temp.replaceOutgoing(a, b);
        a.to.replaceIncoming(a, b);
        temp = b.to;
      } else
        temp = a.to;
      assert temp != null;
    }
  }

  /**
   * Verify a graph by breadth-first traversal, using exceptions.
   */
  public void verify() throws MVDException {
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    HashSet<VariantGraphNode<T>> printed = new HashSet<VariantGraphNode<T>>();
    start.verify();
    queue.add(start);
    while (!queue.isEmpty()) {
      VariantGraphNode<T> node = queue.poll();
      node.verify();
      ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs(this);
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        a.verify();
        a.to.printArc(a);
        printed.add(a.to);
        if (a.to != end && a.to.allPrintedIncoming(constraint)) {
          queue.add(a.to);
        }
      }
    }
    Iterator<VariantGraphNode<T>> iter2 = printed.iterator();
    while (iter2.hasNext()) {
      VariantGraphNode<T> n = iter2.next();
      n.reset();
    }
    end.verify();
  }

  /**
   * Classic override of toString method, but check structure
   * of graph also. Works for subgraphs too.
   *
   * @return a String representation of this Graph
   */
  public String toString() {
    int totalOutdegree = 0;
    int totalIndegree = 0;
    int totalNodes = 0;
    int maxIndegree = 0;
    int maxOutdegree = 0;
    StringBuffer sb = new StringBuffer();
    HashSet<VariantGraphNode<T>> printed = new HashSet<VariantGraphNode<T>>();
    try {
      SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
      queue.add(start);
      while (!queue.isEmpty()) {
        VariantGraphNode<T> node = queue.poll();
        if (node.indegree() > maxIndegree)
          maxIndegree = node.indegree();
        if (node.outdegree() > maxOutdegree)
          maxOutdegree = node.outdegree();
        totalIndegree += node.indegree();
        totalOutdegree += node.outdegree();
        totalNodes++;
        node.verify();
        ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs(this);
        while (iter.hasNext()) {
          VariantGraphArc<T> a = iter.next();
          sb.append(a.toString() + "\n");
          a.to.printArc(a);
          printed.add(a.to);
          if (a.to != end && a.to.allPrintedIncoming(constraint)) {
            queue.add(a.to);
          }
        }
      }
      Iterator<VariantGraphNode<T>> iter2 = printed.iterator();
      while (iter2.hasNext()) {
        VariantGraphNode<T> n = iter2.next();
        n.reset();
      }
    } catch (Exception e) {
      Errors.LOG.error(sb.toString(), e);
    }
    float averageOutdegree = (float) totalOutdegree / (float) totalNodes;
    float averageIndegree = (float) totalIndegree / (float) totalNodes;
    sb.append("\naverageOutdegree=" + averageOutdegree);
    sb.append("\naverageIndegree=" + averageIndegree);
    sb.append("\nmaxOutdegree=" + maxOutdegree);
    sb.append("\nmaxIndegree=" + maxIndegree);
    return sb.toString();
  }

  /**
   * Clear all the printed arcs. Report any that are already
   * printed but shouldn't be.
   */
  void clearPrinted() {
    HashMap<Integer, VariantGraphNode<T>> hash = new HashMap<Integer, VariantGraphNode<T>>(1500);
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    queue.add(start);
    while (!queue.isEmpty()) {
      VariantGraphNode<T> node = queue.poll();
      ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs();
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        if (!hash.containsKey(a.to.nodeId)) {
          queue.add(node);
          node.reset();
          hash.put(a.to.nodeId, a.to);
        }
      }
    }
  }
  // extra routines for nmerge

  /**
   * Get the start node (read only)
   *
   * @return a node
   */
  public VariantGraphNode<T> getStart() {
    return start;
  }

  /**
   * Remove the text of a version from the graph. Don't adjust the
   * versions sets but leave a hole.
   *
   * @param version the version to remove
   */
  public void removeVersion(Witness version) {
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    queue.add(start);
    while (!queue.isEmpty()) {
      VariantGraphNode<T> node = queue.poll();
      node.reset();
      ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs(this);
      VariantGraphArc<T> del = null;
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        a.to.printArc(a);
        if (a.versions.contains(version)) {
          if (a.versions.size() == 1)
            del = a;
          else {
            a.versions.remove(version);
            a.to.removeIncomingVersion(version);
            a.from.removeOutgoingVersion(version);
          }
        }
        if (a.to != end && a.to.allPrintedIncoming()) {
          queue.add(a.to);
        }
      }
      // can't delete arc inside iterator
      // so we do it outside
      if (del != null) {
        del.from.removeOutgoing(del);
        del.to.removeIncoming(del);
        // is it a child arc?
        if (del.parent != null)
          del.parent.removeChild(del);
        else if (del.children != null)
          del.passOnData();
      }
      node.reset();
      try {
        node.verify();
      } catch (Exception e) {
        Errors.LOG.error(e.getMessage(), e);
      }
    }
    // we removed the version, so clear the constraint
    this.constraint.remove(version);
  }
}
