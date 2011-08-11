package eu.interedition.text.query;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeLengthCriterion implements Criterion {
  private final long length;

  public RangeLengthCriterion(long length) {
    this.length = length;
  }

  public long getLength() {
    return length;
  }
}
