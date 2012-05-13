package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.dekker.matrix.MatchMatrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.MatchMatrix.Island;

public class MatchMatrixLinker implements TokenLinker {

  @Override
  public Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    MatchMatrix buildMatrix = MatchMatrix.create(base, witness, comparator);
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    Archipelago preferredIslands = archipelago.createNonConflictingVersion();
    List<Token> columnTokens = buildMatrix.columnTokens();
    List<VariantGraphVertex> rowVertices = buildMatrix.rowVertices();
    Map<Token, VariantGraphVertex> map = Maps.newHashMap();
    for (Island island : preferredIslands.iterator()) {
      for (Coordinate c : island) {
        map.put(columnTokens.get(c.column), rowVertices.get(c.row));
      }
    }
    return map;
  }

}
