/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text.util;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.interedition.text.Range;

import java.util.*;

public class Ranges {
  public static final Ordering<Range> START_ORDERING = Ordering.from(new Comparator<Range>() {

    public int compare(Range o1, Range o2) {
      final long result = o1.getStart() - o2.getStart();
      return (result < 0 ? -1 : (result > 0 ? 1 : 0));
    }
  });

  public static final Ordering<Range> END_ORDERING = Ordering.from(new Comparator<Range>() {

    public int compare(Range o1, Range o2) {
      final long result = o2.getEnd() - o1.getEnd();
      return (result < 0 ? -1 : (result > 0 ? 1 : 0));
    }
  });

  public static final Ordering<Range> NATURAL_ORDERING = Ordering.natural();

  public static SortedSet<Range> compressAdjacent(SortedSet<Range> ranges) {
    final SortedSet<Range> compressed = Sets.newTreeSet();

    Range current = null;
    for (Iterator<Range> rangeIt = ranges.iterator(); rangeIt.hasNext(); ) {
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
    for (ListIterator<Range> it = result.listIterator(); it.hasNext(); ) {
      final Range r = it.next();
      it.remove();

      for (Iterator<Range> exIt = excluded.iterator(); exIt.hasNext(); ) {
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
