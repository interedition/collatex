package eu.interedition.collatex.dekker;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class TranspositionGraphTest extends AbstractTest {

  @Before
  public void setup() {
    collationAlgorithm = new DekkerAlgorithm(new EqualityTokenComparator());
    ((DekkerAlgorithm)collationAlgorithm).setMergeTranspositions(true);
  }
  
  @Test
  public void transpositions() {
    final SimpleWitness[] w = createWitnesses("the black and white cat", "the white and black cat", "the black and black cat");
    final VariantGraph graph = collate(w[0], w[1]);
    assertEquals(2, graph.transpositions().size());
    collate(graph, w[2]);
    final Set<VariantGraph.Transposition> transposed = graph.transpositions();
    assertEquals(2, transposed.size());
  }

  @Test
  public void noTransposition() {
    assertEquals(0, collate("no transposition", "no transposition").transpositions().size());
    assertEquals(0, collate("a b", "c a").transpositions().size());
  }

  @Test
  public void oneTransposition() {
    assertEquals(1, collate("a b", "b a").transpositions().size());
  }

  @Test
  public void multipleTranspositions() {
    assertEquals(1, collate("a b c", "b c a").transpositions().size());
  }

  @Test
  public void testTranspositionLimiter1() {
    final SimpleWitness a = new SimpleWitness("A","X a b");
    final SimpleWitness b = new SimpleWitness("B","a b X");
    VariantGraph graph = collate(a,b);
    assertEquals(1, graph.transpositions().size());
  }
}
