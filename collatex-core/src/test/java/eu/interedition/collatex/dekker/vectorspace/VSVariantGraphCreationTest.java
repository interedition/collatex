package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Iterables;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Edge;
import eu.interedition.collatex.VariantGraph.Transposition;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;

public class VSVariantGraphCreationTest extends AbstractTest {
  private SimpleWitness createWitness(String sigil, String content) {
    return new SimpleWitness(sigil, content);
  }
  
  private void assertPhrase(String expectedPhrase, List<Token> tokensFromVector) {
    assertEquals(expectedPhrase, SimpleToken.toString(tokensFromVector));
  }
  
  void debugEdges(VariantGraph graph) {
    for (Edge e : graph.edges()) {
      System.out.println(e.from()+":"+e.to());
    }
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
  
  @Test
  public void testTransposition3Witnesses() {
    SimpleWitness textD1 = createWitness("D1", "natuurlijk is alles betrekkelijk");
    SimpleWitness textD9 = createWitness("D9", "Natuurlijk, alles mag relatief zijn");
    SimpleWitness textDmd1 = createWitness("textDmd1", "Natuurlijk, alles is betrekkelijk");
    VariantGraph graph = new JungVariantGraph();
    TokenVectorSpace s = new TokenVectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, textD1, textD9, textDmd1);
    assertEquals(1, graph.transpositions().size());
  }
    
  // test taken from match table linker test
  //TODO: a/b, b/a transpositions should be handled to succeed!
  @Test
  public void testCreationOfVGWith3WitnessesAndATransposition() {
    SimpleWitness textD1 = createWitness("D1", "natuurlijk is alles betrekkelijk");
    SimpleWitness textD9 = createWitness("D9", "Natuurlijk, alles mag relatief zijn");
    SimpleWitness textDmd1 = createWitness("textDmd1", "Natuurlijk, alles is betrekkelijk");
    VariantGraph graph = new JungVariantGraph();
    TokenVectorSpace s = new TokenVectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, textD1, textD9, textDmd1);
    // to start we test the creation of the variant graph with the first
    // witnesses
    // check the first witness
    VariantGraph.Vertex a1 = vertexWith(graph, "natuurlijk", textD1);
    vertexWith(graph, "is", textD1);
    VariantGraph.Vertex a3 = vertexWith(graph, "alles", textD1);
    VariantGraph.Vertex a4 = vertexWith(graph, "betrekkelijk", textD1);
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
    // check third witness
    VariantGraph.Vertex c1 = vertexWith(graph, "natuurlijk", textDmd1);
    vertexWith(graph, ",", textDmd1);
    vertexWith(graph, "alles", textDmd1);
    vertexWith(graph, "is", textDmd1);
    VariantGraph.Vertex c5 = vertexWith(graph, "betrekkelijk", textDmd1);
    // check alignment
    assertEquals(a1, c1);
    assertEquals(a4, c5);
    //TODO: check (b: is) -> (c: is)
  }

  //Test taken from IslandConflictResolverTest
  @Test
  public void testTransposition() {
    SimpleWitness[] w = createWitnesses("The cat and the dog", "the dog and the cat");
    VariantGraph graph = new JungVariantGraph();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm();
    algo.collate(graph, w[0], w[1]);
    Set<Transposition> transpositions = graph.transpositions();
    assertEquals(2, transpositions.size());
    assertEquals(12, Iterables.size(graph.edges()));
  }
  
  //TODO: add extra asserts! (edges)
  @Test
  public void testVariantGraphThreeWitnesses() {
    SimpleWitness textD1 = createWitness("D1", "a b");
    SimpleWitness textD9 = createWitness("D9", "a");
    SimpleWitness textDmd1 = createWitness("textDmd1", "b");
    VariantGraph graph = new JungVariantGraph();
    TokenVectorSpace s = new TokenVectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, textD1, textD9, textDmd1);
    assertEquals(4, Iterables.size(graph.vertices()));
  }
  
  //TODO: add more asserts! (black, cat)
  @Test
  public void testCreationVariantGraphThreeWitnesses() {
    SimpleWitness a = createWitness("A", "The black cat");
    SimpleWitness b = createWitness("B", "The black and white cat");
    SimpleWitness c = createWitness("C", "The black and green cat");
    VariantGraph graph = new JungVariantGraph();
    TokenVectorSpace s = new TokenVectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, a, b, c);
    // first assert that the is the same vertex in all three witnesses
    VariantGraph.Vertex the1 = vertexWith(graph, "the", a);
    VariantGraph.Vertex the2 = vertexWith(graph, "the", b);
    VariantGraph.Vertex the3 = vertexWith(graph, "the", c);
    assertEquals(the1, the2);
    assertEquals(the2, the3);
    // second assert that the and is same vertex in b and c
    VariantGraph.Vertex and1 = vertexWith(graph, "and", b);
    VariantGraph.Vertex and2 = vertexWith(graph, "and", c);
    assertEquals(and1, and2);
  }
  
  @Test
  public void testGetTokensFromVector() {
    SimpleWitness a = new SimpleWitness("A", "a b c x y z");
    SimpleWitness b = new SimpleWitness("B", "e a b c f g");
    TokenVectorSpace s = new TokenVectorSpace();
    VSToVGBuilder builder = new VSToVGBuilder(s);
    Vector v = s.new Vector(3, 1, 2);
    assertPhrase("a b c", builder.getTokensFromVector(v, 0, a));
    assertPhrase("a b c", builder.getTokensFromVector(v, 1, b));
  }
}
