package eu.interedition.text.query;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RangeLengthCriterion implements Criterion {
  private final int length;

  public RangeLengthCriterion(int length) {
    this.length = length;
  }

  public int getLength() {
    return length;
  }
}
