package eu.interedition.text.predicate;

import eu.interedition.text.Range;
import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextRangePredicate implements AnnotationPredicate {
  private final Text text;
  private final Range range;

  public TextRangePredicate(Text text, Range range) {
    this.text = text;
    this.range = range;
  }

  public Text getText() {
    return text;
  }

  public Range getRange() {
    return range;
  }
}
