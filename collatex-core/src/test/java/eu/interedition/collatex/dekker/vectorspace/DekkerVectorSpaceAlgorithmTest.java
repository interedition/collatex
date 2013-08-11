package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;

public class DekkerVectorSpaceAlgorithmTest extends AbstractTest {

  private SimpleWitness createWitness(String sigil, String content) {
    return new SimpleWitness(sigil, content);
  }
  
  private void assertPhrase(String expectedPhrase, List<Token> tokensFromVector) {
    assertEquals(expectedPhrase, SimpleToken.toString(tokensFromVector));
  }

  @Test
  public void testCreatingOfVectorSpace() {
    SimpleWitness a = createWitness("A", "a b c x y z");
    SimpleWitness b = createWitness("B", "e a b c f g");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, a, b);
    List<Vector> alignment = algo.getAlignment();
    assertTrue(alignment.contains(s.new Vector(3, 1, 2, 0)));
    assertEquals(1, alignment.size());
  }

  @Test
  public void testGetTokensFromVector() {
    SimpleWitness a = new SimpleWitness("A", "a b c x y z");
    SimpleWitness b = new SimpleWitness("B", "e a b c f g");
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm();
    VectorSpace s = new VectorSpace();
    Vector v = s.new Vector(3, 1, 2);
    assertPhrase("a b c", algo.getTokensFromVector(v, 0, a));
    assertPhrase("a b c", algo.getTokensFromVector(v, 1, b));
  }
  
  // Test taken from IslandConflictResolverTest
  // Note: the 3 vectors of size 2 overlap partly
  @Test
  public void testPartlyOverlappingIslands() {
    SimpleWitness[] w = createWitnesses("The cat and the dog", "the dog and the cat");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, w[0], w[1]);
    List<Vector> alignment = algo.getAlignment();
    assertTrue(alignment.contains(s.new Vector(2, 4, 1, 0)));
    assertTrue(alignment.contains(s.new Vector(1, 3, 3, 0)));
    assertTrue(alignment.contains(s.new Vector(2, 1, 4, 0)));
    assertEquals(3, alignment.size());
  }
  
  // test taken from match table linker
  @Test
  public void testUsecase1() {
    final SimpleWitness[] w = createWitnesses("The black cat", "The black and white cat");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, w[0], w[1]);
    List<Vector> alignment = algo.getAlignment();
    assertTrue(alignment.contains(s.new Vector(2, 1, 1, 0)));
    assertTrue(alignment.contains(s.new Vector(1, 3, 5, 0)));
    assertEquals(2, alignment.size());
  }
  
  // test taken from match table linker
  @Test
  public void testGapsEverythingEqual() {
    // All the witness are equal
    // There are choices to be made however, since there is duplication of tokens
    // Optimal alignment has no gaps
    final SimpleWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, w[0], w[1]);
    List<Vector> alignment = algo.getAlignment();
    assertTrue(alignment.contains(s.new Vector(7, 1, 1, 0)));
    assertEquals(1, alignment.size());
  }

  // test taken from match table linker
  @Test
  public void testGapsOmission() {
    // There is an omission
    // Optimal alignment has 1 gap
    final SimpleWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, w[0], w[1]);
    List<Vector> alignment = algo.getAlignment();
    assertTrue(alignment.contains(s.new Vector(3, 5, 1, 0)));
    assertEquals(1, alignment.size());
  }
  
  // test taken from match table linker test
  /*
   * This tests test the creation of a 3d dimension vector space by comparing 3 witnesses against each other.
   */
  @Test
  public void testHermansAllesIsBetrekkelijk1() {
    SimpleWitness textD1 = createWitness("D1", "natuurlijk is alles betrekkelijk");
    SimpleWitness textD9 = createWitness("D9", "Natuurlijk, alles mag relatief zijn");
    SimpleWitness textDmd1 = createWitness("textDmd1", "Natuurlijk, alles is betrekkelijk");
    VariantGraph graph = new JungVariantGraph();
    VectorSpace s = new VectorSpace();
    DekkerVectorSpaceAlgorithm algo = new DekkerVectorSpaceAlgorithm(s);
    algo.collate(graph, textD1, textD9, textDmd1);
    List<Vector> alignment = algo.getAlignment();
    assertTrue(alignment.contains(s.new Vector(1, 1, 1, 1)));
    assertTrue(alignment.contains(s.new Vector(1, 3, 3, 3)));
    assertTrue(alignment.contains(s.new Vector(1, 2, 0, 4)));
    assertTrue(alignment.contains(s.new Vector(1, 4, 0, 5)));
    assertTrue(alignment.contains(s.new Vector(1, 0, 2, 2)));
    assertEquals(5, alignment.size());
  }

}
