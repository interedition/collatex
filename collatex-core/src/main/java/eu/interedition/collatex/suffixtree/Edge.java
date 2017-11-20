package eu.interedition.collatex.suffixtree;

import java.util.Iterator;

/**
 * @param <T>
 * @param <S>
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 */
class Edge<T, S extends Iterable<T>> implements Iterable<T> {
    private final int start;
    private int end = -1;
    private final Node<T, S> parentNode;
    private final Sequence<T, S> sequence;

    private Node<T, S> terminal = null;
    private SuffixTree<T, S> tree = null;

    /**
     * Create a new <code>Edge</code> object.
     *
     * @param start    The position in the master sequence of the first item in this
     *                 suffix.
     * @param parent   The parent {@link Node}
     * @param sequence The master sequence which the {@link SuffixTree} indexes.
     * @param tree     The master {@link SuffixTree} containing the root element
     *                 which this edge is a child of.
     */
    Edge(int start, Node<T, S> parent, Sequence<T, S> sequence, SuffixTree<T, S> tree) {
        this.start = start;
        this.parentNode = parent;
        this.sequence = sequence;
        this.tree = tree;
    }

    /**
     * Checks to see if the edge starts with the given item.
     *
     * @param item The possible start item.
     * @return True if this edge starts with item. False if not.
     */
    boolean isStarting(Object item) {
        return sequence.getItem(start).equals(item);
    }

    /**
     * Insert the given suffix at the supplied active point.
     *
     * @param suffix      The suffix to insert.
     * @param activePoint The active point to insert it at.
     * @return
     */
    void insert(Suffix<T, S> suffix, ActivePoint<T, S> activePoint) {
        Object item = suffix.getEndItem();
        Object nextItem = getItemAt(activePoint.getLength());
        if (item.equals(nextItem)) {
            activePoint.incrementLength();
        } else {
            split(suffix, activePoint);
            suffix.decrement();
            activePoint.updateAfterInsert(suffix);

            if (suffix.isEmpty()) {
            }
            else
                tree.insert(suffix);
        }
    }

    /**
     * Splits the edge to enable the insertion of supplied suffix at the
     * supplied active point.
     *
     * @param suffix      The suffix to insert.
     * @param activePoint The active point to insert it at.
     */
    private void split(Suffix<T, S> suffix, ActivePoint<T, S> activePoint) {
        Node<T, S> breakNode = new Node<>(this, sequence, tree);
        Edge<T, S> newEdge = new Edge<>(suffix.getEndPosition() - 1, breakNode,
                sequence, tree);
        breakNode.insert(newEdge);
        Edge<T, S> oldEdge = new Edge<>(start + activePoint.getLength(),
                breakNode, sequence, tree);
        oldEdge.end = end;
        oldEdge.terminal = this.terminal;
        breakNode.insert(oldEdge);
        this.terminal = breakNode;
        end = start + activePoint.getLength();
        tree.setSuffixLink(breakNode);
        tree.incrementInsertCount();
    }

    /**
     * Gets the index of the true end of the edge.
     *
     * @return The index of the end item, of this edge, in the original
     * sequence.
     */
    int getEnd() {
        tree.getCurrentEnd();
        return end != -1 ? end : tree.getCurrentEnd();
    }

    /**
     * Tests if this edge is terminates at a node.
     *
     * @return True if this edge ends at a node. False if not.
     */
    boolean isTerminating() {
        return terminal != null;
    }

    /**
     * Retrieves the length of this edge.
     *
     * @return
     */
    int getLength() {
        int realEnd = getEnd();
        return realEnd - start;
    }

    /**
     * Retrieves the terminating node of this edge if it has any, null if not.
     *
     * @return The terminating node if any exists, null otherwise.
     */
    Node<T, S> getTerminal() {
        return terminal;
    }

    /**
     * Retrieves the item at given position within the current edge.
     *
     * @param position The index of the item to retrieve relative to the start of
     *                 edge.
     * @return The item at position.
     * @throws IllegalArgumentException when the position exceeds the length of the current edge.
     */
    @SuppressWarnings("unchecked")
    T getItemAt(int position) {
        if (position > getLength())
            throw new IllegalArgumentException("Index " + position
                + " is greater than " + getLength()
                + " - the length of this edge.");
        return (T) sequence.getItem(start + position);
    }

    /**
     * Retrieves the starting item of this edge.
     *
     * @return The item at index 0 of this edge.
     */
    @SuppressWarnings("unchecked")
    T getStartItem() {
        return (T) sequence.getItem(start);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < getEnd(); i++) {
            sb.append(sequence.getItem(i).toString()).append(", ");
            if (sequence.getItem(i).getClass().equals(SequenceTerminal.class))
                break;
        }
        return sb.toString();
    }

    /**
     * Retrieves an iterator that steps over the items in this edge.
     *
     * @return An iterator that walks this edge up to the end or terminating
     * node.
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentPosition = start;
            private boolean hasNext = true;

            public boolean hasNext() {
                return hasNext;
            }

            @SuppressWarnings("unchecked")
            public T next() {
                if (end == -1)
                    hasNext = !sequence.getItem(currentPosition).getClass().equals(SequenceTerminal.class);
                else
                    hasNext = currentPosition < getEnd() - 1;
                return (T) sequence.getItem(currentPosition++);
            }

            public void remove() {
                throw new UnsupportedOperationException(
                    "The remove method is not supported.");
            }
        };
    }
}
