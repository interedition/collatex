package org.lmnl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class Ranges {
	public static final Ordering<Range> START_ORDERING = Ordering.from(new Comparator<Range>() {

		public int compare(Range o1, Range o2) {
			return o1.getStart() - o2.getStart();
		}
	});

	public static final Ordering<Range> END_ORDERING = Ordering.from(new Comparator<Range>() {

		public int compare(Range o1, Range o2) {
			return o2.getEnd() - o1.getEnd();
		}
	});

	public static final Ordering<Range> NATURAL_ORDERING = Ordering.natural();

	public static SortedSet<Range> compressAdjacent(SortedSet<Range> ranges) {
		final SortedSet<Range> compressed = Sets.newTreeSet();

		Range current = null;
		for (Iterator<Range> rangeIt = ranges.iterator(); rangeIt.hasNext();) {
			final Range range = rangeIt.next();
			if (current == null) {
				current = new Range(range);
			} else {
				if (current.getEnd() == range.getStart()) {
					current.setEnd(range.getEnd());
				} else {
					compressed.add(current);
					current = new Range(range);
				}
			}
			if (!rangeIt.hasNext()) {
				compressed.add(current);
			}
		}

		return compressed;
	}

	public static int length(SortedSet<Range> ranges) {
		int length = 0;
		for (Range r : ranges) {
			length += r.length();
		}
		return length;
	}

	public static List<Range> exclude(Iterable<Range> ranges, List<Range> excluded) {
		excluded = START_ORDERING.sortedCopy(excluded);
		
		final List<Range> result = START_ORDERING.sortedCopy(ranges);
		for (ListIterator<Range> it = result.listIterator(); it.hasNext();) {
			final Range r = it.next();
                        it.remove();

			for (Iterator<Range> exIt = excluded.iterator(); exIt.hasNext();) {
				final Range ex = exIt.next();
				if (ex.precedes(r)) {
					exIt.remove();
				} else if (r.precedes(ex)) {
					continue;
				}
				for (Range remainder : r.substract(ex)) {
					it.add(remainder);
				}

			}
		}
		return result;
	}
}
