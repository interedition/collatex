package eu.interedition.collatex.suffixtree;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @param <T>
 * @param <S>
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 */
public class Cursor<T, S extends Iterable<T>> {

    private final SuffixTree<T, S> tree;
    private Node<T, S> node;
    private Edge<T, S> edge;
    private int length;

    Cursor(SuffixTree<T, S> tree) {
        this.tree = tree;
        node = tree.getRoot();
        edge = null;
        length = 0;
    }

    boolean proceedTo(T item) {
        if (edge == null) {
            Edge<T, S> tmpEdge = node.getEdgeStarting(item);
            if (tmpEdge != null) {
                edge = tmpEdge;
                length = 1;
                return true;
            }
            return false;
        } else if (edge.getLength() > length) {
            T nextItem = edge.getItemAt(length);
            if (nextItem != null && item.equals(nextItem)) {
                length++;
                return true;
            }
            return false;
        } else {
            Node<T, S> terminal = edge.getTerminal();
            if (terminal == null)
                return false;
            Edge<T, S> tmpEdge = terminal.getEdgeStarting(item);
            if (tmpEdge != null) {
                edge = tmpEdge;
                length = 1;
                node = terminal;
                return true;
            }
            return false;
        }
    }

    Collection<SequenceTerminal<S>> getSequenceTerminals() {
        if (edge == null) {
            return node.getSuffixTerminals();
        }

        if ((edge.getLength() - 1 == length && !edge.isTerminating())//
                || (edge.getItemAt(length).getClass().equals(SequenceTerminal.class)) //
                        && !edge.isTerminating()//
        ) {
            Object seqTerminal = edge.getItemAt(length);
            @SuppressWarnings("unchecked")
            SequenceTerminal<S> term = (SequenceTerminal<S>) seqTerminal;
            Collection<SequenceTerminal<S>> collection = new HashSet<>();
            collection.add(term);
            return collection;
        }
        Node<T, S> terminal = edge.getTerminal();
        if (terminal == null)
            return Collections.emptySet();

        Collection<Edge<T, S>> edges = terminal.getEdges();
        Collection<SequenceTerminal<S>> returnCollection = new HashSet<>();
        for (Edge<T, S> edge : edges) {
            Object o = edge.getStartItem();
            if (o.getClass().equals(SequenceTerminal.class)) {
                @SuppressWarnings("unchecked")
                SequenceTerminal<S> returnTerminal = (SequenceTerminal<S>) o;
                returnCollection.add(returnTerminal);
            }
        }
        return returnCollection;
    }

    void returnToRoot() {
        node = tree.getRoot();
        edge = null;
        length = 0;
    }

}
