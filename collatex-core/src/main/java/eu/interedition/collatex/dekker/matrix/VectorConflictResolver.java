/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.dekker.matrix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Objects;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import eu.interedition.collatex.dekker.matrix.VectorConflictResolver.Vector;

public class VectorConflictResolver {
  Logger LOG = Logger.getLogger(VectorConflictResolver.class.getName());
	private Set<Vector> vectors;
	
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
			if (length==0) {
				throw new RuntimeException("Lenght can not be zero!");
			}
		}

		@Override
		public String toString() {
			return "V:("+x+","+y+"):"+length;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Vector)) {
				return false;
			}
			Vector other = (Vector) obj;
			return x==other.x&&y==other.y&&length==other.length;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(x, y, length);
		}
	}

	
	public VectorConflictResolver(Set<Vector> vectors) {
		this.vectors = vectors;
	}

	public Set<Vector> getUnresolvedVectors() {
		return vectors;
	}

	//This method returns all the vectors that are in range
	//of the given Vector
	//There multiple possible relations:
	//1) The given Vector can overpower the other Vector
	//2) The given Vector can partly overlap with the other Vector
	//3) The given Vector can directly compete with the other Vector
	//4) The given can lay outside of the range of the other Vector
	//   Those will not be returned
	public Map<Vector, VectorRelation> getRelatedVectors(Vector vector) {
		//TODO: this implementation is too simple..
		//TODO: first we need more unit tests!
		Map<Vector, VectorRelation> relations = Maps.newHashMap();
		List<Vector> conflicting = getConflictingVectorsFor(vector);
		for (Vector v : conflicting) {
			relations.put(v, VectorRelation.COMPETING);
		}
		return relations;
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
		Range<Integer> v1horirange = getHorizontalRange(one);
		Range<Integer> v2horirange = getHorizontalRange(other);
		Range<Integer> v1verirange = getVerticalRange(one);
		Range<Integer> v2verirange = getVerticalRange(other);
		return !(one.equals(other))&&one.length == other.length&&(v1horirange.isConnected(v2horirange)||v1verirange.isConnected(v2verirange));
	}

	private Range<Integer> getVerticalRange(Vector one) {
		return Ranges.closed(one.y, one.y+one.length-1);
	}

	private Range<Integer> getHorizontalRange(Vector one) {
		return Ranges.closed(one.x, one.x+one.length-1);
	}

	public boolean isOverpowered(Vector one, Vector other) {
		Range<Integer> v1horirange = getHorizontalRange(one);
		Range<Integer> v2horirange = getHorizontalRange(other);
		Range<Integer> v1verirange = getVerticalRange(one);
		Range<Integer> v2verirange = getVerticalRange(other);
		return v1horirange.encloses(v2horirange) || v1verirange.encloses(v2verirange);
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
	
	//TODO: test (only tested implicitly by getNumberofConflicts
	public List<Vector> getConflictingVectorsFor(Vector v) {
		List<Vector> conflicting = Lists.newArrayList();
		for (Vector other: vectors) {
			if (isInConflict(v, other)) {
				conflicting.add(other);
			}
		}
		return conflicting;
	}

	public List<Vector> getOverpoweredVectors(Vector v) {
		List<Vector> overpowered = Lists.newArrayList();
		for (Vector other: vectors) {
			if (other!=v&&isOverpowered(v, other)) {
				overpowered.add(other);
			}
		}
		return overpowered;
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
		LOG.log(Level.INFO, "Splitting "+one+":"+"with "+other);
		int lengththatwewanttoremove;
		// Note there can be a horizontal or vertical intersection
		// first check horizontal intersection
		Range<Integer> v1horirange = Ranges.closed(one.x, one.x+one.length-1);
		Range<Integer> v2horirange = Ranges.closed(other.x, other.x+other.length-1);
		if (v1horirange.isConnected(v2horirange)) {
			Range<Integer> intersection = v1horirange.intersection(v2horirange);
			ContiguousSet<Integer> stuffthatwewantremove = intersection.asSet(DiscreteDomains.integers());
			lengththatwewanttoremove = stuffthatwewantremove.size();
		} else {
			// check vertical intersection
			Range<Integer> v1vertirange = Ranges.closed(one.y, one.y+one.length-1);
			Range<Integer> v2vertirange = Ranges.closed(other.y, other.y+other.length-1);
			if (v1vertirange.isConnected(v2vertirange)) {
				Range<Integer> intersection = v1vertirange.intersection(v2vertirange);
				ContiguousSet<Integer> stuffthatwewantremove = intersection.asSet(DiscreteDomains.integers());
				lengththatwewanttoremove = stuffthatwewantremove.size();
			} else {
				throw new RuntimeException("There is no horizontal or vertical intersection!");
			}
		}
		return new Vector(other.x, other.y, other.length-lengththatwewanttoremove);
	}

	public Vector commitPriorityVector() {
		Vector priority = selectPriorityVector();
		LOG.log(Level.INFO, "Notify: vector about to commit: "+priority);
//		committed.add(priority);
		vectors.remove(priority);
		List<Vector> conflicting = getConflictingVectorsFor(priority);
		for (Vector v : conflicting) {
			vectors.remove(v);
			//TODO: The differences types of conflict should be made
			//more explicit
			//and handled differently
			//just separating them by start position is crude
			if (v.x!=priority.x&&v.y!=priority.y) {
				Vector split = this.split(priority, v);
				vectors.add(split);
			}
		}
		List<Vector> overpowered = getOverpoweredVectors(priority);
		for (Vector v : overpowered) {
			vectors.remove(v);
		}
		return priority;
	}

	public List<Vector> resolveConflicts() {
		List<Vector> committedV = Lists.newArrayList();
		while(!vectors.isEmpty()) {
			Vector committed = commitPriorityVector();
			committedV.add(committed);
		}
		return committedV;
	}
}