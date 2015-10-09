package eu.interedition.collatex.dekker.legacy;

import eu.interedition.collatex.dekker.island.Island;

import java.util.Comparator;

/**
 * Created by ronalddekker on 09/10/15.
 */
public class IslandPositionComparator implements Comparator<Island> {
    @Override
    public int compare(Island o1, Island o2) {
        return o1.getLeftEnd().compareTo(o2.getLeftEnd());
    }
}
