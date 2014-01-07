package eu.interedition.collatex.dekker.matrix;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.VariantGraph;

// @author: Ronald Haentjens Dekker
public class MatchTableSelection {
  //this fields are needed for the locking of table cells
  private final Set<Integer> fixedRows = Sets.newHashSet();
  private final Set<VariantGraph.Vertex> fixedVertices = Sets.newHashSet();
  private final MatchTable table;

  public MatchTableSelection(MatchTable table) {
    this.table = table;
  }
  
  /*
   * Commit an island in the match table
   * Island will be part of the final alignment
   */
  public void commitIsland(Island isl) {
    for (Coordinate coordinate : isl) {
      fixedRows.add(coordinate.row);
      fixedVertices.add(table.vertexAt(coordinate.row, coordinate.column));
    }
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
}
