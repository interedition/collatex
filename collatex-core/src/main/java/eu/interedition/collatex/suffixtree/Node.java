package eu.interedition.collatex.suffixtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @param <T>
 * @param <S>
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 */
class Node<T, S extends Iterable<T>> implements Iterable<Edge<T, S>> {
    private final Map<T, Edge<T, S>> edges = new HashMap<>();
    private final Edge<T, S> incomingEdge;
    private Set<SequenceTerminal<S>> sequenceTerminals = new HashSet<>();
    private final Sequence<T, S> sequence;
    private final SuffixTree<T, S> tree;
    private Node<T, S> link = null;

    /**
     * Create a new node, for the supplied tree and sequence.
     *
     * @param incomingEdge The parent edge, unless this is a root node.
     * @param sequence     The sequence this tree is indexing.
     * @param tree         The tree to which this node belongs.
     */
    Node(Edge<T, S> incomingEdge, Sequence<T, S> sequence, SuffixTree<T, S> tree) {
        this.incomingEdge = incomingEdge;
        this.sequence = sequence;
        this.tree = tree;
    }

    /**
     * Inserts the suffix at the given active point.
     *
     * @param suffix      The suffix to insert.
     * @param activePoint The active point to insert it at.
     */
    @SuppressWarnings("unchecked")
    void insert(Suffix<T, S> suffix, ActivePoint<T, S> activePoint) {
        Object item = suffix.getEndItem();

        if (edges.containsKey(item)) {
            if (tree.isNotFirstInsert() && activePoint.getNode() != tree.getRoot())
                tree.setSuffixLink(activePoint.getNode());
            activePoint.setEdge(edges.get(item));
            activePoint.incrementLength();
        } else {
            saveSequenceTerminal(item);
            Edge<T, S> newEdge = new Edge<>(suffix.getEndPosition() - 1, this,
                    sequence, tree);
            edges.put((T) suffix.getEndItem(), newEdge);
            suffix.decrement();
            activePoint.updateAfterInsert(suffix);

            if (tree.isNotFirstInsert() && !this.equals(tree.getRoot())) {
                tree.getLastNodeInserted().setSuffixLink(this);
            }
            if (suffix.isEmpty()) {
            }
            else
                tree.insert(suffix);
        }
    }

    private void saveSequenceTerminal(Object item) {
        if (item.getClass().equals(SequenceTerminal.class)) {
            @SuppressWarnings("unchecked")
            SequenceTerminal<S> terminal = (SequenceTerminal<S>) item;
            sequenceTerminals.add(terminal);
        }
    }

    /**
     * Inserts the given edge as a child of this node. The edge must not already
     * exist as child or an IllegalArgumentException will be thrown.
     *
     * @param edge The edge to be inserted.
     * @throws IllegalArgumentException This is thrown when the edge already exists as an out bound
     *                                  edge of this node.
     */
    void insert(Edge<T, S> edge) {
        if (edges.containsKey(edge.getStartItem()))
            throw new IllegalArgumentException("Item " + edge.getStartItem()
                + " already exists in node " + toString());
        edges.put(edge.getStartItem(), edge);
    }

    /**
     * Retrieves the edge starting with item or null if none exists.
     *
     * @param item
     * @return The edge extending from this node starting with item.
     */
    Edge<T, S> getEdgeStarting(Object item) {
        return edges.get(item);
    }

    /**
     * True if the node has a suffix link extending from it.
     *
     * @return True if node has suffix link. False if not.
     */
    boolean hasSuffixLink() {
        return link != null;
    }

    /**
     * Gets the number of edges extending from this node.
     *
     * @return The count of the number edges extending from this node.
     */
    int getEdgeCount() {
        return edges.size();
    }

    /**
     * @return An iterator which iterates over the child edges. No order is
     * guaranteed.
     */
    public Iterator<Edge<T, S>> iterator() {
        return edges.values().iterator();
    }

    /**
     * @return The node that this nodes suffix link points to if it has one.
     * Null if not.
     */
    Node<T, S> getSuffixLink() {
        return link;
    }

    /**
     * Sets the suffix link of this node to point to the supplied node.
     *
     * @param node The node this suffix link should point to.
     */
    void setSuffixLink(Node<T, S> node) {
        link = node;
    }

    @Override
    public String toString() {
        if (incomingEdge == null)
            return "root";
        else {
            return "end of edge [" + incomingEdge.toString() + "]";
        }
    }

    public Collection<SequenceTerminal<S>> getSuffixTerminals() {
        return sequenceTerminals;
    }

    public Collection<Edge<T, S>> getEdges() {
        return edges.values();
    }
}
