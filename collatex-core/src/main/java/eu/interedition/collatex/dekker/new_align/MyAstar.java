package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.dekker.astar.AstarAlgorithm;

/**
 * Created by ronalddekker on 30/11/15.
 */
class MyAStar extends AstarAlgorithm<DecisionNode, DecisionCost> {

    private final DecisionTree tree;

    public MyAStar(DecisionTree tree) {
        super();
        this.tree = tree;
    }

    public void MyAStar() {

    }

    public void find() {
        DecisionNode startNode = tree.getRoot();
        DecisionCost initialCost = new DecisionCost();
        this.aStar(startNode, initialCost);

    }
    @Override
    protected boolean isGoal(DecisionNode node) {
        return false;
    }

    @Override
    protected Iterable<DecisionNode> neighborNodes(DecisionNode current) {
        return null;
    }

    @Override
    protected DecisionCost heuristicCostEstimate(DecisionNode node) {
        return null;
    }

    @Override
    protected DecisionCost distBetween(DecisionNode current, DecisionNode neighbor) {
        return null;
    }
}

