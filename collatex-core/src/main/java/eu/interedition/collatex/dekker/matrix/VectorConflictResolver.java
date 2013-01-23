package eu.interedition.collatex.dekker.matrix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;

public class VectorConflictResolver {
	private Set<Vector> vectors;
	private List<Vector> committed;
	
	//NOTE: vector class
	//NOTE: is meant as a replacement for the current Island class
	public static class Vector {
		int x;
		int y;
		int length;
		
		public Vector(int x, int y, int length) {
			this.x = x;
			this.y = y;
			this.length = length;
		}

		@Override
		public String toString() {
			return "V:("+x+","+y+"):"+length;
		}
	}

	
	public VectorConflictResolver(Set<Vector> vectors) {
		this.vectors = vectors;
		this.committed = Lists.newArrayList();
	}

	public List<Vector> orderVectorsBySizePosition() {
		Comparator<Vector> comp = new Comparator<Vector>() {
			@Override
			public int compare(Vector v1, Vector v2) {
				int i = v2.length - v1.length;
				if (i != 0) return i;
				Coordinate v1start = new Coordinate(v1.x, v1.y);
				Coordinate v2start = new Coordinate(v2.x, v2.y);
				return v2start.compareTo(v1start);
			}
		};
		List<Vector> ordered = Lists.newArrayList(vectors);
		Collections.sort(ordered, comp);
		return ordered;
	}

	// a vector is in conflict with another vector
	// if they both have the same size and are in the same
	// horizontal or vertical range
	//TODO: check length --> I think it should be length -1..
	//TODO: not enough tests apparently
	public boolean isInConflict(Vector one, Vector other) {
		Range<Integer> v1horirange = Ranges.closed(one.x, one.x+one.length);
		Range<Integer> v2horirange = Ranges.closed(other.x, other.x+other.length);
		Range<Integer> v1verirange = Ranges.closed(one.y, one.y+one.length);
		Range<Integer> v2verirange = Ranges.closed(other.y, other.y+other.length);
		return one.length == other.length&&(v1horirange.isConnected(v2horirange)||v1verirange.isConnected(v2verirange));
	}
		
	//TODO: remove duplication between this method and the next!
	public Integer getNumberOfConflictsFor(Vector v) {
		int conflicts = 0;
		for (Vector other: vectors) {
			if (other!=v&&isInConflict(v, other)) {
				conflicts++;
			}
		}
		return conflicts;
	}
	
	//TODO: test
	public List<Vector> getConflictingVectorsFor(Vector v) {
		List<Vector> conflicting = Lists.newArrayList();
		for (Vector other: vectors) {
			if (other!=v&&isInConflict(v, other)) {
				conflicting.add(other);
			}
		}
		return conflicting;
	}

	// To get the next vector to commit
	// we order the vectors based on several properties:
	// 1. length
	// 2. if multiple vectors have the same length; order on least amount
	// of conflicts
	public Vector selectPriorityVector() {
		List<Vector> prioritizedVectors = Lists.newArrayList(vectors);
		Comparator<Vector> comp = new Comparator<Vector>() {
			@Override
			public int compare(Vector one, Vector other) {
				int i = other.length - one.length;
				if (i != 0) return i;
				int oneNrOfConflicts = getNumberOfConflictsFor(one);
				int otherNrOfConflicts = getNumberOfConflictsFor(other);
				return oneNrOfConflicts - otherNrOfConflicts;
			}
		};
		Collections.sort(prioritizedVectors, comp);
		return prioritizedVectors.get(0);
	}
	
	// The intersection is the part that we want to remove
	// from the second vector
	// Note: this method only works when the other vector
	// intersects with the one vector at the end
	public Vector split(Vector one, Vector other) {
		Range<Integer> v1horirange = Ranges.closed(one.x, one.x+one.length-1);
		Range<Integer> v2horirange = Ranges.closed(other.x, other.x+other.length-1);
		Range<Integer> intersection = v1horirange.intersection(v2horirange);
		ContiguousSet<Integer> stuffthatwewantremove = intersection.asSet(DiscreteDomains.integers());
		int lengththatwewanttoremove = stuffthatwewantremove.size();
		return new Vector(other.x, other.y, other.length-lengththatwewanttoremove);
	}

	public List<Vector> commitPriorityVector() {
		Vector priority = selectPriorityVector();
		committed.add(priority);
		vectors.remove(priority);
		List<Vector> conflicting = getConflictingVectorsFor(priority);
		System.out.println(conflicting);
		for (Vector v : conflicting) {
			vectors.remove(v);
			//TODO: add the split vector!
		}
		return committed;
	}

	public Set<Vector> getUnresolvedVectors() {
		return vectors;
	}
}
