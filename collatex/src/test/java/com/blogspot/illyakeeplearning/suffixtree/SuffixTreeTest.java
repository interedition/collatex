/**
 * This is a Java-port of Mark Nelson's C++ implementation of Ukkonen's algorithm.
 * http://illya-keeplearning.blogspot.com/2009/04/suffix-trees-java-ukkonens-algorithm.html
 */

package com.blogspot.illyakeeplearning.suffixtree;

import org.junit.Test;

import static java.lang.System.err;
import static java.lang.System.out;

public class SuffixTreeTest {

  @Test
  public void simpleTree() {
    final SuffixTree st = SuffixTree.create("cacao");
    st.printTo(System.out);
    validate(st);
  }


  // The validation code consists of two routines.  All it does is traverse the entire tree.
  // walk_tree() calls itself recursively, building suffix strings up as it goes.
  // When walk_tree() reaches a leaf node, it checks
  // to see if the suffix derived from the tree matches the suffix starting
  // at the same point in the input text. If so, it tags that suffix as correct in the GoodSuffixes[] array.
  //  When the tree has been traversed, every entry in the GoodSuffixes array should have a value of 1.

  // In addition, the BranchCount[] array is updated while the tree is walked as well.
  // Every count in the array has the number of child edges emanating from that node.
  // If the node is a leaf node, the value is set to -1.  When the routine finishes,
  // every node should be a branch or a leaf.
  // The number of leaf nodes should match the number of suffixes (the length) of the input string.
  // The total number of branches from all nodes should match the node count.

  public static char CurrentString[] = new char[SuffixTree.MAX_LENGTH];
  public static byte GoodSuffixes[] = new byte[SuffixTree.MAX_LENGTH];
  public static byte BranchCount[] = new byte[SuffixTree.MAX_LENGTH * 2];

  public static void validate(SuffixTree st) {
    for (int i = 0; i < st.contentLastIndex; i++) {
      GoodSuffixes[i] = 0;
    }

    for (int i = 0; i < BranchCount.length; i++) {
      BranchCount[i] = 0;
    }

    walk_tree(st, 0, 0);
    int error = 0;
    for (int i = 0; i < st.contentLastIndex; i++) {
      if (GoodSuffixes[i] != 1) {
        out.println("Suffix " + i + " count wrong!");
        error++;
      }
    }
    if (error == 0) {
      out.println("All Suffixes present!");
    }
    int leaf_count = 0;
    int branch_count = 0;
    for (int i = 0; i < Node.Count; i++) {
      if (BranchCount[i] == 0) {
        out.println("Logic error on node " + i + ", not a leaf or internal node!");
      } else if (BranchCount[i] == -1) {
        leaf_count++;
      } else {
        branch_count += BranchCount[i];
      }
    }
    out.println("Leaf count : " + leaf_count + (leaf_count == (st.contentLastIndex + 1) ? " OK" : " Error!"));
    out.println("Branch count : " + branch_count + (branch_count == (Node.Count - 1) ? " OK" : " Error!"));
  }

  public static boolean walk_tree(SuffixTree st, int start_node, int last_char_so_far) {
    int edges = 0;
    for (int i = 0; i < 256; i++) {
      Edge edge = Edge.find(st, start_node, i);
      if (edge.startNode != -1) {
        if (BranchCount[edge.startNode] < 0) {
          err.println("Logic error on node " + edge.startNode);
        }
        BranchCount[edge.startNode]++;
        edges++;
        int l = last_char_so_far;
        for (int j = edge.firstCharIndex; j <= edge.lastCharIndex; j++) {
          CurrentString[l++] = st.content[j];
        }
        CurrentString[l] = '\0';
        if (walk_tree(st, edge.endNode, l)) {
          if (BranchCount[edge.endNode] > 0) {
            err.println("Logic error on node " + edge.endNode);
          }
          BranchCount[edge.endNode]--;
        }
      }
    }

    // If this node didn't have any child edges, it means we are at a leaf node,
    // and can check on this suffix.  We check to see if it matches the input string,
    // then tick off it's entry in the GoodSuffixes list.
    if (edges == 0) {
      out.print("Suffix : ");
      for (int m = 0; m < last_char_so_far; m++) {
        out.print(CurrentString[m]);
      }
      out.println();
      String curr = new String(CurrentString, 0, strlen(CurrentString));
      GoodSuffixes[curr.length() - 1]++;
      String comp = new String(st.content, st.contentLastIndex - curr.length() + 1, strlen(st.content) - (st.contentLastIndex - curr.length() + 1));
      out.println("comparing: " + comp + " to " + curr);
      if (!curr.equals(comp)) {
        out.println("Comparison failure!");
      }
      return true;
    } else {
      return false;
    }
  }

  public static int strlen(char[] chars) {
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '\0') {
        return i;
      }
    }
    return chars.length;
  }

}
