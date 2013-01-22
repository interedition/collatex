package eu.interedition.collatex.medite;

import com.google.common.base.Predicate;
import com.google.common.collect.Range;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
class IndexRangeSet extends HashSet<Range<Integer>> implements Predicate<Integer> {

  IndexRangeSet() {
  }

  IndexRangeSet(Collection<? extends Range<Integer>> c) {
    super(c);
  }

  @Override
  public boolean apply(@Nullable Integer input) {
    for (Range<Integer> range : this) {
      if (range.contains(input)) {
        return true;
      }
    }
    return false;
  }
}
