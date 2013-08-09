package eu.interedition.collatex.dekker.vectorspace;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;

public class DekkerVectorSpaceAlgorithmTest extends AbstractTest {
  
  @Test
  public void testCreationOfVGFromVectorSpace() {
    SimpleWitness a = new SimpleWitness("A", "a b c x y z");
    SimpleWitness b = new SimpleWitness("B", "e a b c f g");
    VariantGraph graph = new JungVariantGraph();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm();
    algo.collate(graph, a, b);
    // check the first witness
    vertexWith(graph, "a", a);
    vertexWith(graph, "b", a);
    vertexWith(graph, "c", a);
    vertexWith(graph, "x", a);
    vertexWith(graph, "y", a);
    vertexWith(graph, "z", a);
  }

}
