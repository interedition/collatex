/*
 * Copyright (c) 2013 The Interedition Development Group.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.interedition.collatex.VariantGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * 
 * @author Ronald Haentjens Dekker
 * @author Bram Buitendijk
 * @author Meindert Kroese
 */
public class ArchipelagoWithVersions extends Archipelago {
  private static final int MINIMUM_OUTLIER_DISTANCE_FACTOR = 5;
  Logger LOG = Logger.getLogger(ArchipelagoWithVersions.class.getName());
  private final MatchTable table;
  Set<Integer> fixedRows = Sets.newHashSet();
  Set<VariantGraph.Vertex> fixedVertices = Sets.newHashSet();
  private final int outlierTranspositionsSizeLimit;

  public ArchipelagoWithVersions(MatchTable table, int outlierTranspositionsSizeLimit) {
    this.table = table;
    this.outlierTranspositionsSizeLimit = outlierTranspositionsSizeLimit;
    setIslands(new ArrayList<Island>());
  }

  @Override
  public void add(Island island) {
    super.add(island);
  }

  @Override
  public ArchipelagoWithVersions copy() {
    ArchipelagoWithVersions result = new ArchipelagoWithVersions(this.table, outlierTranspositionsSizeLimit);
    for (Island isl : getIslands()) {
      result.add(new Island(isl));
    }
    return result;
  }

  /*
    * Create a non-conflicting version by simply taken all the islands
    * that do not conflict with each other, largest first. This presuming
    * that Archipelago will have a high value if it contains the largest
    * possible islands
    */
  public Archipelago createNonConflictingVersion(Archipelago archipelago) {
    fixedRows = Sets.newHashSet();
    fixedVertices = Sets.newHashSet();
    Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
    for (Island isl : getIslands()) {
      islandMultimap.put(isl.size(), isl);
    }
    List<Integer> keySet = Lists.newArrayList(islandMultimap.keySet());
    Collections.sort(keySet);
    List<Integer> decreasingIslandSizes = Lists.reverse(keySet);
    for (Integer islandSize : decreasingIslandSizes) {
      //      if (islandSize > 0) { // limitation to prevent false transpositions
      List<Island> islands = possibleIslands(islandMultimap.get(islandSize));
      if (islands.size() == 1) {
        addIslandToResult(islands.get(0), archipelago);
      } else if (islands.size() > 1) {
        Set<Island> competingIslands = getCompetingIslands(islands, archipelago);
        Set<Island> competingIslandsOnIdealLine = Sets.newHashSet();
        Set<Island> otherCompetingIslands = Sets.newHashSet();
        for (Island island : competingIslands) {
          Coordinate leftEnd = island.getLeftEnd();
          if (archipelago.getIslandVectors().contains(leftEnd.row - leftEnd.column)) {
            competingIslandsOnIdealLine.add(island);
          } else {
            otherCompetingIslands.add(island);
          }
        }
        Multimap<Double, Island> distanceMap1 = makeDistanceMap(competingIslandsOnIdealLine, archipelago);
        LOG.fine("addBestOfCompeting with competingIslandsOnIdealLine");
        addBestOfCompeting(archipelago, distanceMap1);

        Multimap<Double, Island> distanceMap2 = makeDistanceMap(otherCompetingIslands, archipelago);
        LOG.fine("addBestOfCompeting with otherCompetingIslands");
        addBestOfCompeting(archipelago, distanceMap2);

        for (Island i : getNonCompetingIslands(islands, competingIslands)) {
          addIslandToResult(i, archipelago);
        }
      }
    }
    return archipelago;
  }

  // TODO: find a better way to determine the best choice of island
  private void addBestOfCompeting(Archipelago archipelago, Multimap<Double, Island> distanceMap1) {
    for (Double d : shortestToLongestDistances(distanceMap1)) {
      for (Island ci : distanceMap1.get(d)) {
        if (islandIsPossible(ci)) {
          addIslandToResult(ci, archipelago);
        }
      }
    }
  }

  private Multimap<Double, Island> makeDistanceMap(Set<Island> competingIslands, Archipelago archipelago) {
    Multimap<Double, Island> distanceMap = ArrayListMultimap.create();
    for (Island isl : competingIslands) {
      distanceMap.put(archipelago.smallestDistance(isl), isl);
    }
    return distanceMap;
  }

  public Archipelago createNonConflictingVersion() {
    return createNonConflictingVersion(new Archipelago());
  }

  private List<Double> shortestToLongestDistances(Multimap<Double, Island> distanceMap) {
    List<Double> distances = Lists.newArrayList(distanceMap.keySet());
    Collections.sort(distances);
    return distances;
  }

  private Set<Island> getNonCompetingIslands(List<Island> islands, Set<Island> competingIslands) {
    Set<Island> nonCompetingIslands = Sets.newHashSet(islands);
    nonCompetingIslands.removeAll(competingIslands);
    return nonCompetingIslands;
  }

  private Set<Island> getCompetingIslands(List<Island> islands, Archipelago result) {
    Set<Island> competingIslands = Sets.newHashSet();
    for (int i = 0; i < islands.size(); i++) {
      Island i1 = islands.get(i);
      for (int j = 1; j < islands.size() - i; j++) {
        Island i2 = islands.get(i + j);
        if (result.islandsCompete(i1, i2)) {
          competingIslands.add(i1);
          competingIslands.add(i2);
        }
      }
    }
    return competingIslands;
  }

  @Override
  public boolean islandsCompete(Island i1, Island i2) {
    return false;
  };

  private List<Island> possibleIslands(Collection<Island> islandsOfSize) {
    List<Island> islands = Lists.newArrayList();
    for (Island island : islandsOfSize) {
      if (islandIsPossible(island)) {
        islands.add(island);
      } else {
        removeConflictingEndCoordinates(island);
        if (island.size() > 1) {
          islands.add(island);
        }
      }
    }
    return islands;
  }

  private void removeConflictingEndCoordinates(Island island) {
    boolean goOn = true;
    while (goOn) {
      Coordinate leftEnd = island.getLeftEnd();
      if (coordinateOverlapsWithFixed(leftEnd)) {
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
      if (coordinateOverlapsWithFixed(rightEnd)) {
        island.removeCoordinate(rightEnd);
        if (island.size() == 0) {
          return;
        }
      } else {
        goOn = false;
      }
    }
  }

  private boolean islandIsPossible(Island island) {
    for (Coordinate coordinate : island) {
      if (coordinateOverlapsWithFixed(coordinate)) return false;
    }
    return true;
  }

  private boolean coordinateOverlapsWithFixed(Coordinate coordinate) {
    return fixedRows.contains(coordinate.row) || //
        fixedVertices.contains(table.vertexAt(coordinate.row, coordinate.column));
  }

  private void addIslandToResult(Island isl, Archipelago result) {
    if (islandIsNoOutlier(result, isl)) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "adding island: '{0}'", isl);
      }
      result.add(isl);
      for (Coordinate coordinate : isl) {
        fixedRows.add(coordinate.row);
        fixedVertices.add(table.vertexAt(coordinate.row, coordinate.column));
      }
    } else {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "island: '{0}' is an outlier, not added", isl);
      }
    }
  }

  private boolean islandIsNoOutlier(Archipelago a, Island isl) {
    double smallestDistance = a.smallestDistanceToIdealLine(isl);
    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "island {0}, distance={1}", new Object[] { isl, smallestDistance });
    }
    int islandSize = isl.size();
    return (!(a.size() > 0 && islandSize <= outlierTranspositionsSizeLimit && smallestDistance >= islandSize * MINIMUM_OUTLIER_DISTANCE_FACTOR));
  }
}