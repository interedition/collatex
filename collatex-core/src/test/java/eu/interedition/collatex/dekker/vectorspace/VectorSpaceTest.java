package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;

public class VectorSpaceTest extends AbstractTest {
	@Test
	public void testVectorEquals() {
    VectorSpace s = new VectorSpace();
    Vector v1 = s.new Vector(1, 2);
    Vector v2 = s.new Vector(2, 3);
    Vector v3 = s.new Vector(1, 2);
    String v4 = "fake";
    assertTrue(v1.equals(v1));
    assertTrue(v1.equals(v3));
    assertFalse(v1.equals(v2));
    assertFalse(v1.equals(null));
    assertFalse(v1.equals(v4));
	}

	//TODO: add more tests
  //TODO: check partial dimensions (vectors containing 0's)
	@Test
	public void testVectorIsAdjacent() {
	  VectorSpace s = new VectorSpace();
	  Vector v1 = s.new Vector(1, 2);
	  Vector v2 = s.new Vector(2, 3);
	  assertTrue(v1.isAdjacent(v2));
	}
	
	@Test
	public void testVectorIsAdjacent2() {
	  VectorSpace s = new VectorSpace();
	  Vector v1 = s.new Vector(1, 1, 4, 4);
	  Vector v2 = s.new Vector(1, 2, 5, 5);
	  Vector v3 = s.new Vector(1, 1, 4, 1);
	  assertTrue(v1.isAdjacent(v2));
	  assertFalse(v3.isAdjacent(v2));
	}
	
  @Test
  public void testVectorIsAdjacent3() {
    VectorSpace s = new VectorSpace();
    Vector v1 = s.new Vector(1, 0, 3, 3);
    Vector v2 = s.new Vector(1, 1, 4, 4);
    assertFalse(v1.isAdjacent(v2));
  }

  @Test
	public void testVectorIsParallel() {
	  VectorSpace s = new VectorSpace();
	  Vector v1 = s.addVector(1, 1, 0);
	  Vector v2 = s.addVector(1, 0, 1);
	  assertTrue(v1.isParallel(v2));
	}
	
  @Test
  public void testVectorIsParallelDifferenceInLength() {
    VectorSpace s = new VectorSpace();
    Vector v1 = s.addVector(1, 1, 0);
    Vector v2 = s.addVector(1, 0, 1);
    v2.extendLength();
    assertFalse(v1.isParallel(v2));
  }
  
	@Test
	public void testMergeParallelVectors() {
    VectorSpace s = new VectorSpace();
    Vector v1 = s.addVector(1, 1, 0);
    Vector v2 = s.addVector(1, 0, 1);
    Vector v3 = v1.merge(v2);
    assertEquals(s.new Vector(1,1,1,1), v3);
	}
	
	// test adding 3d vector 
	@Test
	public void testAddVector() {
    VectorSpace s = new VectorSpace();
    s.addVector(1, 2, 3);
    List<Vector> vectors = s.getVectors();
    assertTrue(vectors.contains(s.new Vector(1, 1, 2, 3)));
    assertEquals(1, vectors.size());
	}
	
  @Test
  public void testVectorOverlapsPartially() {
    VectorSpace s = new VectorSpace();
    Vector v1 = s.addVector(4, 1, 0);
    Vector v2 = s.addVector(3, 3, 0);
    Vector v3 = s.addVector(1, 4, 0);
    v1.extendLength();
    v2.extendLength();
    v3.extendLength();
    assertTrue(v1.overlapsPartially(v2));
    assertFalse(v1.overlapsPartially(v3));
    assertTrue(v2.overlapsPartially(v3));
  }
  
  @Test
  public void testEmptyDimensionDoNotPartiallyOverlap() {
    VectorSpace s = new VectorSpace();
    Vector v1 = s.addVector(4, 0, 5);
    Vector v2 = s.addVector(2, 0, 4);
    assertFalse(v1.overlapsPartially(v2));
  }
}
