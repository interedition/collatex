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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ArchipelagoWithVersions {
  private static final int MINIMUM_OUTLIER_DISTANCE_FACTOR = 5;
  Logger LOG = Logger.getLogger(ArchipelagoWithVersions.class.getName());
  private final MatchTable table;
  private final int outlierTranspositionsSizeLimit;

  public ArchipelagoWithVersions(MatchTable table, int outlierTranspositionsSizeLimit) {
    this.table = table;
    this.outlierTranspositionsSizeLimit = outlierTranspositionsSizeLimit;
  }

   /*
    * Create a non-conflicting version by simply taken all the islands
    * that do not conflict with each other, largest first. This presuming
    * that Archipelago will have a high value if it contains the largest
    * possible islands
    */
  public Archipelago createNonConflictingVersion(Set<Island> islands) {
    Archipelago result = new Archipelago();
  	Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
    for (Island isl : islands) {
      islandMultimap.put(isl.size(), isl);
    }
    List<Integer> keySet = Lists.newArrayList(islandMultimap.keySet());
    Collections.sort(keySet);
    List<Integer> decreasingIslandSizes = Lists.reverse(keySet);
    for (Integer islandSize : decreasingIslandSizes) {
      //      if (islandSize > 0) { // limitation to prevent false transpositions
      List<Island> possibleIslands = possibleIslands(islandMultimap.get(islandSize));
      if (possibleIslands.size() == 1) {
        addIslandToResult(possibleIslands.get(0), result);
      } else if (possibleIslands.size() > 1) {
        handleMultipleIslandSameSize(result, possibleIslands);
      }
    }
    return result;
  }

	private void handleMultipleIslandSameSize(Archipelago archipelago, List<Island> islandsOfSameSize) {
		Multimap<IslandCompetition, Island> conflictMap = analyse(archipelago, islandsOfSameSize);
		
		Multimap<Double, Island> distanceMap1 = makeDistanceMap(conflictMap.get(IslandCompetition.CompetingIslandAndOnIdealIine), archipelago);
		LOG.fine("addBestOfCompeting with competingIslandsOnIdealLine");
		addBestOfCompeting(archipelago, distanceMap1);
	
		Multimap<Double, Island> distanceMap2 = makeDistanceMap(conflictMap.get(IslandCompetition.CompetingIsland), archipelago);
		LOG.fine("addBestOfCompeting with otherCompetingIslands");
		addBestOfCompeting(archipelago, distanceMap2);
		
		List<Island> islandsToCommit = Lists.newArrayList();
		for (Island i : conflictMap.get(IslandCompetition.NonCompetingIsland)) {
			islandsToCommit.add(i);
		}
		
    /* Add the islands to commit to the result Archipelago
		 * If we want to re-factor this into a pull construction
		 * rather then a push construction
		 * we have to move this code out of this method
		 * and move it to the caller class
		 */
		for (Island i: islandsToCommit) {
			addIslandToResult(i, archipelago);
		}
	}

	private Multimap<IslandCompetition, Island> analyse(Archipelago archipelago, List<Island> islandsOfSameSize) {
		Multimap<IslandCompetition, Island> conflictMap = ArrayListMultimap.create();
		Set<Island> competingIslands = getCompetingIslands(islandsOfSameSize, archipelago);
		for (Island island : competingIslands) {
		  Coordinate leftEnd = island.getLeftEnd();
		  if (archipelago.getIslandVectors().contains(leftEnd.row - leftEnd.column)) {
		    conflictMap.put(IslandCompetition.CompetingIslandAndOnIdealIine, island);
		  } else {
		    conflictMap.put(IslandCompetition.CompetingIsland, island);
		  }
		}
		for (Island island : getNonCompetingIslands(islandsOfSameSize, competingIslands)) {
			conflictMap.put(IslandCompetition.NonCompetingIsland, island);
		}
		return conflictMap;
	}

  // TODO: find a better way to determine the best choice of island
  private void addBestOfCompeting(Archipelago archipelago, Multimap<Double, Island> distanceMap1) {
    for (Double d : shortestToLongestDistances(distanceMap1)) {
      for (Island ci : distanceMap1.get(d)) {
        if (table.isIslandPossibleCandidate(ci)) {
          addIslandToResult(ci, archipelago);
        }
      }
    }
  }

  private Multimap<Double, Island> makeDistanceMap(Collection<Island> competingIslands, Archipelago archipelago) {
    Multimap<Double, Island> distanceMap = ArrayListMultimap.create();
    for (Island isl : competingIslands) {
      distanceMap.put(archipelago.smallestDistance(isl), isl);
    }
    return distanceMap;
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

  private List<Island> possibleIslands(Collection<Island> islandsOfSize) {
    List<Island> islands = Lists.newArrayList();
    for (Island island : islandsOfSize) {
      if (table.isIslandPossibleCandidate(island)) {
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
      if (table.doesCoordinateOverlapWithCommittedCoordinate(leftEnd)) {
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
      if (table.doesCoordinateOverlapWithCommittedCoordinate(rightEnd)) {
        island.removeCoordinate(rightEnd);
        if (island.size() == 0) {
          return;
        }
      } else {
        goOn = false;
      }
    }
  }


  private void addIslandToResult(Island isl, Archipelago result) {
    if (islandIsNoOutlier(result, isl)) {
      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "adding island: '{0}'", isl);
      }
      table.commitIsland(isl);
      result.add(isl);
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