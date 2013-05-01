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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;
import eu.interedition.collatex.util.VariantGraphRanking;

// author: Ronald Haentjens Dekker
//
// This class represents a table of the matches
// Since this table is sparse a Hashmap based implementation
// is used rather than a Arraylist based one.
// However the API of this class looks very much like an array based one
// since you can use tokenAt(row, column) or vertexAt(row, column)
public class MatchTable {
  private final HashBasedTable<Integer, Integer, MatchTableCell> table;
  private final Iterable<Token> witness;
  private final List<Integer> ranks;
  //this fields are needed for the locking of table cells
  private final Set<Integer> fixedRows = Sets.newHashSet();
  private final Set<VariantGraph.Vertex> fixedVertices = Sets.newHashSet();

  
  // assumes default token comparator
  public static MatchTable create(VariantGraph graph, Iterable<Token> witness) {
    Comparator<Token> comparator = new EqualityTokenComparator();
    return MatchTable.create(graph, witness, comparator);
  }

  public static MatchTable create(VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
    final VariantGraphRanking ranking = VariantGraphRanking.of(graph);
    // step 1: build the MatchTable
    MatchTable table = createEmptyTable(ranking, graph, witness);
    // step 2: do the matching and fill the table
    table.fillTableWithMatches(ranking, graph, witness, comparator);
    return table;
  }

  public VariantGraph.Vertex vertexAt(int rowIndex, int columnIndex) {
    MatchTableCell cell = table.get(rowIndex, columnIndex);
    return cell==null ? null : cell.vertex;
  }
  
  public Token tokenAt(int rowIndex, int columnIndex) {
    MatchTableCell cell = table.get(rowIndex, columnIndex);
    return cell==null ? null : cell.token;
  }

  // Warning: this method reiterates the witness!
  // This method is only meant for the user interface and serialization classes!
  // Use the tokenAt method in all other cases.
  public List<Token> rowList() {
    return Lists.newArrayList(witness);
  }

  public List<Integer> columnList() {
    return ranks;
  }

  // Since the coordinates in allMatches are ordered from upper left to lower right, 
  // we don't need to check the lower right neighbor.
  public Set<Island> getIslands() {
    Map<Coordinate, Island> coordinateMapper = Maps.newHashMap();
    List<Coordinate> allMatches = allMatches();
    for (Coordinate c : allMatches) {
      //      LOG.debug("coordinate {}", c);
      addToIslands(coordinateMapper, c);
    }
    Set<Coordinate> smallestIslandsCoordinates = Sets.newHashSet(allMatches);
    smallestIslandsCoordinates.removeAll(coordinateMapper.keySet());
    for (Coordinate coordinate : smallestIslandsCoordinates) {
      Island island = new Island();
      island.add(coordinate);
      coordinateMapper.put(coordinate, island);
    }
    return Sets.newHashSet(coordinateMapper.values());
  }

	/*
	 * Commit an island in the match table
	 * Island will be part of the final alignment
	 */
  public void commitIsland(Island isl) {
  	for (Coordinate coordinate : isl) {
      fixedRows.add(coordinate.row);
      fixedVertices.add(vertexAt(coordinate.row, coordinate.column));
	  }
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
	 * Return whether a coordinate overlaps with an already committed coordinate
	 */
	public boolean doesCoordinateOverlapWithCommittedCoordinate(Coordinate coordinate) {
    return fixedRows.contains(coordinate.row) || //
        fixedVertices.contains(vertexAt(coordinate.row, coordinate.column));
	}


	
	private MatchTable(Iterable<Token> tokens, List<Integer> ranks) {
    this.table = HashBasedTable.create();
    this.witness = tokens;
    this.ranks = ranks;
  }

  private static MatchTable createEmptyTable(VariantGraphRanking ranking, VariantGraph graph, Iterable<Token> witness) {
    // -2 === ignore the start and the end vertex
    Range<Integer> ranksRange = Ranges.closed(0, Math.max(0, ranking.apply(graph.getEnd()) - 2));
    ImmutableList<Integer> ranksSet = ranksRange.asSet(DiscreteDomains.integers()).asList();
    return new MatchTable(witness, ranksSet);
  }

  // move parameters into fields?
  private void fillTableWithMatches(VariantGraphRanking ranking, VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
    Matches matches = Matches.between(graph.vertices(), witness, comparator);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    int rowIndex=0;
    for (Token t : witness) {
      if (unique.contains(t) || ambiguous.contains(t)) {
        List<VariantGraph.Vertex> matchingVertices = matches.getAll().get(t);
        for (VariantGraph.Vertex vgv : matchingVertices) {
          set(rowIndex, ranking.apply(vgv) - 1, t, vgv);
        }
      }
      rowIndex++;
    }
  }

  private void set(int rowIndex, int columnIndex, Token token, VariantGraph.Vertex vertex) {
    //    LOG.debug("putting: {}<->{}<->{}", new Object[] { token, columnIndex, variantGraphVertex });
    MatchTableCell cell = new MatchTableCell(token, vertex);
    table.put(rowIndex, columnIndex, cell);
  }

  private void addToIslands(Map<Coordinate, Island> coordinateMapper, Coordinate c) {
    int diff = -1;
    Coordinate neighborCoordinate = new Coordinate(c.row + diff, c.column + diff);
    VariantGraph.Vertex neighbor = null;
    try {
      neighbor = vertexAt(c.row + diff, c.column + diff);
    } catch (IndexOutOfBoundsException e) {}
    if (neighbor != null) {
      Island island = coordinateMapper.get(neighborCoordinate);
      if (island == null) {
        //        LOG.debug("new island");
        Island island0 = new Island();
        island0.add(neighborCoordinate);
        island0.add(c);
        coordinateMapper.put(neighborCoordinate, island0);
        coordinateMapper.put(c, island0);
      } else {
        //        LOG.debug("add to existing island");
        island.add(c);
        coordinateMapper.put(c, island);
      }
    }
  }

  // Note: code taken from MatchMatrix class
  // TODO: might be simpler to work from the valueSet
  // TODO: try remove the call to rowList / columnList
  List<Coordinate> allMatches() {
    List<Coordinate> pairs = Lists.newArrayList();
    int rows = rowList().size();
    int cols = columnList().size();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (vertexAt(i, j) != null) pairs.add(new Coordinate(i, j));
      }
    }
    return pairs;
  }
  
  static Set<Vector> convertIslandsToVectors(Set<Island> islands2) {
		Set<Vector> vectors = Sets.newHashSet();
		for (Island i : islands2) {
			Coordinate leftEnd = i.getLeftEnd();
			Vector v = new Vector(leftEnd.column, leftEnd.row, i.size());
			vectors.add(v);
		}
		return vectors;
	}

	public Set<Vector> getVectors() {
		Set<Island> islands2 = getIslands();
		Set<Vector> vectors = MatchTable.convertIslandsToVectors(islands2);
		return vectors;
	}

	private class MatchTableCell {
	    public final Token token;
	    public final VariantGraph.Vertex vertex;
	
	    public MatchTableCell(Token token, VariantGraph.Vertex vertex) {
	      this.token = token;
	      this.vertex = vertex;
	    }
	}



}
