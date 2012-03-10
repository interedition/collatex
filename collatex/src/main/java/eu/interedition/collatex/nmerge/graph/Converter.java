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
package eu.interedition.collatex.nmerge.graph;

import eu.interedition.collatex.Witness;
import eu.interedition.collatex.nmerge.exception.MVDException;
import eu.interedition.collatex.nmerge.mvd.Match;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

import static java.util.Collections.disjoint;

/**
 * The purpose of this class is to serialise and deserialise
 * an explicit variant graph to and from its pairs list
 * representation. The main methods are create and serialise.
 *
 * @author Desmond Schmidt 12/7/08
 */
public class Converter<T> {
  /**
   * debug
   */
  static int numParents;
  /**
   * incomplete nodes during build
   */
  HashSet<VariantGraphNode<T>> incomplete;
  /**
   * unattached arcs during build
   */
  UnattachedSet<T> unattached;
  /**
   * used to optimise the size of the pairs list vector
   */
  int origSize;
  /**
   * all versions contained in the graph
   */
  Set<Witness> allVersions;
  /**
   * current number of arcs
   */
  int nArcs;
  /**
   * current number of nodes
   */
  int nNodes;
  /**
   * the graph we are building
   */
  VariantGraph<T> graph;
  /**
   * map to help find parents for children
   */
  HashMap<VariantGraphArc<T>, Match<T>> parents;
  /**
   * map to help find children for parents
   */
  HashMap<VariantGraphArc<T>, Match<T>> orphans;

  /**
   * Create a Graph
   *
   * @param matches  the list of pairs to build the graph from.
   * @param versions the versions to go in the graph
   */
  public VariantGraph<T> create(List<Match<T>> matches, Set<Witness> versions)
          throws Exception {
    unattached = new UnattachedSet<T>();
    incomplete = new HashSet<VariantGraphNode<T>>();
    allVersions = Sets.newHashSet(versions);
    graph = new VariantGraph<T>();
    origSize = matches.size();
    if (matches.size() > 0) {
      deserialise(matches);
    }
    // generate the constraint set
    graph.constraint.addAll(versions);
    parents = new HashMap<VariantGraphArc<T>, Match<T>>();
    orphans = new HashMap<VariantGraphArc<T>, Match<T>>();
    return graph;
  }

  /**
   * Create a Node.
   *
   * @return the new Node
   */
  private VariantGraphNode<T> createNode() {
    nNodes++;
    return new VariantGraphNode<T>();
  }

  /**
   * Parse the data to build the graph from the pairs
   *
   * @param matches a Vector containing the pairs to build
   *                into a graph
   */
  private void deserialise(List<Match<T>> matches) throws Exception {
    graph.start = createNode();
    VariantGraphNode<T> u = graph.start;
    HashMap<Match<T>, VariantGraphArc<T>> pnts = new HashMap<Match<T>, VariantGraphArc<T>>();
    HashMap<Match<T>, VariantGraphArc<T>> kids = new HashMap<Match<T>, VariantGraphArc<T>>();
    // go through the pairs and turn them into arcs
    for (int i = 0; i < matches.size(); i++) {
      VariantGraphNode<T> v;
      Match<T> p = matches.get(i);
      VariantGraphArc<T> a = pairToArc(p, pnts, kids);
      if ((i > 0 && !disjoint(a.versions, matches.get(i - 1).witnesses)) || a.isHint()) {
        u = v = createNode();
      } else {
        v = getIntersectingNode(u, a);
      }
      v.addOutgoing(a);
      Set<Witness> bs = a.versions;
      // in case a is itself a hint
      // don't attach incoming arcs that are hints
      if (a.isHint()) {
        bs = cloneVersions(a.versions);
        // FIXME: is a cleared 0-bit used as an indicator for a hint?
        // bs.clear(0);
      }
      unattached.addAsIncoming(v, bs);
      unattached.add(a);
      // update incomplete set
      boolean x = v.isIncomplete();
      boolean y = incomplete.contains(v);
      if (x && !y) {
        incomplete.add(v);
      } else if (!x && y) {
        incomplete.remove(v);
        v.optimise(unattached);
      }
    }
    // attach dangling arcs to end node
    graph.end = createNode();
    unattached.addAllAsIncoming(graph.end);
    // check that all children found parents and vice versa
    if (pnts.size() != 0) {
      throw new MVDException(
              "Unmatched parent node(s) after deserialisation");
    }
    if (kids.size() != 0) {
      throw new MVDException(
              "Unmatched child node(s) after deserialisation");
    }
  }

  /**
   * Regenerate the list of pairs by writing out the Graph
   *
   * @return a list of pairs with hints where needed
   */
  public Vector<Match<T>> serialise() throws MVDException {
    Match.pairId = 1;
    if (origSize < 15) {
      origSize = 15;
    }
    numParents = 0;
    Vector<Match<T>> matches = new Vector<Match<T>>(origSize);
    printAcross(matches, graph.start, allVersions);
    if (parents.size() != 0) {
      throw new MVDException("Mismatched parent arc");
    }
    if (orphans.size() != 0) {
      throw new MVDException("Mismatched child arc");
    }
    return matches;
  }

  /**
   * Find the node that should receive an outgoing arc
   *
   * @param u the current node
   * @param a the arc for which the from node is needed
   */
  private VariantGraphNode<T> getIntersectingNode(VariantGraphNode<T> u, VariantGraphArc<T> a) throws Exception {
    VariantGraphNode<T> v;
    VariantGraphArc<T> b = unattached.getIntersectingArc(a);
    if (b != null) {
      if (b.isHint()) {
        v = b.from;
        if (unattached.removeEmptyArc(b, a.versions)) {
          nArcs--;
        }
      } else {
        v = u;
      }
    } else {
      v = u;
      Iterator<VariantGraphNode<T>> iter = incomplete.iterator();
      while (iter.hasNext()) {
        v = iter.next();
        if (v.wants(a)) {
          break;
        }
      }
    }
    return v;
  }

  /**
   * Convert a Pair to an Arc. The main problem here is keeping
   * track of parents and children involved in transpositions.
   * This will only work if an entire pairs list is deserialised
   * into a graph in one go.
   *
   * @param p        the pair to convert
   * @param pnts     potential parents of the new pair
   * @param children children looking for parents
   * @return an equivalent Arc we can use in the Graph
   */
  private VariantGraphArc<T> pairToArc(Match<T> p, HashMap<Match<T>, VariantGraphArc<T>> pnts,
                                       HashMap<Match<T>, VariantGraphArc<T>> children) {
    nArcs++;
    List<T> pData = (p.isChild() || p.isHint()) ? null : p.getTokens();
    VariantGraphArc<T> a = new VariantGraphArc<T>(cloneVersions(p.witnesses), pData);
    if (p.isChild()) {
      // we're a child - find our parent
      Match<T> parent = p.getParent();
      VariantGraphArc<T> b = pnts.get(parent);
      if (b != null) {
        b.addChild(a);
        // if this is the last child of the parent remove it
        if (b.numChildren() == parent.numChildren()) {
          pnts.remove(parent);
        }
      } else    // we're orphaned for now
      {
        children.put(p, a);
      }
    } else if (p.isParent()) {
      for (Match<T> child : p.getChildren()) {
        VariantGraphArc<T> r = children.get(child);
        if (r != null) {
          a.addChild(r);
          children.remove(child);
        }
      }
      if (p.numChildren() > a.numChildren()) {
        pnts.put(p, a);
      }
    }
    return a;
  }

  /**
   * The clone method is not infallible for bitsets
   *
   * @param versions the bitset to clone
   * @return a cloned bitset
   * @deprecated Still needed?
   */
  private Set<Witness> cloneVersions(Set<Witness> versions) {
    return Sets.newHashSet(versions);
  }

  /**
   * Build a bit of the pairs-list starting from a node
   *
   * @param matches  the part-built pairs-list
   * @param u        the node from which to take outgoing arcs
   * @param incoming the versions of the last incoming arc
   */
  private void printAcross(Vector<Match<T>> matches, VariantGraphNode<T> u, Set<Witness> incoming)
          throws MVDException {
    int hint = -1;
    VariantGraphArc<T> selected = u.pickOutgoingArc(incoming);
    if (selected != null) {
      assert !selected.to.isPrintedIncoming(selected.versions);
      // add an empty tuple as a hint if required
      Set<Witness> clique = u.getClique(selected);
      if (!clique.isEmpty()) {
        hint = matches.size();
        //System.out.println("creating hint at "+hint);
        // FIXME: again! 0-bit as indicator of hint!!
        //clique.set(0);
        // create a hint
        Match<T> h = new Match<T>(clique, Lists.<T>newArrayList());
        matches.add(h);
      }
      hint = printDown(matches, selected, hint);
      ListIterator<VariantGraphArc<T>> iter = u.outgoingArcs();
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        if (a != selected) {
          hint = printDown(matches, a, hint);
        }
      }
    }
  }

  /**
   * Print a single tuple to the list. If this is the last incoming
   * arc, then call printAcross.
   *
   * @param matches the part-built tuple-list
   * @param a       the arc to print
   * @param hint    the previous location of a hint
   * @return the hint or -1 if it was removed
   */
  private int printDown(Vector<Match<T>> matches, VariantGraphArc<T> a, int hint)
          throws MVDException {
    if (a.numChildren() > 0) {
      numParents++;
    }
    Match<T> p = a.toPair(parents, orphans);
    matches.add(p);
    a.to.printArc(a);
    if (a.to != null) {
      VariantGraphNode<T> u = a.to;
      if (u.allPrintedIncoming()) {
        if (hint != -1) {
          hint = reduceHint(matches, hint);
        }
        printAcross(matches, u, a.versions);
        // next node produced, invalidating hint
        hint = -1;
      }
    }
    return hint;
  }

  /**
   * Reduce or remove the hint at offset hint and if removing
   * shunt all the tuples along from hint to pos. Otherwise
   * just reduce the number of versions at offset hint.
   *
   * @param matches the array of tuples to write to
   * @param hint    the offset of the hint
   * @return the index of the hint or -1 if it was removed
   */
  private int reduceHint(Vector<Match<T>> matches, int hint) {
    Match<T> hintMatch = matches.get(hint);
    for (int i = hint + 2; i < matches.size(); i++) {
      Match<T> p = matches.get(i);
      hintMatch.witnesses.removeAll(p.witnesses);
      if (hintMatch.witnesses.isEmpty()) {
        matches.remove(hint);
        //System.out.println("removing hint at "+hint);
        hint = -1;
        break;
      }
    }
    /*if ( hint != -1 )
             System.out.println("hint at "+hint+" versions="
                 +hintPair.versions.toString());*/
    return hint;
  }

  /**
   * Is this graph isomorphic to another? We can do this by printing
   * them both simultaneously breadth-first and noting down the same
   * moves. Any mismatch results in failure.
   *
   * @param other the other Textgraph to compare with this one
   * @return true if they are isomorphic, false otherwise
   */
  public boolean isIsomorphic(VariantGraph<T> other) {
    VariantGraphNode<T> current;
    VariantGraphNode<T> otherCurrent;
    NodeQueue<T> q = new NodeQueue<T>();
    NodeQueue<T> otherQueue = new NodeQueue<T>();
    q.push(graph.start);
    otherQueue.push(other.start);
    current = graph.start;
    int numArcs = 0;
    otherCurrent = other.start;
    while (!q.isEmpty() && !otherQueue.isEmpty()) {
      current = q.pop();
      otherCurrent = otherQueue.pop();
      ListIterator<VariantGraphArc<T>> iter = current.outgoingArcs();
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        VariantGraphArc<T> b = otherCurrent.pickOutgoingArc(a.versions);
        numArcs++;
        if (a == null) {
          graph.clearPrinted();
          other.clearPrinted();
          return false;
        }
        if (b == null) {
          graph.clearPrinted();
          other.clearPrinted();
          return false;
        } else {
          a.to.printArc(a);
          b.to.printArc(b);
        }
        if (a.to.allPrintedIncoming()) {
          q.push(a.to);
          if (b.to.allPrintedIncoming()) {
            otherQueue.push(b.to);
          } else {
            graph.clearPrinted();
            other.clearPrinted();
            return false;
          }
        }
      }
    }
    graph.clearPrinted();
    other.clearPrinted();
    return true;
  }
}
