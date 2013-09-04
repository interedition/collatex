package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.simple.SimpleWitness;

public class TokenVectorSpaceTest {

  private void assertContains(List<Vector> vectors, Vector vector) {
    assertTrue(vectors.contains(vector));
  }

  private SimpleWitness createWitness(String sigil, String content) {
    return new SimpleWitness(sigil, content);
  }
  
  // test parallel vectors will be merging when adding 3d vector 
 @Ignore
 @Test
 public void testMergeParallelVectors() {
   TokenVectorSpace s = new TokenVectorSpace();
   s.addVector(1, 1, 0);
   s.addVector(1, 0, 1);
   s.mergeAdjacentVectors();
   List<Vector> vectors = s.getVectors();
   assertTrue(vectors.contains(s.new Vector(1, 1, 1, 1)));
   assertEquals(1, vectors.size());
 }

 @Test
  public void testCreationVSThreeWitnesses2() {
    SimpleWitness a = createWitness("A", "The black cat");
    SimpleWitness b = createWitness("B", "The black and white cat");
    SimpleWitness c = createWitness("C", "The black and green cat");
    TokenVectorSpace s = new TokenVectorSpace();
    s.addWitnesses(a, b, c);
    List<Vector> vectors = s.getVectors();
    assertContains(vectors, s.new Vector(2, 1, 1, 1));
    assertContains(vectors, s.new Vector(1, 0, 3, 3));
    assertContains(vectors, s.new Vector(1, 3, 5, 5));
    assertEquals(3, vectors.size());
  }
  
  // There should be a vector of length 6
  // I would expect two blocks:
  // 1) The black (a, b)
  // 2) The black cat on the table (a, b)
  @Test
  public void testCreationVSRepetition() {
    SimpleWitness a = createWitness("A", "The black cat on the table");
    SimpleWitness b = createWitness("B", "The black saw the black cat on the table");
    SimpleWitness c = createWitness("C", "");
    TokenVectorSpace s = new TokenVectorSpace();
    s.addWitnesses(a, b, c);
    List<Vector> vectors = s.getVectors();
    assertContains(vectors, s.new Vector(6, 1, 4, 0));
    assertContains(vectors, s.new Vector(2, 1, 1, 0));
    // some more length 1 vectors which are noise caused by repetition
    assertEquals(5, vectors.size());
  }
  
  
  // There should be a vector of length 6
  // I would expect three blocks:
  // 1) The black (a, b, c)
  // 2) The black cat on the table (a, b, c)
  // 3) saw (b, c)
  //Note: repetition in tokens causes overlap in vectors
  @Test
  public void testCreationVSRepetition2() {
    SimpleWitness a = createWitness("A", "The black cat on the table");
    SimpleWitness b = createWitness("B", "The black saw the black cat on the table");
    SimpleWitness c = createWitness("C", "The black saw the black cat on the table");
    TokenVectorSpace s = new TokenVectorSpace();
    s.addWitnesses(a, b, c);
    List<Vector> vectors = s.getVectors();
    assertContains(vectors, s.new Vector(6, 1, 4, 4));
    assertContains(vectors, s.new Vector(2, 1, 1, 1));
    assertContains(vectors, s.new Vector(1, 0, 3, 3));
  }
}
