package eu.interedition.collatex.suffixtree;

/**
 * A suffix tree implementation using Ukkonen's algorithm capable of generating a generialised suffix tree.
 * 
 * The type of both <i>character</i> and the <i>word</i> can be specified, and we call these <i>items</i> 
 * and <i>sequences</i> respectively.
 * 
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 * 
 * @param <I>
 *            The type of the item within the sequence.
 * @param <S>
 * 			  The sequence type, which must iterate over items of type <code>I</code>
 */
public class SuffixTree<I,S extends Iterable<I>> {

	private final Node<I,S> root;
	private final Sequence<I,S> sequence;

	private Suffix<I,S> suffix;
	private final ActivePoint<I,S> activePoint;
	private int currentEnd = 0;
	private int insertsThisStep = 0;
	private Node<I,S> lastNodeInserted = null;
	
	/**
	 * Constructs an empty suffix tree.
	 */
	public SuffixTree(){
		sequence = new Sequence<>();
		root = new Node<>(null, this.sequence, this);
		activePoint = new ActivePoint<>(root);
	}
	
	/**
	 * Construct and represent a suffix tree representation of the given
	 * sequence using Ukkonen's algorithm.
	 * 
	 * @param sequenceArray
	 *            the array of items for which we are going to generate a suffix
	 *            tree.
	 */
	public SuffixTree(S sequenceArray) {
		sequence = new Sequence<>(sequenceArray);
		root = new Node<>(null, this.sequence, this);
		activePoint = new ActivePoint<>(root);
		suffix = new Suffix<>(0, 0, this.sequence);
		extendTree(0,sequence.getLength());
	}
	
	/**
	 * Add a sequence to the suffix tree. It is immediately processed
	 * and added to the tree. 
	 * @param sequence A sequence to be added.
	 */
	public void add(S sequence){
		int start = currentEnd;
		this.sequence.add(sequence);
		suffix = new Suffix<>(currentEnd,currentEnd,this.sequence);
		activePoint.setPosition(root, null, 0);
		extendTree(start, this.sequence.getLength());
	}

	private void extendTree(int from, int to) {
		for (int i = from; i < to; i++){
			suffix.increment();
			insertsThisStep = 0;
			insert(suffix);
			currentEnd++;
		}
	}	
	

	/**
	 * Inserts the given suffix into this tree.
	 * 
	 * @param suffix
	 *            The suffix to insert.
	 */
	void insert(Suffix<I, S> suffix) {
		if (activePoint.isNode()) {
			Node<I, S> node = activePoint.getNode();
			node.insert(suffix, activePoint);
		} else if (activePoint.isEdge()) {
			Edge<I,S> edge = activePoint.getEdge();
			edge.insert(suffix, activePoint);
		}
	}

	/**
	 * Retrieves the point in the sequence for which all proceeding item have
	 * been inserted into the tree.
	 * 
	 * @return The index of the current end point of tree.
	 */
	int getCurrentEnd() {
		return currentEnd;
	}

	/**
	 * Retrieves the root node for this tree.
	 * 
	 * @return The root node of the tree.
	 */
	Node<I,S> getRoot() {
		return root;
	}

	/**
	 * Increments the inserts counter for this step.
	 */
	void incrementInsertCount() {
		insertsThisStep++;
	}

	/**
	 * Indecates if there have been inserts during the current step.
	 * 
	 * @return
	 */
	boolean isNotFirstInsert() {
		return insertsThisStep > 0;
	}

	/**
	 * Retrieves the last node to be inserted, null if none has.
	 * 
	 * @return The last node inserted or null.
	 */
	Node<I,S> getLastNodeInserted() {
		return lastNodeInserted;
	}

	/**
	 * Sets the last node inserted to the supplied node.
	 * 
	 * @param node
	 *            The node representing the last node inserted.
	 */
	void setLastNodeInserted(Node<I,S> node) {
		lastNodeInserted = node;
	}

	/**
	 * Sets the suffix link of the last inserted node to point to the supplied
	 * node. This method checks the state of the step and only applies the
	 * suffix link if there is a previous node inserted during this step. This
	 * method also set the last node inserted to the supplied node after
	 * applying any suffix linking.
	 * 
	 * @param node
	 *            The node to which the last node inserted's suffix link should
	 *            point to.
	 */
	void setSuffixLink(Node<I,S> node) {
		if (isNotFirstInsert()) {
			lastNodeInserted.setSuffixLink(node);
		}
		lastNodeInserted = node;
	}

	@Override
	public String toString() {
		return Utils.printTreeForGraphViz(this);
	}
	
	Sequence<I,S> getSequence(){
		return sequence;
	}
}