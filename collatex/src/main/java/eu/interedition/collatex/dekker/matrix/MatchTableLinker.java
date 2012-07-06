package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

//Note: This class is intended to be the replacement for the MatchMatrixLinker class
public class MatchTableLinker implements TokenLinker {
  static Logger LOG = LoggerFactory.getLogger(MatchTableLinker.class);

  @Override
  public Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    // create MatchTable and fill it with matches
    LOG.info("create MatchTable and fill it with matches");
    MatchTable table = MatchTable.create(base, witness, comparator);

    // the MatchTable getIslands() method does the pattern finding
    LOG.info("getIslands()");
    Set<Island> islands = table.getIslands();

    LOG.info("create Archipelago data structure");
    // create Archipelago data structure and fill it with all the islands
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions(table);
    LOG.info("fill it with all the islands");
    for (Island isl : islands) {
      archipelago.add(isl);
    }

    // The archipelago with version createNonConflictingVersion() method
    // selects the optimal islands
    LOG.info("select the optimal islands");
    Archipelago preferredIslands = archipelago.createNonConflictingVersion();
    LOG.info("Number of preferred Islands: {}", preferredIslands.size());

    // Here the result is put in a map
    Map<Token, VariantGraphVertex> map = Maps.newHashMap();
    for (Island island : preferredIslands.iterator()) {
      for (Coordinate c : island) {
        map.put(table.rowList().get(c.row), table.at(c.row, c.column));
      }
    }
    return map;
  }
}
