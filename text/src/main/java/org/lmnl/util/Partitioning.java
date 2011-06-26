/**
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lmnl.util;

import java.util.SortedSet;
import java.util.TreeSet;

import org.lmnl.Annotation;
import org.lmnl.Range;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Function deriving a set of non-overlapping segments from an (optionally
 * filtered) collection of range annotations.
 * 
 * <p/>
 * 
 * The segments in the resulting set strictly follow each other and are derived
 * from range boundaries in the argument. Thus the segments partition the text
 * segment covered by all given range annotations.
 * 
 * <p/>
 * 
 * A common use case for such segments is the {@link OverlapIndexer indexing of
 * overlapping range annotations} or the construction of new, non-overlapping
 * range annotations according to the partitioning.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class Partitioning implements Function<Iterable<Annotation>, SortedSet<Range>> {

	private final Predicate<Annotation> filterPredicate;

	/**
	 * Creates a function doing complete/ unfiltered partitioning of passed
	 * range annotation collections.
	 * 
	 * @see #Partitioning(Predicate)
	 */
	public Partitioning() {
		this(null);
	}

	/**
	 * Creates a partitioning function with the input collection of range
	 * annotations being filtered.
	 * 
	 * @param filterPredicate
	 *                a filter predicate to determine the subset of range
	 *                annotations constituting the partitioning
	 */
	public Partitioning(Predicate<Annotation> filterPredicate) {
		this.filterPredicate = filterPredicate;
	}

	public SortedSet<Range> apply(Iterable<Annotation> from) {
		SortedSet<Integer> offsets = Sets.newTreeSet();

		if (filterPredicate != null) {
			from = Iterables.filter(from, filterPredicate);
		}

		for (Annotation a : from) {
			offsets.add(a.getRange().getStart());
			offsets.add(a.getRange().getEnd());
		}

		SortedSet<Range> partition = new TreeSet<Range>();
		int start = -1;
		for (int end : offsets) {
			if (start >= 0) {
				partition.add(new Range(start, end));
			}
			start = end;
		}
		return partition;
	}

}
