/**
 * This is a Java-port of Mark Nelson's C++ implementation of Ukkonen's algorithm.
 * http://illya-keeplearning.blogspot.com/2009/04/suffix-trees-java-ukkonens-algorithm.html
 */

package com.blogspot.illyakeeplearning.suffixtree;

import java.io.PrintStream;

public class SuffixTree {
  public static final int MAX_LENGTH = 1000;

  //a prime roughly 10% larger
  public static int HASH_TABLE_SIZE = 2179;

  // This is the hash table where all the currently defined EDGES are stored.
  // You can dump out all the currently defined EDGES by iterating through the table
  // and finding EDGES whose start_node is not -1.
  public Edge[] edges = new Edge[HASH_TABLE_SIZE];

  // The array of defined NODES. The count is 1 at the start
  // because the initial tree has the root node defined, with no children.
  public Node[] nodes = new Node[MAX_LENGTH * 2];

  // The string represented by the tree.
  public char[] content;

  /**
   * The last index of the represented string.
   */
  public int contentLastIndex;

  private SuffixTree() {
    for (int i = 0; i < edges.length; i++) {
      edges[i] = new Edge();
    }
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = new Node();
    }
  }

  public static SuffixTree create(String content) {
    final SuffixTree st = new SuffixTree();
    st.content = content.toCharArray();
    st.contentLastIndex = content.length() - 1;

    // The active point is the first non-leaf suffix in the tree.
    // We start by setting this to be the empty string at node 0.
    // The addPrefix() function will update this value after every new prefix is added.

    // The initial active prefix
    Suffix active = new Suffix(0, 0, -1);
    for (int i = 0; i <= st.contentLastIndex; i++) {
      st.addPrefix(active, i);
    }
    return st;
  }

  /**
   * This routine constitutes the heart of the algorithm. It is called repetitively, once for each of the prefixes
   * of the input string.  The prefix in question is denoted by the index of its last character.
   * <p/>
   * At each prefix, we start at the active point, and add a new edge denoting the new last character,
   * until we reach a point where the new edge is not needed due to the presence of an existing edge
   * starting with the new last character.  This point is the end point.
   * <p/>
   * Luckily for use, the end point just happens to be the active point for the next pass through the tree.
   * All we have to do is update it's last_char_index to indicate that it has grown by a single character,
   * and then this routine can do all its work one more time.
   *
   * @param active
   * @param lastCharIndex
   */
  private void addPrefix(Suffix active, int lastCharIndex) {
    int parentNode;
    int lastParentNode = -1;

    for (; ; ) {
      Edge edge;
      parentNode = active.originNode;

      // Step 1 is to try and find a matching edge for the given node.
      // If a matching edge exists, we are done adding edges, so we break
      // out of this big loop.
      if (active.isExplicit()) {
        edge = Edge.find(this, active.originNode, content[lastCharIndex]);
        if (edge.startNode != -1) {
          break;
        }
      } else {
        //implicit node, a little more complicated
        edge = Edge.find(this, active.originNode, content[active.firstCharIndex]);
        int span = active.lastCharIndex - active.firstCharIndex;
        if (content[edge.firstCharIndex + span + 1] == content[lastCharIndex]) {
          break;
        }
        parentNode = edge.split(this, active);
      }

      // We didn't find a matching edge, so we create a new one, add
      // it to the tree at the parent node position, and insert it
      // into the hash table.  When we create a new node, it also
      // means we need to create a suffix link to the new node from
      // the last node we visited.
      Edge newEdge = new Edge(lastCharIndex, contentLastIndex, parentNode);
      newEdge.insert(this);
      if (lastParentNode > 0) {
        nodes[lastParentNode].suffixNode = parentNode;
      }
      lastParentNode = parentNode;

      // This final step is where we move to the next smaller suffix
      if (active.originNode == 0) {
        active.firstCharIndex++;
      } else {
        active.originNode = nodes[active.originNode].suffixNode;
      }
      active.canonize(this);
    }
    if (lastParentNode > 0) {
      nodes[lastParentNode].suffixNode = parentNode;
    }
    active.lastCharIndex++;  //Now the endpoint is the next active point
    active.canonize(this);
  }

  // This routine prints out the contents of the suffix tree at the end of the program by walking through
  // the hash table and printing out all used edges. It would be really great if I had some code that
  // will print out the tree in a graphical fashion, but I don't!
  public void printTo(PrintStream out) {
    out.println("\tStart \tEnd \tSuf \tFirst \tLast \tString");
    for (int j = 0; j < HASH_TABLE_SIZE; j++) {
      Edge s = edges[j];
      if (s.startNode == -1) {
        continue;
      }

      out.print("\t" + s.startNode + " " +
              "\t\t" + s.endNode + " " +
              "\t\t" + nodes[s.endNode].suffixNode + " " +
              "\t\t" + s.firstCharIndex + " " +
              "\t\t" + s.lastCharIndex + " " +
              "\t\t");

      final int lastCharIndex = Math.min(s.lastCharIndex, contentLastIndex);
      for (int l = s.firstCharIndex; l <= lastCharIndex; l++) {
        out.print(content[l]);
      }
      out.println();
      out.flush();
    }
  }
}
