package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;

public class VSVariantGraphCreationTest extends AbstractTest {
  private SimpleWitness createWitness(String sigil, String content) {
    return new SimpleWitness(sigil, content);
  }
  
  @Test
  public void testCreationOfVGFromVectorSpace() {
    SimpleWitness a = new SimpleWitness("A", "a b c x y z");
    SimpleWitness b = new SimpleWitness("B", "e a b c f g");
    VariantGraph graph = new JungVariantGraph();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm();
    algo.collate(graph, a, b);
    // check the first witness
    VariantGraph.Vertex a1 = vertexWith(graph, "a", a);
    VariantGraph.Vertex a2 = vertexWith(graph, "b", a);
    VariantGraph.Vertex a3 = vertexWith(graph, "c", a);
    vertexWith(graph, "x", a);
    vertexWith(graph, "y", a);
    vertexWith(graph, "z", a);
    // check the second witness
    vertexWith(graph, "e", b);
    VariantGraph.Vertex b1 = vertexWith(graph, "a", b);
    VariantGraph.Vertex b2 = vertexWith(graph, "b", b);
    VariantGraph.Vertex b3 = vertexWith(graph, "c", b);
    vertexWith(graph, "f", b);
    vertexWith(graph, "g", b);
    // check alignment
    assertEquals(a1, b1);
    assertEquals(a2, b2);
    assertEquals(a3, b3);
  }
  
  // test taken from match table linker test
  // TODO: add asserts third witness
  @Test
  public void testCreationOfVGWith3WitnessesAndATransposition() {
    SimpleWitness textD1 = createWitness("D1", "natuurlijk is alles betrekkelijk");
    SimpleWitness textD9 = createWitness("D9", "Natuurlijk, alles mag relatief zijn");
    SimpleWitness textDmd1 = createWitness("textDmd1", "Natuurlijk, alles is betrekkelijk");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, textD1, textD9, textDmd1);
    // to start we test the creation of the variant graph with the first
    // witnesses
    // check the first witness
    VariantGraph.Vertex a1 = vertexWith(graph, "natuurlijk", textD1);
    vertexWith(graph, "is", textD1);
    VariantGraph.Vertex a3 = vertexWith(graph, "alles", textD1);
    vertexWith(graph, "betrekkelijk", textD1);
    // check the second witness
    VariantGraph.Vertex b1 = vertexWith(graph, "natuurlijk", textD9);
    vertexWith(graph, ",", textD9);
    VariantGraph.Vertex b3 = vertexWith(graph, "alles", textD9);
    vertexWith(graph, "mag", textD9);
    vertexWith(graph, "relatief", textD9);
    vertexWith(graph, "zijn", textD9);
    // check alignment
    assertEquals(a1, b1);
    assertEquals(a3, b3);
  }

// TODO: integrate transposition detection
//  //Test taken from IslandConflictResolverTest
//  @Test
//  public void testTransposition() {
//    SimpleWitness[] w = createWitnesses("The cat and the dog", "the dog and the cat");
//    VariantGraph graph = new JungVariantGraph();
//    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm();
//    algo.collate(graph, w[0], w[1]);
//    List<Vector> alignment = algo.getAlignment();
//    System.out.println(alignment);
//    fail();
//    // check the first witness
//    VariantGraph.Vertex a1 = vertexWith(graph, "a", a);
//    VariantGraph.Vertex a2 = vertexWith(graph, "b", a);
//    VariantGraph.Vertex a3 = vertexWith(graph, "c", a);
//  }
}
