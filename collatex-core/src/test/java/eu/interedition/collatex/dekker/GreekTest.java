package eu.interedition.collatex.dekker;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Transposition;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.simple.SimpleWitness;

public class GreekTest extends AbstractTest {

  //test case supplied by Troy
  @Test
  public void testTwoWitnesses() {
    SimpleWitness[] w = createWitnesses("και αποκριθεισ ειπεν αυτω ου βλεπεισ ταυτασ μεγαλασ οικοδομασ αμην λεγω σοι ο(υ μη α)φεθη ωδε λιθοσ επι λιθω (οσ ου) μη καταλυθη", "και αποκριθεισ ο ι̅σ̅ ειπεν αυτω βλεπεισ Ταυτασ τασ μεγαλασ οικοδομασ λεγω υμιν ου μη αφεθη λιθοσ επι λιθου οσ ου μη καταλυθη");
    VariantGraph graph = collate(w[0], w[1]);
    Set<Transposition> transpositions = graph.transpositions();
    assertTrue(transpositions.isEmpty());
  }
  
  //test case supplied by Troy
  @Test
  public void testThreeWitnesses() {
    SimpleWitness[] w = createWitnesses("και αποκριθεισ ειπεν αυτω ου βλεπεισ ταυτασ μεγαλασ οικοδομασ αμην λεγω σοι ο(υ μη α)φεθη ωδε λιθοσ επι λιθω (οσ ου) μη καταλυθη", "και αποκριθεισ ο ι̅σ̅ ειπεν αυτω βλεπεισ Ταυτασ τασ μεγαλασ οικοδομασ λεγω υμιν ου μη αφεθη λιθοσ επι λιθου οσ ου μη καταλυθη", "και ο ι̅σ̅ αποκριθεισ ειπεν αυτω βλεπεισ ταυτασ τασ μεγαλασ οικοδομασ ου μη αφεθη λιθοσ επι λιθον οσ ου μη καταλυθη");
    VariantGraph graph = collate(w[0], w[1], w[2]);
    Set<Transposition> transpositions = graph.transpositions();
    assertEquals(1, transpositions.size());
    Vertex transposedVertex = transpositions.iterator().next().iterator().next();
    assertEquals("[C:2:'ι̅σ̅']", transposedVertex.tokens().toString());
  }
}
