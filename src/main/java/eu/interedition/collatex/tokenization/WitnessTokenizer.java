package eu.interedition.collatex.tokenization;

import java.util.regex.Pattern;

import com.sd_editions.collatex.iterator.ArrayIterator;

// NOTE: normalize == remove punctuation!
public class WitnessTokenizer {
  private final ArrayIterator iterator;
  private final boolean normalize;
  private final static Pattern SPLITTER = Pattern.compile("\\s+");

  public WitnessTokenizer(String witness, boolean _normalize) {
    this.normalize = _normalize;
    String[] tokens = witness.isEmpty() ? new String[0] : SPLITTER.split(witness.trim());
    iterator = new ArrayIterator(tokens);
  }

  public boolean hasNextToken() {
    return iterator.hasNext();
  }

  public Token nextToken() {
    String token = (String) iterator.next();
    if (normalize) {
      token = token.replaceAll("\\p{Punct}", "").toLowerCase();
    }
    Token t = new Token(token);
    return t;
  }
}
