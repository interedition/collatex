package eu.interedition.collatex.dekker.matrix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

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
	
	
}
