package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.dekker.astar.Cost;

/**
 * Created by ronalddekker on 23/11/15.
 */
public class DecisionCost extends Cost<DecisionCost> {
    private int alignedTokens;

    public DecisionCost() {
        this.alignedTokens = 0;
    }

    @Override
    protected DecisionCost plus(DecisionCost other) {
        return null;
    }

    @Override
    public int compareTo(DecisionCost o) {
        return 0;
    }
}
