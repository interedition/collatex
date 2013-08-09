package eu.interedition.collatex.dekker.vectorspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.dekker.vectorspace.VectorSpace.Vector;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class VectorSpaceTest extends AbstractTest {
	private SimpleWitness createWitness(String sigil, String content) {
		return new SimpleWitness(sigil, content);
	}
	
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
	public void testMatching1() {
		SimpleWitness a = createWitness("A", " a b c x y z");
		SimpleWitness b = createWitness("B", " e a b c f g");
		VectorSpace s = new VectorSpace();
		s.fill(a, b, new EqualityTokenComparator());
		List<Vector> vectors = s.getVectors();
		assertTrue(vectors.contains(s.new Vector(1, 2, 3)));
		assertEquals(1, vectors.size());
	}
}
