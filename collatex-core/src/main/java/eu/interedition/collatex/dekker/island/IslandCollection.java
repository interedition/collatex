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
    private final Set<Integer> fixedRows;
    private final Set<VariantGraph.Vertex> fixedVertices;
    private final Comparator<Island> comparator = new IslandSizeComparator();

    public IslandCollection(Set<Island> islands) {
        fixedRows = new HashSet<>();
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
        return fixedRows.contains(coordinate.row) || fixedVertices.contains(coordinate.match.vertex);
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
            fixedRows.add(coordinate.row);
            fixedVertices.add(coordinate.match.vertex);
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
//           LOG.fine( "Highest rated is: "+highestRated+ " "+highestRated.getDepth());
            // LOG.log(Level.FINE, "Highest rated is: "+highestRated);
            // add highest rated
            possibleIslands.add(highestRated);
            // check whether there are other islands with the same rating
            while (!islandPriorityQueue.isEmpty() && comparator.compare(highestRated, islandPriorityQueue.peek()) == 0) {
//              LOG.fine("Same prio is: " + islandPriorityQueue.peek() + " " + islandPriorityQueue.peek().getDepth());
                possibleIslands.add(islandPriorityQueue.poll());
            }
            // check whether the selected islands are possible
            Iterator<Island> candidates = possibleIslands.iterator();
            while (candidates.hasNext()) {
                Island island = candidates.next();
                Coordinate conflict = findConflictingCoordinate(island);
                if (conflict == null) {
                    continue;
                }
                // remove this candidate from the possible islands
                candidates.remove();
                Island smaller = createSmallerIslandSplitAtConflictingCoordinate(island, conflict);
                // add the smaller island to the priority queue
                // LOG.fine("Conflict detected! We add a smaller island! "+smaller);
                if (smaller.size()>0) {
                    islandPriorityQueue.add(smaller);
                }
            }
        }
        return possibleIslands;
    }

    private Island createSmallerIslandSplitAtConflictingCoordinate(Island island, Coordinate conflict) {
        // create a new island which contains the coordinates up to the overlapping coordinate.
        Island smaller = new Island(island.getDepth(), island.getBlockInstance());
        for (Coordinate c : island) {
            if (c == conflict) {
                break;
            }
            smaller.add(c);
        }
        return smaller;
    }

    // maybe going from the right to left makes this faster
    private Coordinate findConflictingCoordinate(Island island){
        for(Coordinate coordinate:island){
            if (doesCoordinateOverlapWithCommittedCoordinate(coordinate)) {
                return coordinate;
            }
        }
        return null;
    }
}