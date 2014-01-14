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

import eu.interedition.collatex.VariantGraph;

// @author: Ronald Haentjens Dekker
// Unselected islands reside in the islandMultimap.
// Selected islands reside in the fixedIsland Archipelago.
// Group the islands together by size; 
// islands may change after commit islands
public class MatchTableSelection {
  Logger LOG = Logger.getLogger(MatchTableSelection.class.getName());
  private final Multimap<Integer, Island> islandMultimap;
  private final Archipelago fixedIslands;
  //this fields are needed for the locking of table cells
  private final Set<Integer> fixedRows;
  private final Set<VariantGraph.Vertex> fixedVertices;
  private final MatchTable table;

  public MatchTableSelection(MatchTable table) {
    fixedRows = Sets.newHashSet();
    fixedVertices = Sets.newHashSet();
    this.table = table;
    this.fixedIslands = new Archipelago();
    islandMultimap = ArrayListMultimap.create();
    for (Island isl : table.getIslands()) {
      islandMultimap.put(isl.size(), isl);
    }
  }
  
  // copy constructor
  public MatchTableSelection(MatchTableSelection orig) {
    // table structure is read only, does not have to be copied
    this.islandMultimap = ArrayListMultimap.create(orig.islandMultimap);
    this.fixedIslands = new Archipelago(orig.fixedIslands);
    this.fixedRows = Sets.newHashSet(orig.fixedRows);
    this.fixedVertices = Sets.newHashSet(orig.fixedVertices);
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
    removeIslandFromPossibilities(isl);
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
  public void removeOrSplitImpossibleIslands(Integer islandSize, Multimap<Integer, Island> islandMultimap) {
    Collection<Island> islandsToCheck = Lists.newArrayList(islandMultimap.get(islandSize));
    for (Island island : islandsToCheck) {
      if (!isIslandPossibleCandidate(island)) {
        islandMultimap.remove(islandSize, island);
        removeConflictingEndCoordinates(island);
        if (island.size() > 0) {
          islandMultimap.put(island.size(), island);
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
    List<Island> possibleIslands = Lists.newArrayList();
    while(possibleIslands.isEmpty()&&!islandMultimap.isEmpty()) {
      // find the maximum island size and traverse groups in descending order
      Integer max = Collections.max(islandMultimap.keySet());
      LOG.fine("Checking islands of size: "+max);
      // check the possible islands of a certain size against 
      // the already committed islands.
      removeOrSplitImpossibleIslands(max, islandMultimap);
      possibleIslands = Lists.newArrayList(islandMultimap.get(max));
    }
    return possibleIslands;
  }

  public void removeIslandFromPossibilities(Island island) {
    islandMultimap.remove(island.size(), island);
  }
}