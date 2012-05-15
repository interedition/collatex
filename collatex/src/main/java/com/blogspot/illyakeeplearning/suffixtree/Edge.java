/**
 * This is a Java-port of Mark Nelson's C++ implementation of Ukkonen's algorithm.
 * http://illya-keeplearning.blogspot.com/2009/04/suffix-trees-java-ukkonens-algorithm.html
 */

package com.blogspot.illyakeeplearning.suffixtree;

/**
 * Represents a string of characters.
 * <p/>
 * The suffix tree is made up of edges connecting nodes. Each edge represents a string of characters starting
 * at first_char_index and ending at last_char_index. Edges can be inserted and removed from a hash table,
 * based on the Hash() function defined here.  The hash table indicates an unused slot
 * by setting the start_node value to -1.
 */
public class Edge {
  public int firstCharIndex;
  public int lastCharIndex;
  public int endNode;
  public int startNode;

  /**
   * The default ctor for Edge just sets start_node to the invalid value.
   * This is done to guarantee that the hash table is initially filled with unused edges.
   */
  public Edge() {
    startNode = -1;
  }

  /**
   * I create new edges in the program while walking up the set of suffixes from the active point to the endpoint.
   * Each time I create a new edge, I also add a new node for its end point.
   * The node entry is already present in the Nodes[] array, and its suffix node is set to -1
   * by the default Node() ctor, so I don't have to do anything with it at this point.
   *
   * @param firstCharIndex
   * @param lastCharIndex
   * @param parentNode
   */
  public Edge(int firstCharIndex, int lastCharIndex, int parentNode) {
    this.firstCharIndex = firstCharIndex;
    this.lastCharIndex = lastCharIndex;
    this.startNode = parentNode;
    this.endNode = Node.Count++;
  }

  public int span() {
    return (lastCharIndex - firstCharIndex);
  }
  /**
   * A given edge gets a copy of itself inserted into the table with this function.
   * It uses a linear probe technique, which means in the case of a collision,
   * we just step forward through the table until we find the first unused slot.
   */
  public void insert(SuffixTree st) {
    int i = hash(startNode, st.content[firstCharIndex]);
    while (st.edges[i].startNode != -1) {
      i = ++i % SuffixTree.HASH_TABLE_SIZE;
    }
    st.edges[i] = this;
  }

  /**
   * Removing an edge from the hash table is a little more tricky.
   * You have to worry about creating a gap in the table that will make it impossible to find
   * other entries that have been inserted using a probe. Working around this means that
   * after setting an edge to be unused, we have to walk ahead in the table,
   * filling in gaps until all the elements can be found.
   * <p/>
   * Knuth, Sorting and Searching, Algorithm R, p. 527
   */
  public void remove(SuffixTree st) {
    int i = hash(startNode, st.content[firstCharIndex]);
    while (st.edges[i].startNode != startNode ||
            st.edges[i].firstCharIndex != firstCharIndex) {
      i = ++i % SuffixTree.HASH_TABLE_SIZE;
    }
    for (; ; ) {
      st.edges[i].startNode = -1;
      int j = i;
      for (; ; ) {
        i = ++i % SuffixTree.HASH_TABLE_SIZE;
        if (st.edges[i].startNode == -1) {
          return;
        }
        int r = hash(st.edges[i].startNode, st.content[st.edges[i].firstCharIndex]);
        if (i >= r && r > j) {
          continue;
        }
        if (r > j && j > i) {
          continue;
        }
        if (j > i && i >= r) {
          continue;
        }
        break;
      }
      st.edges[j] = st.edges[i];
    }
  }

  /**
   * When a suffix ends on an implicit node, adding a new character means I have to split an existing edge.
   * This function is called to split an edge at the point defined by the Suffix argument.
   * The existing edge loses its parent, as well as some of its leading characters.
   * The newly created edge descends from the original parent, and now has the existing edge as a child.
   * <p/>
   * Since the existing edge is getting a new parent and starting character,
   * its hash table entry will no longer be valid.  That's why it gets removed at the start of the function.
   * After the parent and start char have been recalculated, it is re-inserted.
   * <p/>
   * The number of characters stolen from the original node and given to the new node is equal to the number
   * of characters in the suffix argument, which is last - first + 1;
   *
   * @param s
   * @return
   */
  public int split(SuffixTree st, Suffix s) {
    remove(st);
    Edge new_edge = new Edge(firstCharIndex,
            firstCharIndex + s.lastCharIndex - s.firstCharIndex,
            s.originNode);
    new_edge.insert(st);
    st.nodes[new_edge.endNode].suffixNode = s.originNode;
    firstCharIndex += s.lastCharIndex - s.firstCharIndex + 1;
    startNode = new_edge.endNode;
    insert(st);
    return new_edge.endNode;
  }

  /**
   * The whole reason for storing edges in a hash table is that it makes this function fairly efficient.
   * When I want to find a particular edge leading out of a particular node, I call this function.
   * It locates the edge in the hash table, and returns a copy of it. If the edge isn't found,
   * the edge that is returned to the caller will have start_node set to -1, which is the value
   * used in the hash table to flag an unused entry.
   *
   * @param node
   * @param c
   * @return
   */
  public static Edge find(SuffixTree st, int node, int c) {
    int i = hash(node, c);
    for (; ; ) {
      if (st.edges[i].startNode == node) {
        if (c == st.content[st.edges[i].firstCharIndex]) {
          return st.edges[i];
        }
      }
      if (st.edges[i].startNode == -1) {
        return st.edges[i];
      }
      i = ++i % SuffixTree.HASH_TABLE_SIZE;
    }
  }

  /**
   * Edges are inserted into the hash table using this hashing function.
   *
   * @param node
   * @param c
   * @return
   */
  public static int hash(int node, int c) {
    return ((node << 8) + c) % SuffixTree.HASH_TABLE_SIZE;
  }
}
