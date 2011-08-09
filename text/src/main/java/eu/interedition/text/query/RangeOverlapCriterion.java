package eu.interedition.text.query;

import eu.interedition.text.Range;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeOverlapCriterion implements Criterion {
  private final Range range;

  RangeOverlapCriterion(Range range) {
    this.range = range;
  }

  public Range getRange() {
    return range;
  }
}
