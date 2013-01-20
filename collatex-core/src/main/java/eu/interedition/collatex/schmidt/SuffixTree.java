package eu.interedition.collatex.schmidt;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
class SuffixTree<T> {

  final Comparator<T> comparator;
  final Comparator<Integer> sourceComparator;
  final T[] source;
  final Node root;

  static <T> SuffixTree<T> build(Comparator<T> comparator, T... source) {
    return new SuffixTree<T>(comparator, source).build();
  }

  private SuffixTree(Comparator<T> comparator, T... source) {
    this.comparator = comparator;
    this.sourceComparator = new SentinelAwareComparator(comparator);
    this.source = source;
    this.root = new Node();
  }

  public Cursor cursor() {
    return new Cursor();
  }

  public Iterable<EquivalenceClass> match(final Iterable<T> str) {
    return new Iterable<EquivalenceClass>() {
      @Override
      public Iterator<EquivalenceClass> iterator() {
        return new AbstractIterator<EquivalenceClass>() {

          Cursor cursor = cursor();
          final Iterator<T> it = str.iterator();

          @Override
          protected EquivalenceClass computeNext() {
            if (it.hasNext()) {
              cursor = cursor.move(it.next());
              return (cursor == null ? endOfData() : cursor.matchedClass());
            }
            return endOfData();
          }
        };
      }
    };
  }


  private SuffixTree<T> build() {
    for (int suffixStart = 0; suffixStart <= source.length; suffixStart++) {
      root.addSuffix(suffixStart);
    }
    compactNodes(root);
    return this;
  }

  private void compactNodes(Node node) {
    for (Node child : node.children) {
      while (child.children.size() == 1) {
        final Node firstGrandChild = child.children.iterator().next();
        child.incomingLabel.add(firstGrandChild.incomingLabel.getFirst());
        child.children = firstGrandChild.children;
        for (Node formerGrandchild : child.children) {
          formerGrandchild.parent = child;
        }
      }
      compactNodes(child);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Deque<Node> nodes = new ArrayDeque<Node>(Collections.singleton(root));
    while (!nodes.isEmpty()) {
      final Node node = nodes.remove();
      sb.append(Strings.repeat("\t", node.depth())).append(node).append("\n");
      for (Node child : node.children) {
        nodes.addFirst(child);
      }
    }
    return sb.toString();
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  class Node {

    final LinkedList<EquivalenceClass> incomingLabel;

    Node parent;
    List<Node> children = new ArrayList<Node>();

    public Node(Node parent, int firstIndex) {
      this.parent = parent;
      this.incomingLabel = Lists.newLinkedList(Collections.singleton(new EquivalenceClass(firstIndex)));
    }

    public Node() {
      this.parent = null;
      this.incomingLabel = null;
    }


    public int depth() {
      int depth = 0;
      for (Node parent = this.parent; parent != null; parent = parent.parent) {
        depth++;
      }
      return depth;
    }

    public void addSuffix(int start) {
      addSuffix(this, start);
    }

    private Node addSuffix(Node node, int start) {
      for (Node child : node.children) {
        EquivalenceClass childClass = child.incomingLabel.getFirst();
        if (childClass.isMember(start)) {
          childClass.add(start);
          start++;
          if (start == (source.length + 1)) {
            return child;
          }
          return addSuffix(child, start);
        }
      }
      while (start <= source.length) {
        Node child = new Node(node, start);
        node.children.add(child);
        node = child;
        start++;
      }
      return node;
    }

    @Override
    public String toString() {
      return Iterables.toString(incomingLabel == null ? Collections.emptySet() : incomingLabel);
    }
  }

  class EquivalenceClass implements Comparable<EquivalenceClass> {

    int[] members = new int[2];
    int length = 0;

    EquivalenceClass(int first) {
      members[length++] = first;
    }

    void add(int member) {
      if (length == members.length) {
        members = Arrays.copyOf(members, members.length * 2);
      }
      members[length++] = member;
    }

    boolean isMember(int index) {
      return sourceComparator.compare(index, members[0]) == 0;
    }

    public boolean isMember(T symbol) {
      return (members[0] == source.length ? false : comparator.compare(symbol, source[members[0]]) == 0);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof SuffixTree<?>.EquivalenceClass) {
        return members[0] == ((EquivalenceClass)obj).members[0];
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return members[0];
    }

    @Override
    public int compareTo(EquivalenceClass o) {
      return (members[0] - o.members[0]);
    }

    @Override
    public String toString() {
      return "{" + Joiner.on(", ").join(new AbstractIterator<String>() {
        private int mc = 0;
        @Override
        protected String computeNext() {
          if (mc == length) {
            return endOfData();
          }

          final int member = members[mc++];
          return "<[" + member + "] " + (member == source.length ? "$" : source[member].toString()) + ">";
        }
      }) + "}";
    }

  }

  class SentinelAwareComparator implements Comparator<Integer> {

    final Comparator<T> comparator;

    SentinelAwareComparator(Comparator<T> comparator) {
      this.comparator = comparator;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
      if (o1 == source.length || o2 == source.length) {
        return (o2 - o1);
      }
      return comparator.compare(source[o1], source[o2]);
    }
  }

  public class Cursor {
    final Node node;
    final int offset;

    Cursor() {
      this(root, 0);
    }

    Cursor(Node node, int offset) {
      this.node = node;
      this.offset = offset;
    }

    public Cursor move(T symbol) {
      if (node.incomingLabel == null || (offset + 1) == node.incomingLabel.size()) {
        for (Node child : node.children) {
          final Cursor next = new Cursor(child, 0);
          if (next.matchedClass().isMember(symbol)) {
            return next;
          }
        }
        return null;
      }
      return (node.incomingLabel.get(offset + 1).isMember(symbol) ? new Cursor(node, offset + 1) : null);
    }

    EquivalenceClass matchedClass() {
      return node.incomingLabel.get(offset);
    }
  }
}
