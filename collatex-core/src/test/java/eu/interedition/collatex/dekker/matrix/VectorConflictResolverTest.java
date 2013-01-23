package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;

public class VectorConflictResolverTest {
	@Test
	public void testSortingPartlyOverlappingVectors() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		//test
		List<Vector> sorted = resolver.orderVectorsBySizePosition();
		List<Vector> expected = Lists.newArrayList(v4, v3, v2, v1);
		assertEquals(expected, sorted);
	}
	
	@Test
	public void testConflictingVectors() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		//test
		assertTrue(!resolver.isInConflict(v1, v4)); //not the same length
		assertTrue(!resolver.isInConflict(v1, v3)); //not the same length
		assertTrue(!resolver.isInConflict(v1, v2)); //not the same length
		assertTrue(resolver.isInConflict(v2, v3));
		assertTrue(resolver.isInConflict(v3, v4));
	}

	@Test
	public void testNumberOfConflicts() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		//test
		//TODO: boxing?
		assertEquals(0, (int)resolver.getNumberOfConflictsFor(v1));
		assertEquals(1, (int)resolver.getNumberOfConflictsFor(v2));
		assertEquals(2, (int)resolver.getNumberOfConflictsFor(v3));
		assertEquals(1, (int)resolver.getNumberOfConflictsFor(v4));
	}
	
	@Test
	public void testPriority() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		//test
		Vector result = resolver.selectPriorityVector();
		assertTrue(result.toString(), result.equals(v2) || result.equals(v4));
	}
	
	@Test
	public void testSplitVectors() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		Vector result = resolver.split(v2, v3);
		assertEquals(1, result.length);
		assertEquals(2, result.x);
		assertEquals(2, result.y);
	}
	
	@Test
	public void testCommitFirstVector() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		List<Vector> committed = resolver.commitPriorityVector();
		// 1. first find the highest priority vector
		// 2. add the vector to the committed vector list
		// 3. remove it from the unresolved vectors set
		// 4. split conflicting vectors up into smaller parts
		// NOTE: the first one could either be v2 of be v4
		Vector firstCo = committed.get(0);
		assertTrue(firstCo.equals(v2)||firstCo.equals(v4));
		Set<Vector> unresolved = resolver.getUnresolvedVectors();
		// first test that the committed is no longer unresolved
		assertTrue(!unresolved.contains(firstCo));
		// now depending on the one or the other the remaining vectors differ
		// in any case the conflicting vector should be removed
		assertTrue(!unresolved.contains(v3));
		// and then replaced by a new vector, which is split off from the old one
		// TODO: System.out.println(unresolved);
		// TODO: fail();
	}
	
	
//	@Test
//	public void testIdealLine() {
//		// prepare incoming vectors;
//		Vector v1 = new Vector(0,0,1);
//		Vector v2 = new Vector(3,0,2);
//		Vector v3 = new Vector(2,2,2);
//		Vector v4 = new Vector(0,3,2);
//		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
//		// create resolver
//		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
//		//test
//		// hier wil ik behavior driven development doen
//		// er moet gecommit worden op vectors
//		// die vectors kunnen ook nieuwe vectors zijn..
//		// door middel van conflicts kunnen bestaande eilanden conflicteren 
//		// en dus opgesplitst moeten worden..
//		// eigenlijk wil ik dat opsplitsen eerst testen
//		Vector 
//		
//		Vector result = resolver.selectPriorityVector();
//		assertTrue(result.toString(), result.equals(v2) || result.equals(v4));
//	}

}
