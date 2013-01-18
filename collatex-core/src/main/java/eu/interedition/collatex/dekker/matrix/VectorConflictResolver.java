package eu.interedition.collatex.dekker.matrix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.RowSortedTable;

import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;

public class VectorConflictResolver {

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

	private Set<Vector> vectors;
	
	public VectorConflictResolver(Set<Vector> vectors) {
		this.vectors = vectors;
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
	public boolean isInConflict(Vector one, Vector other) {
		Range<Integer> v1horirange = Ranges.closed(one.x, one.x+one.length);
		Range<Integer> v2horirange = Ranges.closed(other.x, other.x+other.length);
		Range<Integer> v1verirange = Ranges.closed(one.y, one.y+one.length);
		Range<Integer> v2verirange = Ranges.closed(other.y, other.y+other.length);
		return one.length == other.length&&(v1horirange.isConnected(v2horirange)||v1verirange.isConnected(v2verirange));
	}
		
	public Integer getNumberOfConflictsFor(Vector v) {
		int conflicts = 0;
		for (Vector other: vectors) {
			if (other!=v&&isInConflict(v, other)) {
				conflicts++;
			}
		}
		return conflicts;
	}

	public RowSortedTable<Vector, Vector, Boolean> getConflictsTable() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
