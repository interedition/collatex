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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * @author Ronald Haentjens Dekker
 * @author Bram Buitendijk
 * @author Meindert Kroese
 */
public class IslandConflictResolver {
    Logger LOG = Logger.getLogger(IslandConflictResolver.class.getName());
    // fixed islands contains all the islands that are selected for the final alignment
    private final IslandSelection selection;

    public IslandConflictResolver(IslandSelection collection) {
        selection = collection;
    }

    /*
     * Create a non-conflicting version by simply taken all the islands that do
     * not conflict with each other, largest first.
     */
    public IslandSelection createNonConflictingVersion() {
        List<Island> possibleIslands;
        do {
            possibleIslands = selection.getPossibleIslands();
            // check the possible islands of a certain size against each other.
            if (possibleIslands.size() == 1) {
                selection.addIsland(possibleIslands.get(0));
            } else if (possibleIslands.size() > 1) {
                Map<IslandCompetition, List<Island>> analysis = analyzeConflictsBetweenPossibleIslands(possibleIslands);
                resolveConflictsBySelectingPreferredIslands(selection, analysis);
            }
        }
        while (!possibleIslands.isEmpty());
        return selection;
    }

    /*
     * This method analyzes the relationship between all the islands of the same
     * size that have yet to be selected. They can compete with one another
     * (choosing one locks out the other), some of them can be on the ideal line.
     *
     * Parameters: the size of the islands that you want to analyze
     */
    public Map<IslandCompetition, List<Island>> analyzeConflictsBetweenPossibleIslands(List<Island> possibleIslands) {
        Map<IslandCompetition, List<Island>> conflictMap = new HashMap<>();
        Set<Island> competingIslands = getCompetingIslands(possibleIslands);
        for (Island island : competingIslands) {
            if (selection.doesCandidateLayOnVectorOfCommittedIsland(island)) {
                conflictMap.computeIfAbsent(IslandCompetition.CompetingIslandAndOnIdealIine, c -> new ArrayList<>()).add(island);
            } else {
                conflictMap.computeIfAbsent(IslandCompetition.CompetingIsland, c -> new ArrayList<>()).add(island);
            }
        }
        for (Island island : getNonCompetingIslands(possibleIslands, competingIslands)) {
            conflictMap.computeIfAbsent(IslandCompetition.NonCompetingIsland, c -> new ArrayList<>()).add(island);
        }
        return conflictMap;
    }

    /*
     * The preferred Islands are directly added to the result Archipelago
     * If we want to
     * re-factor this into a pull construction rather then a push construction
     * we have to move this code out of this method and move it to the caller
     * class
     */
    private void resolveConflictsBySelectingPreferredIslands(IslandSelection selection, Map<IslandCompetition, List<Island>> islandConflictMap) {
        // First select competing islands that are on the ideal line
        LOG.fine("addBestOfCompeting with competingIslandsOnIdealLine");
        makeDistanceMap(islandConflictMap.getOrDefault(IslandCompetition.CompetingIslandAndOnIdealIine, Collections.emptyList()))
            .values().stream()//
            .flatMap(List::stream)//
            .filter(selection::isIslandPossibleCandidate)//
            .forEach(selection::addIsland);

        // Second select other competing islands
        LOG.fine("addBestOfCompeting with otherCompetingIslands");
        makeDistanceMap(islandConflictMap.getOrDefault(IslandCompetition.CompetingIsland, Collections.emptyList()))
            .values().stream()//
            .flatMap(List::stream)//
            .filter(selection::isIslandPossibleCandidate)//
            .forEach(selection::addIsland);

        // Third select non competing islands
        LOG.fine("add non competing islands");
        islandConflictMap.getOrDefault(IslandCompetition.NonCompetingIsland, Collections.emptyList())
            .forEach(selection::addIsland);
    }

    // TODO: This method calculates the distance from the ideal line
    // TODO: by calculating the ratio x/y.
    // TODO: but the ideal line may have moved (due to additions/deletions).
    private SortedMap<Double, List<Island>> makeDistanceMap(Collection<Island> competingIslands) {
        SortedMap<Double, List<Island>> distanceMap = new TreeMap<>();
        for (Island isl : competingIslands) {
            Coordinate leftEnd = isl.getLeftEnd();
            double ratio = ((leftEnd.column + 1) / (double) (leftEnd.row + 1));
            double b2 = Math.log(ratio) / Math.log(2);
            double distanceToIdealLine = Math.abs(b2);
            distanceMap.computeIfAbsent(distanceToIdealLine, d -> new ArrayList<>()).add(isl);
        }
        return distanceMap;
    }

    private Set<Island> getNonCompetingIslands(List<Island> islands, Set<Island> competingIslands) {
        Set<Island> nonCompetingIslands = new HashSet<>(islands);
        nonCompetingIslands.removeAll(competingIslands);
        return nonCompetingIslands;
    }

    private Set<Island> getCompetingIslands(List<Island> islands) {
        Set<Island> competingIslands = new HashSet<>();
        for (int i = 0; i < islands.size(); i++) {
            Island i1 = islands.get(i);
            for (int j = 1; j < islands.size() - i; j++) {
                Island i2 = islands.get(i + j);
                if (i1.isCompetitor(i2)) {
                    competingIslands.add(i1);
                    competingIslands.add(i2);
                }
            }
        }
        return competingIslands;
    }
}