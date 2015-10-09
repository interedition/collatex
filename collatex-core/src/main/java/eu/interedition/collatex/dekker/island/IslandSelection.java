package eu.interedition.collatex.dekker.island;

import java.util.List;

/**
 * Created by ronalddekker on 09/10/15.
 */
public interface IslandSelection {
    /*
     * Return whether a coordinate overlaps with an already committed coordinate
     */
    boolean doesCoordinateOverlapWithCommittedCoordinate(Coordinate coordinate);

    /*
     * Return whether an island overlaps with an already committed island
     */
    boolean isIslandPossibleCandidate(Island island);

    /*
     * Commit an island
     * Island will be part of the final alignment
     */
    void addIsland(Island isl);

    boolean doesCandidateLayOnVectorOfCommittedIsland(Island island);

    int size();

    List<Island> getIslands();

    boolean containsCoordinate(int row, int column);

    List<Island> getPossibleIslands();
}
