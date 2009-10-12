package eu.interedition.collatex.tokenization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sd_editions.collatex.iterator.ArrayIterator;

// NOTE: normalize == remove punctuation!
public class WitnessTokenizer {
  private final ArrayIterator iterator;
  private final boolean normalize;
  private final static Pattern SPLITTER = Pattern.compile("\\s+");
  private final static Pattern PUNCT = Pattern.compile("\\p{Punct}");

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
    String punctuation = "";
    if (normalize) {
      Matcher matcher = PUNCT.matcher(token);
      boolean find = matcher.find();
      if (find) {
        punctuation = matcher.group();
        token = matcher.replaceAll("");
      }
      token = token.toLowerCase();
    }
    Token t = new Token(token, punctuation);
    return t;
  }
}
