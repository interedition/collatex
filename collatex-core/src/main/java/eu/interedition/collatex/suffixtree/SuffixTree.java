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
package eu.interedition.collatex.suffixtree;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a Java translation of Shlomo Yona's open source
 * C implementation of Esko Ukkonen's Suffix Tree algorithm
 * http://mila.cs.technion.ac.il/~yona/suffix_tree/
 *
 * @author Desmond Schmidt 25/10/08
 */
public class SuffixTree<T> {
  private static final Logger LOG = Logger.getLogger(SuffixTree.class.getName());

  /**
   * The one and only real source string of the tree. All edge-labels
   * contain only indices to this string and do not contain the characters
   * themselves
   */
  private final T[] source;

  /**
   * length of {@link #source} minus the '$'
   */
  private final int length;

  private final Comparator<T> comparator;

  /**
   * The node that is the head of all others. It has no siblings nor a
   * father
   */
  private final Node<T> root;

  /**
   * Used to mark the node that has no suffix link yet. According to
   * Ukkonen, it will have one by the end of the current phase.
   */
  private Node<T> suffixless;

  /**
   * The virtual end of all leaves
   */
  private int virtualEnd;

  public static <T> SuffixTree<T> create(Iterable<T> str, Comparator<T> comparator) {
    final SuffixTree<T> suffixTree = new SuffixTree<T>(str, comparator);
    final Position<T> position = suffixTree.at(suffixTree.root, 0);

    // initializing algorithm parameters
    final ExtensionState ep = new ExtensionState(2, (char) 0);

    // Ukkonen's algorithm begins here
    for (int offset = 2; offset < suffixTree.length; offset++) {
      // perform Single Phase Algorithm
      suffixTree.build(position, offset, ep);
    }

    return suffixTree;
  }

  public Position<T> at(Node<T> node, int edgePos) {
    return new Position<T>(node, edgePos);
  }

  public T[] getSource() {
    return source;
  }

  public Node getRoot() {
    return root;
  }

  private boolean compare(T t1, T t2) {
    return (t1 == null && t2 == null) || (t1 != null && t2 != null && comparator.compare(t1, t2) == 0);
  }

  /**
   * Starts Ukkonen's construction algorithm by calling SPA n times, where
   * n is the length of the source string.
   *
   * @param str the source string is a sequence of unsigned characters
   *            (maximum of 256 different symbols). In the original algorithm '$'
   *            was a special character. However,http://www.abc.net.au/news/ in Yona's
   *            version it is appended at the end of the input string and then never used.
   */
  @SuppressWarnings("unchecked")
  private SuffixTree(Iterable<T> str, Comparator<T> comparator) {
    this.length = Iterables.size(str) + 1;
    Preconditions.checkArgument(this.length > 1);
    this.source = (T[]) Array.newInstance(Iterables.getFirst(str, null).getClass(), this.length + 1);
    this.comparator = comparator;
    this.virtualEnd = 1; // added to make 1-character suffix trees work

    int i = 0;
    for (T t : str) {
      this.source[++i] = t;
    }
    // the terminal ('$') is never examined but assumed to be there
    this.source[length] = null;

    this.root = new Node(this, null, 0, 0, 0);

    // allocating first node, child of the root (phase 0), the longest
    // path node
    this.root.firstChild = new Node(this, root, 1, length, 1);
  }

  /**
   * Performs all insertion of a single phase by calling function
   * {@link #extend} starting
   * from the first extension that does not already exist in the tree and ending
   * at the first extension that already exists in the tree.
   *
   * @param position the node and position in its incoming edge where extension begins
   * @param offset    current phase number - offset into text
   * @param ep       the first extension number of that phase, a flag signaling whether
   *                 the extension is the first of this phase, after the last phase ended with
   *                 rule 3. If so - extension will be executed again in this phase, and thus
   *                 its suffix link would not be followed. Updated: The extension number that
   *                 was last executed on this phase. Next phase will start from it and not from 1
   */
  private void build(Position<T> position, int offset, ExtensionState ep) {
    // no such rule (0). Used for entering the loop
    int ruleApplied = 0;
    Path path = new Path(0, 0);

    // leafs Trick: apply implicit extensions 1 through prevPhase
    virtualEnd = offset + 1;

    // apply explicit extensions until last extension of this phase is reached
    // or extension rule 3 is applied once
    while (ep.extension <= offset + 1) {
      path.begin = ep.extension;
      path.end = offset + 1;
      // Call Single-Extension-Algorithm
      ruleApplied = extend(position, path, ep.repeatedExtension, ruleApplied);

      // check if rule 3 was applied for the current extension
      if (ruleApplied == 3) {
        // Signaling that the next phase's first extension will not follow a
        // suffix link because same extension is repeated
        ep.repeatedExtension = 1;
        break;
      }
      ep.repeatedExtension = 0;
      ep.extension++;
    }
  }

  /**
   * Single-Extension-Algorithm (see Ukkonen's algorithm). Ensure that a certain
   * extension is in the tree.
   * <ol><li>Follows the current node's suffix link.</li>
   * <li>Check whether the rest of the extension is in the tree.</li>
   * <li>If it is - reports the calling function SPA of rule 3 (= current phase is
   * done).</li>
   * <li>If it's not - inserts it by applying rule 2.</li></ol>
   *
   * @param position    the node and position in its incoming edge where extension begins
   * @param str         the starting and ending indices of the extension
   * @param afterRule3  a flag indicating whether the last phase ended by rule 3
   *                    (last extension of the last phase already existed in the tree - and
   *                    if so, the current phase starts at not following the suffix link of
   *                    the first extension)
   * @param ruleApplied last rule applied
   * @return The rule that was applied to that extension. Can be 3 (phase is done)
   *         or 2 (a new leaf was created).
   */
  private int extend(Position<T> position, Path str, char afterRule3, int ruleApplied) {
    int charsFound = 0;
    int pathPos = str.begin;
    Path originalPath = new Path(str.begin, str.end);
    Node<T> tmp;

    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "\n{0}extension: {1} phase+1: {2} -- {3} ({4},{5} | {6})", new Object[]{
              toString(),
              str.begin, str.end,
              (afterRule3 == 0 ? "followed from" : "starting at"),
              position.node.edgeStart, position.node.getEdgeEnd(), position.edgePos
      });
    }
    // follow suffix link only if it's not the first extension after rule 3 was applied
    if (afterRule3 == 0) {
      followSuffixLink(position);
    }
    // if node is root - trace whole string starting from the root, else -
    // trace last character only
    if (position.node == root) {
      TraceReturnValue trv = new TraceReturnValue();
      trv.edgePos = position.edgePos;
      trv.charsFound = charsFound;
      position.node = traceString(root, str, trv, SkipType.NO_SKIP);
      position.edgePos = trv.edgePos;
      charsFound = trv.charsFound;
    } else {
      str.begin = str.end;
      charsFound = 0;

      // consider 2 cases:
      // 1. last character matched is the last of its edge
      if (position.atLastChar()) {
        // trace only last symbol of str, search in the  NEXT edge (node)
        tmp = position.node.findChild(source[str.end]);
        if (tmp != null) {
          position.node = tmp;
          position.edgePos = 0;
          charsFound = 1;
        }
      }
      // 2. last character matched is NOT the last of its edge
      else {
        // Trace only last symbol of str, search in the CURRENT edge (node)
        if (compare(source[position.node.edgeStart + position.edgePos + 1], source[str.end])) {
          position.edgePos++;
          charsFound = 1;
        }
      }
    }
    // if whole string was found - rule 3 applies
    if (charsFound == str.end - str.begin + 1) {
      ruleApplied = 3;
      // if there is an internal node that has no suffix link yet (only one may
      // exist) - create a suffix link from it to the father-node of the
      // current position in the tree (pos)
      if (suffixless != null) {
        suffixless.linkTo(position.node.parent);
        // marks that no internal node with no suffix link exists
        suffixless = null;
      }

      if (LOG.isLoggable(Level.FINER)) {
        LOG.log(Level.FINER, "rule 3 ({0},{1})", new Object[] { str.begin, str.end });
      }
      return ruleApplied;
    }

    // if last char found is the last char of an edge - add a character at the
    // next edge
    if (position.atLastChar() || position.node == root) {
      // decide whether to apply rule 2 (newSon) or rule 1
      if (position.node.firstChild != null) {
        // apply extension rule 2 new son - a new leaf is created and returned
        // by applyExtensionRule2
        applyExtensionRule2(position.node, str.begin + charsFound, str.end, pathPos, 0,
                Rule2Type.NEW_CHILD);
        ruleApplied = 2;
        // if there is an internal node that has no suffix link yet (only one
        // may exist) - create a suffix link from it to the father-node of the
        // current position in the tree (pos)
        if (suffixless != null) {
          suffixless.linkTo(position.node);
          // Marks that no internal node with no suffix link exists
          suffixless = null;
        }
      }
    } else {
      // apply extension rule 2 split - a new node is created and returned by
      // applyExtensionRule2
      tmp = applyExtensionRule2(position.node, str.begin + charsFound, str.end, pathPos,
              position.edgePos, Rule2Type.SPLIT);
      if (suffixless != null) {
        suffixless.linkTo(tmp);
      }
      // link root's sons with a single character to the root
      if (tmp.edgeLength() == 1 && tmp.parent == root) {
        tmp.largestSuffix = root;
        // marks that no internal node with no suffix link exists
        suffixless = null;
      } else
      // mark tmp as waiting for a link
      {
        suffixless = tmp;
      }

      // prepare pos for the next extension
      position.node = tmp;
      ruleApplied = 2;
    }
    return ruleApplied;
  }


  /**
   * Connect rightSib as the right sibling of leftSib and vice versa.
   *
   * @param leftSib  one of two nodes to be connected
   * @param rightSib one of two nodes to be connected
   */
  void connect(Node<T> leftSib, Node<T> rightSib) {
    // connect the right node as the right sibling of the left node
    if (leftSib != null) {
      leftSib.nextSibling = rightSib;
    }
    // connect the left node as the left sibling of the right node
    if (rightSib != null) {
      rightSib.previousSibling = leftSib;
    }
  }

  /**
   * Apply "extension rule 2" in 2 cases:
   * 1. A new son (leaf 4) is added to a node that already has sons:
   * (1)	           (1)
   * /   \	 ->   / | \
   * (2)  (3)      (2)(3)(4)
   * 2. An edge is split and a new leaf (2) and an internal node (3) are added:
   * | 	  |
   * | 	 (3)
   * |     ->   / \
   * (1)       (1) (2)
   *
   * @param node           node 1 (see drawings)
   * @param edgeLabelBegin start index of node 2's incoming edge
   * @param edgeLabelEnd   end index of node 2's incoming edge
   * @param pathPos        path start index of node 2
   * @param edgePos        position in node 1's incoming edge where split is to be
   *                       performed
   * @return a newly created leaf (newSon case) or internal node (split case).
   */
  Node<T> applyExtensionRule2(Node<T> node, int edgeLabelBegin, int edgeLabelEnd,
                                     int pathPos, int edgePos, Rule2Type type) {
    Node<T> newLeaf, newInternal, son;
    // newSon
    if (type == Rule2Type.NEW_CHILD) {
      if (LOG.isLoggable(Level.FINER)) {
        LOG.log(Level.FINER, "rule 2: new leaf ({0},{1})", new Object[] { edgeLabelBegin, edgeLabelEnd });
      }
      // create a new leaf (4) with the characters of the extension
      newLeaf = new Node<T>(this, node, edgeLabelBegin, edgeLabelEnd, pathPos);
      // connect newLeaf (4) as the new son of node (1)
      son = node.firstChild;
      while (son.nextSibling != null) {
        son = son.nextSibling;
      }
      connect(son, newLeaf);
      // return (4)
      return newLeaf;
    }
    // split
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "rule 2: split ({0}, {1})", new Object[] { edgeLabelBegin, edgeLabelEnd });
    }
    // create a new internal node (3) at the split point
    newInternal = new Node<T>(this, node.parent, node.edgeStart,
            node.edgeStart + edgePos, node.pathPosition);
    // update the node (1) incoming edge starting index (it now starts
    // where node (3) incoming edge ends)
    node.edgeStart += edgePos + 1;
    // create a new leaf (2) with the characters of the extension
    newLeaf = new Node<T>(this, newInternal, edgeLabelBegin, edgeLabelEnd, pathPos);
    // connect newInternal (3) where node (1) was
    // connect (3) with (1)'s left sibling
    connect(node.previousSibling, newInternal);
    // connect (3) with (1)'s right sibling
    connect(newInternal, node.nextSibling);
    node.previousSibling = null;
    // connect (3) with (1)'s father
    if (newInternal.parent.firstChild == node) {
      newInternal.parent.firstChild = newInternal;
    }
    // connect newLeaf (2) and node (1) as sons of newInternal (3)
    newInternal.firstChild = node;
    node.parent = newInternal;
    connect(node, newLeaf);
    // return (3)
    return newInternal;
  }

  /**
   * Traces for a string in a given node's OUTcoming edge. It searches
   * only in the given edge and not other ones. Search stops when either
   * whole string was found in the given edge, a part of the string was
   * found but the edge ended (and the next edge must be searched too
   * - performed by function traceString) or one non-matching character
   * was found.
   *
   * @param node node to start from
   * @param str  string to trace
   * @param type skip or noSkip
   * @return the node where tracing has stopped
   */
  private Node<T> traceSingleEdge(Node<T> node, Path str, TraceReturnValue trv,
                                         SkipType type) {
    Node<T> contNode;
    int length, strLen;

    // set default return values
    trv.searchDone = true;
    trv.edgePos = 0;

    // search for the first character of the string in the outgoing
    // edge of node
    contNode = node.findChild(source[str.begin]);
    if (contNode == null) {
      // Search is done, string not found
      trv.edgePos = node.edgeLength() - 1;
      trv.charsFound = 0;
      return node;
    }
    // found first character - prepare for continuing the search
    node = contNode;
    length = node.edgeLength();
    strLen = str.end - str.begin + 1;

    // compare edge length and string length.
    // if edge is shorter then the string being searched and skipping
    // is enabled - skip edge
    if (type == SkipType.SKIP) {
      if (length <= strLen) {
        trv.charsFound = length;
        trv.edgePos = length - 1;
        if (length < strLen) {
          trv.searchDone = false;
        }
      } else {
        trv.charsFound = strLen;
        trv.edgePos = strLen - 1;
      }
      return node;
    } else {
      // find minimum out of edge length and string length, and scan it
      if (strLen < length) {
        length = strLen;
      }

      for (trv.edgePos = 1, trv.charsFound = 1; trv.edgePos < length;
           trv.charsFound++, trv.edgePos++) {
        // compare current characters of the string and the edge.
        // if equal - continue
        if (!equals(comparator.compare(source[node.edgeStart + trv.edgePos], source[str.begin + trv.edgePos]))) {
          trv.edgePos--;
          return node;
        }
      }
    }
    // the loop has advanced edgePos one too much
    trv.edgePos--;
    if ((trv.charsFound) < strLen)
    // search is not done yet
    {
      trv.searchDone = false;
    }
    return node;
  }

  /**
   * <p>Traces for a string in the tree. This function is used in
   * construction process only, and not for after-construction search
   * of substrings. It is tailored to enable skipping (when we know a
   * suffix is in the tree (when following a suffix link) we can avoid
   * comparing all symbols of the edge by skipping its length immediately
   * and thus save atomic operations - see Ukkonen's algorithm, skip
   * trick).</p>
   * <p>This function, in contradiction to the function traceSingleEdge,
   * 'sees' the whole picture, meaning it searches a string in the whole
   * tree and not just in a specific edge.</p>
   *
   * @param node node to start from
   * @param str  string to trace
   * @param trv  return values to fill in
   * @param type skip or noSkip
   * @return the node where tracing has stopped
   */
  private Node<T> traceString(Node<T> node, Path str, TraceReturnValue trv,
                                     SkipType type) {
    Path localStr = new Path(str.begin, str.end);
    trv.charsFound = 0;
    TraceReturnValue localTrv = new TraceReturnValue();
    localTrv.searchDone = false;
    while (localTrv.searchDone == false) {
      trv.edgePos = localTrv.edgePos = 0;
      localTrv.charsFound = 0;
      node = traceSingleEdge(node, localStr, localTrv, type);
      localStr.begin += localTrv.charsFound;
      trv.charsFound += localTrv.charsFound;
      trv.edgePos = localTrv.edgePos;
    }
    return node;
  }

  /**
   * Get the Pos from the root that corresponds to the initial byte b
   *
   * @param b the first byte from the root whose Pos is desired
   * @return the relevant Pos, null if not present
   */
  public Position<T> getStartPos(T b) {
    Node<T> node = root.findChild(b);
    if (node != null) {
      return new Position<T>(node, node.edgeStart);
    } else {
      return null;
    }
  }

  /**
   * Advance a Pos in the suffix tree by one byte if possible.
   * On entry the pos is matched with the byte it points to. We
   * try to match the NEXT byte. If we succeed, we update pos.
   * Otherwise we do nothing to pos.
   *
   * @param b        the byte to advance from pos
   * @param position the position in the tree where we were last time
   * @return true if the advance was successful, false otherwise
   */
  public boolean advance(Position<T> position, T b) {
    if (position.node == null) {
      position.node = root.findChild(b);
      if (position.node != null) {
        position.edgePos = position.node.edgeStart;
        return true;
      } else {
        return false;
      }
    } else {
      int nodeLabelEnd = position.node.getEdgeEnd();
      // already matched that byte ...
      if (position.edgePos == nodeLabelEnd) {
        Node<T> localNode = position.node.findChild(b);
        if (localNode != null) {
          position.edgePos = localNode.edgeStart;
          position.node = localNode;
          return true;
        } else {
          return false;
        }
      } else {
        boolean success = compare(source[position.edgePos + 1], b);
        if (success) {
          position.edgePos++;
        }
        return success;
      }
    }
  }

  /**
   * Find the length of a match starting from a pos that represents
   * the first mismatch AFTER some matching string.
   *
   * @param position the end-pos of the string
   * @return the length of the matching string
   */
  int getMatchLength(Position position) {
    if (position.node == null) {
      return 0;
    } else {
      Node temp = position.node;
      int length = position.edgePos - temp.edgeStart;
      temp = temp.parent;
      while (temp != root) {
        length += temp.edgeLength();
        temp = temp.parent;
      }
      return length;
    }
  }

  /**
   * Traces for a string in the tree. This function is used for
   * substring search after tree construction is done. It simply
   * traverses down the tree starting from the root until either
   * the searched string is fully found or one non-matching character
   * is found. In this function skipping is not enabled because we
   * don't know whether the string is in the tree or not (see function
   * traceString above).
   *
   * @param str the substring to find
   * @return the index of the starting position of the substring in
   *         the tree source string. If the substring is not found, returns
   *         <code>null</code>
   */
  public Integer findSubstring(List<T> str) {
    // starts with the root's son that has the first character of W
    // as its incoming edge first character
    Node<T> node = root.findChild(str.get(0));
    int k, j = 0, nodeLabelEnd;

    // scan nodes down from the root until a leaf is reached or the
    // substring is found
    while (node != null) {
      k = node.edgeStart;
      nodeLabelEnd = node.getEdgeEnd();
      // Scan a single edge - compare each character with the searched
      // string
      while (j < str.size() && k <= nodeLabelEnd && compare(source[k], str.get(j))) {
        j++;
        k++;
      }
      // checking which of the stopping conditions are true
      if (j == str.size()) {
        // W was found - it is a substring. Return its path starting
        // index
        return node.pathPosition;
      } else if (k > nodeLabelEnd)
      // current edge is found to match, continue to next edge
      {
        node = node.findChild(str.get(j));
      } else {
        // one non-matching symbols is found - W is not a substring
        return null;
      }
    }
    return null;
  }

  /**
   * Follows the suffix link of the source node according to Ukkonen's
   * rules.
   *
   * @param position a combination of the source node and the position in
   *                 its incoming edge where suffix ends
   * @return The destination node that represents the longest suffix
   *         of node's path. Example: if node represents the path "abcde" then
   *         it returns the node that represents "bcde"
   */
  Position<T> followSuffixLink(Position<T> position) {
    // gama is the string between node and its father, in case node
    // doesn't have a suffix link
    Path gama = new Path(0, 0);
    // dummy argument for trace_string function
    int charsFound = 0;

    if (position.node == root) {
      return position;
    }
    // if node has no suffix link yet or in the middle of an edge - remember the
    // doesn't have edge between the node and its father (gama) and follow its
    // father's suffix link (it must have one by Ukkonen's lemma). After
    // following, trace down gama - it must exist in the tree (and thus can use
    // the skip trick - see traceString function description)
    if (position.node.largestSuffix == null || !position.atLastChar()) {
      // if the node's father is the root, than no use following it's link (it
      // is linked to itself). Tracing from the root (like in the naive
      // algorithm) is required and is done by the calling function SEA upon
      // receiving a return value of tree->root from this function
      if (position.node.parent == root) {
        position.node = root;
        return position;
      }
      // store gama - the indices of node's incoming edge
      gama.begin = position.node.edgeStart;
      gama.end = position.node.edgeStart + position.edgePos;
      // follow father's suffix link
      position.node = position.node.parent.largestSuffix;
      // down-walk gama back to suffixLink's son
      TraceReturnValue trv = new TraceReturnValue();
      trv.edgePos = position.edgePos;
      trv.charsFound = charsFound;
      position.node = traceString(position.node, gama, trv, SkipType.SKIP);
      position.edgePos = trv.edgePos;
    } else {
      // if a suffix link exists - just follow it
      position.node = position.node.largestSuffix;
      position.edgePos = position.node.edgeLength() - 1;
    }
    return position;
  }

  /**
   * This function prints the tree. It simply starts the recursive function
   * printNode with depth 0 (the root).
   */
  @Override
  public String toString() {
    final StringBuilder tree = new StringBuilder("\nroot\n");
    toString(root, 0, tree);
    return tree.toString();
  }

  /**
   * Prints a subtree under a node of a certain tree-depth.
   *
   * @param node the node that is the root of the subtree
   * @param depth the depth of that node. This is used for printing the branches
   *              that are coming from higher nodes and only then the node itself is printed.
   *              This gives the effect of a tree on screen. In each recursive call, the depth
   *              is increased.
   */
  private void toString(Node node, int depth, StringBuilder str) {
    Node child = node.firstChild;
    int d = depth;
    int start = node.edgeStart;
    int end;
    end = node.getEdgeEnd();
    if (depth > 0) {
      // print the branches coming from higher nodes
      while (d > 1) {
        str.append("|");
        d--;
      }
      str.append("+");
      // print the node itself
      while (start <= end) {
        str.append("[").append(Objects.firstNonNull(source[start], "").toString()).append("]");
        start++;
      }
      str.append(" (").append(node.edgeStart).append(",").append(end).append(" | ").append(node.pathPosition).append(")\n");
    }
    // recursive call for all node1's sons
    while (child != null) {
      toString(child, depth + 1, str);
      child = child.nextSibling;
    }
  }

  /**
   * This structure describes a node and its incoming edge
   */
  public static class Node<T> {

    final SuffixTree<T> tree;
    /**
     * A pointer to that node's father
     */
    Node<T> parent;
    /**
     * A linked list of sons of that node
     */
    Node<T> firstChild;
    /**
     * A linked list of right siblings of that node
     */
    Node<T> nextSibling;
    /**
     * A linked list of left siblings of that node
     */
    Node<T> previousSibling;
    /**
     * A pointer to the node that represents the largest
     * suffix of the current node
     */
    Node<T> largestSuffix;
    /**
     * Index of the start position of the node's path
     */
    int pathPosition;
    /**
     * Start index of the incoming edge
     */
    int edgeStart;
    /**
     * End index of the incoming edge
     */
    int edgeEnd;

    /**
     * Create a Node
     *
     * @param parent   father of the node
     * @param start    the starting index of the incoming edge to that node
     * @param end      the end index of the incoming edge to that node
     * @param position the path starting position of the node.
     */
    public Node(SuffixTree<T> tree, Node<T> parent, int start, int end, int position) {
      Preconditions.checkArgument(end >= start, "Error: start greater than end");
      this.tree = tree;
      this.parent = parent;
      this.pathPosition = position;
      this.edgeStart = start;
      this.edgeEnd = end;
    }

    public Node<T> getFirstChild() {
      return firstChild;
    }

    public Node<T> getNextSibling() {
      return nextSibling;
    }

    public Node<T> getPreviousSibling() {
      return previousSibling;
    }

    public Node<T> getParent() {
      return parent;
    }

    public Node<T> getLargestSuffix() {
      return largestSuffix;
    }

    public int getPathPosition() {
      return pathPosition;
    }

    public int getEdgeStart() {
      return edgeStart;
    }

    /**
     * Returns the end index of the incoming edge to that node. This function is
     * needed because for leaves the end index is not relevant, instead we must
     * look at the variable "e" (the global virtual end of all leaves). Never
     * refer directly to a leaf's end-index.
     *
     * @return the end index of that node (meaning the end index of the node's
     *         incoming edge).
     */
    public int getEdgeEnd() {
      // if it's a leaf - return e, otherwise its real end
      return (isLeaf() ? tree.virtualEnd : edgeEnd);
    }

    /**
     * Returns the length of the incoming edge to that node. Uses getEdgeLabelEnd.
     *
     * @return the length of that node.
     */
    int edgeLength() {
      // calculate and return the length of the node
      return getEdgeEnd() - edgeStart + 1;
    }

    /**
     * Does this node have no children, i.e. is it a leaf?
     *
     * @return true if this node is a leaf
     */
    public boolean isLeaf() {
      return (firstChild == null);
    }


    /**
     * Find the son of a node that starts with a certain character.
     *
     * @param character the character to be searched for in the sons
     * @return the son found, or null if no such son.
     */
    Node<T> findChild(T character) {
      for (Node<T> child = firstChild; child != null; child = child.nextSibling) {
        if (tree.compare(tree.source[child.edgeStart], character)) {
          return child;
        }
      }
      return null;
    }

    /**
     * Creates a suffix link between node and the node 'link' which represents its
     * largest suffix. The function could be avoided but is needed to monitor the
     * creation of suffix links when debugging or changing the tree.
     *
     * @param link the node to link to
     */
    private void linkTo(Node<T> link) {
      this.largestSuffix = link;
    }
  }

  public static class Position<T> {
    public Node<T> node;
    public int edgePos;

    public Position(Node<T> node, int edgePos) {
      this.node = node;
      this.edgePos = edgePos;
    }

    /**
     * Is edgePos the last position in node's incoming edge?
     *
     * @return true if edgePos is the last position in node's incoming edge
     */
    boolean atLastChar() {
      return (edgePos == (node.edgeLength() - 1));
    }
  }

  /**
   * needed to store VAR parameters in
   * {@link SuffixTree#traceSingleEdge(eu.interedition.collatex.suffixtree.SuffixTree.Node, eu.interedition.collatex.suffixtree.SuffixTree.Path, eu.interedition.collatex.suffixtree.SuffixTree.TraceReturnValue, eu.interedition.collatex.suffixtree.SuffixTree.SkipType)}
   */
  private static class TraceReturnValue {
    /**
     * Last matching position in edge
     */
    int edgePos;
    /**
     * Last matching position in tree source string
     */
    int charsFound;
    /**
     * true if search is done, false if not
     */
    boolean searchDone;
  }

  /**
   * Used in function
   * {@link SuffixTree#traceString(eu.interedition.collatex.suffixtree.SuffixTree.Node, eu.interedition.collatex.suffixtree.SuffixTree.Path, eu.interedition.collatex.suffixtree.SuffixTree.TraceReturnValue, eu.interedition.collatex.suffixtree.SuffixTree.SkipType)}
   * for skipping (Ukkonen's Skip Trick).
   */
  private enum SkipType {
    SKIP,
    NO_SKIP
  }

  /**
   * Used in method applyRule2 - two types of rule 2 - see function
   * for more details.
   */
  private enum Rule2Type {
    NEW_CHILD,
    SPLIT
  }

  /**
   * required by {@link SuffixTree#build}
   */
  private static class ExtensionState {
    /**
     * The last extension performed in the previous phase
     */
    int extension;
    /**
     * 1 if the last rule applied is 3
     */
    char repeatedExtension;

    private ExtensionState(int extension, char repeatedExtension) {
      this.extension = extension;
      this.repeatedExtension = repeatedExtension;
    }
  }

  private static class Path {
    int begin;
    int end;

    Path(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

  }
}
