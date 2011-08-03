package eu.interedition.text.predicate;

import com.google.common.base.Function;
import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextPredicate implements AnnotationPredicate {
  private final Text text;

  public TextPredicate(Text text) {
    this.text = text;
  }

  public Text getText() {
    return text;
  }

  public static Function<TextPredicate, Text> TO_TEXT = new Function<TextPredicate, Text>() {
    public Text apply(TextPredicate input) {
      return input.getText();
    }
  };
}
