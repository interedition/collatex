/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.medite;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
class SuffixTree<T> {

    final Comparator<T> comparator;
    final Comparator<Integer> sourceComparator;
    final T[] source;
    final Node root;

    @SafeVarargs
    static <T> SuffixTree<T> build(Comparator<T> comparator, T... source) {
        return new SuffixTree<>(comparator, source).build();
    }

    @SafeVarargs
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
        return () -> new Iterator<EquivalenceClass>() {

            final Iterator<T> it = str.iterator();
            Optional<Cursor> cursor = Optional.ofNullable(it.hasNext() ? cursor().move(it.next()) : null);

            @Override
            public boolean hasNext() {
                return cursor.isPresent();
            }

            @Override
            public EquivalenceClass next() {
                final EquivalenceClass next = cursor.get().matchedClass();
                cursor = Optional.ofNullable(it.hasNext() ? cursor.get().move(it.next()) : null);
                return next;
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
        final Deque<Node> nodes = new ArrayDeque<>(Collections.singleton(root));
        while (!nodes.isEmpty()) {
            final Node node = nodes.remove();
            sb.append(IntStream.range(0, node.depth()).mapToObj(i -> "\t").collect(Collectors.joining())).append(node).append("\n");
            node.children.forEach(nodes::addFirst);
        }
        return sb.toString();
    }

    /**
     * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
     */
    class Node {

        final LinkedList<EquivalenceClass> incomingLabel;

        Node parent;
        List<Node> children = new ArrayList<>();

        public Node(Node parent, int firstIndex) {
            this.parent = parent;
            this.incomingLabel = new LinkedList<>(Collections.singleton(new EquivalenceClass(firstIndex)));
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
            return Optional.ofNullable(incomingLabel).map(label -> label.stream().map(Object::toString).collect(Collectors.joining(", "))).orElse("");
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
            return (members[0] != source.length && comparator.compare(symbol, source[members[0]]) == 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof SuffixTree<?>.EquivalenceClass) {
                return members[0] == ((SuffixTree<?>.EquivalenceClass) obj).members[0];
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
            return String.format("{%s}", Arrays.stream(members, 0, length)
                .mapToObj(member -> "<[" + member + "] " + (member == source.length ? "$" : source[member].toString()) + ">")
                .collect(Collectors.joining(", ")));
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
