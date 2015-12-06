package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.dekker.island.Island;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by ronalddekker on 24/11/15.
 */
public class DecisionTree {

    private List<Island> phraseMatchesOnGraphOrder;
    private List<Island> phraseMatchesOnWitnessOrder;
    private DecisionNode root;

    public DecisionTree(List<Island> phraseMatchesBasedOnGraphOrder, List<Island> phraseMatchesOnWitnessOrder) {
        phraseMatchesOnGraphOrder = phraseMatchesBasedOnGraphOrder;
        this.phraseMatchesOnWitnessOrder = phraseMatchesOnWitnessOrder;
        this.root = new DecisionNode(this);
    }

    public DecisionNode getRoot() {
        return root;
    }

    public Island getIslandOnGraphPosition(int positionGraph) {
        return phraseMatchesOnGraphOrder.get(positionGraph);
    }

    public Island getIslandOnWitnessPosition(int positionWitness) {
        return phraseMatchesOnWitnessOrder.get(positionWitness);
    }

    //TODO: implementation is unfinished
    List<DecisionNode> getChildren(DecisionNode current) {
        // check of we aan het einde zijn gekomen van een van de twee lijsten
        if (current.positionGraph == phraseMatchesOnGraphOrder.size()) {
            System.out.println("hmm");
            return null;
        }
        if (current.isWitnessEnd()) {
            System.out.println("interesting");
            return null;
        }

        return null;
    }

    public boolean isNodeAtGraphEnd(DecisionNode decisionNode) {
        return decisionNode.positionGraph == phraseMatchesOnGraphOrder.size();
    }

    //TODO; could move to decision node completely
    public boolean isNodeAtwitnessEnd(DecisionNode decisionNode) {
        return !decisionNode.witnessIterator.hasNext();
    }

    public ListIterator<Island> getWitnessIterator() {
        return phraseMatchesOnWitnessOrder.listIterator();
    }

    public ListIterator<Island> getWitnessIterator(int index) {
        return phraseMatchesOnWitnessOrder.listIterator(index);
    }
}
