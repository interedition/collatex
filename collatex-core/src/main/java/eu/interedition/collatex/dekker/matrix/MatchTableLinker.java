package eu.interedition.collatex.dekker.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;

//Note: This class is intended to be the replacement for the MatchMatrixLinker class
public class MatchTableLinker implements TokenLinker {
  private static final boolean USE_ARCHIPELAGO = true;
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

    if (USE_ARCHIPELAGO==true) {
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
    } else {
      LOG.fine("create the vector space from the matches");
	    Set<Vector> vectors = table.getVectors();
	    VectorConflictResolver resolver = new VectorConflictResolver(vectors);
	    List<Vector> resolved = resolver.resolveConflicts();
	    // Here the result is put in a map
	    Map<Token, VariantGraph.Vertex> map = Maps.newHashMap();
	    for (Vector v2 : resolved) {
	      for (int i=0; i < v2.length; i++) {
	        map.put(table.tokenAt(v2.y+i, v2.x+i), table.vertexAt(v2.y+i, v2.x+i));
	      }
	    }
	  	return map;
    }
  }
}
