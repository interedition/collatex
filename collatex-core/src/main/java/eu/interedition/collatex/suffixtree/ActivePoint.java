package eu.interedition.collatex.suffixtree;

/**
 * Represents the Active Point used in Ukonnen's algorithm. This consists of the
 * triple active node, active edge and active length, which is used to identify
 * the point at which the next insertion should be considered.
 *
 * @param <T>
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 */
class ActivePoint<T, S extends Iterable<T>> {

    private Node<T, S> activeNode;
    private Edge<T, S> activeEdge;
    private int activeLength;
    private final Node<T, S> root;

    /**
     * Initialize the active point to the root of a suffix tree. This sets the
     * active point to <code>{root,null,0}</code>
     *
     * @param root
     */
    ActivePoint(Node<T, S> root) {
        activeNode = root;
        activeEdge = null;
        activeLength = 0;
        this.root = root;
    }

    /**
     * Sets the active point to a new node, edge, length tripple.
     *
     * @param node
     * @param edge
     * @param length
     */
    void setPosition(Node<T, S> node, Edge<T, S> edge, int length) {
        activeNode = node;
        activeEdge = edge;
        activeLength = length;
    }

    /**
     * Sets the active edge.
     *
     * @param edge
     *            The edge to which we set the active edge.
     */
    void setEdge(Edge<T, S> edge) {
        activeEdge = edge;
    }

    /**
     * Increments the active length.
     */
    void incrementLength() {
        activeLength++;
        resetActivePointToTerminal();
    }

    /**
     * Decrements the active length.
     */
    void decrementLength() {
        if (activeLength > 0)
            activeLength--;
        resetActivePointToTerminal();
    }

    /**
     * @return True if the active point is the root node. False if not.
     */
    boolean isRootNode() {
        return activeNode.equals(root) && activeEdge == null && activeLength == 0;
    }

    /**
     * @return True if active point is on a node. False if not.
     */
    boolean isNode() {
        return activeEdge == null && activeLength == 0;
    }

    /**
     * Retrieves the active node.
     *
     * @return The active node.
     */
    Node<T, S> getNode() {
        return activeNode;
    }

    /**
     * @return True if the active point is on an edge. False if not.
     */
    boolean isEdge() {
        return activeEdge != null;
    }

    /**
     * Retrieves the current active edge.
     *
     * @return The active edge.
     */
    Edge<T, S> getEdge() {
        return activeEdge;
    }

    /**
     * Retrieves the current active length.
     *
     * @return The active length.
     */
    int getLength() {
        return activeLength;
    }

    /**
     * Resets the active point after an insert.
     *
     * @param suffix
     *            The remaining suffix to be inserted.
     */
    public void updateAfterInsert(Suffix<T, S> suffix) {
        if (activeNode == root && suffix.isEmpty()) {
            activeNode = root;
            activeEdge = null;
            activeLength = 0;
        } else if (activeNode == root) {
            Object item = suffix.getStart();
            activeEdge = root.getEdgeStarting(item);
            decrementLength();
            fixActiveEdgeAfterSuffixLink(suffix);
            if (activeLength == 0)
                activeEdge = null;
        } else if (activeNode.hasSuffixLink()) {
            activeNode = activeNode.getSuffixLink();
            findTrueActiveEdge();
            fixActiveEdgeAfterSuffixLink(suffix);
            if (activeLength == 0)
                activeEdge = null;
        } else {
            activeNode = root;
            findTrueActiveEdge();
            fixActiveEdgeAfterSuffixLink(suffix);
            if (activeLength == 0)
                activeEdge = null;
        }
    }

    /**
     * Deal with the case when we follow a suffix link but the active length is
     * greater than the new active edge length. In this situation we must walk
     * down the tree updating the entire active point.
     */
    private void fixActiveEdgeAfterSuffixLink(Suffix<T, S> suffix) {
        while (activeEdge != null && activeLength > activeEdge.getLength()) {
            activeLength = activeLength - activeEdge.getLength();
            activeNode = activeEdge.getTerminal();
            Object item = suffix.getItemXFromEnd(activeLength + 1);
            activeEdge = activeNode.getEdgeStarting(item);
        }
        resetActivePointToTerminal();
    }

    /**
     * Finds the edge instance who's start item matches the current active edge
     * start item but comes from the current active node.
     */
    private void findTrueActiveEdge() {
        if (activeEdge != null) {
            Object item = activeEdge.getStartItem();
            activeEdge = activeNode.getEdgeStarting(item);
        }
    }

    /**
     * Resizes the active length in the case where we are sitting on a terminal.
     *
     * @return true if reset occurs false otherwise.
     */
    private boolean resetActivePointToTerminal() {
        if (activeEdge != null && activeEdge.getLength() == activeLength && activeEdge.isTerminating()) {
            activeNode = activeEdge.getTerminal();
            activeEdge = null;
            activeLength = 0;
            return true;
        }
        return false;

    }

    @Override
    public String toString() {
        return "{" + activeNode.toString() + ", " + activeEdge + ", " + activeLength + "}";
    }
}
