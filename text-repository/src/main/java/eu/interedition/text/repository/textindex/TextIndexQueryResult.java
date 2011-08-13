package eu.interedition.text.repository.textindex;

import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextIndexQueryResult {
  private int score;
  private Text text;
  private String textStart;

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public Text getText() {
    return text;
  }

  public void setText(Text text) {
    this.text = text;
  }

  public String getTextStart() {
    return textStart;
  }

  public void setTextStart(String textStart) {
    this.textStart = textStart;
  }
}
