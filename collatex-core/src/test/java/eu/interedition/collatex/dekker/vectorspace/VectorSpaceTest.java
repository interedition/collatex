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
	
	@Test
	public void testVectorIsAdjacent() {
	  VectorSpace s = new VectorSpace();
	  Vector v1 = s.new Vector(1, 2);
	  Vector v2 = s.new Vector(2, 3);
	  assertTrue(v1.isAdjacent(v2));
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
	
	 // test parallel vectors will be merging when adding 3d vector 
  @Test
  public void testAddVectorMergeParallelVectors() {
    VectorSpace s = new VectorSpace();
    s.addVector(1, 1, 0);
    s.addVector(1, 0, 1);
    List<Vector> vectors = s.getVectors();
    assertTrue(vectors.contains(s.new Vector(1, 1, 1, 1)));
    assertEquals(1, vectors.size());
  }
}
