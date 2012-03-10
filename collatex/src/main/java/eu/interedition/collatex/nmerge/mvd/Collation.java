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
package eu.interedition.collatex.nmerge.mvd;

import eu.interedition.collatex.nmerge.Errors;
import eu.interedition.collatex.nmerge.exception.MVDException;
import eu.interedition.collatex.nmerge.graph.*;
import eu.interedition.collatex.suffixtree.SuffixTree;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.util.*;

import static java.util.Collections.disjoint;

/**
 * Represent a multi-version document.
 *
 * @author Desmond Schmidt &copy; 2009
 */
public class Collation<T> {

  // new options
  private final boolean directAlignOnly = false;
  private final String description;

  private final Set<Witness> witnesses = Sets.newHashSet();
  private final Ordering<T> tokenOrdering;
  private final T nullToken;

  private List<Match<T>> matches = Lists.newArrayList();

  public Collation(String description, Ordering<T> tokenOrdering, T nullToken) {
    this.description = description;
    this.tokenOrdering = tokenOrdering;
    this.nullToken = nullToken;
  }

  /**
   * Get the description defined for this MVD
   *
   * @return the description as a String
   */
  public String getDescription() {
    return description;
  }

  public Set<Witness> getWitnesses() {
    return witnesses;
  }

  /**
   * Get the pairs list for converting to a Graph
   *
   * @return the pairs - read only!
   */
  public List<Match<T>> getMatches() {
    return matches;
  }

  /**
   * Compare two versions u and v. If it is in u but not in v
   * then turn that pair and any subsequent pairs with the
   * same characteristic into a match. We also generate merged
   * Matches for the gaps between the state matches (added or
   * deleted). This way we can link up the merged matches in
   * the GUI.
   *
   * @param u     the first version to compare
   * @param v     the second version to compare
   * @param state the state of text belonging only to u
   * @return an array of chunks for special display
   */
  public List<Chunk<T>> compare(Witness u, Witness v, ChunkState state)
          throws MVDException {
    List<Chunk<T>> result = Lists.newArrayList();

    Chunk<T> currentChunk = new Chunk<T>();
    currentChunk.setWitness(u);

    TransposeState oldTransposeState = null;
    ChunkStateSet oldChunkStateSet = null;

    TransposeState transposeState = new TransposeState();
    ChunkStateSet chunkStateSet = new ChunkStateSet();

    int chunkId = 0;
    TransposeState.transposeId = Integer.MAX_VALUE;

    for (Match<T> match : Iterables.filter(matches, new Match.WitnessPredicate(u))) {
      oldTransposeState = transposeState;
      oldChunkStateSet = chunkStateSet;

      transposeState = transposeState.next(match, u, v);
      if (!transposeState.isTransposed()) {
      // not transposed means deleted, inserted or merged
        chunkStateSet = chunkStateSet.next(match, state, v);
      }

      if (!transposeState.equals(oldTransposeState) || !chunkStateSet.equals(oldChunkStateSet)) {
        // then we have to write out currentChunk
        ChunkStateSet currentChunkStates = currentChunk.getStates();
        if (currentChunk.getLength() > 0) {
          if (currentChunkStates.isMerged()) {
            currentChunk.setId(++chunkId);
          }
          result.add(currentChunk);
        }
        // set up a new currentChunk chunk
        ChunkState[] newStates;
        if (transposeState.getId() != 0) {
          newStates = new ChunkState[1];
          newStates[0] = transposeState.getState();
        } else {
          newStates = chunkStateSet.getStates();
        }
        currentChunk = new Chunk<T>(transposeState.getId(), newStates, match.getTokens());
        currentChunk.setWitness(u);
      } else {
        currentChunk.add(match.getTokens());
      }
    }
    // add any lingering result
    if (currentChunk.getStates().isMerged()) {
      currentChunk.setId(++chunkId);
    }
    if (result.size() == 0 || !currentChunk.equals(result.get(result.size() - 1))) {
      result.add(currentChunk);
    }
    return result;
  }

  /**
   * Get the index of the next pair intersecting with a version
   *
   * @param pairIndex the index to start looking from
   * @param u         the version to look for
   * @return the index of the next pair or Integer.MAX_VALUE if not found
   */
  int next(int pairIndex, Witness u) {
    int i = pairIndex;
    while (i < matches.size()) {
      Match<T> p = matches.get(i);
      if (p.contains(u)) {
        return i;
      } else {
        i++;
      }
    }
    return Integer.MAX_VALUE;
  }

  /**
   * Get the index of the previous pair intersecting with a
   * version
   *
   * @param pairIndex the index to start looking from
   * @param u         the version to look for
   * @return the index of the previous pair or -1 if not found
   */
  int previous(int pairIndex, Witness u) {
    int i = pairIndex - 1;
    while (i > 0) {
      Match<T> p = matches.get(i);
      if (p.contains(u)) {
        return i;
      } else {
        i--;
      }
    }
    return -1;
  }

  /**
   * Search for a pattern. Return multiple matches if requested
   * as an array of Match objects
   *
   * @param pattern  the pattern to search for
   * @param bs       the set of versions to search through
   * @param multiple if true return all hits; otherwise only the first
   * @return an array of matches
   */
  public List<Hit<T>> search(List<T> pattern, Set<Witness> bs, boolean multiple)
          throws Exception {
    KMPSearchState<T> inactive = null;
    KMPSearchState<T> active = null;
    List<Hit<T>> hits = Lists.newArrayList();
    if (!witnesses.isEmpty()) {
      inactive = new KMPSearchState<T>(tokenOrdering, pattern, bs);
      for (int i = 0; i < matches.size(); i++) {
        Match<T> temp = matches.get(i);
        // move all elements from active to inactive
        if (inactive == null) {
          inactive = active;
        } else {
          inactive.append(active);
        }
        active = null;
        // move matching SearchStates into active
        KMPSearchState<T> s = inactive;
        while (s != null) {
          KMPSearchState<T> sequential = s.following;
          if (!disjoint(s.v, temp.witnesses)) {
            KMPSearchState<T> child = s.split(temp.witnesses);
            if (active == null) {
              active = child;
            } else {
              active.append(child);
            }
            if (s.v.isEmpty()) {
              inactive = inactive.remove(s);
            }
          }
          s = sequential;
        }
        // now process each byte of the pair
        if (active != null) {
          List<T> data = temp.getTokens();
          for (int j = 0; j < data.size(); j++) {
            KMPSearchState<T> ss = active;
            while (ss != null) {
              if (ss.update(data.get(j))) {
                List<Hit<T>> m = Hit.createHits(pattern.size(), ss.v, this, i, j, multiple, ChunkState.FOUND);
                if (hits == null) {
                  hits = m;
                } else {
                  hits = Hit.merge(hits, m);
                }
                if (!multiple) {
                  break;
                }
              }
              ss = ss.following;
            }
            // now prune the active list
            KMPSearchState<T> s1 = active;
            if (s1.next != null) {
              while (s1 != null) {
                KMPSearchState<T> s2 = s1.following;
                while (s2 != null) {
                  KMPSearchState<T> sequential = s2.following;
                  if (s1.equals(s2)) {
                    s1.merge(s2);
                    active.remove(s2);
                  }
                  s2 = sequential;
                }
                s1 = s1.following;
              }
            }
          }
        }
      }
    }
    return hits;
  }

  /**
   * Create a new empty version.
   *
   * @return the id of the new version
   */
  public Witness add(Witness witness, List<T> data) throws Exception {
    Preconditions.checkArgument(!witnesses.contains(witness));
    witnesses.add(witness);
    return update(witness, data);
  }

  /**
   * Update an existing witness or add a new one.
   *
   * @param witness the id of the witness to add.
   * @param data    the data to merge
   * @return percentage of the new witness that was unique, or 0
   *         if this was the first witness
   */
  public Witness update(Witness witness, List<T> data) throws Exception {
    Preconditions.checkArgument(witnesses.contains(witness));
    // to do: if witness already exists, remove it first
    Converter<T> con = new Converter<T>();
    VariantGraph<T> original = con.create(matches, witnesses);
    original.removeVersion(witness);
    VariantGraph<T> g = original;
    VariantGraphSpecialArc<T> special = g.addSpecialArc(data, witness, 0);
    if (g.getStart().cardinality() > 1) {
      SuffixTree<T> st = makeSuffixTree(special);
      MaximalUniqueMatch<T> bestMUM = MaximalUniqueMatch.findDirectMUM(special, st, g);
      TreeMap<VariantGraphSpecialArc<T>, VariantGraph<T>> specials =
              new TreeMap<VariantGraphSpecialArc<T>, VariantGraph<T>>();
      while (bestMUM != null) {
        if (bestMUM.verify()) {
          bestMUM.merge();
          SimpleQueue<VariantGraphSpecialArc<T>> leftSpecials = bestMUM.getLeftSpecialArcs();
          SimpleQueue<VariantGraphSpecialArc<T>> rightSpecials = bestMUM.getRightSpecialArcs();
          while (leftSpecials != null && !leftSpecials.isEmpty()) {
            installSpecial(specials, leftSpecials.poll(), bestMUM.getLeftSubgraph(), true);
          }
          while (rightSpecials != null && !rightSpecials.isEmpty()) {
            installSpecial(specials, rightSpecials.poll(), bestMUM.getRightSubgraph(), false);
          }
        } else {
          // try again
          bestMUM = recomputeMUM(bestMUM);
          if (bestMUM != null) {
            specials.put(bestMUM.getArc(), bestMUM.getGraph());
          }
        }
        if (Errors.LOG.isTraceEnabled()) {
          for (VariantGraphSpecialArc<T> s : specials.keySet()) {
            final MaximalUniqueMatch<T> bestMatch = s.getBest();
            Errors.LOG.trace("{}{}", (bestMatch.isTransposition() ? "Transposed: " : ""), bestMatch.getMatch());
          }
        }

        // POP topmost entry, if possible
        bestMUM = null;
        if (specials.size() > 0) {
          VariantGraphSpecialArc<T> key = specials.firstKey();
          //assert key.from != null && key.to != null;
          //System.out.println(key.toString());
          if (key != null) {
            g = specials.remove(key);
            bestMUM = key.getBest();
            assert !specials.containsKey(key);
          }
        }
      }
    }
    original.adopt(witness);
    matches = con.serialise();

    if (Errors.LOG.isDebugEnabled()) {
      float percentUnique = (witnesses.size() == 1 ? 0.0f : getPercentUnique(witness));
      Errors.LOG.debug("Updated {} in {}: {} % unique", new Object[]{witness, this, percentUnique});
    }

    return witness;
  }

  /**
   * Get the percentage of the given version that is unique
   *
   * @param version the version to compute uniqueness for
   * @return the percent as a float
   */
  public float getUniquePercentage(Witness version) {
    int totalLen = 0;
    int uniqueLen = 0;
    if (witnesses.size() == 1) {
      return 0.0f;
    } else {
      for (int i = 0; i < matches.size(); i++) {
        Match<T> p = matches.get(i);
        if (p.witnesses.contains(version)) {
          if (p.witnesses.size() == 1) {
            uniqueLen += p.length();
          }
          totalLen += p.length();
        }
      }
      return (float) uniqueLen / (float) totalLen;
    }
  }

  /**
   * Get the percentage of the given version that is unique
   *
   * @param version the version to test
   * @return float fraction of version that is unique
   */
  private float getPercentUnique(Witness version) {
    float unique = 0.0f, shared = 0.0f;
    for (int i = 0; i < matches.size(); i++) {
      Match<T> p = matches.get(i);
      if (p.witnesses.contains(version)) {
        if (p.witnesses.size() == 1) {
          unique += p.length();
        } else {
          shared += p.length();
        }
      }
    }
    return unique / shared;
  }

  /**
   * The MUM is invalid. We have to find a valid one.
   *
   * @param old the old invalid MUM
   * @return a new valid MUM or null
   */
  private MaximalUniqueMatch<T> recomputeMUM(MaximalUniqueMatch<T> old) throws MVDException {
    return computeBestMUM(old.getGraph(), old.getArc());
  }

  /**
   * Compute the best MUM
   *
   * @param g       a graph
   * @param special a special arc aligned with g
   * @return the new MUM or null
   * @throws MVDException
   */
  private MaximalUniqueMatch<T> computeBestMUM(VariantGraph<T> g, VariantGraphSpecialArc<T> special)
          throws MVDException {
    SuffixTree<T> st = makeSuffixTree(special);
    MaximalUniqueMatch<T> directMUM = MaximalUniqueMatch.findDirectMUM(special, st, g);
    MaximalUniqueMatch<T> best = directMUM;
    if (!directAlignOnly) {
      MaximalUniqueMatch<T> leftTransposeMUM = MaximalUniqueMatch.findLeftTransposeMUM(special, st, g);
      MaximalUniqueMatch<T> rightTransposeMUM = MaximalUniqueMatch.findRightTransposeMUM(special, st, g);
      best = getBest(directMUM, leftTransposeMUM, rightTransposeMUM);
    }
    if (best != null) {
      special.setBest(best);
    }
    return best;
  }

  /**
   * Create a new suffix tree based on the data in the special arc.
   * Mask out bytes that are not to be considered.
   *
   * @param special the special arc
   * @return the suffix tree
   * @throws MVDException
   */
  private SuffixTree<T> makeSuffixTree(VariantGraphSpecialArc<T> special) throws MVDException {
    return SuffixTree.create(Lists.newArrayList(special.getData()), tokenOrdering, nullToken);
  }

  /**
   * Install a subarc into specials
   *
   * @param specials the specials TreeMap (red-black tree)
   * @param special  the special subarc to add
   * @param subGraph the directly opposite subgraph
   * @param left     true if we are doing the left subarc, otherwise the
   *                 right
   */
  private void installSpecial(TreeMap<VariantGraphSpecialArc<T>, VariantGraph<T>> specials,
                              VariantGraphSpecialArc<T> special, VariantGraph<T> subGraph, boolean left) throws MVDException {
    assert special.getFrom() != null && special.to != null;
    // this is necessary BEFORE you recalculate the MUM
    // because it will invalidate the special's location
    // in the treemap and make it unfindable
    if (specials.containsKey(special)) {
      specials.remove(special);
    }
    MaximalUniqueMatch<T> best = computeBestMUM(subGraph, special);
    if (best != null) {
      specials.put(special, subGraph);
    }
  }

  /**
   * Find the better of three MUMs or null if they are all null.
   *
   * @param direct          a direct align MUM possibly null
   * @param leftTransposed  the left transpose MUM possibly null
   * @param rightTransposed the right transpose MUM possibly null
   * @return null or the best MUM
   */
  private MaximalUniqueMatch<T> getBest(MaximalUniqueMatch<T> direct, MaximalUniqueMatch<T> leftTransposed,
                                        MaximalUniqueMatch<T> rightTransposed) {
    MaximalUniqueMatch<T> best = null;
    // decide which transpose MUM to use
    MaximalUniqueMatch<T> transposed;
    if (leftTransposed == null) {
      transposed = rightTransposed;
    } else if (rightTransposed == null) {
      transposed = leftTransposed;
    } else if (leftTransposed.compareTo(rightTransposed) > 0) {
      transposed = leftTransposed;
    } else {
      transposed = rightTransposed;
    }
    // decide between direct and transpose MUM
    if (direct != null && transposed != null) {
      int result = direct.compareTo(transposed);
      // remember, we nobbled the compareTo method
      // to produce reverse ordering in the specials
      // treemap, so "less than" is actually longer
      if (result == 0 || result < 0) {
        best = direct;
      } else {
        best = transposed;
      }
    } else if (direct == null) {
      best = transposed;
    } else {
      best = direct;
    }
    return best;
  }

  /**
   * The only way to remove a version from an MVD is to construct
   * a graph and then delete the version from it. Then we serialise
   * it out into pairs again and call the other removeVersion method
   * on EACH and EVERY pair.
   *
   * @param version the version to be removed
   */
  public void removeVersion(Witness version) throws Exception {
    Converter<T> con = new Converter<T>();
    VariantGraph<T> original = con.create(matches, Sets.newHashSet(witnesses));
    original.removeVersion(version);
    original.verify();
    witnesses.remove(version);
    matches = con.serialise();
    for (int i = 0; i < matches.size(); i++) {
      Match<T> p = matches.get(i);
      p.witnesses.remove(version);
    }
  }

  /**
   * Retrieve a witness, copying it from the MVD
   *
   * @param witness the witness to retrieve
   * @return a byte array containing all the data of that witness
   */
  public List<T> getVersion(Witness witness) {
    final List<T> result = Lists.newArrayList();
    for (Match<T> match : Iterables.filter(matches, new Match.WitnessPredicate(witness))) {
        result.addAll(match.getTokens());
    }
    return result;
  }

  /**
   * Get the variants as in an apparatus. The technique is to reconstruct
   * just enough of the variant graph for a given range of pairs to determine
   * what the variants of a given base text are.
   *
   * @param base   the base version
   * @param offset the starting offset in that version
   * @param len    the length of the range to compute variants for
   * @return an array of Variants
   * @throws MVDException
   */
  public SortedSet<Variant<T>> getApparatus(Witness base, int offset, int len)
          throws MVDException {
    int first = getPairIndex(base, offset);
    int last = getPairIndex(base, offset + len);
    /// list of unattached-as-outgoing pairs on the right
    LinkedList<WrappedPair<T>> right = new LinkedList<WrappedPair<T>>();
    LinkedList<CompactNode> nodes = buildBasicNodes(first, last, right, true);
    // find the nodes to which any remaining pairs belong
    // there may still be some ambiguous pairs that are outgoing
    // from nodes within the range
    if (!right.isEmpty()) {
      buildBasicNodes(0, first - 1, right, false);
    }
    return buildVariants(nodes, base);
  }

  /**
   * Build a list of all nodes within the range
   *
   * @param first     first index in the pairs array
   * @param last      last index in the pairs array
   * @param right     list of unattached pairs
   * @param pushRight if true push unattached pairs onto the right list
   * @return a list of nodes
   */
  LinkedList<CompactNode> buildBasicNodes(int first, int last,
                                          LinkedList<WrappedPair<T>> right, boolean pushRight) {
    LinkedList<CompactNode> nodes = new LinkedList<CompactNode>();
    for (int i = last; i >= first; i--) {
      // if not saving unattached pairs
      if (!pushRight && right.isEmpty()) {
        break;
      }
      Match<T> p = matches.get(i);
      if (pushRight && p.isHint()) {
        right.push(new WrappedPair<T>(p));
      } else if (!right.isEmpty() && right.peek().getMatch().isHint()) {
        CompactNode cn = new CompactNode(i);
        // add hint discretely
        cn.addOutgoing(right.pop().getMatch());
        nodes.push(cn);
        addOutgoing(cn, right.pop(), right);
        setDefaultNode(cn, right);
        if (pushRight) {
          right.push(new WrappedPair<T>(p));
        }
      } else if (!right.isEmpty() && !disjoint(right.peek().getMatch().witnesses, p.witnesses)) {
        CompactNode cn = new CompactNode(i);
        addOutgoing(cn, right.pop(), right);
        nodes.push(cn);
        setDefaultNode(cn, right);
        if (pushRight) {
          right.push(new WrappedPair<T>(p));
        }
      } else if (pushRight) {
        right.push(new WrappedPair<T>(p));
      }
    }
    return nodes;
  }

  /**
   * Turn the raw list of nodes and their assigned versions into
   * an array of unique Variants
   *
   * @param nodes the list of variant nodes computed earlier
   * @param base  the base version of the variants
   * @return and array of Variants
   */
  SortedSet<Variant<T>> buildVariants(LinkedList<CompactNode> nodes, Witness base)
          throws MVDException {
    TreeSet<Variant<T>> variants = new TreeSet<Variant<T>>();
    LinkedList<CompactNode> departing = new LinkedList<CompactNode>();
    LinkedList<CompactNode> delenda = new LinkedList<CompactNode>();
    Iterator<CompactNode> iter1 = nodes.iterator();
    Set<Witness> basePath = Sets.newHashSet(base);
    while (iter1.hasNext()) {
      CompactNode node = iter1.next();
      if (node.getIncoming().contains(base)) {
        // clear expended nodes from departing
        if (delenda.size() > 0) {
          Iterator<CompactNode> iter3 = delenda.iterator();
          while (iter3.hasNext()) {
            departing.remove(iter3.next());
          }
          delenda.clear();
        }
        Iterator<CompactNode> iter2 = departing.iterator();
        while (iter2.hasNext()) {
          CompactNode upNode = iter2.next();
          if (!disjoint(upNode.getOutgoing(), node.getIncoming())) {
            // compute intersection
            Set<Witness> bs = Sets.newHashSet(Sets.intersection(upNode.getOutgoing(), node.getIncoming()));
            if (!bs.isEmpty()) {
              List<Set<Witness>> paths = getUniquePaths(upNode, node, bs, base);
              // precompute base variant for later
              List<Variant<T>> w = getWordVariants(upNode.getIndex(), node.getIndex(), basePath);
              // create variants
              for (int i = 0; i < paths.size(); i++) {
                List<Variant<T>> v = getWordVariants(upNode.getIndex(), node.getIndex(), paths.get(i));
                for (int j = 0; j < v.size(); j++) {
                  // prune those equal to base content
                  if (w.size() == 0 || !w.get(0).equalsContent(v.get(j))) {
                    if (variants.size() > 0) {
                      // omit variant if it is within an existing one
                      Variant<T> delendum = null;
                      Iterator<Variant<T>> iter3 = variants.descendingIterator();
                      while (iter3.hasNext()) {
                        Variant<T> x = iter3.next();
                        if (x.endIndex < v.get(j).startIndex) {
                          variants.add(v.get(j));
                          break;
                        } else if (v.get(j).isWithin(x)) {
                          break;
                        } else if (x.isWithin(v.get(j))) {
                          delendum = x;
                          variants.add(v.get(j));
                          break;
                        }
                      }
                      if (delendum != null) {
                        variants.remove(delendum);
                      }
                    } else {
                      variants.add(v.get(j));
                    }
                  }
                }
              }
              // clear that path so we won't follow it again
              upNode.getOutgoing().removeAll(bs);
              if (upNode.getOutgoing().isEmpty()) {
                delenda.add(upNode);
              }
              node.getIncoming().removeAll(bs);
            }
          }
        }
        departing.push(node);
      }
    }
    return variants;
  }

  /**
   * Get an array of variants (usually 1) corresponding to the
   * path between two nodes, extended to the next word-boundaries.
   * The method is pretty simple: just follow the path for each separate
   * version of the variant. Generate one variant for each such path.
   * Then test if they are equal. If they are, merge them. Then return
   * an array of the remaining variants.
   *
   * @param start    index of the start node
   * @param end      index of the end-node
   * @param witnesses the set of witnesses to follow through the
   *                 variant(s)
   * @return an array of Variants
   * @deprecated only works with whitespace in character tokens
   */
  List<Variant<T>> getWordVariants(int start, int end, Set<Witness> witnesses) {
    List<Variant<T>> variants = Lists.newArrayList();

    int offset;
    int length;
    int startIndex = start;
    int origStart;
    int endIndex;
    for (Witness i : witnesses) {
      offset = -1;
      length = 0;
      // get the first outgoing arc containing i
      startIndex = origStart = next(startIndex + 1, i);
      Match<T> p = matches.get(startIndex);
      // start HERE: first outgoing arc, offset 0
      int lastStartIndex = startIndex;
      int lastOffset = 0;
      // move start index backwards, computing offset
      while (startIndex >= 0) {
        if (offset < 0) {
          startIndex = previous(startIndex, i);
          if (startIndex == -1) {
            break;
          }
          p = matches.get(startIndex);
          if (p.length() == 0) {
            offset = -1;
          } else {
            offset = p.length() - 1;
          }
        } else {
          startIndex = lastStartIndex;
          offset = lastOffset;
          break;
        }
      }
      // we may shoot off the start
      if (startIndex == -1) {
        offset = 0;
        startIndex = next(0, i);
      }
      // now advance to end, extending length
      endIndex = origStart;
      while (endIndex <= end) {
        p = matches.get(endIndex);
        length += p.length();
        endIndex = next(endIndex + 1, i);
      }
      // extend to next space after end
      p = matches.get(endIndex);
      int endOffset = 0;
      while (endIndex < matches.size()) {
        if (endOffset == p.length()) {
          endIndex = next(endIndex + 1, i);
          if (endIndex == Integer.MAX_VALUE) {
            break;
          }
          p = matches.get(endIndex);
          endOffset = 0;
        } else {
          break;
        }
      }
      // in case we shot off the end
      if (endIndex == Integer.MAX_VALUE) {
        endIndex = previous(matches.size() - 1, i);
      }
      // now build variant
      Variant<T> temp = new Variant<T>(offset, startIndex, endIndex, length, Sets.newHashSet(i), this);
      int k;
      for (k = 0; k < variants.size(); k++) {
        if (temp.equalsContent(variants.get(k))) {
          variants.get(k).merge(temp);
          break;
        }
      }
      if (k == variants.size()) {
        variants.add(temp);
      }
    }
    return variants;
  }

  /**
   * Set the default node of the wrapped pairs on the right list
   * if not already set. Stop once you meet one that IS set.
   *
   * @param cn    the Compact Node to act as default parent
   * @param right the list
   */
  void setDefaultNode(CompactNode cn, LinkedList<WrappedPair<T>> right) {
    Iterator<WrappedPair<T>> iter = right.iterator();
    while (iter.hasNext()) {
      WrappedPair<T> wp = iter.next();
      if (wp.getDefaultNode() == null) {
        wp.setDefaultNode(cn);
      } else {
        break;
      }
    }
  }

  /**
   * Add an outgoing arc to a node and look for incoming
   * pairs to the left of the node.
   */
  void addOutgoing(CompactNode cn, WrappedPair<T> p, LinkedList<WrappedPair<T>> right) {
    cn.addOutgoing(p.getMatch());
    Set<Witness> wi = cn.getWantsIncoming();
    while (!wi.isEmpty()) {
      int index = cn.getIndex();
      for (int i = index; i >= 0; i--) {
        Match<T> q = matches.get(i);
        if (!disjoint(q.witnesses, wi)) {
          addIncoming(cn, new WrappedPair<T>(q), right);
          wi = cn.getWantsIncoming();
          break;
        }
      }
    }
    // look through right for arcs that intersect
    // with p and must be attached to their default nodes
    Iterator<WrappedPair<T>> iter = right.iterator();
    WrappedPair<T> q = null;
    while (iter.hasNext()) {
      q = iter.next();
      if (!disjoint(q.getMatch().witnesses, p.getMatch().witnesses)) {
        break;
      } else {
        q = null;
      }
    }
    if (q != null) {
      right.remove(q);
      CompactNode c = q.getDefaultNode();
      addIncoming(c, p, right);
      addOutgoing(c, q, right);
    }
  }

  /**
   * Add an incoming arc to a node and look for intersecting
   * pairs in the right list.
   */
  void addIncoming(CompactNode cn, WrappedPair<T> p, LinkedList<WrappedPair<T>> right) {
    cn.addIncoming(p.getMatch());
    Set<Witness> wo = cn.getWantsOutgoing();
    while (!wo.isEmpty()) {
      Iterator<WrappedPair<T>> iter = right.iterator();
      WrappedPair<T> q = null;
      while (iter.hasNext()) {
        q = iter.next();
        if (!disjoint(q.getMatch().witnesses, wo)) {
          break;
        } else {
          q = null;
        }
      }
      if (q != null) {
        right.remove(q);
        addOutgoing(cn, q, right);
        wo = cn.getWantsOutgoing();
      } else {
        break;
      }
    }
  }

  /**
   * Compute an array of unique paths between two nodes in the graph
   * that don't include the base version
   *
   * @param from  the node we are travelling from
   * @param to    the node we are travelling to
   * @param pathV the set of versions to try and follow
   * @param base  the base version
   * @return an array of paths unique to that walk
   */
  List<Set<Witness>> getUniquePaths(CompactNode from, CompactNode to,
                                    Set<Witness> pathV, Witness base) {
    List<Set<Witness>> paths = Lists.newArrayList();
    // for each version in pathV follow the path from-to
    // if any such path contains even one pair that doesn't
    // contain the base version, add it to the paths set.
    for (int i = from.getIndex() + 1; i <= to.getIndex(); i++) {
      Match<T> p = matches.get(i);
      if (!disjoint(p.witnesses, pathV)) {
        // get intersection
        Set<Witness> bs = Sets.newHashSet(Sets.intersection(p.witnesses, pathV));
        if (!bs.isEmpty()) {
          LinkedList<Set<Witness>> queue = new LinkedList<Set<Witness>>();
          queue.push(bs);
          while (!queue.isEmpty()) {
            Set<Witness> b = queue.pop();
            int j = 0;
            int remove = -1;
            for (; j < paths.size(); j++) {
              Set<Witness> c = paths.get(j);
              if (b.equals(c)) {
                break;
              } else if (!disjoint(b, c)) {
                // compute intersection and difference
                Set<Witness> d = Sets.newHashSet(Sets.intersection(b, c));
                Set<Witness> e = Sets.newHashSet(Sets.difference(b, c));
                // push them both and start again
                queue.push(d);
                queue.push(e);
                remove = j;
                break;
              }
            }
            if (remove != -1) {
              paths.remove(remove);
            } else if (j == paths.size()) {
              paths.add(b);
            }
          }
        }
      }
    }
    int k = paths.size() - 1;
    // variants containing the base aren't variants
    while (k >= 0) {
      Set<Witness> b = paths.get(k);
      if (b.contains(base)) {
        paths.remove(k);
      }
      k--;
    }
    return paths;
  }

  /**
   * Get the index of the pair containing the given offset in the
   * given version
   *
   * @param version the version to get the pair index for
   * @param offset  the byte-offset within the version
   * @return the relevant pair index of -1 if not found
   */
  int getPairIndex(Witness version, int offset) {
    int pos = 0;
    int found = -1;
    for (int i = 0; i < matches.size(); i++) {
      Match<T> p = matches.get(i);
      if (p.witnesses.contains(version)) {
        if (offset < pos + p.length()) {
          found = i;
          break;
        } else {
          pos += p.length();
        }
      }
    }
    return found;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
            .addValue(description)
            .add("witnesses", witnesses.size())
            .add("matches", matches.size())
            .toString();
  }

  /**
   * Compute a difference matrix, suitable for inputting into
   * fitch, kitsch or neighbor programs in Phylip. Compute a simple
   * sum of squares between all possible pairs of versions in the MVD
   * such that equal characters are scored as 0, variants as 1 for
   * each character of the longest of the two variants, 1 for each
   * entire transposition, thus scaling them for length (a 10-char
   * transposition costs half that of a 5-char one). Having calculated
   * that in a matrix of nVersions x nVersions, divide by the length
   * of the longest version in each case - 1.
   *
   * @return a 2-D matrix of differences.
   */
  public double[][] computeDiffMatrix(List<Witness> ordered) {
    int s = witnesses.size();
    // keep track of the length of each version
    int[] lengths = new int[s];
    // the length of j last time j and k were joined
    int[][] lastJoinJ = new int[s][s];
    // the length of k last time j and k were joined
    int[][] lastJoinK = new int[s][s];
    // the cost is the longest distance between any two
    // versions since they were last joined
    int[][] costs = new int[s][s];
    for (int i = 0; i < matches.size(); i++) {
      Match<T> p = matches.get(i);
      // consider each combination of j and k, including j=k
      for (Witness jw : p.witnesses) {
        int j = ordered.indexOf(jw);
        for (Witness kw : p.witnesses) {
          int k = ordered.indexOf(kw);
          costs[j][k] += Math.max(lengths[j] - lastJoinJ[j][k], lengths[k] - lastJoinK[j][k]);
          costs[k][j] = costs[j][k];
          lastJoinJ[j][k] = lengths[j] + p.length();
          lastJoinK[j][k] = lengths[k] + p.length();
        }
        lengths[j] += p.length();
      }
    }
    double[][] diffs = new double[s][s];
    for (int i = 0; i < s; i++) {
      for (int j = 0; j < s; j++) {
        // normalise by the longer of the two lengths -1
        double denominator = Math.max(lengths[i], lengths[j]) - 1;
        diffs[i][j] = ((double) costs[i][j]) / denominator;
      }
    }
    return diffs;
  }
}
