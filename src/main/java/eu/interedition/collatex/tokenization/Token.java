package eu.interedition.collatex.tokenization;

public class Token {
  private final String original;
  private final String punctuation;

  public Token(String _orginal) {
    this(_orginal, "");
  }

  public Token(String _orginal, String _punctuation) {
    this.original = _orginal;
    this.punctuation = _punctuation;
  }

  public String getOriginal() {
    return original;
  }

}
