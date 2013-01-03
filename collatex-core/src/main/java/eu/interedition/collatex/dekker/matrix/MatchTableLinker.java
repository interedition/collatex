package eu.interedition.collatex.dekker.matrix;

import com.google.common.collect.Maps;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.TokenLinker;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//Note: This class is intended to be the replacement for the MatchMatrixLinker class
public class MatchTableLinker implements TokenLinker {
  static Logger LOG = Logger.getLogger(MatchTableLinker.class.getName());
  private final int outlierTranspositionsSizeLimit;

  public MatchTableLinker(int outlierTranspositionsSizeLimit) {
    super();
    this.outlierTranspositionsSizeLimit = outlierTranspositionsSizeLimit;
  }

  @Override
  public Map<Token, VariantGraph.Vertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    // create MatchTable and fill it with matches
    LOG.fine("create MatchTable and fill it with matches");
    MatchTable table = MatchTable.create(base, witness, comparator);

    // the MatchTable getIslands() method does the pattern finding
    LOG.fine("getIslands()");
    Set<Island> islands = table.getIslands();

    LOG.fine("create Archipelago data structure");
    // create Archipelago data structure and fill it with all the islands
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions(table, outlierTranspositionsSizeLimit);
    LOG.fine("fill it with all the islands");
    for (Island isl : islands) {
      archipelago.add(isl);
    }

    // The archipelago with version createNonConflictingVersion() method
    // selects the optimal islands
    LOG.fine("select the optimal islands");
    Archipelago preferredIslands = archipelago.createNonConflictingVersion();
    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "Number of preferred Islands: {0}", preferredIslands.size());
    }

    // Here the result is put in a map
    Map<Token, VariantGraph.Vertex> map = Maps.newHashMap();
    for (Island island : preferredIslands.iterator()) {
      for (Coordinate c : island) {
        map.put(table.tokenAt(c.row, c.column), table.vertexAt(c.row, c.column));
      }
    }
    return map;
  }
}
