package eu.interedition.collatex.dekker.island;

import java.util.Comparator;

/**
 * Created by ronalddekker on 09/10/15.
 */
public class IslandSizeComparator implements Comparator<Island> {
    @Override
    public int compare(Island o1, Island o2) {
        return o2.size() - o1.size();
    }
}
