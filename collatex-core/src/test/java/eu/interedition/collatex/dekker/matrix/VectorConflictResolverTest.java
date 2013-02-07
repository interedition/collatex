package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;

public class VectorConflictResolverTest {
	private Set<Vector> example2() {
		Set<Vector> vectors = Sets.newHashSet();
		vectors.add(new Vector(0,0,1));
		vectors.add(new Vector(0,7,1));
		vectors.add(new Vector(0,14,1));
		vectors.add(new Vector(2,13,1));
		vectors.add(new Vector(3,10,2));
		vectors.add(new Vector(5,8,2));
		vectors.add(new Vector(7,0,1));
		vectors.add(new Vector(7,7,1));
		vectors.add(new Vector(7,14,1));
		vectors.add(new Vector(9,16,6));
		return vectors;
	}

	@Test
	public void testVectorRelationsCompetingVectors() {
		Set<Vector> vectors = example2();
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		Map<Vector, VectorRelation> relations = resolver.getRelatedVectors(new Vector(0,0,1));
		assertEquals(VectorRelation.COMPETING, relations.get(new Vector(0,7,1)));
		assertEquals(VectorRelation.COMPETING, relations.get(new Vector(0,14,1)));
		assertEquals(VectorRelation.COMPETING, relations.get(new Vector(7,0,1)));
		assertEquals(3, relations.size());
	}
	
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
	public void testConflictingVectors2() {
		Set<Vector> vectors = example2();
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		Vector v2a = new Vector(5,8,2);
		List<Vector> conflicts = resolver.getConflictingVectorsFor(v2a);
		assertTrue("No conflicts were expected! but found "+conflicts, conflicts.isEmpty());
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
	public void testSplitVectors1() {
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
	public void testSplitVectors2() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		Vector result = resolver.split(v4, v3);
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
		Vector committed = resolver.commitPriorityVector();
		// 1. first find the highest priority vector
		// 2. add the vector to the committed vector list
		// 3. remove it from the unresolved vectors set
		// 4. split conflicting vectors up into smaller parts
		// 5. Remove overpowered vectos
		// NOTE: the first one could either be v2 of be v4
		assertTrue(committed.equals(v2)||committed.equals(v4));
		Set<Vector> unresolved = resolver.getUnresolvedVectors();
		// first test that the committed vector is no longer unresolved
		assertTrue(!unresolved.contains(committed));
		// now depending on the one or the other the remaining vectors differ
		// in any case the conflicting vector should be removed
		assertTrue(!unresolved.contains(v3));
		// and then replaced by a new vector, which is split off from the old one
		assertTrue(unresolved.contains(new Vector(2,2,1)));
		assertEquals(2, unresolved.size());
	}
	
	
	@Test
public void testIsOverpowered() {
	// prepare incoming vectors;
	Vector v1 = new Vector(0,0,1);
	Vector v2 = new Vector(3,0,2);
	Vector v3 = new Vector(2,2,2);
	Vector v4 = new Vector(0,3,2);
	Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
	// create resolver
	VectorConflictResolver resolver = new VectorConflictResolver(vectors);
	// v2 is the one being considered for a commit
	// v1 is the one who should be removed
	assertTrue(resolver.isOverpowered(v2, v1));	
	// v3 is partly overlapping with v2.. so it does not overpower it
	assertTrue(!resolver.isOverpowered(v2, v3));
}
	
	@Test
	public void testFindOverpoweredVectors() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		List<Vector> result = resolver.getOverpoweredVectors(v2);
		assertEquals(v1, result.get(0));
		assertEquals(1, result.size());
	}
	
	
	
	@Test
	public void testIdealLine() {
		// prepare incoming vectors;
		Vector v1 = new Vector(0,0,1);
		Vector v2 = new Vector(3,0,2);
		Vector v3 = new Vector(2,2,2);
		Vector v4 = new Vector(0,3,2);
		Set<Vector> vectors = Sets.newHashSet(v1, v2, v3, v4);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		List<Vector> resolved = resolver.resolveConflicts();
		assertTrue(resolved.get(0).equals(v2)||resolved.get(0).equals(v4));
		assertTrue(resolved.get(1).equals(v2)||resolved.get(1).equals(v4));
		assertTrue(resolved.get(2).equals(new Vector(2,2,1)));
		assertEquals(3, resolved.size());
	}
	
	
	//TODO: this test does not work correctly yet!
	//TODO; after fixing subproblems
	//TODO; add the correct asserts
	@Ignore
	@Test 
	public void testIdealLine2() {
    Set<Vector> vectors = example2();
		Vector v2a = new Vector(5,8,2);
		// create resolver
		VectorConflictResolver resolver = new VectorConflictResolver(vectors);
		List<Vector> result = resolver.resolveConflicts();
		System.out.println(result);
		Iterator<Vector> i = result.iterator();
//		Vector v = resolver.commitPriorityVector();
//		v =	resolver.selectPriorityVector();
//		assertEquals(v2a, v);
//		List<Vector> conflictingVectorsFor = resolver.getConflictingVectorsFor(v);
//		System.out.println(conflictingVectorsFor);
		assertEquals(new Vector(9,16,6), i.next());
		assertEquals(new Vector(5,8,2), i.next());
		assertEquals(new Vector(3,10,2), i.next());
		assertEquals(new Vector(2,13,1), i.next());
	}

	
}
