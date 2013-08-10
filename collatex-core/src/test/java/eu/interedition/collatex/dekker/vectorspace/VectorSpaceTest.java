package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}
