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
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;

import java.util.*;

public class Ranges {
  public static final Ordering<TextRange> START_ORDERING = Ordering.from(new Comparator<TextRange>() {

    public int compare(TextRange o1, TextRange o2) {
      final long result = o1.getStart() - o2.getStart();
      return (result < 0 ? -1 : (result > 0 ? 1 : 0));
    }
  });

  public static final Ordering<TextRange> END_ORDERING = Ordering.from(new Comparator<TextRange>() {

    public int compare(TextRange o1, TextRange o2) {
      final long result = o2.getEnd() - o1.getEnd();
      return (result < 0 ? -1 : (result > 0 ? 1 : 0));
    }
  });

  public static final Ordering<TextTarget> NATURAL_ORDERING = Ordering.natural();

  public static SortedSet<TextRange> compressAdjacent(SortedSet<TextRange> ranges) {
    final SortedSet<TextRange> compressed = Sets.newTreeSet();

    TextRange current = null;
    for (Iterator<TextRange> rangeIt = ranges.iterator(); rangeIt.hasNext(); ) {
      final TextRange range = rangeIt.next();
      if (current == null) {
        current = new TextRange(range);
      } else {
        if (current.getEnd() == range.getStart()) {
          current = new TextRange(current.getStart(), range.getEnd());
        } else {
          compressed.add(current);
          current = new TextRange(range);
        }
      }
      if (!rangeIt.hasNext()) {
        compressed.add(current);
      }
    }

    return compressed;
  }

  public static SortedSet<TextRange> compress(SortedSet<TextRange> ranges) {
    final SortedSet<TextRange> compressed = Sets.newTreeSet();

    TextRange current = null;
    for (Iterator<TextRange> rangeIt = ranges.iterator(); rangeIt.hasNext(); ) {
      final TextRange range = rangeIt.next();
      if (current == null) {
        current = new TextRange(range);
      } else {
        if (current.getEnd() >= range.getStart()) {
          current = new TextRange(current.getStart(), Math.max(current.getEnd(), range.getEnd()));
        } else {
          compressed.add(current);
          current = new TextRange(range);
        }
      }
      if (!rangeIt.hasNext()) {
        compressed.add(current);
      }
    }

    return compressed;
  }

  public static int length(SortedSet<TextTarget> ranges) {
    int length = 0;
    for (TextTarget r : ranges) {
      length += r.length();
    }
    return length;
  }

  public static List<TextRange> exclude(Iterable<TextRange> ranges, List<TextRange> excluded) {
    excluded = START_ORDERING.sortedCopy(excluded);

    final List<TextRange> result = START_ORDERING.sortedCopy(ranges);
    for (ListIterator<TextRange> it = result.listIterator(); it.hasNext(); ) {
      final TextRange r = it.next();
      it.remove();

      for (Iterator<TextRange> exIt = excluded.iterator(); exIt.hasNext(); ) {
        final TextRange ex = exIt.next();
        if (ex.precedes(r)) {
          exIt.remove();
        } else if (r.precedes(ex)) {
          continue;
        }
        for (TextRange remainder : r.substract(ex)) {
          it.add(remainder);
        }

      }
    }
    return result;
  }
}
