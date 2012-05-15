/**
 * This is a Java-port of Mark Nelson's C++ implementation of Ukkonen's algorithm.
 * http://illya-keeplearning.blogspot.com/2009/04/suffix-trees-java-ukkonens-algorithm.html
 */

package com.blogspot.illyakeeplearning.suffixtree;

/**
 * Defines suffix.
 * <p/>
 * When a new tree is added to the table, we step through all the currently defined suffixes
 * from the active point to the end point.  This structure defines a <b>Suffix</b> by its final character.
 * In the canonical representation, we define that last character by starting at a node in the tree,
 * and following a string of characters, represented by <b>first_char_index</b> and <b>last_char_index</b>.
 * The two indices point into the input string.  Note that if a suffix ends at a node,
 * there are no additional characters needed to characterize its last character position.
 * When this is the case, we say the node is <b>explicit</b>,
 * and set <b>first_char_index > last_char_index<b> to flag that.
 */
public class Suffix {
  public int originNode;
  public int firstCharIndex;
  public int lastCharIndex;

  public Suffix(int node, int start, int stop) {
    this.originNode = node;
    this.firstCharIndex = start;
    this.lastCharIndex = stop;
  }

  public boolean isExplicit() {
    return this.firstCharIndex > this.lastCharIndex;
  }

  public boolean isImplicit() {
    return lastCharIndex >= firstCharIndex;
  }

  public void canonize(SuffixTree st) {
    if (!isExplicit()) {
      Edge edge = Edge.find(st, originNode, st.content[firstCharIndex]);
      int edgeSpan = edge.span();
      while (edgeSpan <= (lastCharIndex - firstCharIndex)) {
        firstCharIndex = firstCharIndex + edgeSpan + 1;
        originNode = edge.endNode;
        if (firstCharIndex <= lastCharIndex) {
          edge = Edge.find(st, edge.endNode, st.content[firstCharIndex]);
          edgeSpan = edge.span();
        }
      }
    }
  }
}
