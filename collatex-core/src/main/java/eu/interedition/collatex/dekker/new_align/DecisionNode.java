package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.island.Island;

import java.util.*;

/**
 * Created by ronalddekker on 23/11/15.
 */
public class DecisionNode {

    ListIterator<Island> graphIterator;
    ListIterator<Island> witnessIterator;
    private final DecisionNode parent;
    private final DecisionTree tree;
    private final List<Island> selected;
    private final List<Island> moved;

    public DecisionNode(DecisionTree tree) {
        this.graphIterator = tree.getGraphIterator(0);
        this.witnessIterator = tree.getWitnessIterator(0);
        this.tree = tree;
        this.parent = null;
        // Selected phrasematch should only be one
        selected = new ArrayList<>();
        moved = new ArrayList<>();
    }

    public DecisionNode(DecisionNode parent) {
        // Set iterator indices on the child node to the positions of the parent node and calculate from there
        this.graphIterator = parent.tree.getGraphIterator(parent.graphIterator.nextIndex());
        this.witnessIterator = parent.tree.getWitnessIterator(parent.witnessIterator.nextIndex());
        this.tree = parent.tree;
        this.parent = parent;
        // Selected phrasematch should only be one
        selected = new ArrayList<>();
        moved = new ArrayList<>();
    }

    public Island peekGraphPhrase() {
        return tree.getIslandOnGraphPosition(graphIterator.nextIndex());
    }

    public Island peekWitnessPhrase() {
        return tree.getIslandOnWitnessPosition(witnessIterator.nextIndex());
    }

    public Match getSelected() {
        return selected.get(0).getMatch(0);
    }

    public List<Island> getMoved() {
        return moved;
    }

    @Override
    public String toString() {
        return peekGraphPhrase()+"; "+ peekWitnessPhrase();
    }

    //TODO: should this method be public? I don't think so.
    public void select(Island phraseMatch) {
        System.out.println("selected: "+phraseMatch);
        selected.add(phraseMatch);
    }

    private void move(Island phraseMatch) {
        System.out.println("considered as moved: "+phraseMatch);
        moved.add(phraseMatch);
    }

    //TODO: this should eventually become a public method
    //NOTE: This stuff is still in an experimental state
    protected DecisionNode getDecisionNodeChildForGraphPhrase() {
        // create child node
        DecisionNode child2 = new DecisionNode(this);
        // move the pointer further to the next available phrase match
        Island selectedGraphPhraseMatch = child2.graphIterator.next();
        // move stuff
        child2.moveEverythingInPhraseMatchIteratorBefore(child2.witnessIterator, selectedGraphPhraseMatch);
        // select witness phrase match
        child2.select(selectedGraphPhraseMatch);
        // skip if possible and necessary
        // we have to keep track of the selected vertices and selected tokens to test this
        if (!child2.isGraphEnd()) {
            child2.skipToNextAvailablePhraseMatch(child2.graphIterator);
        }
        if (!child2.isWitnessEnd()) {
            child2.skipToNextAvailablePhraseMatch(child2.witnessIterator);
        }
        return child2;
    }



    //TODO: this should eventually become a public method
    //NOTE: This stuff is still in an experimental state
    protected DecisionNode getDecisionNodeChildForWitnessPhrase() {
        // create child node
        DecisionNode child2 = new DecisionNode(this);
        // move the pointer further to the next available phrase match
        Island selectedWitnessPhraseMatch = child2.witnessIterator.next();
        // move stuff
        child2.moveEverythingInPhraseMatchIteratorBefore(child2.graphIterator, selectedWitnessPhraseMatch);
        // select witness phrase match
        child2.select(selectedWitnessPhraseMatch);
        // skip if possible and necessary
        // we have to keep track of the selected vertices and selected tokens to test this
        if (!child2.isGraphEnd()) {
            child2.skipToNextAvailablePhraseMatch(child2.graphIterator);
        }
        if (!child2.isWitnessEnd()) {
            child2.skipToNextAvailablePhraseMatch(child2.witnessIterator);
        }
        return child2;
    }

    // at the end of the method call we are PAST the island to look for!
    private void moveEverythingInPhraseMatchIteratorBefore(ListIterator<Island> phraseMatchIterator, Island selectedPhraseMatch) {
        // move all the phrase matches before the selected phrase match
        // now find the position of the linked match in the other array
        //NOTE: this implementation is probably too simple; only checks coverage of moved parts underling.
        Set<VariantGraph.Vertex> vertices = new HashSet<>();
        BitSet positions = new BitSet();
        Island nextPhraseMatch = phraseMatchIterator.next();
        while(nextPhraseMatch != selectedPhraseMatch) {
            if (!vertices.contains(nextPhraseMatch.getMatch(0).vertex) && !positions.get(nextPhraseMatch.getLeftEnd().row)) {
                move(nextPhraseMatch);
                convertSinglePhraseMatch(vertices, positions, nextPhraseMatch);
            }
            nextPhraseMatch = phraseMatchIterator.next();
        }
    }

    private void skipToNextAvailablePhraseMatch(ListIterator<Island> phraseMatches) {
        // now we need to skip elements that are not available anymore
        // we can do this the ugly way
        // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
        FillAreaCovered fillAreaCovered = new FillAreaCovered().invoke();
        Set<VariantGraph.Vertex> vertices = fillAreaCovered.getVertices();
        BitSet positions = fillAreaCovered.getPositions();

        // check next phrase on witness order
        Island witnessPhrase = phraseMatches.next();
        //  check whether phrase is available
        //TODO: this check is too simple
        //if the first vertex and token are available it does not mean that the complete phrase
        //is available
        while (phraseMatches.hasNext() && vertices.contains(witnessPhrase.getMatch(0).vertex) || phraseMatches.hasNext() && positions.get(witnessPhrase.getLeftEnd().row)) {
            // skip phrase
            witnessPhrase = phraseMatches.next();
        }
        // the last returned witness phrase was allowed
        // rewind iterator
        phraseMatches.previous();
    }

    public boolean isGraphEnd() {
        return !graphIterator.hasNext();
    }

    public boolean isWitnessEnd() {
        return !witnessIterator.hasNext();
    }

    private class FillAreaCovered {
        private Set<VariantGraph.Vertex> vertices;
        private BitSet positions;

        public Set<VariantGraph.Vertex> getVertices() {
            return vertices;
        }

        public BitSet getPositions() {
            return positions;
        }

        public FillAreaCovered invoke() {
            // now we need to skip elements that are not available anymore
            // we can do this the ugly way
            // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
            vertices = new HashSet<>();
            positions = new BitSet();

            DecisionNode current = DecisionNode.this;
            do {
                convert(vertices, positions, current.moved);
                convert(vertices, positions, current.selected);
                // System.out.println(vertices);
                // System.out.println(positions);
                current = current.parent;
            }  while (current!=null);
            return this;
        }
    }

    private static void convert(Set<VariantGraph.Vertex> vertices, BitSet positions, List<Island> phrases) {
        for (Island taken : phrases) {
            convertSinglePhraseMatch(vertices, positions, taken);
        }
    }

    private static void convertSinglePhraseMatch(Set<VariantGraph.Vertex> vertices, BitSet positions, Island phraseMatch) {
        for (int i=0; i < phraseMatch.size(); i++) {
            // Just storing and testing against the matches is not good enough
            // matches can overlap with each other..
            // storing the rank instead of the vertex is also not good enough
            // there can be multiple vertices on the same rank
            // locking one vertex, does not lock the other(s)
            vertices.add(phraseMatch.getMatch(i).vertex);
            positions.set(phraseMatch.getLeftEnd().row+i);
        }
    }
}
