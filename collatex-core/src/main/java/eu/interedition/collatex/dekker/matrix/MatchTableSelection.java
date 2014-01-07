package eu.interedition.collatex.dekker.matrix;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Sets;

import eu.interedition.collatex.VariantGraph;

// @author: Ronald Haentjens Dekker
public class MatchTableSelection {
  Logger LOG = Logger.getLogger(MatchTableSelection.class.getName());
  //this fields are needed for the locking of table cells
  private final Set<Integer> fixedRows = Sets.newHashSet();
  private final Set<VariantGraph.Vertex> fixedVertices = Sets.newHashSet();
  private final Archipelago result;
  private final MatchTable table;

  public MatchTableSelection(MatchTable table, Archipelago result) {
    this.table = table;
    this.result = result;
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
    result.add(isl);
  }
}
