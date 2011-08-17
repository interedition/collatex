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
import au.edu.uq.nmerge.graph.suffixtree.SuffixTree;
import au.edu.uq.nmerge.mvd.Witness;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

import static java.util.Collections.singleton;

/**
 * Represent the Maximal Unique Match between a new version
 * and a Variant Graph
 *
 * @author Desmond Schmidt 29/10/08
 */
public class MaximalUniqueMatch<T> implements Comparable<MaximalUniqueMatch<T>> {
  private static boolean debug;

  //static int totalHashSize;
  //static int numberOfHashes;
  //static int maxHashSize;
  static int PRINTED_HASH_SIZE = 128;
  /**
   * the arc we are the MUM of
   */
  VariantGraphSpecialArc<T> arc;
  /**
   * the immediately opposite graph to align with
   */
  VariantGraph<T> graph;
  /**
   * the left hand part of the arc left over after alignment
   */
  private VariantGraphSpecialArc<T> leftSubArc;
  /**
   * the right hand part of the arc left over after alignment
   */
  private VariantGraphSpecialArc<T> rightSubArc;
  /**
   * special arcs on the left that need reMUMing
   */
  private SimpleQueue<VariantGraphSpecialArc<T>> leftSpecialArcs;
  /**
   * special arcs on the left that need reMUMing
   */
  private SimpleQueue<VariantGraphSpecialArc<T>> rightSpecialArcs;
  /**
   * The left hand subgraph after alignment
   */
  VariantGraph<T> leftSubGraph;
  /**
   * The right hand subgraph after alignment
   */
  VariantGraph<T> rightSubGraph;
  /**
   * The final Match
   */
  VariantGraphMatch<T> match;
  /**
   * the version of the new version
   */
  Witness version;
  /**
   * store candidate MUMs here
   */
  HashMap<VariantGraphMatch<T>, VariantGraphMatch<T>> table;
  /**
   * are we transposed?
   */
  boolean transposed;
  /**
   * transposing on the left?
   */
  boolean transposeLeft;
  /**
   * minimum length of a MUM
   */
  static final int MIN_LEN = 2;
  /**
   * initial length of table
   */
  static final int INITIAL_QUEUE_LEN = 128;
  /**
   * golden ratio - used for threshold
   */
  static final double PHI = 1.61803399d;

  /**
   * Construct a MUM
   *
   * @param arc        the current or final arc not included yet in path
   * @param graph      the graph to direct align to
   * @param transposed if true we are transposed
   */
  MaximalUniqueMatch(VariantGraphSpecialArc<T> arc, VariantGraph<T> graph, boolean transposed) {
    this.arc = arc;
    this.version = Iterables.getFirst(arc.versions, null);
    this.graph = graph;
    this.transposed = transposed;
    table = new HashMap<VariantGraphMatch<T>, VariantGraphMatch<T>>(INITIAL_QUEUE_LEN);
  }

  /**
   * Store in the priority table.
   *
   * @param start         the nearest node after which the match starts
   * @param graphOffset   the start offset from start in bytes
   * @param matchVersions the versions followed throughout the match
   * @param dataOffset    offset within arc where the alignment starts
   * @param length        the overall path length in bytes
   * @param distance      the distance between graph end and the match
   */
  void update(VariantGraphNode<T> start, int graphOffset, Set<Witness> matchVersions,
              int dataOffset, int length, int distance) {
    if (transposed && transposeLeft)
      distance -= length;
    if (!transposed || withinThreshold(distance, dataOffset, length)) {
      // select first version of match
      Set<Witness> bs = Sets.newHashSet();
      bs.addAll(matchVersions);
      // adjust if alignment is direct
      if (!transposed)
        bs.retainAll(graph.constraint);
      Witness v = Preconditions.checkNotNull(Iterables.getFirst(bs, null));
      VariantGraphMatch<T> m = new VariantGraphMatch<T>(start, graphOffset, v, dataOffset, length, arc.data);
      VariantGraphMatch<T> q = table.get(m);
      if (q != null) {
        if (!m.overlaps(q))
          q.freq++;
        // else it is the same data sharing part of the same
        // path, so it is really the same match
      } else
        table.put(m, m);
    }
  }

  /**
   * Get the length of this MUM
   *
   * @return the length of the data
   */
  int length() {
    if (match == null)
      match = getBestMatch();
    return (match == null) ? 0 : match.length;
  }

  /**
   * Get the match. If it is not already set, fetch it from the table.
   *
   * @return the bext match
   */
  public VariantGraphMatch<T> getMatch() {
    if (match == null)
      match = getBestMatch();
    return match;
  }

  /**
   * Retrieve the best match if possible from the table. Lazily
   * evaluated when we need it and then stored (we hope)
   *
   * @return the best match you could find, must be a MUM
   */
  private VariantGraphMatch<T> getBestMatch() {
    Set<VariantGraphMatch<T>> keys = table.keySet();
    Iterator<VariantGraphMatch<T>> iter = keys.iterator();
    VariantGraphMatch<T> biggest = null;
    VariantGraphMatch<T> other = null;
    while (iter.hasNext()) {
      VariantGraphMatch<T> m = iter.next();
      if (m.freq == 1 && (biggest == null || m.length > biggest.length))
        biggest = m;
      else if (m.freq > 1 && (other == null || m.length > other.length))
        other = m;
    }
    //if ( biggest != null && version == 6 )
    //	System.out.println("transposed="+transposed+" biggest="+biggest);
    return biggest;
  }

  /**
   * Get the left-hand portion of the graph not aligned to the arc.
   * Only call after merge. If it was a transposition this is just the
   * original graph.
   *
   * @return the left subgraph or null if none
   */
  public VariantGraph<T> getLeftSubgraph() {
    return leftSubGraph;
  }

  /**
   * Get the right hand bit of the graph left over alignment.
   * Only call after merge.
   *
   * @return the right subgraph or null if none
   */
  public VariantGraph<T> getRightSubgraph() {
    return rightSubGraph;
  }

  /**
   * Get the left-hand portion of the arc before the aligned bit.
   * Only call after merge.
   *
   * @return the left-hand part of the arc or null if none
   */
  public VariantGraphSpecialArc<T> getLeftSubarc() throws Exception {
    return leftSubArc;
  }

  /**
   * Get the right-hand portion of the arc after the aligned bit.
   * Only call after merge.
   *
   * @return an arc, maybe null
   */
  public VariantGraphSpecialArc<T> getRightSubarc() throws Exception {
    return rightSubArc;
  }

  /**
   * Work out if the proffered transpose match is within range.
   * An important method!
   *
   * @param distance   the distance from the end of the graph to
   *                   the edge of the match
   * @param dataOffset offset within data where the match starts
   * @param length     the length of the match that was found
   * @return true if it is within the threshold, false otherwise
   */
  boolean withinThreshold(int distance, int dataOffset, int length) {
    boolean answer = false;
    double pow = Math.pow((double) length, PHI);
    if (transposeLeft)
      answer = pow > (double) (distance + dataOffset);
    else    // transposing on the right
      answer = pow > (double) (distance + arc.dataLen() - (dataOffset + length));
    return answer;
  }

  /**
   * Find a direct MUM by comparing a special arc with its immediately
   * opposite subgraph
   *
   * @param special  the special arc to find the MUM of
   * @param st       the suffix tree made from special
   * @param subGraph the subgraph directly opposite it
   * @return the best MUM or null
   */
  public static <T> MaximalUniqueMatch<T> findDirectMUM(VariantGraphSpecialArc<T> special, SuffixTree<T> st,
                                                 VariantGraph<T> subGraph) throws MVDException {
    MaximalUniqueMatch<T> mum = new MaximalUniqueMatch<T>(special, subGraph, false);
    HashSet<VariantGraphNode<T>> printedNodes = new HashSet<VariantGraphNode<T>>(PRINTED_HASH_SIZE);
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    queue.add(subGraph.start);
    VariantGraphArc<T> lastArc = null;
    printedNodes.add(subGraph.start);
    List<PrevChar<T>> prevChars = Lists.newArrayList();
    if (debug)
      subGraph.verify();
    while (!queue.isEmpty()) {
      VariantGraphNode<T> node = queue.poll();
      ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs(subGraph);
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        if (a.dataLen() > 0 && (!a.isParent()
                || !a.hasChildInVersion(mum.version))) {
          // first character
          MatchThreadDirect<T> mtd;
          List<T> data = a.getData();
          if (a.from != subGraph.start)
            prevChars = a.from.getPrevChars(subGraph.constraint, subGraph.start);
          mtd = new MatchThreadDirect<T>(mum, subGraph, st, a, a.from, 0, prevChars, subGraph.end);
          mtd.run();
          if (data.size() > 1) {
            prevChars = Lists.newArrayListWithExpectedSize(1);
            Set<Witness> prevVersions = Sets.newHashSet();
            prevVersions.addAll(a.versions);
            prevVersions.retainAll(subGraph.constraint);
            // other (1 to N) characters
            for (int i = 1; i < data.size(); i++) {
              prevChars = Lists.newArrayList(Collections.singleton(new PrevChar<T>(prevVersions, data.get(i - 1))));
              mtd = new MatchThreadDirect<T>(mum, subGraph, st, a, a.from, i, prevChars, subGraph.end);
              mtd.run();
            }
          }
        }
        a.to.printArc(a);
        printedNodes.add(a.to);
        if (a.to != subGraph.end && a.to.allPrintedIncoming(
                subGraph.constraint)) {
          queue.add(a.to);
        }
        lastArc = a;
      }
    }
    assert lastArc.to == subGraph.end;
    clearPrintedArcs(printedNodes);
    if (mum.length() > 0)
      return mum;
    else
      return null;
  }

  /**
   * Check that we can't get back to the source node
   *
   * @param list  the list of previously seen nodes
   * @param a     the arc to check for cycles
   * @param limit number of times to recurse before giving up
   */
  static <T> void checkForCycle(SimpleQueue<VariantGraphNode<T>> list, VariantGraphArc<T> a, int limit)
          throws MVDException {
    if (list.contains(a.to))
      throw new MVDException("Cycle: node" + a.to.nodeId + " already encountered");
    else if (limit > 0) {
      ListIterator<VariantGraphArc<T>> iter = a.to.outgoingArcs();
      while (iter.hasNext()) {
        VariantGraphArc<T> b = iter.next();
        //System.out.println(b.toString() );
        list.add(a.to);
        checkForCycle(list, b, limit - 1);
      }
    }
  }

  /**
   * Simple debug routine
   *
   * @param data      the raw data to compare with a string
   * @param i         the index into data to start at
   * @param compareTo the string to compare the bytes of data to
   * @return true if they matched the whole length of compareTo
   */
  static boolean compareBytes(byte[] data, int i, String compareTo) {
    byte[] c = compareTo.getBytes();
    int j = 0;
    while (i < data.length && j < c.length)
      if (data[i++] != c[j++])
        break;
    return j == c.length;
  }

  /**
   * Find the left transpose MUM by comparing a special arc with
   * the graph to the left of the immediately opposite subgraph
   *
   * @param special  the special arc to find the transpose MUM of
   * @param st       the suffix tree made from special
   * @param subGraph the subgraph directly opposite it
   * @return the best left transpose MUM or null
   */
  public static <T> MaximalUniqueMatch<T> findLeftTransposeMUM(VariantGraphSpecialArc<T> special, SuffixTree<T> st,
                                                        VariantGraph<T> subGraph) {
    MaximalUniqueMatch<T> mum = new MaximalUniqueMatch<T>(special, subGraph, true);
    mum.transposeLeft = true;
    // 1. calculate number of bytes to go backwards
    int distance = Math.round((float) Math.pow(special.dataLen(), PHI));
    // 2. subtract distance between start of special to start of subGraph
    VariantGraphArc<T> a = special;
    Witness version = Iterables.getFirst(special.versions, null);
    while (!a.from.equals(subGraph.start)) {
      a = a.from.pickIncomingArc(version);
      distance -= a.dataLen();
    }
    // 3. Go back distance bytes following *all* paths
    findLeftPositions(mum, st, subGraph.start, distance);
    return (mum.getMatch() == null) ? null : mum;
  }

  /**
   * Find positions in the graph to start looking for transpositions.
   * Proceed backwards using breadth first search. Any arc reachable from
   * the start node of the graph is fair game for a transposition. This means
   * that we will gradually expand our range of versions, while marking the
   * arcs that we have traversed via the outgoing print-arc facility of node.
   * Then threads that generate the actual matches know only to traverse
   * these marked arcs. Special arcs are avoided because we only do
   * transpositions between special arcs and the graph.
   *
   * @param mum      the transpose mum to build
   * @param st       the suffix tree of the special arc
   * @param node     node to look backwards from
   * @param distance the distance to search left in bytes
   */
  static <T> void findLeftPositions(MaximalUniqueMatch<T> mum, SuffixTree<T> st,
                                VariantGraphNode<T> node, int distance) {
    HashSet<VariantGraphNode<T>> printedNodes = new HashSet<VariantGraphNode<T>>(PRINTED_HASH_SIZE);
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    int travelled = 0;
    Witness mumV = mum.version;
    VariantGraphNode<T> origin = node;
    //BitSet range = new BitSet();
    queue.add(node);
    node.setShortestPath(0);
    while (!queue.isEmpty()) {
      node = queue.poll();
      // ALL of the incoming arcs are within range
      //range.or( node.getIncomingSet() );
      // the shortest path to get to this node
      int shortestPath = node.getShortestPath();
      ListIterator<VariantGraphArc<T>> iter = node.incomingArcs();
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        if (a.dataLen() > 0 && !a.versions.contains(mumV) && (!a.isParent() || !a.hasChildInVersion(mumV))) {
          MatchThreadTransposeLeft<T> mtt;
          int limit;
          if (a.dataLen() + shortestPath < distance)
            limit = 0;
          else
            limit = (a.dataLen() + shortestPath) - distance;
          // distance travelled in the current arc
          travelled = 0;
          List<PrevChar<T>> prevChars = Lists.newArrayListWithExpectedSize(1);
          for (int i = a.dataLen() - 1; i >= limit; i--) {
            // if all previous arcs are valid, so too
            // are all previous chars of those arcs
            if (i > 0) {
              prevChars = Lists.newArrayList(Collections.singleton(new PrevChar<T>(a.versions, a.getData().get(i - 1))));
            } else
              prevChars = a.from.getPrevChars();
            mtt = new MatchThreadTransposeLeft<T>(mum, st, a, i, prevChars, shortestPath + travelled, origin);
            mtt.run();
            travelled++;
          }
        }
        // finished with this arc: record distance travelled
        a.from.printOutgoingArc(a, shortestPath);
        // keep track of nodes with printed arcs
        printedNodes.add(a.from);
        if (distance - (shortestPath + travelled) > 0
                && a.from.indegree() > 0
                && a.from.allPrintedOutgoing()) {
          queue.add(a.from);
        }
      }
    }
    clearPrintedArcs(printedNodes);
  }

  /**
   * Clear the printed arcs and outgoing printed arcs of any nodes
   * in the set
   *
   * @param printedNodes a set of nodes with some arcs printed.
   */
  static <T> void clearPrintedArcs(HashSet<VariantGraphNode<T>> printedNodes) {
    Iterator<VariantGraphNode<T>> iter2 = printedNodes.iterator();
    while (iter2.hasNext()) {
      VariantGraphNode<T> n = iter2.next();
      n.reset();
    }
    //totalHashSize += printedNodes.size();
    //numberOfHashes++;
    //if ( printedNodes.size() > maxHashSize )
    //	maxHashSize = printedNodes.size();
  }

  /**
   * Find the right transpose MUM by comparing a special arc with the rest
   * of the overall graph to the right of the immediately opposite subgraph.
   *
   * @param special  the special arc to find the transpose MUM of
   * @param st       the suffix tree made from special
   * @param subGraph the subgraph directly opposite it
   * @return the best right transpose MUM or null
   */
  public static <T> MaximalUniqueMatch<T> findRightTransposeMUM(VariantGraphSpecialArc<T> special, SuffixTree<T> st,
                                                         VariantGraph<T> subGraph) {
    MaximalUniqueMatch<T> mum = new MaximalUniqueMatch<T>(special, subGraph, true);
    // 1. calculate number of bytes to go forwards
    int distance = Math.round((float) Math.pow(special.dataLen(), PHI));
    // 2. subtract distance between end of special to end of subGraph
    VariantGraphArc<T> a = special;
    Witness version = Iterables.getFirst(special.versions, null);
    while (!a.to.equals(subGraph.end)) {
      a = a.to.pickOutgoingArc(version);
      distance -= a.dataLen();
    }
    // 3. Go forward distance bytes following *all* paths
    findRightPositions(mum, st, subGraph.end, distance);
    return (mum.getMatch() == null) ? null : mum;
  }

  /**
   * Find positions in the graph to start looking for transpositions
   * on the right of the subgraph. Proceed forwards using breadth-first
   * search.
   *
   * @param mum      the MUM to update
   * @param st       the suffix tree to lookup matches in
   * @param node     the node to start from
   * @param distance the distance to search forwards
   */
  static <T> void findRightPositions(MaximalUniqueMatch<T> mum, SuffixTree<T> st, VariantGraphNode<T> node,
                                 int distance) {
    SimpleQueue<VariantGraphNode<T>> queue = new SimpleQueue<VariantGraphNode<T>>();
    HashSet<VariantGraphNode<T>> printedNodes = new HashSet<VariantGraphNode<T>>(PRINTED_HASH_SIZE);
    VariantGraphNode<T> origin = node;
    Set<Witness> range = Sets.newHashSet();
    int travelled = 0;
    Witness mumV = mum.version;
    printedNodes.add(node);
    queue.add(node);
    node.setShortestPath(0);
    while (!queue.isEmpty()) {
      node = queue.poll();
      // ALL of the incoming arcs are within range
      range.addAll(node.getOutgoingSet());
      // the shortest path to get to this node
      int shortestPath = node.getShortestPath();
      ListIterator<VariantGraphArc<T>> iter = node.outgoingArcs();
      while (iter.hasNext()) {
        VariantGraphArc<T> a = iter.next();
        if (a.dataLen() > 0 && !a.versions.contains(mumV) && (!a.isParent() || !a.hasChildInVersion(mumV))) {
          MatchThreadTransposeRight<T> mttr;
          List<PrevChar<T>> prevChars;
          // number of bytes to travel in this arc
          int limit;
          if (a.dataLen() + shortestPath < distance)
            limit = a.dataLen();
          else
            limit = distance - shortestPath;
          // distance travelled in this arc
          travelled = 0;
          // the 1st time there are NO prevchars
          prevChars = (a.from == origin) ? Lists.<PrevChar<T>>newArrayList() : a.from.getPrevChars();
          // process the first character separately
          // because it needs different prevChars
          mttr = new MatchThreadTransposeRight<T>(mum, st, a, 0, prevChars, shortestPath + travelled, null);
          mttr.run();
          travelled++;
          prevChars = Lists.newArrayList(singleton(new PrevChar<T>(a.versions, a.getData().get(0))));
          for (int i = 1; i < limit; i++) {
            mttr = new MatchThreadTransposeRight<T>(mum, st, a, i, prevChars, shortestPath + travelled, null);
            mttr.run();
            prevChars = Lists.newArrayList(singleton(new PrevChar<T>(a.versions, a.getData().get(i))));
            travelled++;
          }
        }
        // finished with this arc: record distance travelled
        a.to.printArc(a, shortestPath + a.dataLen());
        // keep track of nodes with printed arcs
        printedNodes.add(a.to);
        if (distance - (shortestPath + a.dataLen()) > 0
                && a.to.outdegree() > 0
                && a.to.allPrintedIncoming(range)) {
          queue.add(a.to);
        }
      }
    }
    // important: clean up all printed arcs
    clearPrintedArcs(printedNodes);
  }

  /**
   * Carry out the merge already calculated
   */
  public void merge() throws MVDException {
    if (match == null)
      match = getBestMatch();
    // create left and right subarcs
    if (match.dataOffset > 0)
      leftSubArc = splitOffLeftArc();
    if (match.length + match.dataOffset < arc.dataLen())
      rightSubArc = splitOffRightArc();
    // create left and right subgraphs
    if (transposed) {
      if (match.dataOffset > 0)
        leftSubGraph = graph;
      if (match.length + match.dataOffset < arc.dataLen())
        rightSubGraph = graph;
      transposeMerge();
    } else {
      createLeftSubGraph();
      createRightSubGraph();
      alignMerge();
    }
    // add left and right subarcs to the special left & right sets
    addSubArcsToSpecials();
  }

  /**
   * Do an align type merge.
   */
  private void alignMerge() throws MVDException {
    getSpecialArcs();
    //SetOfVersions before = new SetOfVersions( graph );
    VariantGraphNode<T> arcFrom = normaliseLeftResidualPath();
    VariantGraphNode<T> arcTo = normaliseRightResidualPath();
    // 3. Actually attach the left and right residual paths
    // to their respective subgraphs if required (otherwise OK)
    if (arcFrom != graph.start)
      moveIncomingArcs(arcFrom, leftSubGraph.end);
    if (arcTo != graph.end)
      moveOutgoingArcs(arcTo, rightSubGraph.start);
    // 3. add version to the aligned section
    match.addVersion(version);
    // debug
    /*if ( leftSubGraph != null )
             leftSubGraph.verify();
         if ( rightSubGraph != null )
             rightSubGraph.verify();
         SetOfVersions after = new SetOfVersions( graph );
         assert( after.equals(before) );
         BitSet out = graph.end.getOutgoingSet();
         BitSet in = graph.end.getIncomingSet();
         assert out.isEmpty()||out.equals(in);*/
  }

  /**
   * Add the left and right subarcs to the left and right
   * special sets so they can be MUMified later
   */
  private void addSubArcsToSpecials() {
    if (leftSubArc != null && leftSubArc.dataLen() >= MaximalUniqueMatch.MIN_LEN) {
      if (leftSpecialArcs == null)
        leftSpecialArcs = new SimpleQueue<VariantGraphSpecialArc<T>>();
      leftSpecialArcs.add(leftSubArc);
    }
    if (rightSubArc != null && rightSubArc.dataLen() >= MaximalUniqueMatch.MIN_LEN) {
      if (rightSpecialArcs == null)
        rightSpecialArcs = new SimpleQueue<VariantGraphSpecialArc<T>>();
      rightSpecialArcs.add(rightSubArc);
    }
  }

  /**
   * Get the special arcs on the left of the main special arc
   *
   * @return a list of left special arcs for reMUMing
   */
  public SimpleQueue<VariantGraphSpecialArc<T>> getLeftSpecialArcs() {
    return leftSpecialArcs;
  }

  /**
   * Get the special arcs on the right of the main special arc
   *
   * @return a list of right special arcs for reMUMing
   */
  public SimpleQueue<VariantGraphSpecialArc<T>> getRightSpecialArcs() {
    return rightSpecialArcs;
  }

  /**
   * Return the original special arc in case we have to
   * be recomputed
   *
   * @return the special arc of the new version, unaltered
   */
  public VariantGraphSpecialArc<T> getArc() {
    return arc;
  }

  /**
   * Return the original graph unaltered in case we have to
   * be recomputed
   *
   * @return the original graph we were aligned to
   */
  public VariantGraph<T> getGraph() {
    return graph;
  }

  /**
   * Get the other special arcs on either side of the main special arc
   * (if any - usually none. There will only be some if we carried out
   * a transposition of this set of arcs spanning this graph.
   */
  private void getSpecialArcs() {
    VariantGraphNode<T> leftFrom = arc.from;
    while (leftFrom != graph.start) {
      if (leftSpecialArcs == null)
        leftSpecialArcs = new SimpleQueue<VariantGraphSpecialArc<T>>();
      VariantGraphArc<T> a = leftFrom.pickIncomingArc(version);
      if (a instanceof VariantGraphSpecialArc<?>)
        leftSpecialArcs.add((VariantGraphSpecialArc<T>) a);
      leftFrom = a.from;
    }
    VariantGraphNode<T> rightTo = arc.to;
    while (rightTo != graph.end) {
      if (rightSpecialArcs == null)
        rightSpecialArcs = new SimpleQueue<VariantGraphSpecialArc<T>>();
      VariantGraphArc<T> a = rightTo.pickOutgoingArc(version);
      if (a instanceof VariantGraphSpecialArc<?>)
        rightSpecialArcs.add((VariantGraphSpecialArc<T>) a);
      rightTo = a.to;
    }
  }

  /**
   * Normalise the left-hand side of the special arc. Try to make it so
   * that the left residual arc exists and spans the left subgraph. Only
   * in the case that no left subgraph or left subarc exists will we
   * create nothing here. This is NOT called for transpositions.
   *
   * @return the node at the end of the left residual path, unattached to
   *         anything to the right
   */
  private VariantGraphNode<T> normaliseLeftResidualPath() throws MVDException {
    VariantGraphNode<T> arcFrom = arc.from;
    arc.from.removeOutgoing(arc);
    if (leftSubArc != null) {
      arcFrom.addOutgoing(leftSubArc);
      arcFrom = new VariantGraphNode<T>();
      arcFrom.addIncoming(leftSubArc);
    }
    if (leftSubGraph == null) {
      if (arcFrom != graph.start)
        leftSubGraph = createEmptyLeftSubgraph();
      // else we do nothing
    } else    // leftSubGraph != null
    {
      if (arcFrom == graph.start) {
        VariantGraphArc<T> a = createEmptyArc(version);
        arcFrom.addOutgoing(a);
        arcFrom = new VariantGraphNode<T>();
        arcFrom.addIncoming(a);
      }
      // else we're good
    }
    return arcFrom;
  }

  /**
   * Normalise the right-hand side of the special arc. Try to make it so
   * that the right residual arc exists and spans the right subgraph. Only
   * in the case that no right subgraph or right subarc exists will we
   * create nothing here.
   *
   * @return the node at the start of the right residual path, unattached to
   *         anything to the left
   */
  private VariantGraphNode<T> normaliseRightResidualPath() throws MVDException {
    VariantGraphNode<T> arcTo = arc.to;
    arc.to.removeIncoming(arc);
    if (rightSubArc != null) {
      arcTo.addIncoming(rightSubArc);
      arcTo = new VariantGraphNode<T>();
      arcTo.addOutgoing(rightSubArc);
    }
    if (rightSubGraph == null) {
      if (arcTo != graph.end)
        rightSubGraph = createEmptyRightSubgraph();
      // else nothing to do
    } else    // rightSubGraph != null
    {
      if (arcTo == graph.end) {
        VariantGraphArc<T> a = createEmptyArc(version);
        arcTo.addIncoming(a);
        arcTo = new VariantGraphNode<T>();
        arcTo.addOutgoing(a);
      }
      // else there's already a residual path
    }
    return arcTo;
  }

  /**
   * We don't have a leftSubGraph but because there is a leftSubArc
   * or residual left path we must create an empty subgraph to span it.
   *
   * @return an empty subgraph
   */
  private VariantGraph<T> createEmptyLeftSubgraph() throws MVDException {
    VariantGraphNode<T> n = new VariantGraphNode<T>();
    // create an empty arc to join n to graph.start
    Set<Witness> bs = Sets.newHashSet(graph.start.getVersions());
    bs.remove(version);
    assert !bs.isEmpty();
    VariantGraphArc<T> a = graph.start.pickOutgoingArc(version);
    assert (a != null && a.versions.size() == 1);
    // temporary remove
    graph.start.removeOutgoing(a);
    moveOutgoingArcs(graph.start, n);
    // now put it back
    graph.start.addOutgoing(a);
    // create an empty bridge arc
    VariantGraphArc<T> b = new VariantGraphArc<T>(bs, Lists.<T>newArrayList());
    graph.start.addOutgoing(b);
    n.addIncoming(b);
    VariantGraph<T> g = new VariantGraph<T>(graph.start, n, b.versions, graph.position);
    // debug
    //g.verify();
    return g;
  }

  /**
   * We don't have a rightSubGraph but because there is a rightSubArc or
   * residual right path we must create an empty sub graph to span it.
   *
   * @return an empty subgraph
   */
  private VariantGraph<T> createEmptyRightSubgraph() throws MVDException {
    VariantGraphNode<T> n = new VariantGraphNode<T>();
    // create an empty arc to join graph.end to n
    Set<Witness> bs = Sets.newHashSet(graph.end.getVersions());
    bs.remove(version);
    assert !bs.isEmpty();
    VariantGraphArc<T> a = graph.end.pickIncomingArc(version);
    assert (a != null && a.versions.size() == 1);
    // temporary removal
    graph.end.removeIncoming(a);
    moveIncomingArcs(graph.end, n);
    // now put it back
    graph.end.addIncoming(a);
    // create an empty bridge arc
    VariantGraphArc<T> b = new VariantGraphArc<T>(bs, Lists.<T>newArrayList());
    graph.end.addIncoming(b);
    n.addOutgoing(b);
    VariantGraph<T> g = new VariantGraph<T>(n, graph.end, b.versions, arc.position + match.dataOffset + match.length);
    // debug
    //g.verify();
    return g;
  }

  /**
   * Move the outgoing arcs of a node to another node.
   *
   * @param from the node from which to remove outgoing arcs
   * @param to   the node to which to move the outgoing arcs
   */
  private void moveOutgoingArcs(VariantGraphNode<T> from, VariantGraphNode<T> to) throws MVDException {
    while (!from.isOutgoingEmpty()) {
      VariantGraphArc<T> a = from.removeOutgoing(0);
      to.addOutgoing(a);
    }
    // check if node is now isolated
    if (from.indegree() == 0 && from.outdegree() == 0)
      from.moveMatches(to);
  }

  /**
   * Move the incoming arcs of a node to another node.
   *
   * @param from the node from which to remove incoming arcs
   * @param to   the node to which to move the incoming arcs
   */
  private void moveIncomingArcs(VariantGraphNode<T> from, VariantGraphNode<T> to) throws MVDException {
    while (!from.isIncomingEmpty()) {
      VariantGraphArc<T> a = from.removeIncoming(0);
      to.addIncoming(a);
    }
    // check if node is now isolated
    if (from.indegree() == 0 && from.outdegree() == 0)
      from.moveMatches(to);
  }

  /**
   * Merge a transposed MUM. In this case we merge the special arc
   * with a subgraph spanned by some other special arc or arcs. On
   * entry leftSubArc and rightSubArc are set, but may be null.
   * They are not attached to anything.
   */
  private void transposeMerge() throws MVDException {
    // attach leftSubArc if any
    VariantGraphNode<T> arcFrom = arc.from;
    //System.out.println(arc.toString());
    arc.from.removeOutgoing(arc);
    if (leftSubArc != null) {
      arcFrom.addOutgoing(leftSubArc);
      arcFrom = new VariantGraphNode<T>();
      arcFrom.addIncoming(leftSubArc);
    }
    // attach rightSubArc if any
    VariantGraphNode<T> arcTo = arc.to;
    arc.to.removeIncoming(arc);
    if (rightSubArc != null) {
      arcTo.addIncoming(rightSubArc);
      arcTo = new VariantGraphNode<T>();
      arcTo.addOutgoing(rightSubArc);
    }
    // now for the bit in the middle
    VariantGraphArc<T>[] parents = match.getMatchPath();
    for (int i = 0; i < parents.length; i++) {
      Set<Witness> versions = Sets.newHashSet(version);
      VariantGraphArc<T> child = new VariantGraphArc<T>(versions, parents[i]);
      arcFrom.addOutgoing(child);
      if (parents[i].versions.contains(version))
        Errors.LOG.error("Ooops!", new Exception());
      if (i < parents.length - 1) {
        arcFrom = new VariantGraphNode<T>();
        arcFrom.addIncoming(child);
      } else
        arcTo.addIncoming(child);
    }
    //graph.verify();
  }

  /**
   * Create an empty arc for attachment somewhere
   *
   * @param version the version of the empty arc
   * @return the empty unattached arc
   */
  private VariantGraphArc<T> createEmptyArc(Witness version) {
    return new VariantGraphArc<T>(Sets.newHashSet(version), Lists.<T>newArrayList());
  }

  /**
   * The special arc needs to be split on the left
   *
   * @return an unaligned fragment of the original special arc
   *         not attached to any node
   */
  VariantGraphSpecialArc<T> splitOffLeftArc() {
    assert arc.parent == null && arc.children == null;
    final List<T> leftArcData = Lists.newArrayList(arc.getData().subList(0, match.dataOffset));
    return new VariantGraphSpecialArc<T>(Sets.newHashSet(version), leftArcData, arc.position);
  }

  /**
   * The special arc needs to be split on the right
   *
   * @return an unaligned fragment of the original special arc
   *         not attached to any node
   */
  VariantGraphSpecialArc<T> splitOffRightArc() {
    final List<T> arcData = arc.getData();
    List<T> rightArcData = Lists.newArrayList(arcData.subList(match.dataOffset + match.length, arcData.size()));
    return new VariantGraphSpecialArc<T>(Sets.newHashSet(version), rightArcData, arc.position + match.dataOffset + match.length);
  }

  /**
   * Create the right subgraph. This should only be called when
   * doing a direct alignment.
   *
   * @return the new right subgraph which may be null
   */
  private void createRightSubGraph() throws MVDException {
    VariantGraphNode<T> right = match.getRightNode();
    if (right == graph.end)
      rightSubGraph = null;
    else {
      rightSubGraph = new VariantGraph<T>(right, graph.end, getConstraint(right, graph.end), arc.position + match.dataOffset + match.length);
      // debug
      //rightSubGraph.verify();
    }
  }

  /**
   * Create the left subgraph, whether or not we are doing a
   * transposition, it doesn't matter.
   */
  void createLeftSubGraph() throws MVDException {
    VariantGraphNode<T> left = match.getLeftNode();
    if (left == graph.start)
      leftSubGraph = null;
    else {
      leftSubGraph = new VariantGraph<T>(graph.start, left, getConstraint(graph.start, left), arc.position);
      // debug
      //leftSubGraph.verify();
    }
  }

  /**
   * Calculate the constraint on moving infallibly between
   * two nodes in the graph
   *
   * @param start the first node of the new subgraph
   * @param end   the last node of the new subgraph
   * @return a set of versions shared by start and end
   */
  Set<Witness> getConstraint(VariantGraphNode<T> start, VariantGraphNode<T> end) {
    return Sets.newHashSet(Sets.intersection(start.getVersions(), end.getVersions()));
  }

  /**
   * Is this MUM a transposition?
   *
   * @return true if we are transposed.
   */
  public boolean isTransposition() {
    return transposed;
  }

  /**
   * This is used in TreeMap to order the special arc keys. Since we want them
   * sorted by decreasing length we return 1 if we are LESS than the other.
   *
   * @param other the other MUM to compare this to
   * @return 0 if equal in length, -1 if we are less than other, 1 if greater
   */
  public int compareTo(MaximalUniqueMatch<T> other) {
    if (other == null)
      return 0;    // what else can we do??
    else if (match.length < other.match.length)
      return 1;
    else if (match.length > other.match.length)
      return -1;
      // prefer direct alignments to transpositions
    else if (transposed && !other.transposed)
      return 1;
    else if (!transposed && other.transposed)
      return -1;
    else
      return 0;
  }

  /**
   * Verify that the MUM can be merged
   *
   * @return if it's a direct alignment, true if all transpositions in the
   *         path don't contain a child with the MUM's version in it. If
   *         a transposition return true if no part of the path is direct
   *         aligned with the MUM's version. Otherwise return false.
   */
  public boolean verify() {
    if (match != null)
      return match.checkPath(version);
    else
      return false;
    /*assert canReachBackwards(arc.from,graph.start,version);
         assert canReachForwards( arc.to,graph.end,version );
         assert arc.versions.nextSetBit(version)==version;
         assert arc.dataLen() >= MIN_LEN;
         // check that match is what it is supposed to be
         if ( !transposed )
         {
             // check that match.start is within the subgraph
             Node temp = graph.start;
             while ( temp != graph.end )
             {
                 if ( temp == match.start )
                     break;
                 else
                 {
                     // this is sometimes not a correct verification
                     // because the match can be in a different version
                     // from that of the subgraph's constraint
                     Arc a = temp.pickOutgoingArc(match.version);
                     assert a != null;
                     temp = a.to;
                 }
             }
             assert temp.outdegree()>0&&temp!=graph.end;
             // check that the match is what it says
             // and doesn't spill over the end
             match.verify( graph.end );
             // check that the match data is in the arc
             byte[] arcData = arc.getData();
             for ( int j=match.dataOffset,i=0;i<match.length;i++,j++ )
                 assert match.data[i] == arcData[j];
         }*/
  }

  /**
   * Is there a path from one node back to another node by
   * following a particular version?
   *
   * @param from    the node from which to search backwards
   * @param to      the node to which to reach backwards
   * @param version the version path to follow
   * @return true if we can reach it
   */
  @SuppressWarnings("unused")
  private boolean canReachBackwards(VariantGraphNode<T> from, VariantGraphNode<T> to, Witness version) {
    while (from != null && from != to) {
      VariantGraphArc<T> a = from.pickIncomingArc(version);
      if (a.versions.size() != 1)
        return false;
      if (from != null)
        from = a.from;
    }
    return from == to;
  }

  /**
   * Is there a path from one node forwards to another node by
   * following a particular version?
   *
   * @param from    the node from which to search forwards
   * @param to      the node to which to reach forwards
   * @param version the version path to follow
   * @return true if we can reach it
   */
  @SuppressWarnings("unused")
  private boolean canReachForwards(VariantGraphNode<T> from, VariantGraphNode<T> to, Witness version) {
    while (from != null && from != to) {
      VariantGraphArc<T> a = from.pickOutgoingArc(version);
      if (a.versions.size() != 1)
        return false;
      if (from != null)
        from = a.to;
    }
    return from == to;
  }
}
