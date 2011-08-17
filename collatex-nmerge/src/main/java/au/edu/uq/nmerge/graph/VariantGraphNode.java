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

import au.edu.uq.nmerge.exception.MVDException;
import au.edu.uq.nmerge.mvd.Witness;
import com.google.common.collect.Sets;

import java.util.*;

import static java.util.Collections.disjoint;

/**
 * A Node is a point of connection for arcs in a variant graph
 *
 * @author Desmond Schmidt 24/10/08
 */
public class VariantGraphNode {
  /**
   * set of incoming arcs, perhaps empty
   */
  private LinkedList<VariantGraphArc> incoming;
  /**
   * set of outgoing arcs, perhaps empty
   */
  private LinkedList<VariantGraphArc> outgoing;
  /**
   * set of incoming versions
   */
  private Set<Witness> incomingSet;
  /**
   * set of outgoing versions
   */
  private Set<Witness> outgoingSet;
  /**
   * used during breadth-first traversal
   */
  Set<Witness> printed;
  /**
   * used during backwards breadth-first traversal
   */
  Set<Witness> printedOutgoing;
  /**
   * the shortest route to the node!
   */
  int shortestPathToNode;
  /**
   * list of Matches
   */
  LinkedList<Match> matches;
  /**
   * new id counter (static)
   */
  static int id = 1;
  /**
   * unique identifier
   */
  int nodeId;

  /**
   * Create a Node
   */
  public VariantGraphNode() {
    incoming = new LinkedList<VariantGraphArc>();
    outgoing = new LinkedList<VariantGraphArc>();
    printed = Sets.newHashSet();
    printedOutgoing = Sets.newHashSet();
    incomingSet = Sets.newHashSet();
    outgoingSet = Sets.newHashSet();
    nodeId = VariantGraphNode.id++;
  }

  /**
   * We are being used as the start point of a match. In case
   * we ever get eliminated by an alignMerge operation our
   * match-relatives will need to be told.
   *
   * @param m a match that relies on us
   */
  public void addMatch(Match m) {
    // lazy evaluation - usually matches is null
    if (matches == null)
      matches = new LinkedList<Match>();
    matches.add(m);
  }

  /**
   * Is the incoming set of arcs empty?
   *
   * @return true if it is
   */
  boolean isIncomingEmpty() {
    return incoming.size() == 0;
  }

  /**
   * Is the outgoing set of arcs empty?
   *
   * @return true if it is
   */
  boolean isOutgoingEmpty() {
    return outgoing.size() == 0;
  }

  /**
   * Add an outgoing Arc
   *
   * @param a the Arc to add
   */
  public void addOutgoing(VariantGraphArc a) throws MVDException {
    if (!disjoint(a.versions, outgoingSet))
      throw new MVDException("There is already an outgoing arc with that version");
    outgoing.add(a);
    outgoingSet.addAll(a.versions);
    printedOutgoing.addAll(a.versions);
    a.setFrom(this);
  }

  /**
   * Does the outgoing set contain a certain arc?
   *
   * @param a the arc to test for
   * @return true if it is present in the outgoing set
   */
  public boolean hasOutgoingArc(VariantGraphArc a) {
    return outgoing.contains(a);
  }

  /**
   * Add an incoming Arc
   *
   * @param a the Arc to add
   */
  public void addIncoming(VariantGraphArc a) throws MVDException {
    if (!disjoint(a.versions, incomingSet))
      throw new MVDException("There is already an incoming arc with that version");
    assert a.from != this;
    incoming.add(a);
    incomingSet.addAll(a.versions);
    a.setTo(this);
    printed.addAll(a.versions);
  }

  /**
   * After printing a node, reset its printed set to the
   * incoming one so we can print it again. Same for outgoing
   * also.
   */
  public void reset() {
    // FIXME: this was BitSet#or(BitSet)? Should'nt it be #and() aka. newHashSet(intersecs)
    printed.addAll(incomingSet);
    printedOutgoing.addAll(outgoingSet);
    shortestPathToNode = 0;
  }

  /**
   * Get the shortest path to the node from either side.
   *
   * @return the shortest path to this node
   */
  public int getShortestPath() {
    return shortestPathToNode;
  }

  /**
   * Set the shortest path
   *
   * @param value the value of the path length
   */
  public void setShortestPath(int value) {
    shortestPathToNode = value;
  }

  /**
   * "Print" an arc, that is remove it from the printed set.
   */
  public void printArc(VariantGraphArc a) {
    printed.removeAll(a.versions);
  }

  /**
   * "Print" an arc, that is remove it from the printed set.
   *
   * @param a             the arc to print
   * @param parentPathLen the shortest path to the from node of a
   */
  public void printArc(VariantGraphArc a, int parentPathLen) {
    printed.removeAll(a.versions);
    if (shortestPathToNode == 0 || parentPathLen < shortestPathToNode)
      shortestPathToNode = parentPathLen;
  }

  /**
   * Is the given incoming arc printed?
   *
   * @param versions the versions of the arc to test
   * @return true if an incoming arc with those versions is printed
   */
  public boolean isPrintedIncoming(Set<Witness> versions) {
    return disjoint(versions, printed);
  }

  /**
   * Is the given outgoing arc printed?
   *
   * @param versions the versions of the arc to test
   * @return true if an outgoing arc with those versions is printed
   */
  public boolean isPrintedOutgoing(Set<Witness> versions) {
    return disjoint(versions, printedOutgoing);
  }

  /**
   * "Print" an arc backwards. Used in left transpose detection.
   * (breadth-first backwards traversal).
   */
  public void printOutgoingArc(VariantGraphArc a) {
    printedOutgoing.removeAll(a.versions);
  }

  /**
   * "Print" an arc, that is remove it from the printed set. Used
   * in left transpose detection.
   *
   * @param a             the arc to print
   * @param parentPathLen the shortest path from the to node of a
   */
  public void printOutgoingArc(VariantGraphArc a, int parentPathLen) {
    printedOutgoing.removeAll(a.versions);
    if (shortestPathToNode == 0 || parentPathLen + a.dataLen() < shortestPathToNode)
      shortestPathToNode = parentPathLen + a.dataLen();
  }

  /**
   * Have all the outgoing arcs been printed (backwards)?
   *
   * @return true if there are none left in the printedOutgoing set
   */
  boolean allPrintedOutgoing() {
    return printedOutgoing.isEmpty();
  }

  /**
   * Have all the incoming arcs been printed?
   *
   * @param range the range within which the incoming arcs are assessed
   * @return true if there are none left in the printed set
   */
  boolean allPrintedIncoming(Set<Witness> range) {
    return disjoint(printed, range);
  }

  /**
   * Have all the incoming arcs been printed?
   *
   * @return true if there are none left in the printed set
   */
  public boolean allPrintedIncoming() {
    return printed.isEmpty();
  }

  /**
   * How many incoming Arcs are there?
   *
   * @return the size of the incoming set
   */
  public int indegree() {
    return incoming.size();
  }

  /**
   * Move any matches in our list to the destination node.
   * Also tell the matches about the change!
   *
   * @param to the to-node
   */
  public void moveMatches(VariantGraphNode to) {
    if (matches != null) {
      Match m = matches.poll();
      while (m != null) {
        m.setStart(to);
        to.addMatch(m);
        m = matches.poll();
      }
    }
  }

  /**
   * Get the number of incoming versions (not arcs)
   *
   * @return the cardinality of the incoming version set
   */
  public int numIncomingVersions() {
    return incomingSet.size();
  }

  /**
   * Get the number of outgoing versions (not arcs)
   *
   * @return the cardinality of the outgoing version set
   */
  public int numOutgoingVersions() {
    return outgoingSet.size();
  }

  /**
   * How many outgoing Arcs are there?
   *
   * @return the size of the outgoing set
   */
  public int outdegree() {
    return outgoing.size();
  }

  /**
   * Get the simple cardinality of this node's versions
   *
   * @return the number of bits set
   */
  public int cardinality() {
    return getVersions().size();
  }

  /**
   * What are the versions handled by this node? Since incoming and
   * outgoing arcs must match, we just return the incoming set. If
   * this is the start node then return the outgoing set.
   */
  public Set<Witness> getVersions() {
    if (incoming.size() > 0)
      return incomingSet;
    else
      return outgoingSet;
  }

  /**
   * Get an iterator over the outgoing arcs of this node
   *
   * @param subgraph provides 2 constraints on the arcs returned:
   *                 if we are the end-node of the subgraph then there are NO
   *                 outgoing arcs. Otherwise only those arcs that may lead to
   *                 the end node, that is those in the constraint set of the
   *                 subgraph, can be followed.
   * @return an Arc iterator
   */
  public ListIterator<VariantGraphArc> outgoingArcs(VariantGraph subgraph) {
    Vector<VariantGraphArc> constrainedArcs = new Vector<VariantGraphArc>();
    if (this != subgraph.end) {
      for (int i = 0; i < outgoing.size(); i++) {
        VariantGraphArc a = outgoing.get(i);
        if (!disjoint(a.versions, subgraph.constraint))
          constrainedArcs.add(a);
      }
    }
    return constrainedArcs.listIterator();
  }

  /**
   * Get an iterator over the outgoing arcs of this node. This is
   * unconstrained and will return all outgoing arcs.
   *
   * @return an Arc iterator
   */
  public ListIterator<VariantGraphArc> outgoingArcs() {
    return outgoing.listIterator();
  }

  /**
   * Get an iterator over the incoming arcs of this node
   *
   * @return an Arc iterator
   */
  public ListIterator<VariantGraphArc> incomingArcs() {
    return incoming.listIterator();
  }

  /**
   * Select the incoming arc corresponding to a version
   *
   * @param version the version for the pick
   * @return an arc or null if not found
   */
  public VariantGraphArc pickIncomingArc(Witness version) {
    ListIterator<VariantGraphArc> iter = incoming.listIterator();
    while (iter.hasNext()) {
      VariantGraphArc a = iter.next();
      if (a.versions.contains(version))
        return a;
    }
    return null;
  }

  /**
   * Select the outgoing arc corresponding to a version
   *
   * @param version the version for the pick
   * @return an arc or null if not found
   */
  public VariantGraphArc pickOutgoingArc(Witness version) {
    ListIterator<VariantGraphArc> iter = outgoing.listIterator();
    while (iter.hasNext()) {
      VariantGraphArc a = iter.next();
      if (a.versions.contains(version))
        return a;
    }
    return null;
  }

  /**
   * Replace outgoing arc a with b
   *
   * @param a the outgoing arc already there
   * @param b the new outgoing arc
   */
  public void replaceOutgoing(VariantGraphArc a, VariantGraphArc b) throws MVDException {
    if (removeOutgoing(a))
      addOutgoing(b);
    else
      throw new MVDException("Failed to remove arc " + a);
  }

  /**
   * Remove an incoming arc
   *
   * @param a the arc to remove
   * @return true if the remove succeeded
   */
  public boolean removeIncoming(VariantGraphArc a) {
    boolean res = incoming.remove(a);
    incomingSet.removeAll(a.versions);
    a.setTo(null);
    return res;
  }

  /**
   * Clear the incoming version set of the given version.
   * Use with extreme caution! This method is used to
   * delete an existing version in the graph, but must be
   * applied consistently.
   *
   * @param version the version to remove
   */
  public void removeIncomingVersion(Witness version) {
    incomingSet.remove(version);
    printed.remove(version);
  }

  /**
   * Clear the outgoing version set of the given version.
   * Use with extreme caution! This method is used to
   * delete an existing version in the graph, but must be
   * applied consistently.
   *
   * @param version the version to remove
   */
  public void removeOutgoingVersion(Witness version) {
    outgoingSet.remove(version);
    printedOutgoing.remove(version);
  }

  /**
   * This is required by the deserialising code. See the IJHCS
   * paper, Algorithm 2. After a hint is added to the node the
   * outgoingSet of course contains the versions of the hint,
   * as well as the versions of the hint itself. BUT the unattached
   * set decrements the hint's versions, and also the outgoing set
   * via this method, then removes it when it is empty.
   *
   * @param set the set of versions to remove
   */
  void removeOutgoingVersions(Set<Witness> set) {
    outgoingSet.removeAll(set);
  }

  /**
   * Remove an incoming arc
   *
   * @param a the arc to remove
   */
  public boolean removeOutgoing(VariantGraphArc a) {
    boolean res = outgoing.remove(a);
    outgoingSet.removeAll(a.versions);
    a.setFrom(null);
    return res;
  }

  /**
   * Remove an incoming arc
   *
   * @param index the index of the arc to remove
   * @return the arc removed
   */
  public VariantGraphArc removeIncoming(int index) {
    VariantGraphArc a = incoming.remove(index);
    incomingSet.removeAll(a.versions);
    a.setTo(null);
    return a;
  }

  /**
   * Add a version to the incoming set (used when merging paths)
   *
   * @param version the version to add
   */
  public void addIncomingVersion(Witness version) {
    incomingSet.add(version);
    printed.add(version);
  }

  /**
   * Add a version to the outgoing set (used when merging paths)
   *
   * @param version the version to add
   */
  public void addOutgoingVersion(Witness version) {
    outgoingSet.add(version);
    printedOutgoing.add(version);
  }

  /**
   * Remove an outgoing arc
   *
   * @param index the index of the arc to remove
   * @return the arc removed
   */
  public VariantGraphArc removeOutgoing(int index) {
    VariantGraphArc a = outgoing.remove(index);
    outgoingSet.removeAll(a.versions);
    a.setFrom(null);
    return a;
  }

  /**
   * Replace incoming arc a with b
   *
   * @param a the incoming arc already there
   * @param b the new incoming arc
   */
  public void replaceIncoming(VariantGraphArc a, VariantGraphArc b) throws MVDException {
    if (removeIncoming(a))
      addIncoming(b);
    else
      throw new MVDException("Failed to remove arc " + a);
  }

  /**
   * Check that a node meets the definition of a variant graph.
   *
   * @throws MVDException if node is invalid
   */
  void verify() throws MVDException {
    Set<Witness> bs1 = null, bs2 = null;
    if (incoming.size() > 0)
      bs1 = checkArcs(incoming.listIterator(), "incoming");
    if (outgoing.size() > 0)
      bs2 = checkArcs(outgoing.listIterator(), "outgoing");
    if (!bs1.equals(bs2))
      throw new MVDException("Incoming and outgoing sets not equal");
  }

  /**
   * Check that all the arcs on one side are mutually exclusive
   *
   * @param iter an enumeration over the arcs
   * @param type a string description of the enumeration type
   * @throws MVDException if they are not mutually exclusive
   */
  private Set<Witness> checkArcs(ListIterator<VariantGraphArc> iter, String type)
          throws MVDException {
    Set<Witness> bs = Sets.newHashSet();
    while (iter.hasNext()) {
      VariantGraphArc a = iter.next();
      for (Witness i : a.versions) {
        if (bs.contains(i)) {
          throw new MVDException("Version " + i + " present twice in " + type + " set");
        }
        bs.add(i);
      }
    }
    return bs;
  }

  /**
   * Get previous characters for all versions without restriction
   *
   * @return the last characters of all preceding arcs
   */
  PrevChar[] getPrevChars() {
    return getPrevChars(incomingSet, null);
  }

  /**
   * Get an array of all possible bytes that immediately precede
   * this node.
   *
   * @param constraint the constraint applied to the subgraph
   *                   - without this we may turn down legitimate matches because
   *                   they are preceded by a matching character in a version
   *                   that is not under consideration
   * @param forbidden  don't recurse backwards beyond this node
   * @return the array of preceding byte objects
   */
  PrevChar[] getPrevChars(Set<Witness> constraint, VariantGraphNode forbidden) {
    Vector<PrevChar> array = new Vector<PrevChar>();
    ListIterator<VariantGraphArc> iter = incomingArcs();
    while (iter.hasNext()) {
      VariantGraphArc a = iter.next();
      if (!disjoint(a.versions, constraint)) {
        if (a.dataLen() > 0) {
          byte[] data = a.getData();
          Set<Witness> prevVersions = Sets.newHashSet();
          prevVersions.addAll(a.versions);
          prevVersions.retainAll(constraint);
          array.add(new PrevChar(prevVersions, data[data.length - 1]));
        } else if (!a.from.equals(forbidden)) {
          PrevChar[] bytes = a.from.getPrevChars(constraint, forbidden);
          for (int i = 0; i < bytes.length; i++)
            array.add(bytes[i]);
        }
      }
    }
    // convert to a PrevChar array
    PrevChar[] prevArray = new PrevChar[array.size()];
    return array.toArray(prevArray);
  }

  /**
   * Get the set of versions covered by the incomingArcs
   *
   * @return a set of arc versions (read only!)
   */
  Set<Witness> getIncomingSet() {
    return incomingSet;
  }

  /**
   * Get the set of versions covered by the outgoingArcs
   *
   * @return a set of arc versions (read only!)
   */
  Set<Witness> getOutgoingSet() {
    return outgoingSet;
  }

  /**
   * Override default implementation. This is used to identify
   * keys in HashSet. Equal nodes should be stored only once.
   */
  public boolean equals(Object other) {
    return nodeId == ((VariantGraphNode) other).nodeId;
  }
  // extra routines for nmerge

  /**
   * Do the incoming and outgoing versions match?
   *
   * @return true if the node is incomplete
   */
  boolean isIncomplete() {
    return !getOverhang().isEmpty();
  }

  /**
   * Select an outgoing arc that has just this set of versions
   *
   * @param versions the set of versions that the arc must intersect with
   * @return the relevant Arc or null if none
   */
  VariantGraphArc pickOutgoingArc(Set<Witness> versions) {
    ListIterator<VariantGraphArc> iter = outgoing.listIterator();
    while (iter.hasNext()) {
      VariantGraphArc a = iter.next();
      if (!disjoint(a.versions, versions))
        return a;
    }
    return null;
  }

  /**
   * Does this node "want" an arc, i.e. do our incoming versions
   * minus our outgoing versions intersect with the versions of a?
   * Note that a node with no incoming versions must be the start node
   * and hence will want any arc that tries to attach itself to it.
   *
   * @param a the arc to test
   * @return true if we want it
   */
  boolean wants(VariantGraphArc a) {
    return (incoming.size() == 0) ? true : !disjoint(getOverhang(), a.versions);
  }

  /**
   * Return incomingVersions-outgoingVersions
   *
   * @return the overhang
   */
  Set<Witness> getOverhang() {
    Set<Witness> overhang = Sets.newHashSet();
    ListIterator<VariantGraphArc> iter1 = incoming.listIterator();
    ListIterator<VariantGraphArc> iter2 = outgoing.listIterator();
    while (iter1.hasNext())
      overhang.addAll(iter1.next().versions);
    while (iter2.hasNext())
      overhang.removeAll(iter2.next().versions);
    return overhang;
  }

  /**
   * Get the set of versions for those incoming arcs of a node
   * that don't overlap with the selected outgoing arc's versions
   *
   * @param selected the chosen outgoing arc of a node
   *                 that intersects with one or more incoming arcs
   * @return a bitset clique of non-overlapping incoming arcs
   */
  Set<Witness> getClique(VariantGraphArc selected) {
    Set<Witness> bs = Sets.newHashSet();
    if (incoming.size() > 0) {
      ListIterator<VariantGraphArc> iter = incoming.listIterator();
      while (iter.hasNext()) {
        VariantGraphArc a = iter.next();
        if (disjoint(a.versions, selected.versions))
          bs.addAll(a.versions);
      }
    }
    // else it's the start node and the clique is empty
    return bs;
  }

  /**
   * If this node has only one incoming and one outgoing arc
   * consider removing it and merging the two arcs. We have
   * to be careful if either arc is a parent or child of a
   * transposition. We'll leave this case alone for now.
   *
   * @param unattached the unattached arcs in the graph being built
   * @throws MVDException
   */
  void optimise(UnattachedSet unattached) throws MVDException {
    if (incoming.size() == 1 && outgoing.size() == 1
            && outgoingSet.equals(incomingSet)) {
      VariantGraphArc a = incoming.get(0);
      VariantGraphArc b = outgoing.get(0);
      if (!a.isParent() && !a.isChild()
              && !b.isParent() && !b.isChild()) {
        byte[] cData = new byte[a.dataLen() + b.dataLen()];
        Set<Witness> bs = Sets.newHashSet();
        bs.addAll(a.versions);
        VariantGraphArc c = new VariantGraphArc(bs, cData);
        a.from.replaceOutgoing(a, c);
        b.to.replaceIncoming(b, c);
        if (unattached.contains(b)) {
          unattached.remove(b);
          unattached.add(c);
        }
      }
    }
  }
}
