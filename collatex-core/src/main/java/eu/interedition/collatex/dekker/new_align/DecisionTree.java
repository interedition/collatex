package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.dekker.island.Island;

import java.util.List;

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
        if (current.positionWitness == phraseMatchesOnWitnessOrder.size()) {
            System.out.println("interesting");
            return null;
        }

        return null;
    }
}
