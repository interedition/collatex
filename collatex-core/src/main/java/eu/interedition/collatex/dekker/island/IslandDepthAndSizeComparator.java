package eu.interedition.collatex.dekker.island;

import java.util.Comparator;

/**
 * Created by ronalddekker on 09/10/15.
 */
public class IslandDepthAndSizeComparator implements Comparator<Island> {
    @Override
    public int compare(Island o1, Island o2) {
        int depthComparison = o2.getDepth() - o1.getDepth();
        if (depthComparison != 0) {
            return depthComparison;
        }
        return o2.size() - o1.size();
    }
}
