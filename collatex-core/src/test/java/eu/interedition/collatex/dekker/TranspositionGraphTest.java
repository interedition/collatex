package eu.interedition.collatex.dekker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Sets;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Transposition;
import eu.interedition.collatex.VariantGraph.Vertex;
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
  
  //test case supplied by Troy
  @Test
  public void testGreekTwoWitnesses() {
    SimpleWitness[] w = createWitnesses(
        "και αποκριθεισ ειπεν αυτω ου βλεπεισ ταυτασ μεγαλασ οικοδομασ αμην λεγω σοι ο(υ μη α)φεθη ωδε λιθοσ επι λιθω (οσ ου) μη καταλυθη", //
        "και αποκριθεισ ο ι̅σ̅ ειπεν αυτω βλεπεισ Ταυτασ τασ μεγαλασ οικοδομασ λεγω υμιν ου μη αφεθη λιθοσ επι λιθου οσ ου μη καταλυθη");
    VariantGraph graph = collate(w[0], w[1]);
    Set<Transposition> transpositions = graph.transpositions();
    assertTrue(transpositions.isEmpty());
  }
  
  //test case supplied by Troy
  @Test
  public void testGreekThreeWitnesses() {
    SimpleWitness[] w = createWitnesses("και αποκριθεισ ειπεν αυτω ου βλεπεισ ταυτασ μεγαλασ οικοδομασ αμην λεγω σοι ο(υ μη α)φεθη ωδε λιθοσ επι λιθω (οσ ου) μη καταλυθη", "και αποκριθεισ ο ι̅σ̅ ειπεν αυτω βλεπεισ Ταυτασ τασ μεγαλασ οικοδομασ λεγω υμιν ου μη αφεθη λιθοσ επι λιθου οσ ου μη καταλυθη", "και ο ι̅σ̅ αποκριθεισ ειπεν αυτω βλεπεισ ταυτασ τασ μεγαλασ οικοδομασ ου μη αφεθη λιθοσ επι λιθον οσ ου μη καταλυθη");
    VariantGraph graph = collate(w[0], w[1], w[2]);
    Set<Transposition> transpositions = graph.transpositions();
    assertEquals(1, transpositions.size());
    Transposition transposition = transpositions.iterator().next();
    Set<String> transposedVertices = Sets.newHashSet();
    for (Vertex transposedVertex : transposition) {
      transposedVertices.add(transposedVertex.toString());
    }
    assertTrue(transposedVertices.contains("[B:2:'ο']"));
    assertTrue(transposedVertices.contains("[C:2:'ι̅σ̅']"));
  }

}
