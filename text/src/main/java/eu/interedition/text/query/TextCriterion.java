package eu.interedition.text.query;

import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextCriterion implements Criterion {
  private final Text text;

  TextCriterion(Text text) {
    this.text = text;
  }

  public Text getText() {
    return text;
  }
}
