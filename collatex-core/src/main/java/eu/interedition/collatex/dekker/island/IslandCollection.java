/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.dekker.island;

import eu.interedition.collatex.VariantGraph;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// @author: Ronald Haentjens Dekker
// Unselected islands reside in the islandPriorityQueue.
// Selected islands reside in the fixedIsland Archipelago.
// Group the islands together by size;
// islands may change after commit islands
public class IslandCollection implements IslandSelection {
    Logger LOG = Logger.getLogger(IslandCollection.class.getName());
    private final PriorityQueue<Island> islandPriorityQueue;
    private final Archipelago fixedIslands;
    //this fields are needed for the locking of table cells
    private final BitSet fixedRows;
    private final Set<VariantGraph.Vertex> fixedVertices;
    private final Comparator<Island> comparator = new IslandSizeComparator();

    public IslandCollection(Set<Island> islands) {
        fixedRows = new BitSet();
        fixedVertices = new HashSet<>();
        this.fixedIslands = new Archipelago();
        islandPriorityQueue = new PriorityQueue<>(comparator);
        islandPriorityQueue.addAll(islands);
    }

    /*
     * Return whether a coordinate overlaps with an already committed coordinate
     */
    @Override
    public boolean doesCoordinateOverlapWithCommittedCoordinate(Coordinate coordinate) {
        return fixedRows.get(coordinate.row) || fixedVertices.contains(coordinate.match.getVertex());
    }

    /*
     * Return whether an island overlaps with an already committed island
     */
    @Override
    public boolean isIslandPossibleCandidate(Island island) {
        for (Coordinate coordinate : island) {
            if (doesCoordinateOverlapWithCommittedCoordinate(coordinate)) return false;
        }
        return true;
    }

    /*
     * Commit an island
     * Island will be part of the final alignment
     */
    @Override
    public void addIsland(Island isl) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "adding island: '{0}'", isl);
        }
        for (Coordinate coordinate : isl) {
            fixedRows.set(coordinate.row);
            fixedVertices.add(coordinate.match.getVertex());
        }
        fixedIslands.add(isl);
    }

    @Override
    public boolean doesCandidateLayOnVectorOfCommittedIsland(Island island) {
        Coordinate leftEnd = island.getLeftEnd();
        return fixedIslands.getIslandVectors().contains(leftEnd.row - leftEnd.column);
    }

    @Override
    public int size() {
        return fixedIslands.size();
    }

    @Override
    public List<Island> getIslands() {
        return fixedIslands.getIslands();
    }

    @Override
    public boolean containsCoordinate(int row, int column) {
        return fixedIslands.containsCoordinate(row, column);
    }

    @Override
    public List<Island> getPossibleIslands() {
        List<Island> possibleIslands = new ArrayList<>();
        while (possibleIslands.isEmpty() && !islandPriorityQueue.isEmpty()) {
            Island highestRated = islandPriorityQueue.poll();
            // LOG.fine( "Highest rated is: "+highestRated+ " "+highestRated.getDepth());
            // add highest rated
            possibleIslands.add(highestRated);
            // check whether there are other islands with the same rating
            while (!islandPriorityQueue.isEmpty() && comparator.compare(highestRated, islandPriorityQueue.peek()) == 0) {
                // LOG.fine("Same prio is: " + islandPriorityQueue.peek() + " " + islandPriorityQueue.peek().getDepth());
                possibleIslands.add(islandPriorityQueue.poll());
            }
            // check whether the selected islands are possible
            checkPossibleIslandsForRightOverlap(possibleIslands);
        }
        return possibleIslands;
    }

    // left overlap is not allowed; the whole island is discarded
    // right overlap is allowed; the island is removed from the queue and a new, smaller island is added
    // complete overlap or no overlap occurs most often
    // so the code is optimized to deal with these cases the fastest
    // complete overlap can be determined with one check
    // no overlap required two checks
    // partial overlap requires more, but since that doesn't happen often, it is not that bad
    //TODO: hard to make a unit test for the partial overlap case when islands are prioritized by size instead of depth!
    //TODO: There is an integration test for this in Darwin paragraph 1 (Also)
    private void checkPossibleIslandsForRightOverlap(List<Island> possibleIslands) {
        Iterator<Island> candidates = possibleIslands.iterator();
        while (candidates.hasNext()) {
            Island island = candidates.next();
            // check whether there is complete overlap
            // if left end coordinate is not available there is no use in checking this island any longer
            if (doesCoordinateOverlapWithCommittedCoordinate(island.getLeftEnd())) {
                candidates.remove();
                continue;
            }
            // if right end coordinate is also available it means the whole island is available
            if (!doesCoordinateOverlapWithCommittedCoordinate(island.getRightEnd())) {
                // no further check necessary
                continue;
            }
            // remove this candidate from the possible islands
            candidates.remove();
            // partial overlap; find the starting point of the conflict
            Island smaller = findConflictingCoordinateAndCreateSmallerIslandSplitAtConflictingCoordinate(island);
            // add the smaller island to the priority queue
            // LOG.fine("Conflict detected! We add a smaller island! "+smaller);
            islandPriorityQueue.add(smaller);
        }
    }

    private Island findConflictingCoordinateAndCreateSmallerIslandSplitAtConflictingCoordinate(Island island){
        // create a new island which contains the coordinates up to the overlapping coordinate.
        Island smaller = new Island(island.getBlockInstance());
        for (Coordinate coordinate : island) {
            if (doesCoordinateOverlapWithCommittedCoordinate(coordinate)) {
                return smaller;
            }
            smaller.add(coordinate);
        }
        throw new RuntimeException("Expected a conflict! This should never happen!");
    }
}