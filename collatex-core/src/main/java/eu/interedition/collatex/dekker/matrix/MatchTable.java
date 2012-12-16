package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.interedition.collatex.VariantGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.Matches;

// author: Ronald Haentjens Dekker
//
// This class represents a table of the matches
// Since this table is sparse a Hashmap based implementation
// is used rather than a Arraylist based one.
// However the API of this class looks very much like an array based one
// since you can use tokenAt(row, column) or vertexAt(row, column)
public class MatchTable {
  static Logger LOG = LoggerFactory.getLogger(MatchTable.class);
  private final HashBasedTable<Integer, Integer, MatchTableCell> table;
  private final Iterable<Token> witness;
  private final List<Integer> ranks;
  
  // assumes default token comparator
  public static MatchTable create(VariantGraph graph, Iterable<Token> witness) {
    Comparator<Token> comparator = new EqualityTokenComparator();
    return MatchTable.create(graph, witness, comparator);
  }

  public static MatchTable create(VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
    // step 1: build the MatchTable
    MatchTable table = createEmptyTable(graph, witness);
    // step 2: do the matching and fill the table
    table.fillTableWithMatches(graph, witness, comparator);
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

  private MatchTable(Iterable<Token> tokens, List<Integer> ranks) {
    this.table = HashBasedTable.create();
    this.witness = tokens;
    this.ranks = ranks;
  }

  private static MatchTable createEmptyTable(VariantGraph graph, Iterable<Token> witness) {
    graph.rank();
    // -2 === ignore the start and the end vertex
    Range<Integer> ranksRange = Ranges.closed(0, Math.max(0, graph.getEnd().getRank() - 2));
    ImmutableList<Integer> ranksSet = ranksRange.asSet(DiscreteDomains.integers()).asList();
    return new MatchTable(witness, ranksSet);
  }

  // move parameters into fields?
  private void fillTableWithMatches(VariantGraph graph, Iterable<Token> witness, Comparator<Token> comparator) {
    Matches matches = Matches.between(graph.vertices(), witness, comparator);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    int rowIndex=0;
    for (Token t : witness) {
      if (unique.contains(t) || ambiguous.contains(t)) {
        List<VariantGraph.Vertex> matchingVertices = matches.getAll().get(t);
        for (VariantGraph.Vertex vgv : matchingVertices) {
          set(rowIndex, vgv.getRank() - 1, t, vgv);
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
  
  private class MatchTableCell {
    public final Token token;
    public final VariantGraph.Vertex vertex;

    public MatchTableCell(Token token, VariantGraph.Vertex vertex) {
      this.token = token;
      this.vertex = vertex;
    }
  }
}
