package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;

//Note: this class is intended to replace the current MatchMatrix class
//The current class is limited to pairwise collation
//This one should not be
public class MatchTable {
  static Logger LOG = LoggerFactory.getLogger(MatchTable.class);
  private final ArrayTable<Token, Integer, VariantGraphVertex> table;

  public MatchTable(Iterable<Token> tokens, Iterable<Integer> ranks) {
    this.table = ArrayTable.create(tokens, ranks);
  }

  // assumes default token comparator
  public static MatchTable create(VariantGraph graph, Iterable<Token> witness) {
    Comparator<Token> comparator = new EqualityTokenComparator();
    return MatchTable.create(graph, witness, comparator);
  }

  public static MatchTable create(VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
    // step 1: build the MatchTable
    MatchTable table = createEmptyTable(graph, witness);
    // step 2: do the matching and fill the table
    fillTableWithMatches(graph, witness, table, comparator);
    return table;
  }

  public VariantGraphVertex at(int rowIndex, int columnIndex) {
    return table.at(rowIndex, columnIndex);
  }

  public List<Token> rowList() {
    return table.rowKeyList();
  }

  public List<Integer> columnList() {
    return table.columnKeyList();
  }

  private static MatchTable createEmptyTable(VariantGraph graph, Iterable<Token> witness) {
    graph.rank();
    // -3 === ignore the start and the end vertex
    Range<Integer> range = Ranges.closed(0, Iterables.size(graph.vertices())-3);
    ImmutableList<Integer> set = range.asSet(DiscreteDomains.integers()).asList();
    return new MatchTable(witness, set);
  }

  // remove static; move parameters into fields
  private static void fillTableWithMatches(VariantGraph graph, Iterable<Token> witness, MatchTable table, Comparator<Token> comparator) {
    Matches matches = Matches.between(graph.vertices(), witness, comparator);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    for (Token t : witness) {
      List<VariantGraphVertex> matchingVertices = matches.getAll().get(t);
      //TODO: dit kan simpeler! zie de duplicatie
      if (unique.contains(t)) {
        table.set(t, matchingVertices.get(0).getRank() - 1, matchingVertices.get(0));
      } else {
        if (ambiguous.contains(t)) {
          for (VariantGraphVertex vgv : matchingVertices) {
            table.set(t, vgv.getRank() - 1, vgv);
          }
        }
      }
    }
  }

  private void set(Token token, int rank, VariantGraphVertex variantGraphVertex) {
    //    LOG.info("putting: {}<->{}<->{}", new Object[] { token, rank, variantGraphVertex });
    table.put(token, rank, variantGraphVertex);
  }

  // Since the coordinates in allMatches are ordered from upper left to lower right, 
  // we don't need to check the lower right neighbor.
  public Set<Island> getIslands() {
    Map<Coordinate, Island> coordinateMapper = Maps.newHashMap();
    List<Coordinate> allMatches = allMatches();
    for (Coordinate c : allMatches) {
      //      LOG.info("coordinate {}", c);
      addToIslands(coordinateMapper, c, -1);
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

  private void addToIslands(Map<Coordinate, Island> coordinateMapper, Coordinate c, int diff) {
    Coordinate neighborCoordinate = new Coordinate(c.row + diff, c.column + diff);
    VariantGraphVertex neighbor = null;
    try {
      neighbor = table.at(c.row + diff, c.column + diff);
    } catch (IndexOutOfBoundsException e) {}
    if (neighbor != null) {
      Island island = coordinateMapper.get(neighborCoordinate);
      if (island == null) {
        //        LOG.info("new island");
        Island island0 = new Island();
        island0.add(neighborCoordinate);
        island0.add(c);
        coordinateMapper.put(neighborCoordinate, island0);
        coordinateMapper.put(c, island0);
      } else {
        //        LOG.info("add to existing island");
        island.add(c);
        coordinateMapper.put(c, island);
      }
    }
  }

  // Note; code taken from MatchMatrix class
  // might be simpler to work from the cellSet
  // problem there is that a token does not have to have a position
  // but a Cell Object might have one?
  private List<Coordinate> allMatches() {
    List<Coordinate> pairs = Lists.newArrayList();
    int rows = table.rowKeySet().size();
    int cols = table.columnKeySet().size();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (table.at(i, j) != null) pairs.add(new Coordinate(i, j));
      }
    }
    return pairs;
  }

}
