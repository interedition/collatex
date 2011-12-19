package eu.interedition.collatex.implementation.matching;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.IWitness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EditDistanceTokenComparator;
import eu.interedition.collatex.matching.Matches;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NearMatcherTest extends AbstractTest {
  
  @Test
  public void nearTokenMatching() {
    final IWitness[] w = createWitnesses("near matching yeah", "nar matching");
    final VariantGraph graph = merge(w[0]);
    final ListMultimap<Token, VariantGraphVertex> matches = Matches.between(graph.vertices(), w[1].getTokens(), new EditDistanceTokenComparator()).getAll();

    assertEquals(4, matches.size()); // 2 matches plus start/end marker matches
    assertEquals(w[0].getTokens().get(0), Iterables.get(matches.get(w[1].getTokens().get(0)), 0).tokens().first());
    assertEquals(w[0].getTokens().get(1), Iterables.get(matches.get(w[1].getTokens().get(1)), 0).tokens().first());
  }
}
