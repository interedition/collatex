package eu.interedition.collatex2.implementation.input.tokenization;

public class AlternateWhitespaceAndPunctuationTokenizer extends AlternateWhitespaceTokenizer {

  @Override
  protected boolean isTokenBoundary(char c) {
    if (Character.isWhitespace(c)) {
      return true;
    }

    switch (Character.getType(c)) {
      case Character.START_PUNCTUATION:
      case Character.END_PUNCTUATION:
      case Character.OTHER_PUNCTUATION:
        return true;
      default:
        return false;
    }
  }
}
