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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.lmnl.Annotation;
import org.lmnl.Range;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * Function indexing range annotations based on a {@link Partitioning
 * partitioning} of their covered text segment.
 * 
 * <p/>
 * 
 * Indizes created by this function can help in determining markup applicable to
 * non-overlapping segments of a text with the segments constructed such, that
 * variance in markup can be recognized.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class OverlapIndexer implements Function<Iterable<Annotation>, SortedMap<Range, List<Annotation>>> {

	private final Predicate<Annotation> partitioningFilter;

	/**
	 * Creates an indexing function, that fully partitions given range
	 * annotation collections.
	 * 
	 * @see #OverlapIndexer(Predicate)
	 */
	public OverlapIndexer() {
		this(null);
	}

	/**
	 * Creates an indexing function with the given predicate determining the
	 * subset of range annotations used for partitioning.
	 * 
	 * @param partitioningFilter
	 *                the filter predicate used for determining the index
	 *                keys
	 */
	public OverlapIndexer(Predicate<Annotation> partitioningFilter) {
		this.partitioningFilter = partitioningFilter;
	}

	public SortedMap<Range, List<Annotation>> apply(Iterable<Annotation> from) {
		return Functions.compose(new Indexer(from), new Partitioning(partitioningFilter)).apply(from);
	}

	private static class Indexer implements Function<SortedSet<Range>, SortedMap<Range, List<Annotation>>> {

		private final Iterable<? extends Annotation> entries;

		private Indexer(Iterable<? extends Annotation> entries) {
			this.entries = entries;

		}

		public SortedMap<Range, List<Annotation>> apply(SortedSet<Range> from) {
			List<Annotation> annotations = Lists.newArrayList(entries);
			final SortedMap<Range, List<Annotation>> index = new TreeMap<Range, List<Annotation>>();

			for (Range segment : from) {
				ArrayList<Annotation> overlapping = new ArrayList<Annotation>();
				for (Iterator<Annotation> annotationIt = annotations.iterator(); annotationIt.hasNext();) {
					Annotation annotation = annotationIt.next();
					Range tr = annotation.getRange();

					if (tr.hasOverlapWith(segment) || tr.getStart() == segment.getStart()) {
						overlapping.add(annotation);
					}

					if (tr.precedes(segment)) {
						annotationIt.remove();
					}
				}
				index.put(segment, overlapping);
			}

			return index;
		}

	}
}
