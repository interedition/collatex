package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;

//Note: This class is intended to be the replacement for the MatchMatrixLinker class
public class MatchTableLinker implements TokenLinker {
  static Logger LOG = LoggerFactory.getLogger(MatchTableLinker.class);
  private final int outlierTranspositionsSizeLimit;

  public MatchTableLinker(int outlierTranspositionsSizeLimit) {
    super();
    this.outlierTranspositionsSizeLimit = outlierTranspositionsSizeLimit;
  }

  @Override
  public Map<Token, Neo4jVariantGraphVertex> link(Neo4jVariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    // create MatchTable and fill it with matches
    LOG.debug("create MatchTable and fill it with matches");
    MatchTable table = MatchTable.create(base, witness, comparator);

    // the MatchTable getIslands() method does the pattern finding
    LOG.debug("getIslands()");
    Set<Island> islands = table.getIslands();

    LOG.debug("create Archipelago data structure");
    // create Archipelago data structure and fill it with all the islands
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions(table, outlierTranspositionsSizeLimit);
    LOG.debug("fill it with all the islands");
    for (Island isl : islands) {
      archipelago.add(isl);
    }

    // The archipelago with version createNonConflictingVersion() method
    // selects the optimal islands
    LOG.debug("select the optimal islands");
    Archipelago preferredIslands = archipelago.createNonConflictingVersion();
    LOG.debug("Number of preferred Islands: {}", preferredIslands.size());

    // Here the result is put in a map
    Map<Token, Neo4jVariantGraphVertex> map = Maps.newHashMap();
    for (Island island : preferredIslands.iterator()) {
      for (Coordinate c : island) {
        map.put(table.tokenAt(c.row, c.column), table.vertexAt(c.row, c.column));
      }
    }
    return map;
  }
}
