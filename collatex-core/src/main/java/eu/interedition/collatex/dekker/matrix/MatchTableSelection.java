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

package eu.interedition.collatex.dekker.matrix;

import eu.interedition.collatex.VariantGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// @author: Ronald Haentjens Dekker
// Unselected islands reside in the islandMultimap.
// Selected islands reside in the fixedIsland Archipelago.
// Group the islands together by size;
// islands may change after commit islands
public class MatchTableSelection {
    Logger LOG = Logger.getLogger(MatchTableSelection.class.getName());
    private final Map<Integer, List<Island>> islandMultimap;
    private final Archipelago fixedIslands;
    //this fields are needed for the locking of table cells
    private final Set<Integer> fixedRows;
    private final Set<VariantGraph.Vertex> fixedVertices;
    private final MatchTable table;

    public MatchTableSelection(MatchTable table) {
        fixedRows = new HashSet<>();
        fixedVertices = new HashSet<>();
        this.table = table;
        this.fixedIslands = new Archipelago();
        islandMultimap = new HashMap<>();
        for (Island isl : table.getIslands()) {
            islandMultimap.computeIfAbsent(isl.size(), s -> new ArrayList<>()).add(isl);
        }
    }

    // copy constructor
    public MatchTableSelection(MatchTableSelection orig) {
        // table structure is read only, does not have to be copied
        this.islandMultimap = orig.islandMultimap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
        this.fixedIslands = new Archipelago(orig.fixedIslands);
        this.fixedRows = new HashSet<>(orig.fixedRows);
        this.fixedVertices = new HashSet<>(orig.fixedVertices);
        this.table = orig.table;
    }

    /*
     * Return whether a coordinate overlaps with an already committed coordinate
     */
    public boolean doesCoordinateOverlapWithCommittedCoordinate(Coordinate coordinate) {
        return fixedRows.contains(coordinate.row) || //
            fixedVertices.contains(table.vertexAt(coordinate.row, coordinate.column));
    }

    /*
     * Return whether an island overlaps with an already committed island
     */
    public boolean isIslandPossibleCandidate(Island island) {
        for (Coordinate coordinate : island) {
            if (doesCoordinateOverlapWithCommittedCoordinate(coordinate)) return false;
        }
        return true;
    }

    /*
     * Commit an island in the match table
     * Island will be part of the final alignment
     */
    public void addIsland(Island isl) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "adding island: '{0}'", isl);
        }
        for (Coordinate coordinate : isl) {
            fixedRows.add(coordinate.row);
            fixedVertices.add(table.vertexAt(coordinate.row, coordinate.column));
        }
        fixedIslands.add(isl);
        islandMultimap.computeIfPresent(isl.size(), (s, i) -> {
            i.remove(isl);
            return (i.isEmpty() ? null : i);
        });
    }

    public boolean doesCandidateLayOnVectorOfCommittedIsland(Island island) {
        Coordinate leftEnd = island.getLeftEnd();
        return fixedIslands.getIslandVectors().contains(leftEnd.row - leftEnd.column);
    }

    public int size() {
        return fixedIslands.size();
    }

    public List<Island> getIslands() {
        return fixedIslands.getIslands();
    }

    public boolean containsCoordinate(int row, int column) {
        return fixedIslands.containsCoordinate(row, column);
    }

    /*
     * For all the possible islands of a certain size this method checks whether
     * they conflict with one of the previously committed islands. If so, the
     * possible island is removed from the multimap. Or in case of overlap, split
     * into a smaller island and then put in back into the map Note that this
     * method changes the possible islands multimap.
     */
    //TODO: the original Island object is modified here
    //TODO: That should not happen, if we want to build a decision tree.
    public void removeOrSplitImpossibleIslands(Integer islandSize, Map<Integer, List<Island>> islandMultimap) {
        Collection<Island> islandsToCheck = new ArrayList<>(islandMultimap.getOrDefault(islandSize, Collections.emptyList()));
        for (Island island : islandsToCheck) {
            if (!isIslandPossibleCandidate(island)) {
                islandMultimap.computeIfPresent(islandSize, (s, i) -> {
                    i.remove(island);
                    return (i.isEmpty() ? null : i);
                });
                removeConflictingEndCoordinates(island);
                if (island.size() > 0) {
                    islandMultimap.computeIfAbsent(island.size(), s -> new ArrayList<>()).add(island);
                }
            }
        }
    }

    private void removeConflictingEndCoordinates(Island island) {
        boolean goOn = true;
        while (goOn) {
            Coordinate leftEnd = island.getLeftEnd();
            if (doesCoordinateOverlapWithCommittedCoordinate(leftEnd)) {
                island.removeCoordinate(leftEnd);
                if (island.size() == 0) {
                    return;
                }
            } else {
                goOn = false;
            }
        }
        goOn = true;
        while (goOn) {
            Coordinate rightEnd = island.getRightEnd();
            if (doesCoordinateOverlapWithCommittedCoordinate(rightEnd)) {
                island.removeCoordinate(rightEnd);
                if (island.size() == 0) {
                    return;
                }
            } else {
                goOn = false;
            }
        }
    }

    public List<Island> getPossibleIslands() {
        List<Island> possibleIslands = new ArrayList<>();
        while (possibleIslands.isEmpty() && !islandMultimap.isEmpty()) {
            // find the maximum island size and traverse groups in descending order
            Integer max = Collections.max(islandMultimap.keySet());
            LOG.fine("Checking islands of size: " + max);
            // check the possible islands of a certain size against
            // the already committed islands.
            removeOrSplitImpossibleIslands(max, islandMultimap);
            possibleIslands = new ArrayList<>(islandMultimap.getOrDefault(max, Collections.emptyList()));
        }
        return possibleIslands;
    }
}