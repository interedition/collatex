package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.dekker.island.Island;

/**
 * Created by ronalddekker on 23/11/15.
 */
public class DecisionNode {

    private final DecisionTree tree;
    int positionGraph;
    int positionWitness;

    public DecisionNode(DecisionTree tree) {
        this.positionGraph = 0;
        this.positionWitness = 0;
        this.tree = tree;
    }

    public Island getGraphPhrase() {
        return tree.getIslandOnGraphPosition(positionGraph);
    }

    public Island getWitnessPhrase() {
        return tree.getIslandOnWitnessPosition(positionWitness);
    }
}
