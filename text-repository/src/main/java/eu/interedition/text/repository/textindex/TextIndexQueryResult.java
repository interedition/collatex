package eu.interedition.text.repository.textindex;

import eu.interedition.text.Text;
import eu.interedition.text.repository.model.TextImpl;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextIndexQueryResult extends TextImpl {
  private int score;

  public TextIndexQueryResult(TextImpl other, int score) {
    super(other);
    this.score = score;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }
}
