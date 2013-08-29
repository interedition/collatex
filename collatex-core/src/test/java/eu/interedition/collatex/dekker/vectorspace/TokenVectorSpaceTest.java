package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
  
  // test parallel vectors will be merging when adding 3d vector 
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
 


}
